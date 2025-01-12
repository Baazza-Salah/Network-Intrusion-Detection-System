module com.app.networkintrusionsystem {
    requires java.net.http;
    requires com.google.gson;
//    requires org.pcap4j.core;
//    requires org.slf4j;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires annotations;
    requires jnetpcap;
    requires json;

    opens com.app.networkintrusionsystem.controller to javafx.fxml;

    exports com.app.networkintrusionsystem.presentation;
}