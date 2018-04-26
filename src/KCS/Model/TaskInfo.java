package KCS.Model;

import KCS.Library.Utility;
import KCS.Store.DataStore;
import KCS.Store.ExpInfo;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * タスク情報を表すクラス
 */
class TaskInfo implements Cloneable {
    // フィールド変数
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
     * 大発による収益増加率(％単位)
     */
    private int addPer;
    /**
     * 大成功するならtrue
     */
    private boolean ciFlg;
    /**
     * 結婚艦を投入しているならtrue
     */
    private boolean marriageFlg;

    // 単なるgetter/setter
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
    public boolean getCiFlg() { return this.ciFlg; }
    public boolean getMarriageFlg() { return this.marriageFlg; }
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
    public void setAddPer(int addPer) { this.addPer = addPer; }
    public void setCiFlg(boolean ciFlg) { this.ciFlg = ciFlg; }
    public void setMarriageFlg(boolean marriageFlg) { this.marriageFlg = marriageFlg; }

    // 計算を伴うgetter
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
     * 遠征名を取得する
     * @return 遠征名
     */
    public String getName() { return expInfo.getName(); }
    /**
     * 遠征色を取得-1
     * @return 遠征色
     */
    public Color getFirstColor(){return expInfo.getFirstColor();}
    /**
     * 遠征色を取得-2
     * @return 遠征色
     */
    public Color getLastColor(){return expInfo.getLastColor();}
    /**
     * 獲得燃料を返す
     * @return 獲得燃料
     */
    public long fuelValue(){ return expInfo.fuelValue(addPer, ciFlg, marriageFlg); }
    /**
     * 獲得弾薬を返す
     * @return 獲得弾薬
     */
    public long ammoValue(){ return expInfo.ammoValue(addPer, ciFlg, marriageFlg); }
    /**
     * 獲得鋼材を返す
     * @return 獲得鋼材
     */
    public long steelValue(){ return expInfo.steelValue(addPer, ciFlg); }
    /**
     * 獲得ボーキを返す
     * @return 獲得ボーキ
     */
    public long bauxValue(){ return expInfo.bauxValue(addPer, ciFlg); }
    /**
     * 獲得バケツを返す
     * @return 獲得バケツ
     */
    public double bucketValue(){ return expInfo.bucketValue(ciFlg); }
    /**
     * 獲得バーナーを返す
     * @return 獲得バーナー
     */
    public double burnerValue(){ return expInfo.burnerValue(ciFlg); }
    /**
     * 獲得開発資材を返す
     * @return 獲得開発資材
     */
    public double gearValue(){ return expInfo.gearValue(ciFlg); }
    /**
     * 獲得家具コインを返す
     * @return 獲得家具コイン
     */
    public double coinValue(){ return expInfo.coinValue(ciFlg); }
    /**
     * 遠征タスクの情報をCSV形式で返す
     * @return 遠征タスクの情報
     */
    public String toCsvData(){
        return String.format("%s,%d,%d,%d,%d,%d%n",
                this.getName(), this.getLane(), this.getTimePosition(), this.addPer,
                this.ciFlg ? 1 : 0, this.marriageFlg ? 1 : 0);
    }
    /**
     * Pair(タスク左座標, タスク幅)のListを返す
     * 煩雑な処理を軽減することが狙い
     */
    public List<Pair<Integer, Integer>> getTimePositionWidthList(){
        List<Pair<Integer, Integer>> list = new ArrayList<>();
        if(this.getTimePosition() <= this.getEndTimePosition()) {
            // タスクが分割されていない場合
            list.add(new Pair<>(this.getTimePosition(), this.getTimePositionWidth()));
        }else{
            // タスクが分割されている場合
            list.add(new Pair<>(this.getTimePosition(), Utility.TASK_PIECE_SIZE - this.getTimePosition()));
            list.add(new Pair<>(0, this.getEndTimePosition()));
        }
        return list;
    }
    /**
     * 2つのタスクが横方向に重複しているかを判定する
     * @param other もう一つのタスク
     * @return 重複していればtrue
     */
    public boolean isInterference(TaskInfo other) {
        List<Pair<Integer, Integer>> list = this.getTimePositionWidthList();
        // もう一つのタスクの左端が、このタスクの中に含まれていた場合は重複
        if(list.stream().anyMatch(p ->
            p.getKey() <= other.getTimePosition()
            && other.getTimePosition() < p.getKey() + p.getValue())){
            return true;
        }
        // もう一つのタスクの右端が、このタスクの中に含まれていた場合は重複
        if(list.stream().anyMatch(p ->
            p.getKey() < other.getEndTimePosition()
            && other.getEndTimePosition() <= p.getKey() + p.getValue())){
            return true;
        }
        return false;
    }

