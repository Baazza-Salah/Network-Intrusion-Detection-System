package com.app.networkintrusionsystem;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

import java.io.IOException;

public class CaptureController {

    public static void main(String[] args) {
        PcapNetworkInterface nif;
        try {
            nif = new NifSelector().selectNetworkInterface();
            if (nif == null) {
                System.out.println("No network interface selected.");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int snapshotLength = 65536; // in bytes
        int readTimeout = 50; // in milliseconds
        PcapHandle handle = null;

        try {
            handle = nif.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);
            PacketListener listener = packet -> System.out.println(packet);
            handle.loop(10, listener);
        } catch (PcapNativeException e) {
            e.printStackTrace();
        } catch (NotOpenException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
    }
}