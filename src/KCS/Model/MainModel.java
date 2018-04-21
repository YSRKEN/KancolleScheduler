package KCS.Model;

import KCS.Library.Utility;
import KCS.Store.DataStore;
import KCS.Store.ExpInfo;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * MainViewと接続されるModel
 */
public class MainModel {
    // publicなプロパティ
    /**
     * 情報表示用のテキスト
     */
    public final ObjectProperty<String> StatusMessage = new SimpleObjectProperty<>();
    // privateなプロパティ
    private final ObjectProperty<Boolean> RightClickBlankFlg = new SimpleObjectProperty<>(true);
    private final ObjectProperty<Boolean> RightClickTaskBlockFlg = new SimpleObjectProperty<>(false);
    private final ObjectProperty<Boolean> CiFlgProperty = new SimpleObjectProperty<>(false);
    private final ObjectProperty<Boolean> MarriageFlgProperty = new SimpleObjectProperty<>(false);

    // privateなフィールド
    /**
     * ドラッグ途中点の座標
     */
    private Pair<Double, Double> dragMediumPoint;
    /**
     * 右クリックした際のマウス座標
     */
    private Pair<Double, Double> mouseRightClickPoint;
    /**
     * 現在選択しているタスクブロックのインデックス
     */
    private int selectedExpTaskIndex = -1;
    /**
     * 現在ドラッグしているタスクブロックのインデックス
     */
    private int draggedExpTaskIndex = -1;
    /**
     * 現在ドラッグしているタスクブロックの左上座標(マウスポインタ基準)
     */
    private Pair<Double, Double> draggedExpTaskOffset;
    /**
     * 遠征のタスクブロック一覧
     */
    private List<TaskInfo> expTaskList = new ArrayList<>();
    /**
     * Canvasを処理するため止むなく引っ張るポインタ
     */
    final private Canvas taskBoard;
    /**
     * 右クリックメニューを処理するため止むなく引っ張るポインタ
     */
    final private ContextMenu taskBoardMenu;

