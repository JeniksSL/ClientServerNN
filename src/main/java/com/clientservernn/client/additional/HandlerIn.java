package com.clientservernn.client.additional;

import com.clientservernn.dataTransfer.DataTransfer;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class HandlerIn {

    int transferCode;
    DataTransfer dataTransferIn;
    public DataTransfer getDataTransferIn() {
        return dataTransferIn;
    }




    HandlerIn(int transferCode){
        this.transferCode=transferCode;
        dataTransferIn=null;
    }

    public void putDataTransfer(DataTransfer dataTransfer) {
        dataTransferIn=dataTransfer;
        synchronized (this){
            this.notify();
        }
    }


    public void handle() throws InterruptedException {
            synchronized (this){
                this.wait(10000);

            }


        if (dataTransferIn!=null) {
            switch (dataTransferIn.getCommand()){
                case DISCONNECT -> doDefault();
                case LEARN -> learning();
                case MESSAGE -> doMessage();
                case RECOGNIZE -> recognizing();
                case USER_DATA -> {}
                case UPLOAD -> upload();
                case EDIT -> edit();
                case EXCEPTION -> exceptionHandle();
                default -> {}
            }


        }

    }

    private void exceptionHandle() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert warning=new Alert(Alert.AlertType.ERROR, dataTransferIn.getMessage(0));
                warning.show();
            }
        });
    }

    private void edit() {

    }

    private void upload() {


    }

    private void recognizing() {
        String[] messageArray=dataTransferIn.getMessage();
        StringBuilder message=new StringBuilder("");
        if (messageArray!=null) {
            message.append(messageArray[0]);
            message.append("\n"+"All possibly results:");
            for (int i = 1; i < messageArray.length; i++) {
                message.append("\n").append(messageArray[i]);
            }
        } else {
            message=new StringBuilder("Recognizing error");
        }
        final String toAlert= message.toString();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert warning=new Alert(Alert.AlertType.INFORMATION, toAlert);
                warning.show();
            }
        });
    }


    private void doMessage() {


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert=new Alert(Alert.AlertType.INFORMATION, dataTransferIn.getMessage(0));
                alert.show();
            }
        });
    }

    private void learning() {


    }


    private void doDefault() {

    }





}
