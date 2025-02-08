module com.example.vpn {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.vpn to javafx.fxml;
    exports com.example.vpn;
}