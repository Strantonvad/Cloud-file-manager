package ru.home.cloud.file.manager.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;

public class Controller {
    NettyClient nettyClient = LoginController.getNettyClient();

    public void btnExitAction(ActionEvent actionEvent) {
        nettyClient.close();
        Platform.exit();
    }
}

