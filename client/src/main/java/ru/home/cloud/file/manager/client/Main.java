package ru.home.cloud.file.manager.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws Exception{
        scene = new Scene(loadFXML("/login"));
        stage.setTitle("Java Cloud Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML (String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
//    @Override
//    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
//        primaryStage.setTitle("Java Cloud Manager");
//        primaryStage.setScene(new Scene(root, 1024, 600));
//        primaryStage.show();
//    }

    public static void main(String[] args) {
        launch(args);
    }
}
