package KCS.Model;

import KCS.Store.DataStore;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import KCS.Library.Utility;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sun.awt.FontDescriptor;
import sun.font.FontFamily;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MainViewと接続されるModel
 */
public class MainModel {
    // publicなプロパティ
    /**
     * 情報表示用のテキスト
     */
    private final ObjectProperty<String> statusMessage = new SimpleObjectProperty<>();
    public final ReadOnlyObjectProperty<String> StatusMessage = statusMessage;

    // privateなプロパティ
    /**
     * ドラッグ開始点のX座標
     */
    private final ObjectProperty<Double> dragStartPointX = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ開始点のY座標
     */
    private final ObjectProperty<Double> dragStartPointY = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ途中点のX座標
     */
    private final ObjectProperty<Double> dragMediumPointX = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ途中点のY座標
     */
    private final ObjectProperty<Double> dragMediumPointY = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ終了点のX座標
     */
    private final ObjectProperty<Double> dragEndPointX = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ終了点のY座標
     */
    private final ObjectProperty<Double> dragEndPointY = new SimpleObjectProperty<>(0.0);
    /**
     * 現在選択しているタスクブロックのインデックス
     */
    private final ObjectProperty<Integer> selectedExpTaskIndex = new SimpleObjectProperty<>(-1);
    /**
     * 現在ドラッグしているタスクブロックのインデックス
     */
    private final ObjectProperty<Integer> draggedExpTaskIndex = new SimpleObjectProperty<>(-1);
    /**
     * 現在ドラッグしているタスクブロックの左上座標(マウスポインタ基準)のX座標
     */
    private final ObjectProperty<Double> draggedExpTaskOffsetX = new SimpleObjectProperty<>(0.0);
    /**
     * 現在ドラッグしているタスクブロックの左上座標(マウスポインタ基準)のY座標
     */
    private final ObjectProperty<Double> draggedExpTaskOffsetY = new SimpleObjectProperty<>(0.0);

