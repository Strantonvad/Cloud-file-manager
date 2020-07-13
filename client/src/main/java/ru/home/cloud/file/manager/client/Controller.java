package ru.home.cloud.file.manager.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import ru.home.cloud.file.manager.common.FileActions;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static Path serverPath;
    @FXML
    VBox leftPanel, rightPanel;
    NettyClient nettyClient = LoginController.getNettyClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serverPath = Paths.get("./", "server-files");
        System.out.println(serverPath);
        PanelController serverPC = (PanelController) rightPanel.getProperties().get("ctrl");
        serverPC.updateList(serverPath.normalize().toAbsolutePath());
        System.out.println("Файловый менеджер запущен");
    }

    public void copyAction(ActionEvent actionEvent) throws IOException {
        PanelController clientPC = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController serverPC = (PanelController) rightPanel.getProperties().get("ctrl");
        if (clientPC.getSelectedFileName() == null && serverPC.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ничего не выбрано", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if (clientPC.getSelectedFileName() != null) {
            FileActions.sendFile(Paths.get(clientPC.getAbsolutePathSelectedFile()), nettyClient.getChannel(), future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл " + clientPC.getSelectedFileName() + " успешно передан");
                    serverPC.updateList(Paths.get(serverPC.getCurrentPath()));
                }
            });
        }
        if (serverPC.getSelectedFileName() != null) {
            FileActions.requestFile(serverPC.getSelectedFileName(), nettyClient.getChannel());
            try {
                Thread.sleep(1000);
                clientPC.updateList(Paths.get(clientPC.getCurrentPath()));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void deleteAction(ActionEvent actionEvent) throws IOException {
        PanelController clientPC = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController serverPC = (PanelController) rightPanel.getProperties().get("ctrl");
        if (clientPC.getSelectedFileName() == null && serverPC.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ничего не выбрано", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        PanelController srcPC = clientPC.getSelectedFileName() != null ? clientPC : serverPC;

        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFileName());

        try {
            if (!Files.isDirectory(srcPath)) {
                Files.delete(srcPath);
            }
            srcPC.updateList(Paths.get(srcPC.getCurrentPath()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не получилось удалить", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnExitAction(ActionEvent actionEvent) throws IOException {
        nettyClient.close();
        Platform.exit();
    }
}