    // privateな処理
    /**
     * 入力座標が、いずれかのタスクの上かを調べる
     * @param mouseX 入力X座標
     * @param mouseY 入力Y座標
     * @return タスクのインデックス(非選択なら-1)
     */
    private int getTaskBlockIndex(double mouseX, double mouseY){
        return IntStream
                .range(0, expTaskList.size())
                .filter(i -> expTaskList.get(i).isInnerPoint(mouseX, mouseY))
                .findFirst()
                .orElse(-1);
    }
    /**
     * コンテキストメニューを初期化
     */
    private void initializeContextMenu() {
        // 遠征ツリーを取得する
        LinkedHashMap<String, List<String>> expNameTree = DataStore.getExpNameTree();
        // 遠征ツリーをコンテキストメニューに反映させる
        for(Map.Entry<String, List<String>> entry : expNameTree.entrySet()){
            // 海域名をbase(Menu型)の名前とする
            Menu base = new Menu();
            base.visibleProperty().bindBidirectional(RightClickBlankFlg);
            base.setText(entry.getKey());
            // baseに、遠征毎の遠征のメニューを追加していく
            entry.getValue().forEach(name ->{
                // 遠征名をitem(MenuItem型)の名前とする
                MenuItem item = new MenuItem();
                item.setText(name);
                item.setOnAction(e -> addTaskBlock(item.getText()));
                base.getItems().add(item);
            });
            taskBoardMenu.getItems().add(base);
        }
        // タスクをコピーするメニュー
        MenuItem copyMenu = new MenuItem();
        copyMenu.visibleProperty().bindBidirectional(RightClickTaskBlockFlg);
        copyMenu.setText("このタスクをコピー");
        copyMenu.setOnAction(e -> copyTaskBlock());
        taskBoardMenu.getItems().add(copyMenu);
        // セパレーター
        taskBoardMenu.getItems().add(new SeparatorMenuItem());
        // 大発による加算を考慮するメニュー
        Menu addPerMenu = new Menu();
        addPerMenu.visibleProperty().bindBidirectional(RightClickTaskBlockFlg);
        addPerMenu.setText("大発加算");
        IntStream.range(0, 5).map(i -> i * 5).forEach(i -> {
            MenuItem item = new MenuItem();
            item.setText(String.format("＋%d％", i));
            item.setOnAction(e -> changeAddPerTaskBlock(item.getText()));
            addPerMenu.getItems().add(item);
        });
        taskBoardMenu.getItems().add(addPerMenu);
        // 遠征に大成功したか否かを考慮するメニュー
        CheckMenuItem ciFlgMenu = new CheckMenuItem();
        ciFlgMenu.visibleProperty().bindBidirectional(RightClickTaskBlockFlg);
        ciFlgMenu.selectedProperty().bindBidirectional(CiFlgProperty);
        ciFlgMenu.setText("大成功フラグ");
        ciFlgMenu.setOnAction(e -> changeCiFlgTaskBlock());
        taskBoardMenu.getItems().add(ciFlgMenu);
        // ケッコン艦パーティーで出撃させたか否かを考慮するメニュー
        CheckMenuItem marriageFlgMenu = new CheckMenuItem();
        marriageFlgMenu.visibleProperty().bindBidirectional(RightClickTaskBlockFlg);
        marriageFlgMenu.selectedProperty().bindBidirectional(MarriageFlgProperty);
        marriageFlgMenu.setText("ケッコン艦フラグ");
        marriageFlgMenu.setOnAction(e -> changeMarriageFlgTaskBlock());
        taskBoardMenu.getItems().add(marriageFlgMenu);
        // セパレーター
        taskBoardMenu.getItems().add(new SeparatorMenuItem());
        // タスクを削除するメニュー
        MenuItem deleteMenu = new MenuItem();
        deleteMenu.visibleProperty().bindBidirectional(RightClickTaskBlockFlg);
        deleteMenu.setText("このタスクを削除");
        deleteMenu.setOnAction(e -> deleteTaskBlock());
        taskBoardMenu.getItems().add(deleteMenu);
    }
    /**
     * 他のどのタスクブロックと干渉しているかを算出
     * @param wantAddingTask 追加したいタスクブロック
     * @param timePosition 追加したい左位置
     * @param lane 追加したい上位置
     * @return 干渉しているタスクブロックの一覧を返す
     */
    private List<TaskInfo> getInterferenceTaskList(TaskInfo wantAddingTask, int timePosition, int lane){
        int newEndtimePosition = (timePosition + wantAddingTask.getTimePositionWidth()) % Utility.TASK_PIECE_SIZE;
        return expTaskList.stream().filter(taskInfo -> {
            // 同一のタスクは飛ばす
            if (wantAddingTask.getX() == taskInfo.getX() && wantAddingTask.getY() == taskInfo.getY())
                return false;
            // 横方向について重なっていなければ飛ばす
            // ・既存タスクAに対して目標タスクBが重なっているかを考える
            // ・A.end = A.startまたはB.end = B.startならば確実に重なるので考慮不要
            // ・Aが2つに分裂している場合、A.end < A.startである。Bがその間に収まって
            // 　いればいいので、A.end <= B.startかつB.end <= A.startであればいい
            // ・Aが2つに分裂していない場合、A.start < A.endである。Bがその前後に入って
            // 　いればいいので、
            // 　・Bが手前にある場合、B.start < B.endかつB.end <= A.start
            // 　・Bが奥にある場合、B.start < B.endかつA.end <= B.start
            // 　・BがAを包み込む場合、B.end < B.startかつB.end <= A.startかつA.end <= B.start
            // ・これをまとめると、
            // 　・A.end < A.startかつA.end <= B.startかつB.end <= A.start
            // 　・A.start < A.endかつB.start <= B.endかつB.end <= A.start
            // 　・A.start < A.endかつB.start <= B.endかつA.end <= B.start
            // 　・B.end < B.startかつB.end <= A.startかつA.end <= B.start
            // ・論理を整理して、
            // 　・A.end <= B.startかつB.end <= A.startかつ(A.end < A.startまたはB.end < B.start)
            // 　・A.start < A.endかつB.start < B.endかつ(B.end <= A.startまたはA.end <= B.start)
            // ・A.end < A.startを[1]、A.end <= B.startを[2]、B.end <= A.startを[3]、B.end < B.startを[4]と置くと
            // 　・[2]かつ[3]かつ([1]または[4])
            // 　・![1]かつ![4]かつ([2]または[3])
            // ・([2]かつ[3]かつ([1]または[4]))または(![1]かつ![4]かつ([2]または[3]))を論理圧縮すると、
            // 　(![1]かつ[3]かつ![4])または(![1]かつ[2]かつ![4])または([2]かつ[3])
            // 　＝([2]かつ[3])または(![1]かつ![4]かつ([2]または[3]))になるそうな
            if(taskInfo.getTimePosition() != taskInfo.getEndTimePosition() && timePosition != newEndtimePosition){
                boolean flg1 = (taskInfo.getEndTimePosition() <= taskInfo.getTimePosition());
                boolean flg2 = (taskInfo.getEndTimePosition() <= timePosition);
                boolean flg3 = (newEndtimePosition <= taskInfo.getTimePosition());
                boolean flg4 = (newEndtimePosition <= timePosition);
                if(flg2 && flg3 && (flg1 || flg4))
                    return false;
                if(!flg1 && !flg4 && (flg2 || flg3))
                    return false;
            }
            // 遠征名が被っている場合はアウト
            if(wantAddingTask.getName().equals(taskInfo.getName()))
                return true;
            // 同一艦隊の場合はアウト
            if(lane == taskInfo.getLane())
                return true;
            return false;
        }).collect(Collectors.toList());
    }
    /**
     * 遠征を画面内の適当な位置に追加する
     */
    private void addTaskBlock(String name){
        // 追加する遠征の情報
        ExpInfo expInfo = DataStore.getExpInfoFromName(name);
        // 左クリックした座標に遠征を配置できるかを調べる
        int timePosition = (int)(mouseRightClickPoint.getKey() / Utility.TASK_PIECE_WIDTH);
        int lane = (int)(mouseRightClickPoint.getValue() / Utility.TASK_PIECE_HEIGHT);
        if(timePosition < 0 || timePosition >= Utility.TASK_PIECE_SIZE
                || lane < 0 || lane >= Utility.LANES)
            return;
        List<TaskInfo> interferenceTaskList = getInterferenceTaskList(new TaskInfo(expInfo, -1, -1), timePosition, lane);
        if(interferenceTaskList.size() > 0)
            return;
        // 遠征を追加する
        expTaskList.add(new TaskInfo(expInfo, lane, timePosition));
        RedrawCanvasCommand();
    }
    /**
     * 遠征タスクをコピーする
     */
    private void copyTaskBlock(){
        // マウスの下に遠征タスクが存在するなら
        int copyTaskBlockIndex = getTaskBlockIndex(mouseRightClickPoint.getKey(), mouseRightClickPoint.getValue());
        if(copyTaskBlockIndex != -1){
            // 遠征タスクをコピーし
            TaskInfo cloneTask = expTaskList.get(copyTaskBlockIndex).clone();
            // 追加し
            expTaskList.add(cloneTask);
            // とりあえず同じ場所に重ねて置く
            draggedExpTaskIndex = expTaskList.size() - 1;
            draggedExpTaskOffset = null;
            RedrawCanvasCommand();
        }
    }
    /**
     * 遠征タスクを削除する
     */
    private void deleteTaskBlock(){
        int deleteTaskBlockIndex = getTaskBlockIndex(mouseRightClickPoint.getKey(), mouseRightClickPoint.getValue());
        if(deleteTaskBlockIndex != -1){
            expTaskList.remove(deleteTaskBlockIndex);
            RedrawCanvasCommand();
        }
    }
    /**
     * 遠征タスクの収益増加率を変更する
     * @param name 変更情報(「＋%d％」という書式)
     */
    private void changeAddPerTaskBlock(String name){
        int taskBlockIndex = getTaskBlockIndex(mouseRightClickPoint.getKey(), mouseRightClickPoint.getValue());
        if(taskBlockIndex != -1){
            int addPer = Integer.parseInt(name.replace("＋", "").replace("％", ""));
            expTaskList.get(taskBlockIndex).setAddPer(addPer);
            RedrawCanvasCommand();
        }
    }
    /**
     * 遠征タスクのカットインフラグを変更する
     */
    private void changeCiFlgTaskBlock(){
        int taskBlockIndex = getTaskBlockIndex(mouseRightClickPoint.getKey(), mouseRightClickPoint.getValue());
        if(taskBlockIndex != -1){
            expTaskList.get(taskBlockIndex).setCiFlg(CiFlgProperty.getValue());
            RedrawCanvasCommand();
        }
    }
    /**
     * 遠征タスクのケッコン艦フラグを変更する
     */
    private void changeMarriageFlgTaskBlock(){
        int taskBlockIndex = getTaskBlockIndex(mouseRightClickPoint.getKey(), mouseRightClickPoint.getValue());
        if(taskBlockIndex != -1){
            expTaskList.get(taskBlockIndex).setMarriageFlg(MarriageFlgProperty.getValue());
            RedrawCanvasCommand();
        }
    }

