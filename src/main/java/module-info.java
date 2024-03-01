module ServerNeuronNet {
    requires javafx.controls;
    requires javafx.fxml;



    exports com.clientservernn.server.guiFX;
    opens com.clientservernn.server.guiFX to javafx.fxml;
    exports com.clientservernn.dataTransfer;
    opens com.clientservernn.dataTransfer to javafx.fxml;
    exports com.clientservernn.server.neuralNetwork;
    opens com.clientservernn.server.neuralNetwork to javafx.fxml;
    exports com.clientservernn.client.guiFX;
    opens com.clientservernn.client.guiFX to javafx.fxml;
    exports com.clientservernn.client.additional;
    opens com.clientservernn.client.additional to javafx.fxml;
}