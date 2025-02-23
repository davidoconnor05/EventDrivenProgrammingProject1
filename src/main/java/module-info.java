module org.example.eventdrivenprogrammingproject1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.eventdrivenprogrammingproject1 to javafx.fxml;
    exports org.example.eventdrivenprogrammingproject1;
}