package KCS.Model;

import KCS.Library.Utility;
import KCS.Store.ExpInfo;

/**
 * タスク情報を表すクラス
 */
public class TaskInfo {
    /**
     * タスクに割り当てられている遠征の情報
     */
    private ExpInfo expInfo;

    /**
     * 第何艦隊に割り当てられているか<br>
     * 第2艦隊→0、第3艦隊→1、第4艦隊→2
     */
    private int lane;
    /**
     * 時間間隔に区切られたうちのどのタイミングかを表す<br>
     * 例えば5分間隔で区切ったうち、頭から3時間後の場合、
     * 3×60/5-1=35と指定する
     */
    private int timePosition;

    /**
     * 第n艦隊かを返す
     * @return 第2艦隊→0、第3艦隊→1、第4艦隊→2
     */
    public int getLane() { return lane; }
    /**
     * どのタイミングかを返す
     * @return タイミング
     */
    public int getTimePosition() { return timePosition; }
    /**
     * 遠征の期間をTaskPiece単位で返す
     * @return 遠征の期間
     */
    public int getTimePositionwidth(){
        return expInfo.getTime() / Utility.MIN_TASK_PIECE_TIME;
    }
    /**
     * 終了タイミングを返す
     * @return 終了タイミング
     */
    public int getEndTimePosition(){
        return getTimePosition() + getTimePositionwidth();
    }
    /**
     * 第n艦隊かをセットする
     * @param lane 第2艦隊→0、第3艦隊→1、第4艦隊→2
     */
    public void setLane(int lane) {
        this.lane = lane;
    }
    /**
     * どのタイミングかをセットする
     * @param timePosition タイミング
     */
    public void setTimePosition(int timePosition) {
        this.timePosition = timePosition;
    }
    /**
     * タスクブロックにした際の横位置
     * @return 横位置
     */
    public double getX(){ return Utility.TASK_BOARD_MARGIN + timePosition * Utility.TASK_PIECE_WIDTH; }
    /**
     * タスクブロックにした際の縦位置
     * @return 縦位置
     */
    public double getY(){ return Utility.TASK_BOARD_MARGIN + lane * Utility.TASK_PIECE_HEIGHT; }
    /**
     * タスクブロックにした際の横幅
     * @return 横幅
     */
    public double getW(){ return expInfo.getTime() / Utility.MIN_TASK_PIECE_TIME * Utility.TASK_PIECE_WIDTH; }
    /**
     * タスクブロックにした際の縦幅
     * @return 縦幅
     */
    public double getH(){ return Utility.TASK_PIECE_HEIGHT; }
    /**
     * 遠征名を取得する
     * @return 遠征名
     */
    public String getName() { return expInfo.getName(); }

    /**
     * 遠征情報を文字列で返す
     * @return 遠征情報
     */
    @Override public String toString() { return expInfo.toString(); }

    /**
     * コンストラクタ
     * @param expInfo 割り当てられる遠征
     * @param lane 割り当てられる艦隊
     * @param timePosition 割り当てられるタイミング
     */
    public TaskInfo(ExpInfo expInfo, int lane, int timePosition){
        this.expInfo = expInfo;
        this.lane = lane;
        this.timePosition = timePosition;
    }
}
