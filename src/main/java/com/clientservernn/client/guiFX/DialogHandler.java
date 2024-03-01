package com.clientservernn.client.guiFX;


import com.clientservernn.common.CharsetList;
import com.clientservernn.common.Dialogues;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import java.util.ArrayList;
import java.util.Optional;

public class DialogHandler {
    Dialogues type;
    StringBuilder returnValue;
    String title;
    ArrayList<Control> controlArrayList=new ArrayList<>();

    Label statusName=null;
    Label responseName=null;
    TextField statusText=null;
    TextField responseText =null;
    ComboBox<String> networkList=null;

    public DialogHandler (Dialogues type){
        returnValue=null;
        this.type=type;
        returnValue=new StringBuilder();
        switch (type){
            case CONNECT -> {
                title="Connection to server";
                statusName=new Label("Server IP:");
                statusText = new TextField(NeuronController.serverIP);
                responseName=new Label("Server Port:");
                responseText = new TextField(String.valueOf(NeuronController.serverPort));
                controlArrayList.add(statusName);
                controlArrayList.add(statusText);
                controlArrayList.add(responseName);
                controlArrayList.add(responseText);

            }
            case USER_DATA ->  {
                title="Enter user name";
                responseName=new Label("Name:");
                responseText= new TextField();
                controlArrayList.add(responseName);
                controlArrayList.add(responseText);
            }
            case DOWNLOAD -> {
                title="Load network";
                responseName=new Label("Select network");
                networkList=new ComboBox<>();
                ArrayList<String> arrayList=new ArrayList<>();
                for (CharsetList pathlist: CharsetList.values()) {
                    arrayList.add(pathlist.name());
                }
                networkList.setItems(FXCollections.observableArrayList(arrayList));
                controlArrayList.add(responseName);
                controlArrayList.add(networkList);
            }
            case CHARSET -> {
                title="Train network";
                responseName=new Label("Training iterations:");
                responseText= new TextField("1000");
                controlArrayList.add(responseName);
                controlArrayList.add(responseText);
            }
            default -> title="";
        }

    }


    public Optional<String> getDialog (){

        ButtonType connectButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<String> dialog = new Dialog<>();
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event->dialog.close());
        dialog.getDialogPane().getButtonTypes().add(connectButton);
        dialog.getDialogPane().getButtonTypes().add(cancelButton);
        HBox hBox=new HBox();
        hBox.setPadding(new Insets(20));
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        for (Control control:controlArrayList) {
            hBox.getChildren().add(control);
        }
        dialog.getDialogPane().setContent(hBox);
        dialog.setTitle(title);
        Button buttonOK=(Button) dialog.getDialogPane().lookupButton(connectButton);
        buttonOK.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (type){
                    case CONNECT -> {

                        returnValue.append(statusText.getText());
                        returnValue.append(" ");
                        returnValue.append(responseText.getText());
                    }
                    case USER_DATA -> {
                        returnValue.append(responseText.getText());
                    }
                }
            }
        });
        dialog.showAndWait();
        title=null;
        Optional.ofNullable(returnValue).ifPresent(returnValue->title=returnValue.toString());
        return  Optional.ofNullable(title);
    }






}
