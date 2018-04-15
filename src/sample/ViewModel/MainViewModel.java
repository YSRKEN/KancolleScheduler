package sample.ViewModel;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import sample.Model.MainModel;

/**
 * MainViewとMainModelとを接続するViewModel
 */
public class MainViewModel {
    /**
     * 「終了」メニュー
     */
    @FXML private MenuItem ExitMenu;
    /**
     * 「バージョン情報」メニュー
     */
    @FXML private MenuItem AboutMenu;
    /**
     * 遠征タスクを表示するためのCanvas
     */
    @FXML private Canvas TaskBoard;
    /**
     * 情報表示用のラベル
     */
    @FXML private Label StatusMessage;

    /**
     * MainViewと接続するモデル
     */
    private MainModel mainModel = new MainModel();

    /**
     * ViewModelを初期化
     */
    @FXML private void initialize(){
        // プロパティのバインディング
        StatusMessage.textProperty().bind(mainModel.StatusMessage);
        // コマンドのバインディング
        ExitMenu.setOnAction(e -> mainModel.ExitCommand());
        AboutMenu.setOnAction(e -> mainModel.ShowVersionInfoCommand());
        TaskBoard.setOnDragDetected(e -> mainModel.TaskBoardDragDetected(e, this.TaskBoard));
        TaskBoard.setOnMouseDragOver(e -> mainModel.TaskBoardMouseDragOver(e, this.TaskBoard));
        TaskBoard.setOnMouseDragReleased(e -> mainModel.TaskBoardMouseDragReleased(e, this.TaskBoard));
    }
}
