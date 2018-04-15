package sample.Lib;

import javafx.scene.control.Alert;

/**
 * ユーティリティクラス
 */
public class Utility {
    /**
     * ソフトウェア名
     */
    public static final String SOFT_NAME = "艦これスケジューラー";
    /**
     * バージョン
     */
    public static final String SOFT_VERSION = "1.0";

    /**
     * ダイアログを表示
     * @param contentText 本文
     * @param headerText タイトル文
     * @param alertType ダイアログの種類
     */
    public static void ShowDialog(String contentText, String headerText, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.setTitle(SOFT_NAME);
        alert.show();
    }
}
