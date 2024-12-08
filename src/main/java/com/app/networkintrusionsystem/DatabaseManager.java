package com.app.networkintrusionsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String DATA_FILE = "src/main/java/com/app/networkintrusionsystem/data.json";
    private ObjectMapper objectMapper;
    private ObjectNode database;

    public DatabaseManager() {
        objectMapper = new ObjectMapper();
        loadDatabase();
    }

    private void loadDatabase() {
        try {
            database = (ObjectNode) objectMapper.readTree(new File(DATA_FILE));
        } catch (IOException e) {
            e.printStackTrace();
            database = objectMapper.createObjectNode(); // Initialize an empty database if loading fails
            database.putArray("users");
            database.putArray("packets");
        }
    }

    private void saveDatabase() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), database);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkUserCredentials(String username, String password) {
        List<Map<String, String>> users = objectMapper.convertValue(database.get("users"), List.class);
        for (Map<String, String> user : users) {
            if (user.get("username").equals(username) && user.get("password").equals(password)) {
                return true;
            }
        }
        return false;
    }

    public void addPacket(int id, String source, String destination, String data) {
        List<Map<String, Object>> packets = objectMapper.convertValue(database.get("packets"), List.class);
        packets.add(Map.of("id", id, "source", source, "destination", destination, "data", data));
        database.putPOJO("packets", packets);
        saveDatabase();
    }

    public List<Map<String, Object>> getPackets() {
        return objectMapper.convertValue(database.get("packets"), List.class);
    }

    public void updatePacket(int id, String source, String destination, String data) {
        List<Map<String, Object>> packets = objectMapper.convertValue(database.get("packets"), List.class);
        for (Map<String, Object> packet : packets) {
            if (packet.get("id") instanceof Integer && (int) packet.get("id") == id) {
                packet.put("source", source);
                packet.put("destination", destination);
                packet.put("data", data);
                break;
            }
        }
        database.putPOJO("packets", packets);
        saveDatabase();
    }

    public void deletePacket(int id) {
        List<Map<String, Object>> packets = objectMapper.convertValue(database.get("packets"), List.class);
        packets.removeIf(packet -> packet.get("id") instanceof Integer && (int) packet.get("id") == id);
        database.putPOJO("packets", packets);
        saveDatabase();
    }

    public List<Map<String, String>> getUsers() {
        return objectMapper.convertValue(database.get("users"), List.class);
    }

    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();

        // Print users
        List<Map<String, String>> users = dbManager.getUsers();
        System.out.println("Users:");
        for (Map<String, String> user : users) {
            System.out.println("Username: " + user.get("username") + ", Password: " + user.get("password"));
        }

        // Print packets
        List<Map<String, Object>> packets = dbManager.getPackets();
        System.out.println("\nPackets:");
        for (Map<String, Object> packet : packets) {
            System.out.println("ID: " + packet.get("id") + ", Source: " + packet.get("source") + ", Destination: " + packet.get("destination") + ", Data: " + packet.get("data"));
        }
    }
}