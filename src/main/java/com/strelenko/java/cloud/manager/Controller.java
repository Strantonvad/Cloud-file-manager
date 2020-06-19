package com.strelenko.java.cloud.manager;

import javafx.application.Platform;
import javafx.event.ActionEvent;

public class Controller {

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }
}
