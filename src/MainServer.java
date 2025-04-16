import java.net.*;

public class MainServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        Logger.log("INFO", "Starting server on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                Logger.log("INFO", "Client connected: " + clientSocket.getInetAddress());

                // Each connection handled in a new thread
                new Thread(new HttpRequestHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            Logger.log("ERROR", "Server exception: " + e.getMessage());
        }
    }
}
