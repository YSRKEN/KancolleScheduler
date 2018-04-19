package KCS.ViewModel;

import KCS.Library.Utility;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import KCS.Model.MainModel;
import javafx.scene.control.ScrollPane;

/**
 * MainViewとMainModelとを接続するViewModel
 */
public class MainViewModel {
    /**
     * 「開く」メニュー
     */
    @FXML private MenuItem LoadFileMenu;
    /**
     * 「保存」メニュー
     */
    @FXML private MenuItem SaveFileMenu;
    /**
     * 「終了」メニュー
     */
    @FXML private MenuItem ExitMenu;
    /**
     * 「情報表示」メニュー
     */
    public MenuItem ShowInfoMenu;
    /**
     * 「全削除」メニュー
     */
    public MenuItem AllDeleteMenu;
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
     * コンテキストメニュー
     */
    @FXML private ContextMenu TaskBoardMenu;

    /**
     * MainViewと接続するモデル
     */
    private MainModel mainModel;

    /**
     * ViewModelを初期化
     */
    @FXML private void initialize(){
        mainModel = new MainModel(TaskBoard, TaskBoardMenu);
        // コントロールの大きさを直接指定
        TaskBoard.setWidth(Utility.CANVAS_WIDTH);
        TaskBoard.setHeight(Utility.CANVAS_HEIGHT);
        // プロパティのバインディング
        StatusMessage.textProperty().bind(mainModel.StatusMessage);
        // コマンドのバインディング
        LoadFileMenu.setOnAction(e -> mainModel.LoadCommand());
        SaveFileMenu.setOnAction(e -> mainModel.SaveCommand());
        ShowInfoMenu.setOnAction(e-> mainModel.ShowInfoCommand());
        AllDeleteMenu.setOnAction(e -> mainModel.AllDeleteCommand());
        ExitMenu.setOnAction(e -> mainModel.ExitCommand());
        AboutMenu.setOnAction(e -> mainModel.ShowVersionInfoCommand());
        TaskBoard.setOnDragDetected(e -> mainModel.TaskBoardDragDetected(e));
        TaskBoard.setOnMouseDragOver(e -> mainModel.TaskBoardMouseDragOver(e));
        TaskBoard.setOnMouseDragReleased(e -> mainModel.TaskBoardMouseDragReleased(e));
        TaskBoard.setOnMouseClicked(e -> mainModel.TaskBoardMouseClicked(e));
        // テスト
        mainModel.RedrawCanvasCommand(false);
    }
}
