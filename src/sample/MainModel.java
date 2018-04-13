package sample;

import javafx.scene.control.Alert;

/**
 * MainViewと接続されるModel
 */
public class MainModel {
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
}
