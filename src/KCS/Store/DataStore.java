package KCS.Store;

import KCS.Library.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * データストア(遠征情報などを記録しておく)
 */
public class DataStore {
    /**
     * 遠征情報の一覧
     */
    private static final List<ExpInfo> expList = new ArrayList<>();

    /**
     * データストアを初期化
     */
    public static void initialize(){
        // ExpList.csvを読み込み、遠征情報一覧のデータを記憶する
        try(InputStream is = ClassLoader.getSystemResourceAsStream("KCS/File/ExpList.csv");
            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr)){
            // 1行読み込む
            String getLine;
            while((getLine = br.readLine()) != null){
                // カンマで区切る
                String[] token = getLine.split(",");
                // ヘッダー行なら飛ばす
                if(token[0].equals("No."))
                    continue;
                // 読み込みを行う
                String no               = token[0];
                String areaName      = token[1];
                int position         = Integer.parseInt(token[2]);
                String name          = token[3];
                int leaderLevel      = Integer.parseInt(token[4]);
                int sumLevel         = Integer.parseInt(token[5]);
                int minMemberSize    = Integer.parseInt(token[6]);
                String wantFleetType = token[7];
                int time             = Integer.parseInt(token[8]);
                int getFuel          = Integer.parseInt(token[9]);
                int getAmmo          = Integer.parseInt(token[10]);
                int getSteel         = Integer.parseInt(token[11]);
                int getBauxite       = Integer.parseInt(token[12]);
                double getLeftBucket    = Double.parseDouble(token[13]);
                double getLeftBurner    = Double.parseDouble(token[14]);
                double getLeftGear      = Double.parseDouble(token[15]);
                double getLeftCoin      = Double.parseDouble(token[16]);
                double getRightBucket   = Double.parseDouble(token[17]);
                double getRightBurner   = Double.parseDouble(token[18]);
                double getRightGear     = Double.parseDouble(token[19]);
                double getRightCoin     = Double.parseDouble(token[20]);
                int lostFuel         = Integer.parseInt(token[21]);
                int lostAmmo         = Integer.parseInt(token[22]);
                ExpInfo expInfo = new ExpInfo(no, areaName, position, name, leaderLevel,
                        sumLevel, minMemberSize, wantFleetType, time,
                        getFuel, getAmmo, getSteel, getBauxite, getLeftBucket,
                        getLeftBurner, getLeftGear, getLeftCoin, getRightBucket,
                        getRightBurner, getRightGear, getRightCoin, lostFuel, lostAmmo);
                expList.add(expInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 遠征情報を遠征名から取得する
     * @param name 遠征名
     * @return 遠征情報
     */
    public static ExpInfo getExpInfoFromName(String name){
        return expList.stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
    }
    /**
     * 遠征一覧をツリー形式で取得する(ツリー形式なのにLinkedHashMapって型名なのはご愛嬌)
     * @return 遠征一覧。expNameTree["海域名"] = {@code List<String> 遠征一覧}として取得できる
     */
    public static LinkedHashMap<String, List<String>> getExpNameTree(){
        // ここでLinkedHashMapとしたのは、キーの順序を投入順にしたかったため
        LinkedHashMap<String, List<String>> expNameTree = new LinkedHashMap<>();
        // 海域名を順に投入し、それぞれの海域名に適合する遠征を追加していく
        List<String> expAreaNameList = expList.stream().map(e -> e.getAreaName()).distinct().collect(Collectors.toList());
        for(String areaName : expAreaNameList) {
            // 遠征一覧を取得(長すぎる遠征は予め除いておく)
            List<String> expListOnArea = expList.stream()
                    .filter(e -> e.getAreaName().equals(areaName))
                    .filter(e -> e.getTime() <= Utility.MINUTE_PER_HOUR * Utility.HOUR_PER_DAY)
                    .map(e -> e.getName())
                    .collect(Collectors.toList());
            // 海域・遠征追加
            expNameTree.put(areaName, expListOnArea);
        }
        return expNameTree;
    }
}
