package KCS.Store;

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
    private final double getLeftBucket, getLeftBurner, getLeftGear, getLeftCoin;
    private final double getRightBucket, getRightBurner, getRightGear, getRightCoin;
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
        // 基本情報
        sb.append(String.format("【%s】%s", areaName, name));
        // 必要な艦娘に関する情報
        sb.append(String.format("%n旗艦Lv：%d", leaderLevel));
        sb.append(String.format("　合計Lv：%s", (sumLevel > 0 ? Integer.toString(sumLevel) : "―")));
        sb.append(String.format("　最小隻数：%d", minMemberSize));
        sb.append(String.format("%n必要艦種：%s　遠征時間：%d分", wantFleetType, time));
        // 報酬資材に関する情報-1
        sb.append(String.format("%n通常資材："));
        {
            int count = 0;
            if(getFuel != 0){
                sb.append(String.format("燃料%d", getFuel));
                ++count;
            }
            if(getAmmo != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("弾薬%d", getAmmo));
                ++count;
            }
            if(getSteel != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("鋼材%d", getSteel));
                ++count;
            }
            if(getBauxite != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("ボーキ%d", getBauxite));
                ++count;
            }
            if(count == 0)
                sb.append("なし");
        }
        // 報酬資材に関する情報-2
        sb.append(String.format("%n特殊資材(通常)："));
        {
            int count = 0;
            if(getLeftBucket != 0){
                // Javaの%g指定がアホで末尾0抑制しやがらないので、
                // 正規表現を使って末尾0抑制をスクラッチした
                sb.append(String.format("バケツ%f", getLeftBucket)
                        .replaceAll("0+$", "").replaceAll("\\.$", ""));
                ++count;
            }
            if(getLeftBurner != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("バーナー%f", getLeftBurner)
                        .replaceAll("0+$", "").replaceAll("\\.$", ""));
                ++count;
            }
            if(getLeftGear != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("開発資材%f", getLeftGear)
                        .replaceAll("0+$", "").replaceAll("\\.$", ""));
                ++count;
            }
            if(getLeftCoin != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("家具コイン%f", getLeftCoin)
                        .replaceAll("0+$", "").replaceAll("\\.$", ""));
                ++count;
            }
            if(count == 0)
                sb.append("なし");
        }
        // 報酬資材に関する情報-3
        sb.append(String.format("%n特殊資材(大成功)："));
        {
            int count = 0;
            if(getRightBucket != 0){
                sb.append(String.format("バケツ%f", getRightBucket)
                        .replaceAll("0+$", "").replaceAll("\\.$", ""));
                ++count;
            }
            if(getRightBurner != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("バーナー%f", getRightBurner)
                        .replaceAll("0+$", "").replaceAll("\\.$", ""));
                ++count;
            }
            if(getRightGear != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("開発資材%f", getRightGear)
                        .replaceAll("0+$", "").replaceAll("\\.$", ""));
                ++count;
            }
            if(getRightCoin != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("家具コイン%f", getRightCoin)
                        .replaceAll("0+$", "").replaceAll("\\.$", ""));
                ++count;
            }
            if(count == 0)
                sb.append("なし");
        }
        // 消費資材に関する情報-1
        sb.append(String.format("%n消費資材："));
        {
            int count = 0;
            if(lostFuel != 0){
                sb.append(String.format("燃料%d", lostFuel));
                ++count;
            }
            if(lostAmmo != 0){
                if(count > 0) sb.append(",");
                sb.append(String.format("弾薬%d", lostAmmo));
                ++count;
            }
            if(count == 0)
                sb.append("なし");
        }
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
                   int getFuel, int getAmmo, int getSteel, int getBauxite, double getLeftBucket,
                   double getLeftBurner, double getLeftGear, double getLeftCoin, double getRightBucket,
                   double getRightBurner, double getRightGear, double getRightCoin, int lostFuel, int lostAmmo) {
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

    /**
     * 遠征名を取得
     * @return 遠征名
     */
    public String getName(){ return name; }

    /**
     * 遠征時間を取得
     * @return 遠征時間(分)
     */
    public int getTime(){ return time; }

    /**
     * 海域名を取得
     * @return 海域名
     */
    public String getAreaName() { return areaName; }

    /**
     * 燃料の収益
     * @param addPer 加算％
     * @param ciFlg 大成功するか？
     * @return 燃料の収益
     */
    public long fuelValue(int addPer, boolean ciFlg){
        return Math.round(getFuel * (ciFlg ? 1.5 : 1.0) * (1.0 * (100 + addPer) / 100)) - lostFuel;
    }
    /**
     * 弾薬の収益
     * @param addPer 加算％
     * @param ciFlg 大成功するか？
     * @return 弾薬の収益
     */
    public long ammoValue(int addPer, boolean ciFlg){
        return Math.round(getAmmo * (ciFlg ? 1.5 : 1.0) * (1.0 * (100 + addPer) / 100)) - lostAmmo;
    }
    /**
     * 鋼材の収益
     * @param addPer 加算％
     * @param ciFlg 大成功するか？
     * @return 鋼材の収益
     */
    public long steelValue(int addPer, boolean ciFlg){
        return Math.round(getSteel * (ciFlg ? 1.5 : 1.0) * (1.0 * (100 + addPer) / 100));
    }
    /**
     * ボーキの収益
     * @param addPer 加算％
     * @param ciFlg 大成功するか？
     * @return ボーキの収益
     */
    public long bauxValue(int addPer, boolean ciFlg){
        return Math.round(getBauxite * (ciFlg ? 1.5 : 1.0) * (1.0 * (100 + addPer) / 100));
    }

    /**
     * バケツの収益
     * @param ciFlg 大成功するか？
     * @return バケツの収益
     */
    public double bucketValue(boolean ciFlg){
        return getLeftBucket + (ciFlg ? getRightBucket : 0.0);
    }
    /**
     * バーナーの収益
     * @param ciFlg 大成功するか？
     * @return バーナーの収益
     */
    public double burnerValue(boolean ciFlg){
        return getLeftBurner + (ciFlg ? getRightBurner : 0.0);
    }
    /**
     * 開発資材の収益
     * @param ciFlg 大成功するか？
     * @return 開発資材の収益
     */
    public double gearValue(boolean ciFlg){
        return getLeftGear + (ciFlg ? getRightGear : 0.0);
    }
    /**
     * 家具コインの収益
     * @param ciFlg 大成功するか？
     * @return 家具コインの収益
     */
    public double coinValue(boolean ciFlg){
        return getLeftCoin + (ciFlg ? getRightCoin : 0.0);
    }
}
