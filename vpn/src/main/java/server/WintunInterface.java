package server;

public class WintunInterface {
    // Load the native library
    static {
        WintunLoader.init();
    }

    // Native method to create an adapter
    public static native long createAdapter(String adapterName);

    // Native method to delete an adapter
    public static native void deleteAdapter(long adapterHandle);

    // Native method to write data to the VPN tunnel
    public static native void writePacket(long adapterHandle, byte[] data);

    // Native method to read data from the VPN tunnel
    public static native byte[] readPacket(long adapterHandle);
}
