package com.clientservernn.client.guiFX;

import com.clientservernn.client.additional.Loader;
import com.clientservernn.client.additional.ServerChecker;
import com.clientservernn.client.additional.TinyLoader;
import com.clientservernn.common.Dialogues;
import com.clientservernn.common.CharsetList;
import com.clientservernn.dataTransfer.Command;

import com.clientservernn.dataTransfer.DataTransfer;
import com.clientservernn.dataTransfer.ImageData;
import com.clientservernn.dataTransfer.ImageDataUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

import static com.clientservernn.client.guiFX.DefaultData.CANVAS_HEIGHT;
import static com.clientservernn.client.guiFX.DefaultData.CANVAS_WIDTH;
public class NeuronController implements Initializable {


    public Label serverResponse;
    public Canvas myCanvas;
    public MenuBar menuBar;
    public BorderPane borderPane;
    public ComboBox<String> itemsList;
    public Label connectionStatus;
    public VBox tableBox;
    public TableColumn <ImageTable,Integer>tableColumn1;
    public TableColumn<ImageTable,String> tableColumn2;
    public TableColumn<ImageTable,String> tableColumn3;
    public TableColumn <ImageTable,String>tableColumn4;
    public TableColumn <ImageTable, ImageTable.Changes> tableColumn5;
    public Label charSetLabel;

    // private ProgressTrend progressTrend;

    @FXML
    private TableView<ImageTable> itemTable;

    boolean isPressed;
    double coordinateX, coordinateY;



    Socket socket;
    static int serverPort;
    static String serverIP;
    HandleInOut handleInOut;

    String userName="unsigned";

