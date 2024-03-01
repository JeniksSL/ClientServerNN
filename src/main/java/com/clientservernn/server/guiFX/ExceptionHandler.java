
package com.clientservernn.server.guiFX;

import java.util.Date;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;

public class ExceptionHandler implements Runnable {
    private static final int TABLE_MAX_LENGTH = 10;
    private static ExceptionHandler exceptionHandler;
    private TableView<ExceptionItem> exceptionsTable;
    private TreeSet<ExceptionItem> exceptions;
    private final Thread thread;

    public static void setException(String source, Exception exception) {
        exceptionHandler.add(source, exception);
    }

    public static String getSource(Object...sources){
        StringBuilder stringBuilder=new StringBuilder();
        for (int i = 0; i < sources.length; i++) {
            stringBuilder.append(sources[i].toString()).append(", ");
        }
        return stringBuilder.toString();
    }

    private void add(String source, Exception exception) {
        ExceptionItem exceptionItem = new ExceptionItem(new Date(), source, exception);
        this.exceptions.add(exceptionItem);
        if (this.exceptions.size() <= 10) {
        } else {
            this.exceptions.remove(exceptions.last());
        }
        synchronized(this.thread) {
            this.thread.notify();
        }
    }

    public ExceptionHandler(TableView<ExceptionItem> exceptionsTable) {
        exceptionHandler = this;
        this.exceptionsTable = exceptionsTable;
        this.exceptions = new TreeSet<>(ComparableTo::compareReverseIndex);
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void run() {
        while(true) {
            synchronized(this.thread) {
                try {
                    this.thread.wait();
                } catch (InterruptedException var4) {
                    throw new RuntimeException(var4);
                }
            }

            Platform.runLater(new Runnable() {
                public void run() {
                    ExceptionHandler.this.exceptionsTable.setItems(FXCollections.observableArrayList(exceptions));
                    ExceptionHandler.this.exceptionsTable.refresh();
                }
            });
        }
    }
}
