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
    public static final String SOFT_VERSION = "1.2.0";
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
     * 一時間は何分？
     */
    public static final int MINUTE_PER_HOUR = 60;
    /**
     * 一日は何時間？
     */
    public static final int HOUR_PER_DAY = 24;
    /**
     * 時間間隔が1時間で横に幾つ並ぶか？
     */
    public static final int TASK_PIECE_PER_HOUR = MINUTE_PER_HOUR / MIN_TASK_PIECE_TIME;
    /**
     * 時間間隔が24時間で横に幾つ並ぶか？
     */
    public static final int TASK_PIECE_SIZE = HOUR_PER_DAY * TASK_PIECE_PER_HOUR;
    /**
     * 1時間分の時間間隔のピクセル数
     */
    public static final double HOUR_TASK_PIECE_WIDTH = TASK_PIECE_PER_HOUR * TASK_PIECE_WIDTH;
    /**
     * TaskBoardが何時から始まるか？
     * (5だとAM5:00からということになる)
     */
    public static final int TASK_BOARD_FIRST_HOUR = 5;
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

    /**
     * 浮動小数点数を良い感じに文字列にする
     * @param x 浮動小数点数
     * @return 文字列にした結果
     */
    public static String DoubleToString(double x){
        return String.format("%f", x).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /**
     * マウスのX座標をタイミングに変換する
     * @param mouseX マウスのX座標
     * @return タイミング
     */
    public static int mouseXToTimePosition(double mouseX){
        return (int)Math.round((mouseX - TASK_BOARD_MARGIN) / TASK_PIECE_WIDTH);
    }
    /**
     * マウスのY座標を艦隊番号に変換する
     * @param mouseY マウスのY座標
     * @return 艦隊番号
     */
    public static int mouseYToLane(double mouseY){
        return (int)Math.round((mouseY - Utility.TASK_BOARD_MARGIN) / Utility.TASK_PIECE_HEIGHT);
    }
}
