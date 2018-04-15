package KCS.Library;

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
     * 24時間を区切る最小間隔(時間間隔)を分刻みで指定する
     */
    public static final int MIN_TASK_PIECE_TIME = 5;
    /**
     * 1つの時間間隔における横ピクセル数
     */
    public static final double TASK_PIECE_WIDTH = 5;
    /**
     * 1つの時間間隔における縦ピクセル数
     */
    public static final double TASK_PIECE_HEIGHT = 100;
    /**
     * 遠征に出撃できる最大艦隊数
     */
    public static final int LANES = 3;
    /**
     * 時間間隔が24時間で横に幾つ並ぶか？
     */
    public static final int TASK_PIECE_SIZE = 24 * 60 / MIN_TASK_PIECE_TIME;
    /**
     * TaskBoardの横幅
     */
    public static final double TASK_BOARD_WIDTH = TASK_PIECE_WIDTH * TASK_PIECE_SIZE;
    /**
     * TaskBoardの縦幅
     */
    public static final double TASK_BOARD_HEIGHT = TASK_PIECE_HEIGHT * LANES;
    /**
     * 時刻表示をするためのスペース
     */
    public static final double ADD_TASK_BOARD_HEIGHT = 24;
    /**
     * 周囲へのマージン
     */
    public static final double TASK_BOARD_MARGIN = 20;
    /**
     * Canvasの横幅
     */
    public static final double CANVAS_WIDTH = TASK_BOARD_WIDTH + TASK_BOARD_MARGIN * 2;
    /**
     * Canvasの縦幅
     */
    public static final double CANVAS_HEIGHT = TASK_BOARD_HEIGHT + ADD_TASK_BOARD_HEIGHT + TASK_BOARD_MARGIN * 2;

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