    // 各種コマンド
    /**
     * 開くコマンド
     */
    public void LoadCommand(){
        // ファイルを選択
        FileChooser fc = new FileChooser();
        fc.setTitle("ファイルを開く");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("ALL", "*.*")
        );
        File file = fc.showOpenDialog(null);
        if(file != null){
            // ファイルが開けたら、CSVデータに対する処理を行う
            try(Stream<String> data = Files.lines(file.toPath(), StandardCharsets.UTF_8)){
                expTaskList = new ArrayList<>();
                // 文字列をパースできるか判定を行い、できる場合はTaskInfo、できない場合はnullを返す
                data.map(getLine -> {
                    try{
                        return new TaskInfo(getLine);
                    }catch(NumberFormatException e){
                        //e.printStackTrace();
                        return null;
                    }
                })
                // nullを除外
                .filter(Objects::nonNull)
                // とりあえず配置してみて、置けないものは無視する
                .forEach(t -> {
                    List<TaskInfo> temp = getInterferenceTaskList(t, t.getTimePosition(), t.getLane());
                    if(temp.size() == 0){
                        expTaskList.add(t);
                    }
                });
                selectedExpTaskIndex = -1;
                RedrawCanvasCommand();
            } catch (IOException e) {
                Utility.ShowDialog("ファイルを開けませんでした。", "読み込み情報", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    /**
     * 保存コマンド
     */
    public void SaveCommand(){
        // ファイルを選択
        FileChooser fc = new FileChooser();
        fc.setTitle("ファイルを保存");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("ALL", "*.*")
        );
        File file = fc.showSaveDialog(null);
        if(file == null)
            return;
        // 保存用のテキストを作成
        if(expTaskList.size() == 0) {
            Utility.ShowDialog("遠征タスクが1つも存在しません。", "書き込み情報", Alert.AlertType.ERROR);
            return;
        }
        try(BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)){
            bw.write(String.format("遠征名,艦隊番号,タイミング,収益増加率,大成功フラグ,ケッコン艦フラグ%n"));
            for(TaskInfo task : expTaskList){
                bw.write(task.toCsvData());
            }
        } catch (IOException e) {
            Utility.ShowDialog("ファイルを保存できませんでした。", "読み込み情報", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    /**
     * 終了コマンド
     */
    public void ExitCommand(){
        System.exit(0);
    }
    /**
     * 情報表示コマンド
     */
    public void ShowInfoCommand(){
        long allFuel = expTaskList.stream().mapToLong(TaskInfo::fuelValue).sum();
        long allAmmo = expTaskList.stream().mapToLong(TaskInfo::ammoValue).sum();
        long allSteel = expTaskList.stream().mapToLong(TaskInfo::steelValue).sum();
        long allBaux = expTaskList.stream().mapToLong(TaskInfo::bauxValue).sum();
        double allBucket = expTaskList.stream().mapToDouble(TaskInfo::bucketValue).sum();
        double allBurner = expTaskList.stream().mapToDouble(TaskInfo::burnerValue).sum();
        double allGear = expTaskList.stream().mapToDouble(TaskInfo::gearValue).sum();
        double allCoin = expTaskList.stream().mapToDouble(TaskInfo::coinValue).sum();
        Utility.ShowDialog(String.format(
                "燃料：%d　弾薬：%d　鋼材：%d　ボーキ：%d%nバケツ：%s　バーナー：%s%n開発資材：%s　家具コイン：%s",
                allFuel, allAmmo, allSteel, allBaux,
                Utility.DoubleToString(allBucket), Utility.DoubleToString(allBurner),
                Utility.DoubleToString(allGear), Utility.DoubleToString(allCoin)),
                "遠征収益", Alert.AlertType.INFORMATION);
    }
    /**
     * 全削除コマンド
     */
    public void AllDeleteCommand(){
        Alert alert = new Alert(Alert.AlertType.WARNING, "遠征タスクを全部削除します。よろしいですか？", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("全削除コマンド");
        alert.setContentText("遠征タスクを全部削除します。よろしいですか？");
        alert.setTitle(Utility.SOFT_NAME);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES){
            expTaskList.clear();
            RedrawCanvasCommand();
        }
    }
    /**
     * バージョン情報コマンド
     */
    public void ShowVersionInfoCommand(){
        String contentText = String.format("ソフト名：%s%nバージョン：%s", Utility.SOFT_NAME, Utility.SOFT_VERSION);
        Utility.ShowDialog(contentText, "バージョン情報", Alert.AlertType.INFORMATION);
    }
    /**
     * TaskBoard上でマウスによるドラッグを開始した際のイベント
     * @param e マウスイベント
     */
    public void TaskBoardDragDetected(MouseEvent e){
        // 左クリックじゃないと無視する
        if(e.getButton() != MouseButton.PRIMARY)
            return;
        // ドラッグ開始点がいずれかのタスクブロックの上だった場合、タスクブロックに対する相対座標を記録する
        if(draggedExpTaskIndex == -1){
            int index = getTaskBlockIndex(e.getX(), e.getY());
            if(index != -1){
                draggedExpTaskIndex = index;
            }
        }else{
            expTaskList.get(draggedExpTaskIndex).setTimePosition((int)((e.getX() - Utility.TASK_BOARD_MARGIN) / Utility.TASK_PIECE_WIDTH));
            expTaskList.get(draggedExpTaskIndex).setLane((int)((e.getY() - Utility.TASK_BOARD_MARGIN) / Utility.TASK_PIECE_HEIGHT));
        }
        if(draggedExpTaskIndex != -1){
            draggedExpTaskOffset = new Pair<>(
                    expTaskList.get(draggedExpTaskIndex).getX() - e.getX(),
                    expTaskList.get(draggedExpTaskIndex).getY() - e.getY());
            // ドラッグイベントを許可する
            taskBoard.startFullDrag();
        }
        e.consume();
    }
    /**
     * TaskBoard上でマウスによるドラッグの中間状態のイベント
     * @param e ドラッグイベント
     */
    public void TaskBoardMouseDragOver(MouseDragEvent e){
        // ドラッグ途中点の座標を記録する
        dragMediumPoint = new Pair<>(e.getX(), e.getY());
        //Canvasを再描画する
        RedrawCanvasCommand();
        //
        e.consume();
    }
    /**
     * TaskBoard上でマウスによるドラッグを終了した際のイベント
     * @param e ドラッグイベント
     */
    public void TaskBoardMouseDragReleased(MouseDragEvent e){
        // ドラッグを終了したので、放したタスクブロックの位置を再計算する
        if(draggedExpTaskIndex != -1){
            // 当該タスクブロックのインデックス
            int index = draggedExpTaskIndex;
            // 当該タスクブロックの情報
            TaskInfo draggedTask = expTaskList.get(index);
            // 当該タスクブロックの左上位置
            double newX = e.getX() + draggedExpTaskOffset.getKey();
            double newY = e.getY() + draggedExpTaskOffset.getValue();
            // 当該タスクブロックのタイミングおよび終了タイミングおよび艦隊番号
            int newTimePosition = (int)Math.round((newX - Utility.TASK_BOARD_MARGIN) / Utility.TASK_PIECE_WIDTH);
            int newLane = (int)Math.round((newY - Utility.TASK_BOARD_MARGIN) / Utility.TASK_PIECE_HEIGHT);
            // 他のどのタスクブロックと干渉しているかを算出
            List<TaskInfo> interferenceList = getInterferenceTaskList(draggedTask, newTimePosition, newLane);
            // 何も干渉してない場合はそれでOK
            // 1つだけ干渉している場合はそれと被らないように移動
            // 2つ以上干渉している場合は移動させない
            switch (interferenceList.size()){
            case 0:
                draggedTask.setTimePosition(newTimePosition);
                draggedTask.setLane(newLane);
                break;
            case 1:
            {
                int newEndtimePosition = (newTimePosition + draggedTask.getTimePositionWidth()) % Utility.TASK_PIECE_SIZE;
                // 横方向について、ギリギリまで寄れるようにする
                //右の距離と左の距離とを測定する
                TaskInfo taskInfo = interferenceList.get(0);
                int rightDist = Math.abs(taskInfo.getEndTimePosition() - newTimePosition);
                int leftDist = Math.abs(taskInfo.getTimePosition() - newEndtimePosition);
                // より近い方に寄せる
                if(rightDist < leftDist && taskInfo.getEndTimePosition() > newTimePosition){
                    // 「右から寄った」と仮定する
                    draggedTask.setTimePosition(taskInfo.getEndTimePosition());
                }else if(taskInfo.getTimePosition() < newEndtimePosition){
                    // 「左から寄った」と仮定する
                    draggedTask.setTimePosition(taskInfo.getTimePosition() - draggedTask.getTimePositionWidth());
                }
            }
                break;
            default:
                break;
            }
            // タスクブロックの位置丸め
            if(draggedTask.getTimePosition() < 0)
                draggedTask.setTimePosition((draggedTask.getTimePosition() + Utility.TASK_PIECE_SIZE) % Utility.TASK_PIECE_SIZE);
            if(draggedTask.getTimePosition() >= Utility.TASK_PIECE_SIZE)
                draggedTask.setTimePosition(draggedTask.getTimePosition() % Utility.TASK_PIECE_SIZE);
            int lane = draggedTask.getLane();
            draggedTask.setLane(lane < 0 ? 0 : lane >= Utility.LANES ? Utility.LANES - 1 : lane);
            // 当該タスクブロックのドラッグ状態を解除
            draggedExpTaskIndex = -1;
        }
        //Canvasを再描画する
        RedrawCanvasCommand();
        //
        e.consume();
    }
    /**
     *　TaskBoard上でマウスクリックした際のイベント
     * @param e マウスイベント
     */
    public void TaskBoardMouseClicked(MouseEvent e){
        // 左クリックじゃない場合は、座標だけ記憶する
        if(e.getButton() != MouseButton.PRIMARY) {
            selectedExpTaskIndex = -1;
            mouseRightClickPoint = new Pair<>(e.getX(), e.getY());
            int taskBlockIndex = getTaskBlockIndex(mouseRightClickPoint.getKey(), mouseRightClickPoint.getValue());
            RightClickTaskBlockFlg.setValue(taskBlockIndex != -1);
            if(taskBlockIndex != -1) {
                CiFlgProperty.setValue(expTaskList.get(taskBlockIndex).getCiFlg());
                MarriageFlgProperty.setValue(expTaskList.get(taskBlockIndex).getMarriageFlg());
            }
            return;
        }
        // クリックしたらその遠征についての情報をステータスバーに表示・画面も再描画
        selectedExpTaskIndex = getTaskBlockIndex(e.getX(), e.getY());
        RedrawCanvasCommand();
        // ダブルクリックなら詳細表示
        if(e.getClickCount() >= 2 && selectedExpTaskIndex >= 0){
            Utility.ShowDialog(expTaskList.get(selectedExpTaskIndex).toString(), "遠征の詳細", Alert.AlertType.INFORMATION);
        }
    }
    /**
     * TaskBoardを再描画する
     */
    public void RedrawCanvasCommand() {
        // グラフィックスコンテキストを作成
        GraphicsContext gc = taskBoard.getGraphicsContext2D();
        // 画面を一旦削除
        gc.clearRect(0, 0, Utility.CANVAS_WIDTH, Utility.CANVAS_HEIGHT);
        // 格子を表示する
        gc.setStroke(Color.GRAY);
        IntStream.range(0, Utility.LANES + 1).forEach(row ->
                gc.strokeLine(
                    Utility.TASK_BOARD_MARGIN,
                    Utility.TASK_BOARD_MARGIN + row * Utility.TASK_PIECE_HEIGHT,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_WIDTH,
                    Utility.TASK_BOARD_MARGIN + row * Utility.TASK_PIECE_HEIGHT
                )
        );
        IntStream.range(0, Utility.HOUR_PER_DAY + 1).forEach(column ->
            gc.strokeLine(
                    Utility.TASK_BOARD_MARGIN + column * Utility.HOUR_TASK_PIECE_WIDTH,
                    Utility.TASK_BOARD_MARGIN,
                    Utility.TASK_BOARD_MARGIN + column * Utility.HOUR_TASK_PIECE_WIDTH,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_HEIGHT
            )
        );
        // 時刻を表示する
        gc.setFont(Font.font(16));
        gc.setFill(Color.BLACK);
        IntStream.range(Utility.TASK_BOARD_FIRST_HOUR, Utility.TASK_BOARD_FIRST_HOUR + Utility.HOUR_PER_DAY + 1).forEach(hour -> {
            int hour2 = hour % Utility.HOUR_PER_DAY;
            gc.fillText(
                    String.format("%d:00", hour2),
                    Utility.TASK_BOARD_MARGIN + (hour - Utility.TASK_BOARD_FIRST_HOUR) * Utility.HOUR_TASK_PIECE_WIDTH - 16,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_HEIGHT + 24);
        });
        // 既存のタスクを表示する(ドラッグ中のものは除く)
        gc.setStroke(Color.BLACK);
        IntStream.range(0, expTaskList.size())
                .filter(i -> i != draggedExpTaskIndex)
                .forEach(i -> {
                    TaskInfo taskInfo = expTaskList.get(i);
                    double x = taskInfo.getX();
                    double y = taskInfo.getY();
                    double w = taskInfo.getW();
                    double h = taskInfo.getH();
                    if (i == selectedExpTaskIndex) {
                        gc.setFill(Color.ORANGE);
                    } else {
                        gc.setFill(Color.LIGHTSKYBLUE);
                    }
                    if(taskInfo.getTimePosition() <= taskInfo.getEndTimePosition()) {
                        gc.fillRect(x, y, w, h);
                        gc.strokeRect(x, y, w, h);
                        gc.setFill(Color.RED);
                        gc.setFont(Font.font("", FontWeight.BOLD, 16));
                        gc.fillText(taskInfo.getName(), x + 5, y + 16 + 5);
                    }else{
                        double w2 = Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_WIDTH - x;
                        double w3 = w - w2;
                        double x2 = Utility.TASK_BOARD_MARGIN;
                        gc.fillRect(x, y, w2, h);
                        gc.strokeRect(x, y, w2, h);
                        gc.fillRect(x2, y, w3, h);
                        gc.strokeRect(x2, y, w3, h);
                        gc.setFill(Color.RED);
                        gc.setFont(Font.font("", FontWeight.BOLD, 16));
                        gc.fillText(taskInfo.getName(), x + 5, y + 16 + 5);
                        gc.fillText(taskInfo.getName(), x2 + 5, y + 16 + 5);
                    }
                });
        // ドラッグ中のタスクを表示する
        if (draggedExpTaskIndex != -1 && draggedExpTaskOffset != null) {
            // 選択されているタスク
            TaskInfo taskInfo = expTaskList.get(draggedExpTaskIndex);
            double x = dragMediumPoint.getKey() + draggedExpTaskOffset.getKey();
            double y = dragMediumPoint.getValue() + draggedExpTaskOffset.getValue();
            double w = taskInfo.getW();
            double h = taskInfo.getH();
            gc.setFill(Color.GREEN);
            gc.setGlobalAlpha(0.5);
            if(Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_WIDTH - x >= w) {
                gc.fillRect(x, y, w, h);
                gc.strokeRect(x, y, w, h);
            }else{
                double w2 = Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_WIDTH - x;
                double w3 = w - w2;
                double x2 = Utility.TASK_BOARD_MARGIN;
                gc.fillRect(x, y, w2, h);
                gc.strokeRect(x, y, w2, h);
                gc.fillRect(x2, y, w3, h);
                gc.strokeRect(x2, y, w3, h);
            }
        }
        // 選択されているタスクの情報を表示する
        if (selectedExpTaskIndex != -1) {
            // 選択されているタスク
            TaskInfo taskInfo = expTaskList.get(selectedExpTaskIndex);
            // タスクの開始時刻を時：分形式に変換
            int allMinute = taskInfo.getTimePosition() * Utility.MIN_TASK_PIECE_TIME;
            int hour = ((allMinute / Utility.MINUTE_PER_HOUR) + Utility.TASK_BOARD_FIRST_HOUR) % Utility.HOUR_PER_DAY;
            int minute = allMinute % Utility.MINUTE_PER_HOUR;
            // 終了時刻も時：分形式に変換
            int allMinute2 = taskInfo.getEndTimePosition() * Utility.MIN_TASK_PIECE_TIME;
            int hour2 = ((allMinute2 / Utility.MINUTE_PER_HOUR) + Utility.TASK_BOARD_FIRST_HOUR) % Utility.HOUR_PER_DAY;
            int minute2 = allMinute2 % Utility.MINUTE_PER_HOUR;
            // 結果を表示
            Platform.runLater(() -> StatusMessage.setValue(
                    String.format(
                            "%s(第%d艦隊,%02d:%02d-%02d:%02d)",
                            taskInfo.getName(),
                            taskInfo.getLane() + 2,
                            hour,
                            minute,
                            hour2,
                            minute2
                    )
            ));
        }
    }

    /**
     * コンストラクタ
     */
    public MainModel(Canvas taskBoard, ContextMenu taskBoardMenu){
        this.taskBoard = taskBoard;
        this.taskBoardMenu = taskBoardMenu;
        this.RightClickTaskBlockFlg.addListener((ob,o,n) -> RightClickBlankFlg.setValue(!n));
        // コンテキストメニューを初期化
        initializeContextMenu();
    }
}
