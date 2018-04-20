package KCS.Model;

import KCS.Library.Utility;
import KCS.Store.DataStore;
import KCS.Store.ExpInfo;

/**
 * タスク情報を表すクラス
 */
class TaskInfo implements Cloneable {
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
    public int getTimePositionWidth(){
        return expInfo.getTime() / Utility.MIN_TASK_PIECE_TIME;
    }
    /**
     * 終了タイミングを返す
     * @return 終了タイミング
     */
    public int getEndTimePosition(){
        return (getTimePosition() + getTimePositionWidth()) % Utility.TASK_PIECE_SIZE;
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
    public ExpInfo getExpInfo(){ return expInfo; }

    /**
     * 遠征情報を文字列で返す
     * @return 遠征情報
     */
    @Override public String toString() { return expInfo.toString(); }
    /**
     * cloneメソッドをオーバライド
     * @return 自身と同じ内容を持つインスタンス
     */
    @Override public TaskInfo clone(){
        TaskInfo result = null;
        try {
            result = (TaskInfo)super.clone();
            result.expInfo = this.expInfo.clone();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 遠征タスクの情報をCSV形式で返す
     * @return 遠征タスクの情報
     */
    public String toCsvData(){
        return String.format("%s,%d,%d%n", this.getName(), this.getLane(), this.getTimePosition());
    }

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
    /**
     * コンストラクタ
     * @param csvData CSVの1行
     */
    public TaskInfo(String csvData){
        // 3つに分割できなければアウト
        String[] temp = csvData.split(",");
        if(temp.length < 3)
            throw new NumberFormatException("CSVの列数に異常があります。");
        String name = temp[0];
        // パースできない or 範囲がおかしい場合はアウト
        int lane = Integer.parseInt(temp[1]);
        if(lane < 0 || lane >= Utility.LANES)
            throw new NumberFormatException("艦隊番号の指定に異常があります。");
        int timePosition = Integer.parseInt(temp[2]);
        if(timePosition < 0 || timePosition >= Utility.TASK_PIECE_SIZE)
            throw new NumberFormatException("タイミングの指定に異常があります。");
        // 遠征名から遠征情報を取り出せない場合はアウト
        ExpInfo expInfo = DataStore.getExpInfoFromName(name);
        if(expInfo == null)
            throw new NumberFormatException("遠征名の指定に異常があります。");
        // 正常に初期化される
        this.expInfo = expInfo;
        this.lane = lane;
        this.timePosition = timePosition;
    }
}
