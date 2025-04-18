import java.io.*;
import java.nio.file.*;
import java.time.Instant;

public class FormHandler implements Runnable {
    private final String formData;

    public FormHandler(String formData) {
        this.formData = formData;
    }

    @Override
    public void run() {
        try {
            if (formData.contains("..")) {
                Logger.log("WARN", "Suspicious form data blocked.");
                return;
            }

            String filename = "../submissions/submission-" + Instant.now().toEpochMilli() + ".txt";
            Files.write(Paths.get(filename), formData.getBytes(), StandardOpenOption.CREATE);
            Logger.log("INFO", "Form data saved to: " + filename);

        } catch (IOException e) {
            Logger.log("ERROR", "FormHandler error: " + e.getMessage());
        }
    }
}