    // TaskBoardに置く際の座標関係のメソッド
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
     * Pair(ピクセル左座標, ピクセル幅)のListを返す
     * 煩雑な処理を軽減することが狙い
     */
    public List<Pair<Double, Double>> getXWList(){
        List<Pair<Double, Double>> list = new ArrayList<>();
        if(this.getTimePosition() <= this.getEndTimePosition()) {
            // タスクが分割されていない場合
            list.add(new Pair<>(getX(), getW()));
        }else{
            // タスクが分割されている場合
            list.add(new Pair<>(getX(), Utility.TASK_BOARD_MARGIN + Utility.TASK_BOARD_WIDTH - getX()));
            list.add(new Pair<>(Utility.TASK_BOARD_MARGIN, getX() + getW() - Utility.TASK_BOARD_MARGIN - Utility.TASK_BOARD_WIDTH));
        }
        return list;
    }
    /**
     * 入力座標が当該タスク内かを判定する
     * @param mouseX 入力座標X
     * @param mouseY 入力座標Y
     * @return 入っていればtrue(境界線上はtrue扱い)
     */
    public boolean isInnerPoint(double mouseX, double mouseY){
        if(mouseY < getY() || getY() + getH() < mouseY)
            return false;
        return getXWList().stream().anyMatch(p -> (p.getKey() <= mouseX && mouseX <= p.getKey() + p.getValue()));
    }

    // オーバライドされたメソッド
    /**
     * 遠征情報を文字列で返す
     * @return 遠征情報
     */
    @Override public String toString() { return expInfo.toString(); }
    /**
     * cloneメソッドをオーバライド
     * 実装の参考：https://qiita.com/SUZUKI_Masaya/items/8da8c0038797f143f5d3
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
     * equalsメソッドをオーバライド
     * 実装の参考：http://education.yachinco.net/tips/java/01/3.html
     * @param other 判定したいオブジェクト
     * @return 判定結果
     */
    @Override public boolean equals(Object other){
        if(this == other)
            return true;
        if (!(other instanceof TaskInfo))
            return false;
        TaskInfo otherTaskInfo = (TaskInfo) other;
        if(this.timePosition != otherTaskInfo.timePosition)
            return false;
        if(this.lane != otherTaskInfo.lane)
            return false;
        return this.getName().equals(otherTaskInfo.getName());
    }
    /**
     * hashCodeメソッドをオーバライド
     * 実装の参考：http://education.yachinco.net/tips/java/01/4.html
     * @return equals()がtrueになるなら必ずhashCode()は等しい
     */
    @Override public int hashCode(){
        return (this.lane << 10) + this.timePosition;
    }

    // コンストラクタ
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
        // 収益増加率および大成功フラグおよびケッコン艦フラグは、取り出せた場合にのみ取り出す
        int addPer = 0;
        boolean ciFlg = false;
        boolean marriageFlg = false;
        if(temp.length >= 4){
            if(temp.length < 6)
                throw new NumberFormatException("CSVの列数に異常があります。");
            addPer = Integer.parseInt(temp[3]);
            addPer = (addPer / 5) * 5;
            addPer = (addPer < 0 ? 0 : addPer > 20 ? 20 : addPer);
            ciFlg = Integer.parseInt(temp[4]) > 0;
            marriageFlg = Integer.parseInt(temp[5]) > 0;
        }
        // 正常に初期化される
        this.expInfo = expInfo;
        this.lane = lane;
        this.timePosition = timePosition;
        this.addPer = addPer;
        this.ciFlg = ciFlg;
        this.marriageFlg = marriageFlg;
    }
}
