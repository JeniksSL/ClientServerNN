
package com.clientservernn.server.guiFX;

import com.clientservernn.common.CharsetList;
import com.clientservernn.common.Dialogues;
import com.clientservernn.dataTransfer.DataTransfer;
import com.clientservernn.server.neuralNetwork.NetworkCommander;
import com.clientservernn.server.neuralNetwork.NetworkItem;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerController extends Application implements Initializable {
    private static int serverPort = 3340;

    ExceptionHandler exceptionHandler;

    private Server server;

    public BorderPane borderPane;
    public Label serverStatus;
    public TableView<NetworkItem> networkTable;
    public TableColumn<ExceptionItem, Date> exceptionsColumn1;
    public TableColumn<ExceptionItem, String> exceptionsColumn2;
    public TableColumn<ExceptionItem, Exception> exceptionsColumn3;
    public MenuBar menuBar;

    @FXML
    private TableView<ExceptionItem> exceptionsTable;
    @FXML
    private TableView<ClientItem> clientsTable;
    @FXML
    private TableColumn<ClientItem, String> clientColumn1;
    @FXML
    private TableColumn<ClientItem, String> clientColumn2;
    @FXML
    private TableColumn<ClientItem, Date> clientColumn3;
    @FXML
    private TableColumn<ClientItem, DataTransfer> clientColumn4;
    @FXML
    private TableColumn<ClientItem, Integer> clientColumn5;
    public TableColumn<NetworkItem, String> networkColumn1;
    public TableColumn<NetworkItem, Integer> networkColumn2;
    public TableColumn<NetworkItem, String> networkColumn3;
    public TableColumn<NetworkItem, String> networkColumn4;
    public TableColumn<NetworkItem, Boolean> networkColumn5;


    public ServerController() {
    }


    public static int getServerPort() {
        return serverPort;
    }

    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerController.class.getResource("ServerGui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900.0, 600.0);
        stage.setTitle("Server panel NN");
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
                System.exit(0);
            }
        });
        stage.show();
    }


    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.menuBar.prefWidthProperty().bind(this.borderPane.widthProperty());
        this.clientColumn1.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        this.clientColumn2.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.clientColumn3.setCellValueFactory(new PropertyValueFactory<>("date"));
        this.clientColumn4.setCellValueFactory(new PropertyValueFactory<>("lastTransfer"));
        this.clientColumn5.setCellValueFactory(new PropertyValueFactory<>("hash"));
        this.networkColumn1.setCellValueFactory(new PropertyValueFactory<>("charset"));
        this.networkColumn2.setCellValueFactory(new PropertyValueFactory<>("numberItems"));
        this.networkColumn3.setCellValueFactory(new PropertyValueFactory<>("trainDate"));
        this.networkColumn4.setCellValueFactory(new PropertyValueFactory<>("lastModified"));
        this.networkColumn5.setCellValueFactory(new PropertyValueFactory<>("access"));
        this.exceptionsColumn1.setCellValueFactory(new PropertyValueFactory<>("date"));
        this.exceptionsColumn2.setCellValueFactory(new PropertyValueFactory<>("source"));
        this.exceptionsColumn3.setCellValueFactory(new PropertyValueFactory<>("exception"));
        this.exceptionHandler = new ExceptionHandler(this.exceptionsTable);
    }


    public void handleStartServerAction() {
        DialogHandler dialogHandler = new DialogHandler(Dialogues.SERVER_START);
        String result = dialogHandler.getDialog().orElse(null);
        if (result != null) {
            server = new Server(this::handleRefreshAction);
            try {
                serverPort = server.start(Integer.parseInt(result));
                serverStatus.setText("Server on, port: " + serverPort);
            } catch (IOException| NumberFormatException exception) {
                Alert alert = new Alert(AlertType.ERROR, exception.toString());
                alert.showAndWait();
            }
        }

    }

    public void handleStopServerAction(ActionEvent actionEvent) {
        DialogHandler dialogHandler = new DialogHandler(Dialogues.SERVER_STOP);
        if (dialogHandler.getDialog().isPresent()) {
            try {
                this.server.stop();
                this.serverStatus.setText("Server off");
            } catch (IOException exception) {
                Alert alert = new Alert(AlertType.ERROR, exception.toString());
                alert.showAndWait();
                ExceptionHandler.setException(this.toString(),exception);
            }
        }
    }

    public Void handleRefreshAction() {
        Platform.runLater(() -> {
            ObservableList<ClientItem> clientThreads = FXCollections.observableArrayList(new ArrayList<>(server.getClientItems()).stream().sorted(ComparableTo::compareByIndex).toList());
            clientsTable.setItems(clientThreads);
            clientsTable.refresh();
        });
        return null;
    }


    private void refreshNetworksTable() {
        ObservableList<NetworkItem> networks = FXCollections.observableArrayList(CommandHandler.getNetworkItems());
        this.networkTable.setItems(networks);
        this.networkTable.refresh();
    }

    public void handleLoadNLAction() {
        DialogHandler dialogHandler = new DialogHandler(Dialogues.LOAD_NETWORK);
        String result = dialogHandler.getDialog().orElse(null);
        if (result != null) {
            try {
                CommandHandler.uploadNetwork(CharsetList.valueOf(result));
            } catch (Exception exception) {
                Alert alert = new Alert(AlertType.ERROR, exception.toString());
                alert.showAndWait();
            }
        }
        this.refreshNetworksTable();
    }

    public void handleEnableNetwork(ActionEvent actionEvent) {
        NetworkItem networkItem = (NetworkItem)this.networkTable.getSelectionModel().getSelectedItem();
        if (networkItem != null) {
            String path = networkItem.getCharset().name();
            Optional<NetworkCommander> optional = Optional.ofNullable((NetworkCommander)CommandHandler.networkList.get(path));
            optional.ifPresent(NetworkCommander::changeAccess);
        } else {
            Alert alert = new Alert(AlertType.WARNING, "No items selected", new ButtonType[0]);
            alert.show();
        }

        this.refreshNetworksTable();
    }

    public void handleTrainNetwork() {
        final NetworkItem networkItem = this.networkTable.getSelectionModel().getSelectedItem();
        if (networkItem != null) {
            String charset = networkItem.getCharset().name();
            final NetworkCommander networkCommander = (NetworkCommander)CommandHandler.networkList.get(charset);
            DialogHandler dialogHandler = new DialogHandler(Dialogues.TRAIN_NETWORK);
            String result = (String)dialogHandler.getDialog().orElse(null);
            if (result != null) {
                try {
                    final int count = Integer.parseInt(result);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                trainNetwork(networkItem,count).join();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    ServerController.this.refreshNetworksTable();
                                }
                            });
                        }
                    }).start();

                } catch (IllegalArgumentException var9) {
                    Alert alert = new Alert(AlertType.ERROR, var9.toString(), new ButtonType[0]);
                    alert.showAndWait();
                }
            }
        } else {
            Alert alert = new Alert(AlertType.WARNING, "No items selected", new ButtonType[0]);
            alert.show();
        }

    }

    public Thread trainNetwork(NetworkItem networkItem, int iterations) {
        final NetworkCommander networkCommander = CommandHandler.networkList.get(networkItem.getCharset());
        Thread thread = new Thread(new Runnable() {
            public void run() {
                networkCommander.refreshTrainData();
                for(int i = 0; i <  iterations; ++i) {
                    networkCommander.train();
                    final String progress = String.format("%.2f %s", (double)i * 100.0 / (double)iterations, " %");
                    Platform.runLater(new Runnable() {
                        public void run() {
                            networkItem.setProgress(progress);
                            ServerController.this.networkTable.refresh();
                        }
                    });
                }

                try {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            networkItem.setProgress("100.00 %");
                            ServerController.this.networkTable.refresh();
                        }
                    });
                    Thread.sleep(1000L);
                } catch (InterruptedException var3) {
                    throw new RuntimeException(var3);
                }
            }
        });
        thread.start();
        return thread;
    }

    public void handleStopNetwork(ActionEvent actionEvent) {
        NetworkItem networkItem = (NetworkItem)this.networkTable.getSelectionModel().getSelectedItem();
        if (networkItem != null) {
            String path = networkItem.getCharset().name();
            CommandHandler.networkList.remove(path);
        } else {
            Alert alert = new Alert(AlertType.WARNING, "No items selected", new ButtonType[0]);
            alert.show();
        }

        this.refreshNetworksTable();
    }

    public void handleTrainAllAction() {
        DialogHandler dialogHandler = new DialogHandler(Dialogues.TRAIN_NETWORK);
        String result = dialogHandler.getDialog().orElse(null);
        if (result!=null) {
            int iterations;
            try {
                iterations=Integer.parseInt(result);
            }
            catch (NumberFormatException exception){
            return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int size=networkTable.getItems().size();
                    Thread[] threads=new Thread[size];
                    for (int i = 0; i < size; i++) {
                        threads[i]=trainNetwork(networkTable.getItems().get(i),iterations);

                    }
                    for (int i = 0; i < threads.length; i++) {
                        try {
                            threads[i].join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Platform.runLater(new Runnable() {
                        public void run() {
                            ServerController.this.refreshNetworksTable();
                        }
                    });
                }
            }).start();
        }
    }

    public void handleLoadAllAction() {
        for (CharsetList charsetList:CharsetList.values()) {
            try {
                CommandHandler.uploadNetwork(charsetList);
            } catch (Exception exception) {
                Alert alert = new Alert(AlertType.ERROR, exception.toString());
                alert.showAndWait();
            }
        }
        this.refreshNetworksTable();
    }

    public void handleRemoveAction() {

    }
}
