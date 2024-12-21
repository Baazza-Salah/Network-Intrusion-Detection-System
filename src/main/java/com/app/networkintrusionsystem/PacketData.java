package com.app.networkintrusionsystem;

public class PacketData {
    private int id;
    private String source;
    private String destination;
    private String data;
    private String timestamp;
    private String protocol;
    private int size;
    private String status = "received"; // Default status
    private int sourcePort;
    private int destinationPort;

    private static int idCounter = 1; // Static counter for ID generation

    public PacketData(String source, String destination, String data, String timestamp, String protocol, int size, int sourcePort, int destinationPort) {
        this.id = idCounter++;
        this.source = source;
        this.destination = destination;
        this.data = data;
        this.timestamp = timestamp;
        this.protocol = protocol;
        this.size = size;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }

    // Getters for all fields (important for Gson serialization)
    public int getId() { return id; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getData() { return data; }
    public String getTimestamp() { return timestamp; }
    public String getProtocol() { return protocol; }
    public int getSize() { return size; }
    public String getStatus() { return status; }
    public int getSourcePort() { return sourcePort; }
    public int getDestinationPort() { return destinationPort; }
}