    // privateなフィールド
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
        for(int i = 0; i < expTaskList.size(); ++i) {
            TaskInfo taskInfo = expTaskList.get(i);
            if(taskInfo.getX() <= mouseX && mouseX <= taskInfo.getX() + taskInfo.getW()
                    && taskInfo.getY() <= mouseY && mouseY <= taskInfo.getY() + taskInfo.getH()){
                return i;
            }
        }
        return -1;
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
        // ドラッグ開始点の座標を記録する
        dragStartPointX.setValue(e.getX());
        dragStartPointY.setValue(e.getY());
        // ドラッグ開始点がいずれかのタスクブロックの上だった場合、タスクブロックに対する相対座標を記録する
        int index = getTaskBlockIndex(e.getX(), e.getY());
        if(index != -1){
            draggedExpTaskIndex.setValue(index);
            draggedExpTaskOffsetX.setValue(expTaskList.get(index).getX() - e.getX());
            draggedExpTaskOffsetY.setValue(expTaskList.get(index).getY() - e.getY());
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
        dragMediumPointX.setValue(e.getX());
        dragMediumPointY.setValue(e.getY());
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
        // ドラッグ終了点の座標を記録する
        dragEndPointX.setValue(e.getX());
        dragEndPointY.setValue(e.getY());
        // ドラッグを終了したので、放したタスクブロックの位置を再計算する
        if(draggedExpTaskIndex.getValue() != -1){
            // 当該タスクブロックのインデックス
            int index = draggedExpTaskIndex.getValue();
            // 当該タスクブロックの情報
            TaskInfo draggedTask = expTaskList.get(index);
            // 当該タスクブロックの左上位置
            double newX = dragEndPointX.getValue() + draggedExpTaskOffsetX.getValue();
            double newY = dragEndPointY.getValue() + draggedExpTaskOffsetY.getValue();
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
            // 当該タスクブロックのドラッグ状態を解除
            draggedExpTaskIndex.setValue(-1);
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
        selectedExpTaskIndex.setValue(getTaskBlockIndex(e.getX(), e.getY()));
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
        for(int row = 0; row <= Utility.LANES; ++row){
            gc.strokeLine(
                    Utility.TASK_BOARD_MARGIN,
                    Utility.TASK_BOARD_MARGIN + row * Utility.TASK_PIECE_HEIGHT,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_WIDTH,
                    Utility.TASK_BOARD_MARGIN + row * Utility.TASK_PIECE_HEIGHT);
        }
        for(int column = 0; column <= Utility.TASK_PIECE_SIZE; column += Utility.TASK_PIECE_PER_HOUR){
            gc.strokeLine(
                    Utility.TASK_BOARD_MARGIN + column * Utility.TASK_PIECE_WIDTH,
                    Utility.TASK_BOARD_MARGIN,
                    Utility.TASK_BOARD_MARGIN + column * Utility.TASK_PIECE_WIDTH,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_HEIGHT);
        }
        // 時刻を表示する
        gc.setFont(Font.font(16));
        gc.setFill(Color.BLACK);
        for(int hour = Utility.TASK_BOARD_FIRST_HOUR; hour <= Utility.TASK_BOARD_FIRST_HOUR + Utility.HOUR_PER_DAY; ++hour){
            int hour2 = hour % Utility.HOUR_PER_DAY;
            gc.fillText(
                    String.format("%d:00", hour2),
                    Utility.TASK_BOARD_MARGIN + (hour - Utility.TASK_BOARD_FIRST_HOUR) * Utility.TASK_PIECE_WIDTH * Utility.TASK_PIECE_PER_HOUR - 16,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_HEIGHT + 24);
        }
        // 既存のタスクを表示する(ドラッグ中のものは除く)
        gc.setStroke(Color.BLACK);
        for(int i = 0; i < expTaskList.size(); ++i){
            TaskInfo taskInfo  =expTaskList.get(i);
            double x = taskInfo.getX();
            double y = taskInfo.getY();
            double w = taskInfo.getW();
            double h = taskInfo.getH();
            if(i == draggedExpTaskIndex.getValue()){
                continue;
            }else if(i == selectedExpTaskIndex.getValue()){
                gc.setFill(Color.ORANGE);
            }else {
                gc.setFill(Color.LIGHTSKYBLUE);
            }
            gc.fillRect(x, y, w, h);
            gc.strokeRect(x, y, w, h);
        }
        // ドラッグ中のタスクを表示する
        if(draggedExpTaskIndex.getValue() != -1){
            // 選択されているタスク
            TaskInfo taskInfo = expTaskList.get(draggedExpTaskIndex.getValue());
            double x = dragMediumPointX.getValue() + draggedExpTaskOffsetX.getValue();
            double y = dragMediumPointY.getValue() + draggedExpTaskOffsetY.getValue();
            double w = taskInfo.getW();
            double h = taskInfo.getH();
            gc.setFill(Color.GREEN);
            gc.setGlobalAlpha(0.5);
            gc.fillRect(x, y, w, h);
            gc.strokeRect(x, y, w, h);
        }
        // 選択されているタスクの情報を表示する
        if(selectedExpTaskIndex.getValue() != -1){
            // 選択されているタスク
            TaskInfo taskInfo = expTaskList.get(selectedExpTaskIndex.getValue());
            // タスクの開始時刻を時：分形式に変換
            int allMinute = taskInfo.getTimePosition() * Utility.MIN_TASK_PIECE_TIME;
            int hour = ((allMinute / Utility.MINUTE_PER_HOUR) + Utility.TASK_BOARD_FIRST_HOUR) % Utility.HOUR_PER_DAY;
            int minute = allMinute % Utility.MINUTE_PER_HOUR;
            // 結果を表示
            Platform.runLater(() -> statusMessage.setValue(
                    String.format(
                            "%s(第%d艦隊,%d:%d)",
                            taskInfo.getName(),
                            taskInfo.getLane() + 1,
                            hour,
                            minute
                    )
            ));
        }
    }

    /**
     * コンストラクタ
     */
    public MainModel(Canvas taskBoard){
        this.taskBoard = taskBoard;
        // イベント処理
        selectedExpTaskIndex.addListener((b,o,n)->RedrawCanvasCommand(false));
        // テスト
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("海上護衛任務"), 0, 5));
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("海上護衛任務"), 1, 30));
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("長距離練習航海"), 0, 30));
    }
}
