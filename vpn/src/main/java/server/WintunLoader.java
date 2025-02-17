package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

public class WintunLoader {
    static {
        try {
            // Extract DLL from resources
            String dllName = "wintun.dll";
            InputStream in = WintunLoader.class.getClassLoader().getResourceAsStream(dllName);

            if (in == null) {
                throw new RuntimeException("Failed to load " + dllName + " from resources.");
            }

            // Create a temporary file
            File tempDll = Files.createTempFile("wintun", ".dll").toFile();
            tempDll.deleteOnExit();  // Ensure deletion on exit

            // Write the DLL contents to the temporary file
            try (FileOutputStream out = new FileOutputStream(tempDll)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Load the DLL
            System.load(tempDll.getAbsolutePath());
            System.out.println("Wintun DLL loaded successfully!");

        } catch (Exception e) {
            throw new RuntimeException("Error loading Wintun DLL", e);
        }

    }

    public static void init() {
    }
}
