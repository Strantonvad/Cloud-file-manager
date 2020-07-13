package ru.home.cloud.file.manager.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("run fxml login");
    }

    public void connectAction(ActionEvent actionEvent) throws IOException {
        Main.setRoot("/main");
    }
}
