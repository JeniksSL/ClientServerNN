package com.clientservernn.client.additional;

import com.clientservernn.dataTransfer.DataTransfer;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.HashMap;

public class StreamIn implements Runnable {

    public boolean isAlive() {
        return isAlive;
    }

    boolean isAlive;
    InputStream inputStream;
    Socket socket;
    HashMap<Integer, HandlerIn> threadInHashMap=new HashMap<>();





    public StreamIn(Socket socket) throws IOException {
        this.socket=socket;
        this.inputStream=socket.getInputStream();
        new Thread(this).start();

    }


    public synchronized HandlerIn getThread(int transferCode){
        HandlerIn handlerIn;
        if (threadInHashMap.containsKey(transferCode)) {
            handlerIn = threadInHashMap.get(transferCode);
        } else {
            handlerIn =new HandlerIn(transferCode);
            threadInHashMap.put(transferCode, handlerIn);
        }
        return handlerIn;
    }



    @Override
    public void run() {
        isAlive=true;
        while (!socket.isClosed()){
            try {
                System.out.println("READ from server message:");
                PushbackInputStream pushbackInputStream=new PushbackInputStream(inputStream);
                byte[] first=new byte[1];
                boolean isEnd=pushbackInputStream.read(first)==-1;
                if (isEnd) {socket.close();  break;}
                pushbackInputStream.unread(first);
                int length=pushbackInputStream.available();
                byte[] byteIn=pushbackInputStream.readNBytes(length);
                DataTransfer dataTransfer= DataTransfer.fromByteArray(byteIn);
                System.out.println("Client get data:"+ dataTransfer);
                int code=dataTransfer.getTransferCode();
                HandlerIn handlerIn =getThread(code);
                handlerIn.putDataTransfer(dataTransfer);
                if (code<0) {
                    try {
                        handlerIn.handle();
                    }catch (InterruptedException e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert=new Alert(Alert.AlertType.ERROR,e.getMessage());
                                alert.show();
                            }
                        });
                    }
                    remove(code);
                }



            } catch (IOException e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Alert alert=new Alert(Alert.AlertType.ERROR,e.getMessage());
                        alert.show();
                    }
                });
                break;
            }

        }
        isAlive=false;

    }

    public void remove(int currentNumber) {
        threadInHashMap.remove(currentNumber);
    }
}
