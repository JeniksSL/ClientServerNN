package com.clientservernn.client.guiFX;
import com.clientservernn.client.additional.*;
import com.clientservernn.dataTransfer.DataTransfer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.PriorityQueue;


public class HandleInOut implements Runnable {

    StreamIn streamIn;
    ServerChecker serverChecker;
    final Thread thread;
    Socket socket;
    OutputStream outputStream;

    PriorityQueue<ComparablePair<Integer,Transferable>> priorityQueue;

    int serialNumber;
    private static HandleInOut instance;

    private HandleInOut() {
        priorityQueue=new PriorityQueue<>();
        this.serverChecker = null;
        this.socket=null;
        this.serialNumber=0;
        thread=new Thread(this);
        thread.start();
    }


    public static HandleInOut getInstance() {
        if (instance==null) {
            instance=new HandleInOut();
        }
       return instance;
    }


    public void setData(Socket socket, ServerChecker serverChecker) {
        this.socket = socket;
        try {
            streamIn=new StreamIn(socket);
            this.outputStream=socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.serverChecker = serverChecker;
    }

    public void send(DataTransfer dataTransfer){
        if (isConnected()&&dataTransfer!=null) {
            priorityQueue.add(new ComparablePair<>(serialNumber,new Transfer(dataTransfer)));
            serialNumber++;
        }
        synchronized (this) {
            this.notify();
        }

    }

    public void send(Transferable transferable){
        if (isConnected()&&transferable !=null) {
            priorityQueue.add(new ComparablePair<>(serialNumber,transferable));
            serialNumber++;
        }
        synchronized (this) {
            this.notify();
        }
    }

    public boolean isConnected(){
        return socket!=null&&!socket.isClosed()&&streamIn.isAlive();
    }


    @Override
    public void run() {

        while (true) {

            try {

                if (priorityQueue!=null&&!priorityQueue.isEmpty()&&isConnected()) {
                    DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
                    ComparablePair<Integer,Transferable> comparablePair =priorityQueue.remove();
                    Transferable transferable= comparablePair.value();
                    int currentNumber= comparablePair.key();
                    System.out.println(currentNumber+" "+(transferable==null));
                    DataTransfer dataTransferOut=transferable==null?null:transferable.getTransferOut();
                    if (dataTransferOut!=null) {
                        dataTransferOut.setTransferCode(currentNumber);
                        byte[] byteOut=dataTransferOut.toByteArray();

                        if (isConnected()) {
                            dataOutputStream.write(byteOut);

                            HandlerIn exeTread;
                            exeTread=streamIn.getThread(currentNumber);

                            exeTread.handle();//wait() inside

                            DataTransfer dataTransferIn=exeTread.getDataTransferIn();
                            streamIn.remove(currentNumber);

                            if (dataTransferIn!=null&&transferable.isReceive()) { //&&transferable.isReceive()
                                transferable.setTransferIn(dataTransferIn);

                                this.send(transferable);
                            }
                            NeuronController.serverChecker.setDataString(dataTransferIn);
                        } else if (!socket.isClosed()) {
                            socket.close();
                        }


                    }  else {

                    }

                } else {
                    synchronized (this) {
                        this.wait();
                    }

                }


            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }




        }


    }




}
