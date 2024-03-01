package com.clientservernn.client.additional;

import com.clientservernn.dataTransfer.Command;
import com.clientservernn.dataTransfer.DataTransfer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class TinyLoader implements Transferable {

    String name;
    Button button;
    boolean isLoaded;
    ComboBox<String> stringComboBox;

    public TinyLoader(Button button,ComboBox<String> stringComboBox, String name) {

        this.button=button;
        this.stringComboBox=stringComboBox;
        this.name=name;
        isLoaded=false;
    }

    @Override
    public DataTransfer getTransferOut() {
        DataTransfer dataTransfer=null;
        if (!isLoaded) {
            dataTransfer= new DataTransfer(null, Command.LEARN,name);
        }
        return dataTransfer;
    }

    @Override
    public void setTransferIn(DataTransfer dataTransferIn) {
        if (dataTransferIn.getCommand().equals(Command.LEARN)) {
            isLoaded=true;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    button.setDisable(false);
                    stringComboBox.setItems(FXCollections.observableArrayList(dataTransferIn.getMessage()));
                }
            });

        } else {
            isLoaded=true;
            Platform.runLater(()->Event.fireEvent(button, new ActionEvent()));

        }

    }

    @Override
    public boolean isReceive() {
        return !isLoaded;
    }
}

