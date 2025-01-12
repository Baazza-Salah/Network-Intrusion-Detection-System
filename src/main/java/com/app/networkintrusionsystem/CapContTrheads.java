package com.app.networkintrusionsystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CapContTrheads {

    @FXML
    private ListView<String> packetListView;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button backButton;

    @FXML
    private Label statusLabel;

    private List<Pcap> pcaps = new ArrayList<>();
    private volatile boolean capturing = false;

    @FXML
    public void initialize() {
        stopButton.setDisable(true);
    }

    @FXML
    public void startCapture() {
        capturing = true;
        startButton.setDisable(true);
        stopButton.setDisable(false);
        statusLabel.setText("Capturing network traffic...");

        new Thread(this::startPacketSniffers).start();
    }

    @FXML
    public void stopCapture() {
        capturing = false;
        startButton.setDisable(false);
        stopButton.setDisable(true);
        statusLabel.setText("Stopping capture...");

        // Break the pcap loops
        for (Pcap pcap : pcaps) {
            if (pcap != null) {
                pcap.breakloop();
            }
        }
    }

    private void startPacketSniffers() {
        List<PcapIf> devices = new ArrayList<>();
        StringBuilder errorBuffer = new StringBuilder();

        int result = Pcap.findAllDevs(devices, errorBuffer);
        if (result != Pcap.OK || devices.isEmpty()) {
            Platform.runLater(() -> {
                statusLabel.setText("Error finding devices: " + errorBuffer.toString());
            });
            return;
        }

        for (PcapIf device : devices) {
            new Thread(() -> packetSniffer(device)).start();
        }
    }

    private void packetSniffer(PcapIf device) {
        StringBuilder errorBuffer = new StringBuilder();
        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;

        Pcap pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errorBuffer);
        if (pcap == null) {
            Platform.runLater(() -> {
                statusLabel.setText("Error opening device for capture: " + errorBuffer.toString());
            });
            return;
        }

        pcaps.add(pcap);

        PcapPacketHandler<String> packetHandler = (packet, user) -> {
            Ip4 ip = new Ip4();
            Tcp tcp = new Tcp();
            Udp udp = new Udp();
            String sourceIp = null;
            String destIp = null;
            String protocol = "Unknown";
            int srcPort = 0;
            int dstPort = 0;

            long timestamp = packet.getCaptureHeader().timestampInMillis();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));

            if (packet.hasHeader(ip)) {
                sourceIp = byteArrayToIPv4(ip.source());
                destIp = byteArrayToIPv4(ip.destination());
                protocol = "IP";
            }
            if (packet.hasHeader(tcp)) {
                protocol = "TCP";
                srcPort = tcp.source();
                dstPort = tcp.destination();
            } else if (packet.hasHeader(udp)) {
                protocol = "UDP";
                srcPort = udp.source();
                dstPort = udp.destination();
            } else if (packet.hasHeader(new org.jnetpcap.protocol.lan.Ethernet())) {
                protocol = "Ethernet";
            }

            String packetInfo = String.format(
                    "[%s] Packet: %s -> %s, Protocol: %s, Src Port: %d, Dst Port: %d",
                    formattedDate,
                    sourceIp == null ? "Unknown" : sourceIp,
                    destIp == null ? "Unknown" : destIp,
                    protocol,
                    srcPort,
                    dstPort
            );

            Platform.runLater(() -> packetListView.getItems().add(packetInfo));

            Map<String, Object> packetData = new HashMap<>();
            packetData.put("timestamp", formattedDate);
            packetData.put("source_ip", sourceIp == null ? "Unknown" : sourceIp);
            packetData.put("dest_ip", destIp == null ? "Unknown" : destIp);
            packetData.put("protocol", protocol);
            packetData.put("src_port", srcPort);
            packetData.put("dst_port", dstPort);
            packetData.put("data", packet.toHexdump());

            savePacketToJson(packetData);
        };

        pcap.loop(Pcap.LOOP_INFINITE, packetHandler, "Packet Capture");
        pcap.close();
    }

    private void savePacketToJson(Map<String, Object> packetData) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("/com/app/networkintrusionsystem/data/packet.json");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            List<Map<String, Object>> packets = new ArrayList<>();
            if (file.length() > 0) {
                packets = objectMapper.readValue(file, List.class);
            }

            packets.add(packetData);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, packets);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String byteArrayToIPv4(byte[] ipAddress) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ipAddress.length; i++) {
            sb.append(ipAddress[i] & 0xFF);
            if (i < ipAddress.length - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("com/app/networkintrusionsystem/VisualizationPage.fxml"));
            Scene scene = new Scene(root, 1000, 500);

            scene.getStylesheets().add(getClass().getResource("com/app/networkintrusionsystem/style/VisualizationStyle.css").toExternalForm());

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Visualization Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




/************** latest one works with threads all devices **************/