
package com.clientservernn.server.guiFX;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;


/**
 * The class {@code Server} represents server in client-server network.
 * Also class enable multi-thread connection.
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */
public class Server implements Runnable {

    /**
     * The {@link ServerSocket} instance of this {@code NetworkCommander}.
     */
    private ServerSocket socket;

    /**
     * The {@link ArrayList} contains all {@link ClientThread} that connected to server.
     */
    private ArrayList<ClientThread> clientArrayList;

    /**
     * The {@link Callable} task, that method call() performs refresh content in the table {@link javafx.scene.control.TableView}
     * of connected clients.
     */
    private final Callable<Void> refresher;

    /**
     * Allocate the new {@code Server} with given {@link Callable} {@code refresher}.
     * Server not starting automatically.
     *
     * @param  refresher
     *         given {@link Callable} task.
     * @throws NullPointerException if {@code refresher} is null.
     */
    public Server(Callable<Void> refresher) {
        Objects.requireNonNull(refresher);
        this.refresher = refresher;
        this.clientArrayList = new ArrayList<>();
        this.socket = null;
    }

    /**
     * Performs starting {@code Server} on given {@code port}
     * and returns port if no exceptions occurred.
     * Starts the server thread.
     * @param  port given port for {@link ServerSocket}.
     * @return {@code port} number on which this {@code socket} is listening.
     *
     * @throws IOException if an I/O error occurs when opening the socket
     * or server is already started.
     * @throws IllegalArgumentException if the {@code port} parameter is
     * outside the specified range of valid port values.
     */
    public int start(int port) throws IOException {
        if (this.socket != null && !this.socket.isClosed()) {
            throw new IOException("Server is already started");
        } else {
            this.socket = new ServerSocket(port);
            new Thread(this).start();
        }
        return socket.getLocalPort();
    }

    /**
     * Performs stopping the {@code Server}.
     * Stops all {@link ClientThread} in {@code clientArrayList} and
     * clear {@code clientArrayList}.
     *
     * @throws IOException if an I/O error occurs when closing the socket
     * or server is not started.
     */
    public void stop() throws IOException {
        if (this.socket != null && !this.socket.isClosed()) {
            clientArrayList.forEach(ClientThread::stop);
            clientArrayList.clear();
            this.socket.close();
        } else {
            throw new IOException("Server is not started");
        }
    }

    /**
     * Returns {@link ArrayList} with {@link ClientItem} instances that
     * represents all current connected {@link ClientThread} instances.
     * @return {@link ArrayList} with {@link ClientItem} instances
     * represents all current connected {@link ClientThread} instances
     * from {@code clientArrayList}.
     */
   public ArrayList<ClientItem> getClientItems(){
       ArrayList<ClientItem> clientItems=new ArrayList<>();
       for (ClientThread clientThread : clientArrayList) {
           clientItems.add(clientThread.getClientItem());
       }
       return clientItems;
   }

    /**
     * Removes the given {@link ClientThread} instance from {@code clientArrayList}.
     * @param  clientThread given {@code ClientThread} instance for remove.
     */

   public void remove(ClientThread clientThread){
       clientArrayList.remove(clientThread);
   }

    /**
     * Work cycle of server. Server blocked until {@code socket.accept()}
     * returns a new client {@link Socket}, then create a new {@link ClientThread}
     * contains {@code client} and adds it to {@code clientArrayList}
     * and perform {@link Callable} {@code refresher} method call().
     * If any {@link IOException} occurred {@code clientArrayList} clears.
     * If any {@link Exception} occurred add it to {@link ExceptionHandler}.
     * @see ExceptionHandler#setException(String, Exception)
     * @see ServerController#handleRefreshAction()
     */

   @Override
    public void run() {
        while(!this.socket.isClosed()) {
            try {
                Socket client = this.socket.accept();
                ClientThread clientThread = new ClientThread(client, this.refresher, this::remove);
                this.clientArrayList.add(clientThread);
                this.refresher.call();
            } catch (IOException exception) {
                this.clientArrayList.clear();
                ExceptionHandler.setException(this.toString(),exception);
            } catch (Exception exception) {
                ExceptionHandler.setException(this.toString(),exception);
            }
        }

    }
}
