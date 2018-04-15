package KCS.Model;

import KCS.Store.DataStore;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.PointLight;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import KCS.Library.Utility;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import sun.awt.FontDescriptor;
import sun.font.FontFamily;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * MainViewと接続されるModel
 */
public class MainModel {
    // publicなプロパティ
    /**
     * 情報表示用のテキスト
     */
    public final ObjectProperty<String> StatusMessage = new SimpleObjectProperty<>();

    // privateなフィールド
    /**
     * ドラッグ途中点の座標
     */
    private Pair<Double, Double> dragMediumPoint;
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
    private Canvas taskBoard;

    // privateな処理
    /**
     * 入力座標が、いずれかのタスクの上かを調べる
     * @param mouseX 入力X座標
     * @param mouseY 入力Y座標
     * @return タスクのインデックス(非選択なら-1)
     */
    private int getTaskBlockIndex(double mouseX, double mouseY){
        return IntStream.range(0, expTaskList.size()).filter(i -> {
            TaskInfo taskInfo = expTaskList.get(i);
            if(taskInfo.getX() <= mouseX && mouseX <= taskInfo.getX() + taskInfo.getW()
                    && taskInfo.getY() <= mouseY && mouseY <= taskInfo.getY() + taskInfo.getH()){
                return true;
            }
            return false;
        }).findFirst().orElse(-1);
    }

    // 各種コマンド
    /**
     * 終了コマンド
     */
    public void ExitCommand(){
        System.exit(0);
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
        // ドラッグ開始点がいずれかのタスクブロックの上だった場合、タスクブロックに対する相対座標を記録する
        int index = getTaskBlockIndex(e.getX(), e.getY());
        if(index != -1){
            draggedExpTaskIndex = index;
            draggedExpTaskOffset = new Pair<>(
                    expTaskList.get(draggedExpTaskIndex).getX() - e.getX(),
                    expTaskList.get(draggedExpTaskIndex).getY() - e.getY());
        }
        // ドラッグイベントを許可する
        taskBoard.startFullDrag();
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
        RedrawCanvasCommand(true);
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
            int newEndtimePosition = newTimePosition + draggedTask.getTimePositionwidth();
            int newLane = (int)Math.round((newY - Utility.TASK_BOARD_MARGIN) / Utility.TASK_PIECE_HEIGHT);
            // 他のどのタスクブロックと干渉しているかを算出
            List<TaskInfo> interferenceList = expTaskList.stream().filter(taskInfo -> {
                // 同一のタスクは飛ばす
                if (draggedTask.getX() == taskInfo.getX() && draggedTask.getY() == taskInfo.getY())
                    return false;
                // 横方向について重なっていなければ飛ばす
                if(taskInfo.getEndTimePosition() <= newTimePosition)
                    return false;
                if(taskInfo.getTimePosition() >= newEndtimePosition)
                    return false;
                // 遠征名が被っている場合はアウト
                if(draggedTask.getName().equals(taskInfo.getName()))
                    return true;
                // 同一艦隊の場合はアウト
                if(newLane == taskInfo.getLane())
                    return true;
                return false;
            }).collect(Collectors.toList());
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
                    draggedTask.setTimePosition(taskInfo.getTimePosition() - draggedTask.getTimePositionwidth());
                }
            }
                break;
            default:
                break;
            }
            // タスクブロックの位置丸め
            if(draggedTask.getTimePosition() < 0)
                draggedTask.setTimePosition(0);
            if(draggedTask.getEndTimePosition() >= Utility.TASK_PIECE_SIZE)
                draggedTask.setTimePosition(Utility.TASK_PIECE_SIZE - draggedTask.getTimePositionwidth());
            int lane = draggedTask.getLane();
            draggedTask.setLane(lane < 0 ? 0 : lane >= Utility.LANES ? Utility.LANES - 1 : lane);
            // 当該タスクブロックのドラッグ状態を解除
            draggedExpTaskIndex = -1;
        }
        //Canvasを再描画する
        RedrawCanvasCommand(false);
        //
        e.consume();
    }
    /**
     *　TaskBoard上でマウスクリックした際のイベント
     * @param e マウスイベント
     */
    public void TaskBoardMouseClicked(MouseEvent e){
        int temp = getTaskBlockIndex(e.getX(), e.getY());
        if(temp == selectedExpTaskIndex){
            Utility.ShowDialog(expTaskList.get(selectedExpTaskIndex).toString(), "遠征の詳細", Alert.AlertType.INFORMATION);
        }else{
            selectedExpTaskIndex = getTaskBlockIndex(e.getX(), e.getY());
            RedrawCanvasCommand(false);
        }
    }
    /**
     * TaskBoardを再描画する
     * @param mediumFlg trueなら、移動中の中間状態なオブジェクトを表示。<br>falseなら中間状態なオブジェクトを確定
     */
    public void RedrawCanvasCommand(boolean mediumFlg){
        // グラフィックスコンテキストを作成
        GraphicsContext gc = taskBoard.getGraphicsContext2D();
        // 画面を一旦削除
        gc.clearRect(0,0, Utility.CANVAS_WIDTH, Utility.CANVAS_HEIGHT);
        // 格子を表示する
        gc.setStroke(Color.GRAY);
        IntStream.range(0, Utility.LANES + 1).forEach(row -> {
            gc.strokeLine(
                    Utility.TASK_BOARD_MARGIN,
                    Utility.TASK_BOARD_MARGIN + row * Utility.TASK_PIECE_HEIGHT,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_WIDTH,
                    Utility.TASK_BOARD_MARGIN + row * Utility.TASK_PIECE_HEIGHT);
        });
        IntStream.range(0, Utility.HOUR_PER_DAY + 1).forEach(column -> {
            gc.strokeLine(
                    Utility.TASK_BOARD_MARGIN + column * Utility.HOUR_TASK_PIECE_WIDTH,
                    Utility.TASK_BOARD_MARGIN,
                    Utility.TASK_BOARD_MARGIN + column * Utility.HOUR_TASK_PIECE_WIDTH,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_HEIGHT);
        });
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
                TaskInfo taskInfo  =expTaskList.get(i);
                double x = taskInfo.getX();
                double y = taskInfo.getY();
                double w = taskInfo.getW();
                double h = taskInfo.getH();
                if(i == selectedExpTaskIndex){
                    gc.setFill(Color.ORANGE);
                }else {
                    gc.setFill(Color.LIGHTSKYBLUE);
                }
                gc.fillRect(x, y, w, h);
                gc.strokeRect(x, y, w, h);
        });
        // ドラッグ中のタスクを表示する
        if(draggedExpTaskIndex != -1){
            // 選択されているタスク
            TaskInfo taskInfo = expTaskList.get(draggedExpTaskIndex);
            double x = dragMediumPoint.getKey()   + draggedExpTaskOffset.getKey();
            double y = dragMediumPoint.getValue() + draggedExpTaskOffset.getValue();
            double w = taskInfo.getW();
            double h = taskInfo.getH();
            gc.setFill(Color.GREEN);
            gc.setGlobalAlpha(0.5);
            gc.fillRect(x, y, w, h);
            gc.strokeRect(x, y, w, h);
        }
        // 選択されているタスクの情報を表示する
        if(selectedExpTaskIndex != -1){
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
                            taskInfo.getLane() + 1,
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
    public MainModel(Canvas taskBoard){
        this.taskBoard = taskBoard;
        // テスト
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("海上護衛任務"), 0, 5));
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("海上護衛任務"), 1, 30));
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("長距離練習航海"), 0, 30));
    }
}
