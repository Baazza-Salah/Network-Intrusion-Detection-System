package com.app.networkintrusionsystem;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import org.pcap4j.core.*;
//import org.pcap4j.packet.IpV4Packet;
//import org.pcap4j.packet.Packet;
//import org.pcap4j.packet.TcpPacket;
//import org.pcap4j.packet.UdpPacket;
//
//import java.io.*;
//import java.net.Inet4Address;
//import java.util.*;
//import java.util.concurrent.*;
//
//import com.app.networkintrusionsystem.PacketData;

public class CaptureController {
//    private static final String OUTPUT_FILE = "src/main/java/com/app/networkintrusionsystem/packets.json";
//    private static final int SNAPLEN = 65536;
//    private static final int TIMEOUT = 1000;
//    private static volatile boolean isCapturing = true;
//    private static final Gson gson = new GsonBuilder()
//            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
//            .setPrettyPrinting()  // Enable pretty printing
//            .create();
//
//    public static void main(String[] args) {
//        try {
//            List<PcapNetworkInterface> interfaces = Pcaps.findAllDevs();
//            if (interfaces == null || interfaces.isEmpty()) {
//                System.out.println("No network interfaces found.");
//                return;
//            }
//
//            for (PcapNetworkInterface networkInterface : interfaces) {
//                if (isValidInterface(networkInterface)) {
//                    capturePacketsOnInterface(networkInterface);
//                }
//            }
//        } catch (PcapNativeException e) {
//            System.err.println("Error finding network devices: " + e.getMessage());
//        }
//    }
//
//    private static boolean isValidInterface(PcapNetworkInterface networkInterface) {
//        if (networkInterface == null || networkInterface.getName() == null) {
//            return false;
//        }
//
//        String description = networkInterface.getDescription() != null ?
//                networkInterface.getDescription().toLowerCase() : "";
//
//        return !description.contains("wan miniport") &&
//                !description.contains("loopback") &&
//                !description.contains("virtual");
//    }
//
//    private static void capturePacketsOnInterface(PcapNetworkInterface networkInterface) {
//        System.out.println("\nStarting capture on: " + networkInterface.getName());
//
//        List<PacketData> capturedPackets = new CopyOnWriteArrayList<>();
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//
//        try (PcapHandle handle = networkInterface.openLive(SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, TIMEOUT)) {
//
//            PacketListener listener = packet -> {
//                if (packet != null && packet.contains(IpV4Packet.class)) {
//                    PacketData packetData = extractPacketData(packet);
//                    if (packetData != null) {
//                        capturedPackets.add(packetData);
//                        System.out.println("Captured packet: " + packetData.getSize() + " bytes");
//                    }
//                }
//            };
//
//            Future<?> captureTask = executor.submit(() -> {
//                try {
//                    handle.loop(-1, listener);
//                } catch (PcapNativeException | NotOpenException | InterruptedException e) {
//                    System.err.println("Capture stopped: " + e.getMessage());
//                }
//            });
//
//            Thread.sleep(10000); // Capture for 10 seconds
//
//            handle.breakLoop();
//            captureTask.cancel(true);
//
//            if (!capturedPackets.isEmpty()) {
//                savePacketsToJson(networkInterface.getName(), capturedPackets);
//            }
//
//        } catch (Exception e) {
//            System.err.println("Error capturing on interface " + networkInterface.getName() + ": " + e.getMessage());
//        } finally {
//            executor.shutdownNow();
//        }
//    }
//
//    private static PacketData extractPacketData(Packet packet) {
//        IpV4Packet ipv4Packet = packet.get(IpV4Packet.class);
//        if (ipv4Packet == null) return null;
//
//        Inet4Address srcAddr = ipv4Packet.getHeader().getSrcAddr();
//        Inet4Address dstAddr = ipv4Packet.getHeader().getDstAddr();
//        String protocol = ipv4Packet.getHeader().getProtocol().toString();
//        int size = packet.length();
//        String data = packet.getPayload().toString();
//        String timestamp = new Date().toString();
//
//        int srcPort = 0;
//        int dstPort = 0;
//        if (ipv4Packet.getPayload() instanceof TcpPacket) {
//            TcpPacket tcpPacket = (TcpPacket) ipv4Packet.getPayload();
//            srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
//            dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();
//        } else if (ipv4Packet.getPayload() instanceof UdpPacket) {
//            UdpPacket udpPacket = (UdpPacket) ipv4Packet.getPayload();
//            srcPort = udpPacket.getHeader().getSrcPort().valueAsInt();
//            dstPort = udpPacket.getHeader().getDstPort().valueAsInt();
//        }
//        return new PacketData(srcAddr.getHostAddress(), dstAddr.getHostAddress(), data, timestamp, protocol, size, srcPort, dstPort);
//    }
//
//    private static void savePacketsToJson(String interfaceName, List<PacketData> packets) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
//            writer.write(gson.toJson(packets));
//            System.out.println("Saved " + packets.size() + " packets from " + interfaceName + " to " + OUTPUT_FILE);
//        } catch (IOException e) {
//            System.err.println("Error writing to JSON: " + e.getMessage());
//        }
//    }
}