package com.clientservernn.client.additional;

import com.clientservernn.client.additional.Transferable;
import com.clientservernn.client.guiFX.ImageTable;
import com.clientservernn.client.guiFX.ImageTable.Changes;
import com.clientservernn.dataTransfer.Command;
import com.clientservernn.dataTransfer.DataTransfer;
import com.clientservernn.dataTransfer.ImageData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;

import java.util.ArrayList;


public class Loader implements Runnable, Transferable {


    ArrayList<ImageTable> table;
    ProgressBar progressBar;
    int loaded;
    int dataLength;
    String letter;
    String path;
    Command command;


    public Loader(ProgressBar progressBar, String path, ObservableList<ImageTable> table) {
        this.progressBar = progressBar;
        this.letter=null;
        this.path=path;
        this.loaded = Transferable.loadedAtStart;
        this.table = new ArrayList<>(table);
        this.dataLength=table.size();
        this.command=Command.EDIT;
        new Thread(this).start();
    }

    public Loader(ProgressBar progressBar, String path, String letter) {
        this.progressBar = progressBar;
        this.letter=letter;
        this.path=path;
        this.loaded =Transferable.loadedAtStart;
        this.dataLength=-1;
        this.command=Command.UPLOAD;
        table = new ArrayList<>();
        new Thread(this).start();
    }


    public ObservableList<ImageTable> getTable() {
        return FXCollections.observableArrayList(table);
    }


    private void addRow(ImageTable row) {
        this.table.add(row);
        loaded++;
    }
    private ImageTable getRow() {
        loaded++;
        ImageTable imageTable=table.get(0);
        table.remove(0);
        return imageTable;
    }
    public boolean isLoaded(){
        return loaded==dataLength;
    }
    public boolean isInterrupted(){
        return command.equals(Command.EXCEPTION);
    }

    @Override
    public void run() {
        double progress=0.0;
        double current=0.0;
        double step;
        while (progress <1.0) {
            if (dataLength==0) {
                progress=1.0;
                continue;
            }
            step=0.1/dataLength;
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            progress=loaded*1.0/dataLength;
            current= current<(progress)?current+step:current;
            final double finalCurrent=current;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(finalCurrent);
                }
            });

        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(1.0);
                Event.fireEvent(progressBar,new ActionEvent());

            }
        });

    }

    public DataTransfer getTransferOut() {
        DataTransfer dataTransfer=null;
        if (!isLoaded()) {
            if (command.equals(Command.UPLOAD)) {
                String[] message={path,letter};

                if (loaded<dataLength) {
                    dataTransfer=new DataTransfer(null, command, loaded,message);
                } else{
                    dataTransfer=new DataTransfer(null, command,message);
                }
            } else if (command.equals(Command.EDIT)) {
                String[] message=null;
                ImageData imageData=null;
                int position=-1;
                while (!isLoaded()) {
                    ImageTable imageTable=getRow();
                    imageData=imageTable.getImageData();
                    message= new String[]{path, imageTable.getLetter(), imageTable.getName(), String.valueOf(imageTable.getLongTime())};
                    if (imageTable.getChange().equals(Changes.Add)) {
                        imageTable.setAsAdd();
                        table.add(imageTable);
                        break;
                    } else if (imageTable.getChange().equals(Changes.Delete)) {
                        position=imageTable.getSerial();
                        break;
                    } else {
                        table.add(imageTable);
                    }
                }
                dataTransfer=new DataTransfer(imageData, command, position,message);
            } else {
                dataTransfer=null;
            }


        }

        return dataTransfer;
    }

    public void setTransferIn(DataTransfer dataTransferIn) {
        switch (dataTransferIn.getCommand()) {
            case UPLOAD -> {
                String[] message=dataTransferIn.getMessage();
                if (message!=null) {
                    if (message.length==2) {
                        dataLength=dataTransferIn.getPosition();
                    } else if (message.length==4) {
                        ImageTable imageTable=new ImageTable(message[1], message[2], Long.parseLong(message[3]),dataTransferIn.getPosition(),dataTransferIn.getImageData());
                        this.addRow(imageTable);
                    }
                }
            }
            case EDIT -> {

            }
            case EXCEPTION, MESSAGE -> {
                command=Command.EXCEPTION;
                table=null;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(0.0);
                        Event.fireEvent(progressBar,new ActionEvent());
                    }
                });
            }

        }

    }

    @Override
    public boolean isReceive() {
        return !isLoaded();
    }
}
