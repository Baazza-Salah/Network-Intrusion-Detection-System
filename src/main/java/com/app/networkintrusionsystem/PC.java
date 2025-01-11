package com.app.networkintrusionsystem;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PC {

    public static void main(String[] args) {
        packetSniffer();
    }

    public static void packetSniffer() {
        List<PcapIf> devices = new ArrayList<>();
        StringBuilder errorBuffer = new StringBuilder();

        // Find all available devices
        int result = Pcap.findAllDevs(devices, errorBuffer);
        if (result == Pcap.NOT_OK || devices.isEmpty()) {
            System.err.printf("Error finding devices: %s\n", errorBuffer.toString());
            return;
        }

        // Look for the specific device
        PcapIf selectedDevice = null;
        for (PcapIf device : devices) {
            if (device.getName().equals("\\Device\\NPF_{CAA5E8E3-FEF0-4429-B7A1-DACE0B677B0C}")) {
                selectedDevice = device;
                break;
            }
        }

        if (selectedDevice == null) {
            System.err.println("Device not found.");
            return;
        }

        System.out.printf("Using device: %s - %s\n", selectedDevice.getName(),
                selectedDevice.getDescription() == null ? "No description" : selectedDevice.getDescription());

        // Open the device for packet capture
        int snaplen = 64 * 1024; // Capture all packets, no truncation
        int flags = Pcap.MODE_PROMISCUOUS; // Capture all packets
        int timeout = 10 * 1000; // 10 seconds in milliseconds

        Pcap pcap = Pcap.openLive(selectedDevice.getName(), snaplen, flags, timeout, errorBuffer);

        if (pcap == null) {
            System.err.printf("Error opening device for capture: %s\n", errorBuffer.toString());
            return;
        }

        System.out.println("Starting packet capture...");

        // Packet handler to process captured packets
        PcapPacketHandler<String> packetHandler = (packet, user) -> {
            Ip4 ip = new Ip4();
            String sourceIp = null;
            String destIp = null;
            String protocol = "Unknown";

            long timestamp = packet.getCaptureHeader().timestampInMillis();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));

            if (packet.hasHeader(ip)) {
                sourceIp = byteArrayToIPv4(ip.source());
                destIp = byteArrayToIPv4(ip.destination());
                protocol = "IP";
            } else if (packet.hasHeader(new org.jnetpcap.protocol.lan.Ethernet())) {
                protocol = "Ethernet";
            }

            // Print packet information with formatted date
            System.out.printf("[%s] Packet: %s -> %s, Protocol: %s\n",
                    formattedDate,
                    sourceIp == null ? "Unknown" : sourceIp,
                    destIp == null ? "Unknown" : destIp,
                    protocol);

            // Prepare the packet data
            Map<String, Object> packetData = new HashMap<>();
            packetData.put("timestamp", formattedDate);
            packetData.put("source_ip", sourceIp == null ? "Unknown" : sourceIp);
            packetData.put("dest_ip", destIp == null ? "Unknown" : destIp);
            packetData.put("protocol", protocol);

            // Save the captured packet data to the JSON file
            savePacketToJson(packetData);
        };

        // Start capturing packets for 10 seconds
        pcap.loop(Pcap.LOOP_INFINITE, packetHandler, "Packet Capture");

        // Close capturing
        pcap.close();
    }

    private static void savePacketToJson(Map<String, Object> packetData) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("packet.json");

        try {
            // Check if the file exists, if not, create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // Read the existing JSON data
            List<Map<String, Object>> packets = new ArrayList<>();
            if (file.length() > 0) {
                packets = objectMapper.readValue(file, List.class);
            }

            // Add the new packet data
            packets.add(packetData);

            // Write updated packet data to the JSON file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, packets);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String byteArrayToIPv4(byte[] ipAddress) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ipAddress.length; i++) {
            sb.append(ipAddress[i] & 0xFF);  // Mask with 0xFF to treat byte as unsigned
            if (i < ipAddress.length - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }
}
