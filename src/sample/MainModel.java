package sample;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * MainViewと接続されるModel
 */
public class MainModel {
    // publicなプロパティ
    /**
     * 情報表示用のテキスト
     */
    private ObjectProperty<String> statusMessage = new SimpleObjectProperty<>();
    public ReadOnlyObjectProperty<String> StatusMessage = statusMessage;

    // privateなプロパティ
    /**
     * ドラッグ開始点
     */
    private ObjectProperty<Double> dragStartPointX = new SimpleObjectProperty<>(0.0);
    private ObjectProperty<Double> dragStartPointY = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ途中点
     */
    private ObjectProperty<Double> dragMediumPointX = new SimpleObjectProperty<>(0.0);
    private ObjectProperty<Double> dragMediumPointY = new SimpleObjectProperty<>(0.0);
    /**
     * ドラッグ終了点
     */
    private ObjectProperty<Double> dragEndPointX = new SimpleObjectProperty<>(0.0);
    private ObjectProperty<Double> dragEndPointY = new SimpleObjectProperty<>(0.0);

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

    /**
     * TaskBoardを再描画する
     * @param p TaskBoard
     * @param mediumFlg trueなら、移動中の中間状態なオブジェクトを表示。<br>falseなら中間状態なオブジェクトを確定
     */
    private void redrawCanvas(Canvas p, boolean mediumFlg){
        // グラフィックスコンテキストを作成
        GraphicsContext gc = p.getGraphicsContext2D();
        // 描画指示を送る
        gc.clearRect(0,0,800,800);
        gc.setFill(Color.RED);
        gc.fillOval(dragStartPointX.getValue() - 10, dragStartPointY.getValue() - 10, 20, 20);
        gc.setFill(Color.BLUE);
        gc.setGlobalAlpha(0.5);
        gc.fillOval(dragMediumPointX.getValue() - 20, dragMediumPointY.getValue() - 20, 40, 40);
        gc.setGlobalAlpha(1.0);
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
    public void TaskBoardDragDetected(MouseEvent e, Canvas p){
        // ドラッグ開始点の座標を記録する
        dragStartPointX.setValue(e.getX());
        dragStartPointY.setValue(e.getY());
        // ドラッグイベントを許可する
        p.startFullDrag();
        e.consume();
    }
    /**
     * TaskBoard上でマウスによるドラッグの中間状態のイベント
     * @param e ドラッグイベント
     */
    public void TaskBoardMouseDragOver(MouseDragEvent e, Canvas p){
        // ドラッグ途中点の座標を記録する
        dragMediumPointX.setValue(e.getX());
        dragMediumPointY.setValue(e.getY());
        //Canvasを再描画する
        redrawCanvas(p, true);
        //
        e.consume();
    }
    /**
     * TaskBoard上でマウスによるドラッグを終了した際のイベント
     * @param e ドラッグイベント
     */
    public void TaskBoardMouseDragReleased(MouseDragEvent e, Canvas p){
        // ドラッグ終了点の座標を記録する
        dragEndPointX.setValue(e.getX());
        dragEndPointY.setValue(e.getY());
        //Canvasを再描画する
        redrawCanvas(p, false);
        //
        e.consume();
    }

    /**
     * コンストラクタ
     */
    public MainModel(){
        // マウスドラッグによるイベント処理
        dragStartPointX.addListener((b,o,n)->redrawStatusMessage());
        dragStartPointY.addListener((b,o,n)->redrawStatusMessage());
        dragMediumPointX.addListener((b,o,n)->redrawStatusMessage());
        dragMediumPointY.addListener((b,o,n)->redrawStatusMessage());
        dragEndPointX.addListener((b,o,n)->redrawStatusMessage());
        dragEndPointY.addListener((b,o,n)->redrawStatusMessage());
    }
}
