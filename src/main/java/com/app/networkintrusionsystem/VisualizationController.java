package com.app.networkintrusionsystem;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private static final String JSON_FILE_PATH = "src/main/java/com/app/networkintrusionsystem/data.json";

    @FXML
    public void initialize() {
        loadDataAndInitializeCharts();
    }

    private void loadDataAndInitializeCharts() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> data = mapper.readValue(new File(JSON_FILE_PATH), Map.class);
            initializeLineChart((List<Map<String, Object>>) data.get("packets"));
            initializePieChart((List<Map<String, Object>>) data.get("packets"));
            initializeBarChartIP((List<Map<String, Object>>) data.get("packets"));
            initializeBarChartProtocol((List<Map<String, Object>>) data.get("packets"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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