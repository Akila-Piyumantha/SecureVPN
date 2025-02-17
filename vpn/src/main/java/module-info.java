module com.example.vpn {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.net.http;
    requires org.pcap4j.core;


    opens com.example.vpn to javafx.fxml;
    exports com.example.vpn;
}