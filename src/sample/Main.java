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
        Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
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
