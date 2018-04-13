package sample;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

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
     * MainViewと接続するモデル
     */
    private MainModel mainModel = new MainModel();

    /**
     * ViewModelを初期化
     */
    @FXML private void initialize(){
        // コマンドのバインディング
        ExitMenu.setOnAction(e -> mainModel.ExitCommand());
        AboutMenu.setOnAction(e -> mainModel.ShowVersionInfoCommand());
    }
}
