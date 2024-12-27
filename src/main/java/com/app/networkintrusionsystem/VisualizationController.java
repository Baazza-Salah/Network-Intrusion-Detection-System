package com.app.networkintrusionsystem;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.Cursor;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualizationController {
    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChartIP;

    @FXML
    private BarChart<String, Number> barChartProtocol;

    @FXML
    private Button capturePacketsButton;

    @FXML
    private Label intrusionStatus;

    @FXML
    private ListView<String> packetListView;

    private static final String JSON_FILE_PATH = "src/main/java/com/app/networkintrusionsystem/data.json";

    @FXML
    public void initialize() {
        loadDataAndInitializeCharts();
        capturePacketsButton.setCursor(Cursor.HAND);

        // Set custom cell factory for coloring ListView items
        packetListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("blocked")) {
                        setStyle("-fx-background-color: #ff6969"); // Blocked packets in red
                    } else {
                        setStyle("-fx-background-color: #74ff74"); // Received packets in light green
                    }
                }
            }
        });

        // Set up a timeline to refresh data every few seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> loadDataAndInitializeCharts()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void loadDataAndInitializeCharts() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> data = mapper.readValue(new File(JSON_FILE_PATH), Map.class);
            List<Map<String, Object>> packets = (List<Map<String, Object>>) data.get("packets");

            lineChart.getData().clear(); // Clear previous data
            pieChart.getData().clear(); // Clear previous data
            barChartIP.getData().clear(); // Clear previous data
            barChartProtocol.getData().clear(); // Clear previous data
            logPacketInfo(packets); // Log packet information

            initializeLineChart(packets);
            initializePieChart(packets);
            initializeBarChartIP(packets);
            initializeBarChartProtocol(packets);
        } catch (IOException e) {
            e.printStackTrace();
            intrusionStatus.setText("Error loading data: " + e.getMessage());
        }
    }

    private void logPacketInfo(List<Map<String, Object>> packets) {
        ObservableList<String> packetList = FXCollections.observableArrayList();
        for (Map<String, Object> packet : packets) {
            String status = (String) packet.get("status");
            String logEntry = String.format("Time: %s, Status: %s, Destination: %s, Protocol: %s",
                    packet.get("timestamp"), status, packet.get("destination"), packet.get("protocol"));
            packetList.add(logEntry);
        }
        packetListView.setItems(packetList); // Set items in ListView
    }

    private void initializeLineChart(List<Map<String, Object>> packets) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Packets Captured");
        Map<String, Integer> hourlyCount = new HashMap<>();

        for (Map<String, Object> packet : packets) {
            String timestamp = (String) packet.get("timestamp");
            String hour = timestamp.substring(11, 13);
            hourlyCount.put(hour, hourlyCount.getOrDefault(hour, 0) + 1);
        }

        hourlyCount.forEach((hour, count) -> series.getData().add(new XYChart.Data<>(hour, count)));
        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false); // Disable symbols for a smoother look
    }

    private void initializePieChart(List<Map<String, Object>> packets) {
        int receivedCount = 0;
        int blockedCount = 0;

        for (Map<String, Object> packet : packets) {
            String status = (String) packet.get("status");
            if ("received".equals(status)) {
                receivedCount++;
            } else if ("blocked".equals(status)) {
                blockedCount++;
            }
        }

        pieChart.getData().add(new PieChart.Data("Received (" + receivedCount + ")", receivedCount));
        pieChart.getData().add(new PieChart.Data("Blocked (" + blockedCount + ")", blockedCount));

        // Intrusion detection
        if (blockedCount > 0) {
            intrusionStatus.setText("Intrusion Detected: " + blockedCount + " blocked packets!");
        } else {
            intrusionStatus.setText("No Intrusions Detected.");
        }
    }

    private void initializeBarChartIP(List<Map<String, Object>> packets) {
        Map<String, Integer> ipCount = new HashMap<>();

        for (Map<String, Object> packet : packets) {
            String destination = (String) packet.get("destination");
            ipCount.put(destination, ipCount.getOrDefault(destination, 0) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("IP Destination Usage");
        ipCount.forEach((ip, count) -> series.getData().add(new XYChart.Data<>(ip, count)));
        barChartIP.getData().add(series);
    }

    private void initializeBarChartProtocol(List<Map<String, Object>> packets) {
        Map<String, Integer> protocolCount = new HashMap<>();

        for (Map<String, Object> packet : packets) {
            String protocol = (String) packet.get("protocol");
            protocolCount.put(protocol, protocolCount.getOrDefault(protocol, 0) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Protocol Usage");
        protocolCount.forEach((protocol, count) -> series.getData().add(new XYChart.Data<>(protocol, count)));
        barChartProtocol.getData().add(series);
    }
}