package network;  // Ensure the package matches your project structure
import org.pcap4j.core.*;

public class CaptureTAPPackets {
    public static void main(String[] args) {
        try {
            PcapNetworkInterface tapDevice = Pcaps.getDevByName("TAP-Windows Adapter V9");
            if (tapDevice == null) {
                System.out.println("TAP adapter not found!");
                return;
            }
            System.out.println("Found TAP Adapter: " + tapDevice.getName());
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
    }
}
