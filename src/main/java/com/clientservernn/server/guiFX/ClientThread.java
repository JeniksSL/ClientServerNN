
package com.clientservernn.server.guiFX;

import com.clientservernn.dataTransfer.DataTransfer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import static java.lang.Thread.currentThread;



/**
 * The class {@code ClientThread} represents class that implements {@link Runnable}
 * and used for perform actions requested by one client through {@link Socket}
 * {@code socket}.
 * @see Server
 * @see ClientItem
 * @see ExceptionHandler#setException(String, Exception)
 * @see DataTransfer
 * @see CommandHandler
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */

public class ClientThread implements Runnable {



    /**
     * The {@link Callable} task, that method call() performs refresh content in
     * the table {@link javafx.scene.control.TableView} of connected clients.
     */
    private final Callable<Void> refresher;

    /**
     * The {@link Consumer} operation, that method accept() performs removing this
     * {@code ClientThread} from the {@link ArrayList} {@code clientArrayList}
     * of {@link Server} server.
     */
    private final Consumer<ClientThread> remove;

    /**
     * The {@link Socket} of current {@code ClientThread} instance.
     */
    private final Socket socket;

    /**
     * The {@link ClientItem} of current {@code ClientThread} instance for representation
     * in {@link javafx.scene.control.TableView} of connected clients.
     */
    private final ClientItem clientItem;

    /**
     * Allocate the new {@code ClientThread} with given {@code socket},
     * {@link Callable} {@code refresher}, {@link Consumer} {@code terminator}.
     * Client starting automatically.
     * Part of information about client stored in {@code clientItem}, such
     * as name of client (default set as "unnamed") and connection date.
     *
     * @param socket given {@link Socket} instance for this {@code ClientThread}.
     * @param  refresher
     *         given {@link Callable} task.
     * @param terminator given {@link Consumer} operation.
     *
     * @throws NullPointerException if any parameter is null.
     */
    public ClientThread(Socket socket, Callable<Void> refresher, Consumer<ClientThread> terminator) {
        Objects.requireNonNull(socket);
        Objects.requireNonNull(refresher);
        Objects.requireNonNull(terminator);
        this.refresher = refresher;
        this.remove=terminator;
        this.socket = socket;
        this.clientItem = new ClientItem(socket);
        this.clientItem.setName("unnamed");
        (new Thread(this)).start();
    }

    /**
     * Performs stopping the {@code ClientThread}.
     * Close {@code socket}, remove this {@code ClientThread} from
     * the {@link ArrayList} {@code clientArrayList} of {@link Server} server.
     * Interrupt current thread.
     *
     * If any exception occurs when call() method of  {@code refresher} invoke
     * caught exception adds to {@link ExceptionHandler}.
     */
    public void stop() {
        try {
            this.socket.close();
            remove.accept(this);
            this.refresher.call();
            currentThread().interrupt();
        } catch (Exception e) {
            remove.accept(this);
            currentThread().interrupt();
            ExceptionHandler.setException(this.toString(),e);
        }
    }

    /**
     * Returns copy of this {@code ClientItem} instance.
     *
     * @return copy of this {@code clientItem}.
     */
    public ClientItem getClientItem() {
        return new ClientItem(this.clientItem);
    }



    /**
     * Work cycle of clientThread. ClientThread blocked until
     * some input is available. After gets input from client, reading all byte data from {@code inputStream}
     * and convert it to {@link DataTransfer} instance {@code dataTransfer}. Puts {@code dataTransfer}
     * to handling {@link CommandHandler#add(DataTransfer, String)} and waits for handle Thread is join.
     * In my program, access level is configured depending on the client's name.
     * After gets response as {@code dataTransfer} {@link CommandHandler#getResponse(Thread)} and sends it
     * back to client.
     * If any {@link IOException} or {@link Exception} occurred {@code ClientThread} stops.
     * Add occurred exception to {@link ExceptionHandler}.
     * If response {@code dataTransfer} {@code command} have some specified values, performs
     * additional operations, such as adds exception to {@link ExceptionHandler} if command is EXCEPTION.
     */
    public void run() {

        try(OutputStream outputStream = this.socket.getOutputStream();
            InputStream inputStream = this.socket.getInputStream()) {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
            while(!this.socket.isClosed()) {
                try {
                    System.out.println("READ from client message:");
                    byte[] first = new byte[1];
                    boolean isEndStream = pushbackInputStream.read(first) == -1;
                    if (isEndStream) {
                        this.stop();
                    } else {
                        pushbackInputStream.unread(first);
                        byte[] byteIn = pushbackInputStream.readNBytes(pushbackInputStream.available());
                        DataTransfer dataTransfer = DataTransfer.fromByteArray(byteIn);
                        System.out.println("Server get data:" + dataTransfer);
                        this.clientItem.setLastTransfer(dataTransfer);

                        Thread executeThread = CommandHandler.add(dataTransfer, clientItem.getName());
                        executeThread.join();
                        dataTransfer = CommandHandler.getResponse(executeThread);
                        System.out.println("Server send data:" + dataTransfer);
                        switch (dataTransfer.command) {
                            case DISCONNECT -> this.stop();
                            case EXCEPTION -> ExceptionHandler.setException(dataTransfer.toString(), new Exception(dataTransfer.getMessage(0)));
                            case USER_DATA -> this.clientItem.setName(dataTransfer.getMessage(0));//Sets a new name to client.
                        }
                        this.refresher.call(); //refreshing clients table.
                        if (!this.socket.isClosed()) {
                            dataOutputStream.write(dataTransfer.toByteArray());
                        }
                    }
                } catch (Exception exception) {
                    this.stop();
                    ExceptionHandler.setException(this.toString(), exception);
                    break;
                }
            }

        } catch (IOException exception) {
            ExceptionHandler.setException(this.toString(), exception);
            this.stop();
        }

    }

   
}
