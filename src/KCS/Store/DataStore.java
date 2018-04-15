package KCS.Store;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * データストア(遠征情報などを記録しておく)
 */
public class DataStore {
    public static final List<ExpInfo> ExpList = new ArrayList<ExpInfo>();

    /**
     * データストアを初期化
     */
    public static void initialize(){
        // ExpList.csvを読み込み、遠征情報一覧のデータを記憶する
        File file = null;
        try {
            file = new File(ClassLoader.getSystemResource("KCS/File/ExpList.csv").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        try(FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr)){
            // 1行読み込む
            String getLine;
            while((getLine = br.readLine()) != null){
                // カンマで区切る
                String[] token = getLine.split(",");
                // ヘッダー行なら飛ばす
                if(token[0].equals("No."))
                    continue;
                // 読み込みを行う
                int no               = Integer.parseInt(token[0]);
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
                ExpList.add(expInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
