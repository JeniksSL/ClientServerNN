
package com.clientservernn.server.guiFX;

import com.clientservernn.common.Dialogues;
import com.clientservernn.common.CharsetList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

public class DialogHandler {
    Dialogues type;
    Optional<String> returnValue = Optional.empty();
    String title;
    ArrayList<Control> controlArrayList = new ArrayList();
    Label responseName = null;
    TextField responseText = null;
    ComboBox<String> networkList = null;

    public DialogHandler(Dialogues type) {
        this.type = type;
        switch (type) {
            case SERVER_STOP:
                this.title = "Stop server";
                this.responseName = new Label("Are you sure to stop server");
                this.controlArrayList.add(this.responseName);
                break;
            case SERVER_START:
                this.title = "Start server";
                this.responseName = new Label("Server Port:");
                this.responseText = new TextField(String.valueOf(ServerController.getServerPort()));
                this.controlArrayList.add(this.responseName);
                this.controlArrayList.add(this.responseText);
                break;
            case START_NETWORK:
                this.title = "Start network";
                break;
            case LOAD_NETWORK:
                this.title = "Load network";
                this.responseName = new Label("Select network");
                this.networkList = new ComboBox();
                ArrayList<String> arrayList = new ArrayList();
                CharsetList[] var3 = CharsetList.values();
                for (CharsetList pathlist : var3) {
                    arrayList.add(pathlist.name());
                }

                this.networkList.setItems(FXCollections.observableArrayList(arrayList));
                this.controlArrayList.add(this.responseName);
                this.controlArrayList.add(this.networkList);
                break;
            case TRAIN_NETWORK:
                this.title = "Train network";
                this.responseName = new Label("Training iterations:");
                this.responseText = new TextField("1000");
                this.controlArrayList.add(this.responseName);
                this.controlArrayList.add(this.responseText);
                break;
            default:
                this.title = "";
        }

    }

    public Optional<String> getDialog() {
        ButtonType connectButton = new ButtonType("OK", ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        Dialog<ButtonType> dialog = new Dialog();
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest((event) -> {
            dialog.close();
        });
        dialog.getDialogPane().getButtonTypes().add(connectButton);
        dialog.getDialogPane().getButtonTypes().add(cancelButton);
        dialog.setTitle(this.title);
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(20.0));
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10.0);
        Iterator var6 = this.controlArrayList.iterator();

        while(var6.hasNext()) {
            Control control = (Control)var6.next();
            hBox.getChildren().add(control);
        }

        dialog.getDialogPane().setContent(hBox);
        Button buttonOK = (Button)dialog.getDialogPane().lookupButton(connectButton);
        buttonOK.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {
                String input = null;
                if (DialogHandler.this.responseText != null) {
                    input = DialogHandler.this.responseText.getText();
                } else if (DialogHandler.this.networkList != null) {
                    input = (String)DialogHandler.this.networkList.getSelectionModel().getSelectedItem();
                } else {
                    input = DialogHandler.this.responseName.getText();
                }
                DialogHandler.this.returnValue = Optional.ofNullable(input);
            }
        });
        dialog.showAndWait();
        return this.returnValue;
    }
}
