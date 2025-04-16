import java.io.*;
import java.time.LocalDateTime;

public class Logger {
    private static final String LOG_FILE = "logs/server.log";

    public synchronized static void log(String level, String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(LocalDateTime.now() + " [" + level + "] " + message + "\n");
        } catch (IOException e) {
            System.err.println("Logging failed: " + e.getMessage());
        }
    }
}