    public static ServerChecker serverChecker;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        serverIP="127.0.0.1";
        serverPort=3340;
        isPressed=false;
        coordinateX=0;
        coordinateY=0;
        myCanvas.setHeight(CANVAS_HEIGHT);
        myCanvas.setWidth(CANVAS_WIDTH);
        myCanvas.getGraphicsContext2D().setFill(Color.WHITE);
        myCanvas.getGraphicsContext2D().fillRect(0,0, myCanvas.getWidth(), myCanvas.getHeight() );
        menuBar.prefWidthProperty().bind(borderPane.widthProperty());
        itemsList.getSelectionModel().select(0);
        handleInOut = HandleInOut.getInstance();
        serverChecker=new ServerChecker(serverResponse,connectionStatus,handleInOut::isConnected);
        tableColumn1.setCellValueFactory(new PropertyValueFactory<>("serial"));
        tableColumn2.setCellValueFactory(new PropertyValueFactory<>("letter"));
        tableColumn3.setCellValueFactory(new PropertyValueFactory<>("date"));
        tableColumn4.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumn5.setCellValueFactory(new PropertyValueFactory<>("change"));
    }

    public void handleConnectAction() {
        if (handleInOut.isConnected()) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "You are already connected");
            warning.show();
            return;
        }
        DialogHandler dialogHandler=new DialogHandler(Dialogues.CONNECT);

        String input=dialogHandler.getDialog().orElse(null);
        if (input!=null){
            try
            {
                Scanner scanner=new Scanner(input);
                for (int i = 0; i < 2; i++) {
                    if(scanner.hasNextInt()) {
                        serverPort=scanner.nextInt();
                    } else if (scanner.hasNext()) {
                        serverIP=scanner.next();
                    } else {
                        break;
                    }
                }
                socket=new Socket(serverIP, serverPort);
                handleInOut.setData(socket, serverChecker);
                handleInOut.send(new DataTransfer(null, Command.USER_DATA,userName));
            } catch (IOException e) {
               Alert alert=new Alert(Alert.AlertType.ERROR, e.getMessage());
               alert.show();
            }
        }




    }

    public void onMDragged(MouseEvent mouseEvent) {
        myCanvas.getGraphicsContext2D().setStroke(Color.BLACK);
        myCanvas.getGraphicsContext2D().setLineWidth(5);
        myCanvas.getGraphicsContext2D().strokeLine(coordinateX,coordinateY, mouseEvent.getX(),mouseEvent.getY());
        coordinateX=mouseEvent.getX();
        coordinateY=mouseEvent.getY();
    }
    public void onMPressed(MouseEvent mouseEvent) {
        isPressed=true;
        coordinateX=mouseEvent.getX();
        coordinateY=mouseEvent.getY();

    }
    public void onMReleased(MouseEvent mouseEvent) {
        isPressed=false;
    }


    public void handleClearPressed(MouseEvent mouseEvent) {
        myCanvas.getGraphicsContext2D().setFill(Color.WHITE);
        myCanvas.getGraphicsContext2D().fillRect(0,0, myCanvas.getWidth(), myCanvas.getHeight() );

    }



    public void handleDisconnectAction(ActionEvent actionEvent) {
        if (handleInOut.isConnected()) {
            try {
                DataTransfer dataTransfer =new DataTransfer(null, Command.DISCONNECT, "close");

                handleInOut.send(dataTransfer);
                //TODO
                Thread.sleep(1000);
                socket.close();


            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else  {
            Alert warning=new Alert(Alert.AlertType.WARNING, "You are not connected");
            warning.show();

        }
    }

    public void handleCheckPressed(MouseEvent mouseEvent) {
        if (!handleInOut.isConnected()) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "You are not connected");
            warning.show();
            return;
        }
        ImageData imageData = ImageDataUtil.ofImage(myCanvas.snapshot(null,null));
        DataTransfer dataTransfer =new DataTransfer(ImageDataUtil.getStandard(imageData), Command.RECOGNIZE, charSetLabel.getText());
        handleInOut.send(dataTransfer);
    }



    public void handleDownloadPressed(MouseEvent mouseEvent) {
        if (!handleInOut.isConnected()) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "You are not connected");
            warning.show();
            return;
        }

        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<String> dialog = new Dialog<>();
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event->{dialog.close();});
        dialog.getDialogPane().getButtonTypes().add(cancelButton);
        HBox hBox=new HBox();
        hBox.setPadding(new Insets(20));
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        Label downloadName=new Label("Download progress");
        ProgressBar progressBar=new ProgressBar();
        hBox.getChildren().addAll(downloadName, progressBar);
        dialog.getDialogPane().setContent(hBox);
        final Loader loader =new Loader(progressBar,charSetLabel.getText(), itemsList.getValue().toString());
        handleInOut.send(loader);
        progressBar.addEventFilter(ActionEvent.ANY, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (loader.isLoaded()){
                    itemTable.setItems(loader.getTable());
                dialog.close();
                } else if (loader.isInterrupted()){
                    dialog.close();
                }
            }
        });

        dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent dialogEvent) {
                if (!loader.isLoaded()){
                Alert alert=new Alert(Alert.AlertType.WARNING,"Loading is stopped");
                alert.show();
                }

            }
        });
        dialog.show();
    }

    public void handleSaveChangesPressed(MouseEvent mouseEvent) {
        if (!handleInOut.isConnected()) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "You are not connected");
            warning.show();
            return;
        }
        if (itemTable==null||itemTable.getItems()==null) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "There is nothing to save");
            warning.show();
            return;
        }

        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<String> dialog = new Dialog<>();
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event->{dialog.close();});

        dialog.getDialogPane().getButtonTypes().add(cancelButton);
        HBox hBox=new HBox();
        hBox.setPadding(new Insets(20));
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        Label downloadName=new Label("Upload progress");
        ProgressBar progressBar=new ProgressBar();
        hBox.getChildren().addAll(downloadName, progressBar);
        dialog.getDialogPane().setContent(hBox);

        final Loader loader =new Loader(progressBar,charSetLabel.getText(), itemTable.getItems());
        handleInOut.send(loader);
        progressBar.addEventFilter(ActionEvent.ANY, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (loader.isLoaded()) {

                    itemTable.setItems(loader.getTable());
                    itemTable.refresh();
                    dialog.close();
                }
            }
        });

        dialog.showAndWait();
    }


    public void handleTableClicked(MouseEvent mouseEvent) {
        ImageTable imageTable=itemTable.getSelectionModel().getSelectedItem();
        if (imageTable==null) {return;}
        Image image=imageTable.getImage();
        if (image!=null) {myCanvas.getGraphicsContext2D().drawImage(image,0,0,CANVAS_WIDTH,CANVAS_HEIGHT);}
        else {
            Alert warning=new Alert(Alert.AlertType.WARNING, "Image is null");
            warning.show();
        }

    }

    public void handleAddPressed(MouseEvent mouseEvent) {

        if (itemTable==null||itemTable.getItems()==null||itemsList.getValue()==null) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "There is no selected item");
            warning.show();
            return;
        }
        ImageData imageData= ImageDataUtil.getStandard(ImageDataUtil.ofImage(myCanvas.snapshot(null,null)));
        Date date=new Date();
        int number=0;
        for (ImageTable imageTable:itemTable.getItems()) {
            number=Math.max(number,imageTable.getSerial());
        }
        ImageTable imageTable=new ImageTable(itemsList.getValue().toString(), userName,date.getTime(),++number,imageData);
        imageTable.setAsAdd();
        itemTable.getItems().add(imageTable);
        itemTable.refresh();
    }

    public void handleDeletePressed(MouseEvent mouseEvent) {
        if (itemTable.getSelectionModel().isEmpty()||itemTable.getItems()==null) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "There is no selected item");
            warning.show();
            return;
        }

        ImageTable imageTable=itemTable.getSelectionModel().getSelectedItem();
        imageTable.setAsDelete();
        itemTable.refresh();
    }

    public void handleRescPressed(MouseEvent mouseEvent) {
        ImageData imageData = ImageDataUtil.ofImage(myCanvas.snapshot(null,null));
        Image image=ImageDataUtil.getStandard(imageData).getImage();

        if (image!=null) {myCanvas.getGraphicsContext2D().drawImage(image,0,0,CANVAS_WIDTH,CANVAS_HEIGHT);}
        else {
            Alert warning=new Alert(Alert.AlertType.WARNING, "Image is null");
            warning.show();
        }

    }

    public void handleSelectCharsetAction(ActionEvent actionEvent)  {
        if (socket==null||socket.isClosed()) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "You are not connected");
            warning.show();
            return;
        }
        ButtonType connectButton = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<String> dialog = new Dialog<>();
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event->dialog.close());
        dialog.getDialogPane().getButtonTypes().add(connectButton);
        dialog.getDialogPane().getButtonTypes().add(cancelButton);
        Button buttonOK=(Button) dialog.getDialogPane().lookupButton(connectButton);
        HBox hBox=new HBox();
        hBox.setPadding(new Insets(20));
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        Label statusName=new Label("Charsets:");
        ComboBox<CharsetList> comboBox=new ComboBox<>();
        ObservableList<CharsetList> paths= FXCollections.observableArrayList(CharsetList.values());
        comboBox.setItems(paths);
        comboBox.getSelectionModel().selectFirst();
        ComboBox<String> stringComboBox=new ComboBox<>();
       comboBox.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                buttonOK.setDisable(true);
                CharsetList charsetList =comboBox.getSelectionModel().getSelectedItem();
                if (charsetList !=null) {
                    final TinyLoader tinyLoader =new TinyLoader(buttonOK,stringComboBox, charsetList.name());
                    handleInOut.send(tinyLoader);

                }
            }
        });
        Event.fireEvent(comboBox, new ActionEvent());

        hBox.getChildren().addAll(statusName, comboBox);
        dialog.getDialogPane().setContent(hBox);
        buttonOK.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (stringComboBox.getItems().size()>0) {
                charSetLabel.setText(comboBox.getSelectionModel().getSelectedItem().name());
                itemsList.setItems(stringComboBox.getItems());
                itemsList.getSelectionModel().selectFirst();
                }
            }
        });
        dialog.showAndWait();

    }

    public void handleSignInAction(ActionEvent actionEvent) {
        if (!handleInOut.isConnected()) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "You are not connected");
            warning.show();
            return;
        }
        DialogHandler dialogHandler=new DialogHandler(Dialogues.USER_DATA);
        dialogHandler.getDialog().ifPresent(input -> {
            userName= input;
            handleInOut.send(new DataTransfer(null, Command.USER_DATA, userName));
        });
    }

    public void handleSignOutAction(ActionEvent actionEvent) {
        userName="unsigned";
        handleInOut.send(new DataTransfer(null, Command.USER_DATA,userName));
    }

    public void handleExitAction() {
        Platform.exit();
        System.exit(0);
    }

    public void handleCheckAllPressed() {
        if (!handleInOut.isConnected()) {
            Alert warning=new Alert(Alert.AlertType.WARNING, "You are not connected");
            warning.show();
            return;
        }
        ImageData imageData = ImageDataUtil.ofImage(myCanvas.snapshot(null,null));
        DataTransfer dataTransfer =new DataTransfer(ImageDataUtil.getStandard(imageData), Command.RECOGNIZE, (String) null);
        handleInOut.send(dataTransfer);
    }
}
