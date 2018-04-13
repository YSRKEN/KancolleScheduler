package sample;

import javafx.fxml.FXML;

/**
 * MainViewとMainModelとを接続するViewModel
 */
public class MainViewModel {
    /**
     * MainViewと接続するモデル
     */
    private MainModel mainModel = new MainModel();

    /**
     * ViewModelを初期化
     */
    @FXML private void initialize(){

    }
}
