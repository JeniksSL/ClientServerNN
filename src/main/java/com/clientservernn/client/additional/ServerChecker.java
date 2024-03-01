package com.clientservernn.client.additional;

import com.clientservernn.client.guiFX.HandleInOut;
import com.clientservernn.dataTransfer.DataTransfer;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ServerChecker implements Runnable {

    final ServerChecker serverChecker;

    Label serverResponse;
    String dataString;
    Label connectionStatus;

    Supplier<Boolean> handleInOut;

    private Timer responseTimer;


    public ServerChecker(Label serverResponse, Label connectionStatus,   Supplier<Boolean> handleInOut) {
        this.serverResponse = serverResponse;
        this.connectionStatus = connectionStatus;
        this.handleInOut = handleInOut;
        dataString =null;
        serverChecker=this;
        new Thread(this).start();
        responseTimer=new Timer();
    }

    public void setDataString(DataTransfer dataTransfer) {
        this.dataString = Optional.ofNullable(dataTransfer).map(DataTransfer::getDescription).orElse(null);
        responseTimer.cancel();
        responseTimer=new Timer();
        responseTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (serverChecker){
                    serverChecker.notify();
                }

            }
        },0,5000);

    }


    @Override
    public void run() {

        while (true){

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (dataString ==null) {serverResponse.setText("none");}
                    else {
                        final String response= dataString.toString();
                        serverResponse.setText(response);
                        dataString=null;
                    }
                    if (handleInOut.get()) {
                        connectionStatus.setText("Connected");

                    } else {
                        connectionStatus.setText("Disconnected");
                    }
                }
            });
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


        }
    }


}
