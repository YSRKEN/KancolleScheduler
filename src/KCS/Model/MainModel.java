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
     * ドラッグ開始点
     */
    private final ObjectProperty<Double> dragStartPointX = new SimpleObjectProperty<>(0.0);
    private final ObjectProperty<Double> dragStartPointY = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ途中点
     */
    private final ObjectProperty<Double> dragMediumPointX = new SimpleObjectProperty<>(0.0);
    private final ObjectProperty<Double> dragMediumPointY = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ終了点
     */
    private final ObjectProperty<Double> dragEndPointX = new SimpleObjectProperty<>(0.0);
    private final ObjectProperty<Double> dragEndPointY = new SimpleObjectProperty<>(0.0);
    private final ObjectProperty<Integer> selectedExpTaskIndex = new SimpleObjectProperty<>(-1);

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
     * 情報表示用のテキストを変更する
     */
    private void redrawStatusMessage(){
        Platform.runLater(() -> statusMessage.setValue(
                String.format("マウス座標：(%d,%d)->(%d,%d)->(%d,%d)",
                        Math.round(dragStartPointX.getValue()),
                        Math.round(dragStartPointY.getValue()),
                        Math.round(dragMediumPointX.getValue()),
                        Math.round(dragMediumPointY.getValue()),
                        Math.round(dragEndPointX.getValue()),
                        Math.round(dragEndPointY.getValue())
                )
        ));
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
        //Canvasを再描画する
        RedrawCanvasCommand(false);
        //
        e.consume();
    }
    public void TaskBoardMouseClicked(MouseEvent e){
        // クリックした座標が、いずれかのタスクの上かを調べる
        double mouseX = e.getX();
        double mouseY = e.getY();
        for(int i = 0; i < expTaskList.size(); ++i) {
            TaskInfo taskInfo = expTaskList.get(i);
            if(taskInfo.getX() <= mouseX && mouseX <= taskInfo.getX() + taskInfo.getW()
                    && taskInfo.getY() <= mouseY && mouseY <= taskInfo.getY() + taskInfo.getH()){
                selectedExpTaskIndex.setValue(i);
                return;
            }
        }
        selectedExpTaskIndex.setValue(-1);
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
        for(int column = 0; column <= Utility.TASK_PIECE_SIZE; column += 60 / Utility.MIN_TASK_PIECE_TIME){
            gc.strokeLine(
                    Utility.TASK_BOARD_MARGIN + column * Utility.TASK_PIECE_WIDTH,
                    Utility.TASK_BOARD_MARGIN,
                    Utility.TASK_BOARD_MARGIN + column * Utility.TASK_PIECE_WIDTH,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_HEIGHT);
        }
        // 時刻を表示する
        gc.setFont(Font.font(16));
        gc.setFill(Color.BLACK);
        for(int hour = 5; hour <= 5 + 24; ++hour){
            int hour2 = hour % 24;
            gc.fillText(
                    String.format("%d:00", hour2),
                    Utility.TASK_BOARD_MARGIN + (hour - 5) * Utility.TASK_PIECE_WIDTH * 60 / Utility.MIN_TASK_PIECE_TIME - 16,
                    Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_HEIGHT + 24);
        }
        // 既存のタスクを表示する
        gc.setStroke(Color.BLACK);
        for(int i = 0; i < expTaskList.size(); ++i){
            TaskInfo taskInfo  =expTaskList.get(i);
            int x = taskInfo.getX();
            int y = taskInfo.getY();
            int w = taskInfo.getW();
            int h = taskInfo.getH();
            if(i == selectedExpTaskIndex.getValue()){
                gc.setFill(Color.ORANGE);
            }else {
                gc.setFill(Color.LIGHTSKYBLUE);
            }
            gc.fillRect(x, y, w, h);
            gc.strokeRect(x, y, w, h);
        }
        // 選択されているタスクの情報を表示する
        if(selectedExpTaskIndex.getValue() != -1){
            // 選択されているタスク
            TaskInfo taskInfo = expTaskList.get(selectedExpTaskIndex.getValue());
            // タスクの開始時刻を時：分形式に変換
            int allMinute = taskInfo.getTimePosition() * Utility.MIN_TASK_PIECE_TIME;
            int hour = ((allMinute / 60) + 5) % 24;
            int minute = allMinute % 60;
            // 結果を表示
            Platform.runLater(() -> statusMessage.setValue(
                    String.format(
                            "%s(第%d艦隊,%d:%d)",
                            taskInfo.getExpInfo().getName(),
                            taskInfo.getLane() + 1,
                            hour,
                            minute
                    )
            ));
        }
        //
        gc.setFill(Color.RED);
        gc.fillOval(dragStartPointX.getValue() - 10, dragStartPointY.getValue() - 10, 20, 20);
        gc.setFill(Color.BLUE);
        gc.setGlobalAlpha(0.5);
        gc.fillOval(dragMediumPointX.getValue() - 20, dragMediumPointY.getValue() - 20, 40, 40);
        gc.setGlobalAlpha(1.0);
    }

    /**
     * コンストラクタ
     */
    public MainModel(Canvas taskBoard){
        this.taskBoard = taskBoard;
        // マウスドラッグによるイベント処理
        dragStartPointX.addListener((b,o,n)->redrawStatusMessage());
        dragStartPointY.addListener((b,o,n)->redrawStatusMessage());
        dragMediumPointX.addListener((b,o,n)->redrawStatusMessage());
        dragMediumPointY.addListener((b,o,n)->redrawStatusMessage());
        dragEndPointX.addListener((b,o,n)->redrawStatusMessage());
        dragEndPointY.addListener((b,o,n)->redrawStatusMessage());
        // その他の行動によるイベント処理
        selectedExpTaskIndex.addListener((b,o,n)->RedrawCanvasCommand(false));
        // テスト
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("海上護衛任務"), 0, 5));
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("海上護衛任務"), 1, 30));
        expTaskList.add(new TaskInfo(DataStore.GetExpInfoFromName("長距離練習航海"), 0, 30));
    }
}
