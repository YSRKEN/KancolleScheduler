package sample.Store;

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

    }
}
