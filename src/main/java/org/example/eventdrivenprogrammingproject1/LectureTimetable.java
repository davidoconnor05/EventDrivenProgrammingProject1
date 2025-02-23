package org.example.eventdrivenprogrammingproject1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;


public class LectureTimetable extends Application { // Extend Application
    private TextArea responseArea;
    private ComboBox<String> actionBox;
    private TextField moduleField;
    private ComboBox<String> dateBox, startBox, endBox, roomBox;
    private Button sendButton, stopButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void start(Stage primaryStage) { // Override start method
        primaryStage.setTitle("Lecture Scheduler Client");


        // Dropdown for selecting action
        actionBox = new ComboBox<>();
        actionBox.getItems().addAll("Add Lecture", "Remove Lecture");
        actionBox.setPromptText("Select Action");

        // Input fields
        moduleField = new TextField();
        moduleField.setPromptText("Module Name");

        dateBox = new ComboBox<>();
        dateBox.setPromptText("Day");
        dateBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");

        startBox = new ComboBox<>();
        startBox.setPromptText("Start Time");
        startBox.getItems().addAll("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00");

        endBox = new ComboBox<>();
        endBox.setPromptText("End Time");
        endBox.getItems().addAll("10:00", "11:00","12:00", "13:00", "14:00", "15:00", "16:00", "17:00");

        roomBox = new ComboBox<>();
        roomBox.setPromptText("Room");
        roomBox.getItems().addAll("Room 101", "Room 102", "Room 103");

        // Buttons
        sendButton = new Button("Send Request");
        stopButton = new Button("STOP");

        // Response area
        responseArea = new TextArea();
        responseArea.setEditable(false);
        responseArea.setPromptText("Server response will appear here...");

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.getChildren().addAll(actionBox, moduleField, dateBox, startBox, endBox, roomBox, sendButton, stopButton, responseArea);

        // Button event handlers
        sendButton.setOnAction(e -> sendRequest());
        stopButton.setOnAction(e -> stopConnection());

        primaryStage.setScene(new Scene(layout, 350, 400));
        Scene scene = primaryStage.getScene(); // Get the scene reference
       //scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // Add the stylesheet to the scene
        primaryStage.show();


        // Establish connection with the server
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 291); // Adjust port if needed
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            responseArea.appendText("Connected to Server\n");
        } catch (IOException e) {
            responseArea.appendText("Failed to connect to server\n");
        }
    }

    private void sendRequest() {
        /*if (socket == null || socket.isClosed()) {
            responseArea.appendText("Not connected to server!\n");
            return;
        }
        */
        String action = actionBox.getValue();
        String module = moduleField.getText();
        String date = dateBox.getValue();
        String timeStart = startBox.getValue();
        String timeEnd = endBox.getValue();
        String room = roomBox.getValue();

        if (action == null || (action.equals("Add Lecture") || action.equals("Remove Lecture")) &&
                (module.isEmpty() || date == null || timeStart == null || timeEnd == null || room == null)) {
            responseArea.appendText("Please fill in all required fields!\n");
            return;
        }

        String message = action + "; " + module + "; " + date + "; " + timeStart + "; " + timeEnd + "; " + room;
        out.println(message);
        responseArea.appendText(message);

        try {
            String response = in.readLine();
            responseArea.appendText("Server: " + response + "\n");
        } catch (IOException e) {
            responseArea.appendText("Error receiving response from server\n");
        }
    }

    private void stopConnection() {
        if (socket != null && !socket.isClosed()) {
            out.println("STOP");
            responseArea.appendText("Sent STOP to server\n");
            try {
                socket.close();
            } catch (IOException e) {
                responseArea.appendText("Error closing connection\n");
            }
        }
    }

    public static void main(String[] args) {
        launch(args); // Call launch() to start JavaFX application
    }
}
