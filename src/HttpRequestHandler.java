import java.io.*;
import java.net.*;
import java.nio.file.*;

public class HttpRequestHandler implements Runnable {
    private final Socket socket;

    public HttpRequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true)
        ) {
            String request = reader.readLine();
            if (request == null) return;

            Logger.log("INFO", "Request: " + request);

            String[] parts = request.split(" ");
            String method = parts[0];
            String path = parts[1];

            if (method.equals("GET")) {
                if ("/".equals(path)) path = "/index.html";
                serveFile(path, writer, output);
            } else if (method.equals("POST") && path.equals("/submit.html")) {
                // Handle POST request (read data in parent thread, then isolate handler)
                handlePost(reader, writer);
            } else {
                send404(writer);
            }
        } catch (IOException e) {
            Logger.log("ERROR", "Request handling error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                Logger.log("ERROR", "Socket close error: " + e.getMessage());
            }
        }
    }

    private void serveFile(String path, PrintWriter writer, OutputStream output) {
        try {
            File file = new File("www", URLDecoder.decode(path, "UTF-8")).getCanonicalFile();

            // Prevent directory traversal
            if (!file.getPath().startsWith(new File("www").getCanonicalPath())) {
                send404(writer);
                Logger.log("WARN", "Blocked directory traversal: " + path);
                return;
            }

            if (file.exists() && file.isFile()) {
                byte[] content = Files.readAllBytes(file.toPath());
                writer.print("HTTP/1.1 200 OK\r\n");
                writer.print("Content-Length: " + content.length + "\r\n");
                writer.print("Content-Type: text/html\r\n\r\n");
                writer.flush();
                output.write(content);
                output.flush();
                Logger.log("INFO", "Served file: " + path);
            } else {
                send404(writer);
                Logger.log("WARN", "File not found: " + path);
            }
        } catch (IOException e) {
            Logger.log("ERROR", "GET error: " + e.getMessage());
        }
    }


    private void handlePost(BufferedReader reader, PrintWriter writer) {
        try {
            String line;
            int contentLength = 0;

            // Read headers and find Content-Length
            while (!(line = reader.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            // Read the body from the POST request
            char[] buffer = new char[contentLength];
            reader.read(buffer);
            String formData = new String(buffer);

            // Save data using FormHandler in background
            new Thread(new FormHandler(formData)).start();

            // ✅ Send HTTP 302 Redirect to success page
            writer.print("HTTP/1.1 302 Found\r\n");
            writer.print("Location: /submit.html\r\n");
            writer.print("Content-Length: 0\r\n");
            writer.print("\r\n");
            writer.flush();

        } catch (IOException e) {
            Logger.log("ERROR", "POST read error: " + e.getMessage());
            writer.print("HTTP/1.1 500 Internal Server Error\r\n\r\n");
            writer.flush();
        }
    }







    private void send404(PrintWriter writer) {
        String body = "<html><body><h1>404 Not Found</h1></body></html>";
        writer.print("HTTP/1.1 404 Not Found\r\n");
        writer.print("Content-Length: " + body.length() + "\r\n");
        writer.print("Content-Type: text/html\r\n\r\n");
        writer.print(body);
        writer.flush();
    }
}
