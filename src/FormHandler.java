import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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

            Map<String, String> fields = parseAndValidate(formData);
            if (fields == null) {
                Logger.log("WARN", "Invalid input detected in form.");
                return;
            }

            // Build a clean formatted string to store
            StringBuilder content = new StringBuilder();
            content.append("=== Form Submission ===\n");
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                content.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            String filename = "submissions/submission-" + Instant.now().toEpochMilli() + ".txt";
            Files.write(Paths.get(filename), content.toString().getBytes(), StandardOpenOption.CREATE);
            Logger.log("INFO", "Form data saved to: " + filename);

        } catch (IOException e) {
            Logger.log("ERROR", "FormHandler error: " + e.getMessage());
        }
    }

    // Parse, decode, validate, and escape inputs
    private Map<String, String> parseAndValidate(String formData) {
        Map<String, String> fields = new HashMap<>();

        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);

                // Basic input validation
                if (!isValidInput(key, value)) {
                    return null; // Reject form
                }

                fields.put(key, htmlEscape(value)); // Store escaped version
            }
        }
        return fields;
    }

    // Simple allowlist validation (you can expand this)
    private boolean isValidInput(String key, String value) {
        return key.matches("^[a-zA-Z0-9_]{1,30}$") && value.length() <= 200;
    }

    // HTML escape dangerous characters (optional for logging or web output)
    private String htmlEscape(String input) {
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }
}
