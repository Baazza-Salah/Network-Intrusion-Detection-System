package com.app.networkintrusionsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
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
    private Button logOut;

    @FXML
    private Label intrusionStatus;

    @FXML
    private ListView<String> packetListView;

    private static final String JSON_FILE_PATH = "src/main/resources/com/app/networkintrusionsystem/data/packet.json";

    @FXML
    public void initialize() {
        // Load initial data and initialize charts
        updateCharts(); // Initial load
        capturePacketsButton.setCursor(Cursor.HAND);
        logOut.setCursor(Cursor.HAND);

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
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> updateCharts()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateCharts() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Object>> packets = mapper.readValue(new File(JSON_FILE_PATH), List.class);

            logPacketInfo(packets); // Log packet information

            // Update charts
            updateLineChart(packets);
            updatePieChart(packets);
            updateBarChartIP(packets);
            updateBarChartProtocol(packets);
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
                    packet.get("timestamp"), status, packet.get("dest_ip"), packet.get("protocol"));
            packetList.add(logEntry);
        }
        packetListView.setItems(packetList); // Set items in ListView
    }

    private void updateLineChart(List<Map<String, Object>> packets) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Packets Captured");
        Map<String, Integer> hourlyCount = new HashMap<>();

        for (Map<String, Object> packet : packets) {
            String timestamp = (String) packet.get("timestamp");
            String hour = timestamp.substring(11, 13);
            hourlyCount.put(hour, hourlyCount.getOrDefault(hour, 0) + 1);
        }

        // Add new data to the series without clearing previous
        hourlyCount.forEach((hour, count) -> {
            series.getData().add(new XYChart.Data<>(hour, count));
        });

        lineChart.getData().clear(); // Clear previous data for smooth updates
        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false); // Disable symbols for a smoother look

        // Set line color for the line chart
        series.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-stroke: #2196F3;"); // Blue color for line
            }
        });
    }

    private void updatePieChart(List<Map<String, Object>> packets) {
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

        pieChart.getData().clear(); // Clear previous data
        pieChart.getData().add(new PieChart.Data("Received (" + receivedCount + ")", receivedCount));
        pieChart.getData().add(new PieChart.Data("Blocked (" + blockedCount + ")", blockedCount));

        // Set colors for the pie chart
        for (PieChart.Data data : pieChart.getData()) {
            if (data.getName().contains("Received")) {
                data.getNode().setStyle("-fx-pie-color: #2196F3;"); // Blue for received
            } else {
                data.getNode().setStyle("-fx-pie-color: #f44336;"); // Red for blocked
            }
        }

        // Intrusion detection
        if (blockedCount > 0) {
            intrusionStatus.setText("Intrusion Detected: " + blockedCount + " blocked packets!");
        } else {
            intrusionStatus.setText("No Intrusions Detected.");
        }
    }

    private void updateBarChartIP(List<Map<String, Object>> packets) {
        Map<String, Integer> ipCount = new HashMap<>();

        for (Map<String, Object> packet : packets) {
            String destination = (String) packet.get("dest_ip");
            ipCount.put(destination, ipCount.getOrDefault(destination, 0) + 1);
        }

        barChartIP.getData().clear(); // Clear previous data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("IP Destination Usage");
        ipCount.forEach((ip, count) -> series.getData().add(new XYChart.Data<>(ip, count)));

        // Set colors for the bar chart
        series.getData().forEach(data -> {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #42a5f5;"); // Light blue color for bars
                }
            });
        });

        barChartIP.getData().add(series);
    }

    private void updateBarChartProtocol(List<Map<String, Object>> packets) {
        Map<String, Integer> protocolCount = new HashMap<>();

        for (Map<String, Object> packet : packets) {
            String protocol = (String) packet.get("protocol");
            protocolCount.put(protocol, protocolCount.getOrDefault(protocol, 0) + 1);
        }

        barChartProtocol.getData().clear(); // Clear previous data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Protocol Usage");
        protocolCount.forEach((protocol, count) -> series.getData().add(new XYChart.Data<>(protocol, count)));

        // Set colors for the bar chart
        series.getData().forEach(data -> {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #42a5f5;"); // Light blue color for bars
                }
            });
        });

        barChartProtocol.getData().add(series);
    }

    @FXML
    private void goToCapturePage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/app/networkintrusionsystem/fxmlFiles/capturepackets.fxml"));
            Scene scene = new Scene(root, 1000, 500);

            // Load CSS for the visualization page
            scene.getStylesheets().add(getClass().getResource("/com/app/networkintrusionsystem/style/capturestyle.css").toExternalForm());

            Stage stage = (Stage) capturePacketsButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLoginPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/app/networkintrusionsystem/fxmlFiles/LoginPage.fxml"));
            Scene scene = new Scene(root, 1000, 500);

            // Load CSS for the visualization page
            scene.getStylesheets().add(getClass().getResource("/com/app/networkintrusionsystem/style/LoginStyle.css").toExternalForm());

            Stage stage = (Stage) logOut.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}