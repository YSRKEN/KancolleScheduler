package KCS.Model;

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
     * タスクに割り当てられている遠征の情報を返す
     * @return 遠征情報
     */
    public ExpInfo getExpInfo() { return expInfo; }
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
