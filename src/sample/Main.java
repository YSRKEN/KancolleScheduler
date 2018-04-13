package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Mainクラス
 */
public class Main extends Application {

    /**
     * JavaFXのGUIを立ち上げる
     * @param primaryStage 基本となるStage
     * @throws Exception 例外が投げられたらアプリが終了する
     */
    @Override public void start(Stage primaryStage) throws Exception{
        // FXMLを読み込む
        Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        // タイトルを設定する
        primaryStage.setTitle("艦これスケジューラー");
        // シーングラフとウィンドウの大きさを設定する
        primaryStage.setScene(new Scene(root, 600, 400));
        // ×ボタンを押した際の挙動を設定する
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        // Stageを表示する
        primaryStage.show();
    }

    /**
     * mainメソッド
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        launch(args);
    }
}
