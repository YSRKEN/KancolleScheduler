package sample.Store;

/**
 * 遠征情報を表すクラス
 */
public class ExpInfo {
    // フィールド群
    private final int no;
    private final String areaName;
    private final int position;
    private final String name;
    private final int leaderLevel;
    private final int sumLevel;
    private final int minMemberSize;
    private final String wantFleetType;
    private final int time;
    private final int getFuel, getAmmo, getSteel, getBauxite;
    private final int getLeftBucket, getLeftBurner, getLeftGear, getLeftCoin;
    private final int getRightBucket, getRightBurner, getRightGear, getRightCoin;
    private final int lostFuel, lostAmmo;

    /**
     * 遠征情報を文字列化する
     * @return 戻り値の例：<br>
     * 【鎮守府海域】海上護衛任務<br>
     * 旗艦Lv：3　合計Lv：0　最小隻数：4<br>
     * 必要艦種：軽1,駆2　遠征時間：90分<br>
     * 通常資材：燃料200,弾薬200,鋼材20,ボーキ20<br>
     * 特殊資材(通常)：なし<br>
     * 特殊資材(大成功)：なし<br>
     * 消費資材：燃料35
     */
    @Override public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s】%s", areaName, name));
        sb.append(String.format("%n旗艦Lv：%d　合計Lv：%s　最小隻数：%d", leaderLevel, (sumLevel > 0 ? Integer.toString(sumLevel) : "―"), minMemberSize));
        sb.append(String.format("%n必要艦種：%s　遠征時間：%d分", wantFleetType, time));
        return sb.toString();
    }

    /**
     * コンストラクタ
     * @param no 遠征番号
     * @param areaName 遠征海域名
     * @param position 遠征位置
     * @param name 遠征名
     * @param leaderLevel 最小旗艦レベル
     * @param sumLevel 最小合計レベル
     * @param minMemberSize 最少隻数
     * @param wantFleetType 必要艦種
     * @param time 遠征時間(分)
     * @param getFuel 報酬燃料
     * @param getAmmo 報酬弾薬
     * @param getSteel 報酬鉄鋼
     * @param getBauxite 報酬ボーキサイト
     * @param getLeftBucket 報酬高速修復材(通常)
     * @param getLeftBurner 報酬高速建造材(通常)
     * @param getLeftGear 報酬開発資材(通常)
     * @param getLeftCoin 報酬家具コイン(通常)
     * @param getRightBucket 報酬高速修復材(大成功)
     * @param getRightBurner 報酬高速建造材(大成功)
     * @param getRightGear 報酬開発資材(大成功)
     * @param getRightCoin 報酬家具コイン(大成功)
     * @param lostFuel 最小消費燃料
     * @param lostAmmo 最小消費弾薬
     */
    public ExpInfo(int no, String areaName, int position, String name, int leaderLevel,
                   int sumLevel, int minMemberSize, String wantFleetType, int time,
                   int getFuel, int getAmmo, int getSteel, int getBauxite, int getLeftBucket,
                   int getLeftBurner, int getLeftGear, int getLeftCoin, int getRightBucket,
                   int getRightBurner, int getRightGear, int getRightCoin, int lostFuel, int lostAmmo) {
        this.no = no;
        this.areaName = areaName;
        this.position = position;
        this.name = name;
        this.leaderLevel = leaderLevel;
        this.sumLevel = sumLevel;
        this.minMemberSize = minMemberSize;
        this.wantFleetType = wantFleetType;
        this.time = time;
        this.getFuel = getFuel;
        this.getAmmo = getAmmo;
        this.getSteel = getSteel;
        this.getBauxite = getBauxite;
        this.getLeftBucket = getLeftBucket;
        this.getLeftBurner = getLeftBurner;
        this.getLeftGear = getLeftGear;
        this.getLeftCoin = getLeftCoin;
        this.getRightBucket = getRightBucket;
        this.getRightBurner = getRightBurner;
        this.getRightGear = getRightGear;
        this.getRightCoin = getRightCoin;
        this.lostFuel = lostFuel;
        this.lostAmmo = lostAmmo;
    }
}
