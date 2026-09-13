import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LocalServer {
    private static final int PORT = 3000;
    private static final String BASE_DIR = "./frontend";
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("html", "text/html; charset=UTF-8");
        MIME_TYPES.put("htm", "text/html; charset=UTF-8");
        MIME_TYPES.put("css", "text/css; charset=UTF-8");
        MIME_TYPES.put("js", "application/javascript; charset=UTF-8");
        MIME_TYPES.put("json", "application/json; charset=UTF-8");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");
        MIME_TYPES.put("ttf", "font/ttf");
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new StaticHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("===============================================================");
        System.out.println("  \uD83D\uDEE1\uFE0F  SecureBank Localhost Web Server is Running!");
        System.out.println("===============================================================");
        System.out.println("  Access the app in your browser at:");
        System.out.println("  -> Home / Landing:       http://localhost:" + PORT + "/index.html");
        System.out.println("  -> Customer Dashboard:   http://localhost:" + PORT + "/customer-dashboard.html");
        System.out.println("  -> Admin Dashboard:      http://localhost:" + PORT + "/admin-dashboard.html");
        System.out.println("  -> Login Page:           http://localhost:" + PORT + "/login.html");
        System.out.println("  -> Registration:         http://localhost:" + PORT + "/register.html");
        System.out.println("===============================================================");
        System.out.println("  Press Ctrl+C to stop the server.");
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uriPath = exchange.getRequestURI().getPath();
            if (uriPath.equals("/") || uriPath.isEmpty()) {
                uriPath = "/index.html";
            }

            // Sanitize path to prevent directory traversal
            String safePath = uriPath.replace("../", "").replace("..\\", "");
            Path filePath = Paths.get(BASE_DIR, safePath).toAbsolutePath().normalize();
            Path baseDirPath = Paths.get(BASE_DIR).toAbsolutePath().normalize();

            if (!filePath.startsWith(baseDirPath) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                String notFoundMsg = "<h1>404 Not Found</h1><p>The requested file does not exist.</p>";
                byte[] notFoundBytes = notFoundMsg.getBytes("UTF-8");
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFoundBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFoundBytes);
                }
                return;
            }

            String ext = "";
            String fileName = filePath.getFileName().toString();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                ext = fileName.substring(dotIndex + 1).toLowerCase();
            }

            String contentType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            byte[] fileBytes = Files.readAllBytes(filePath);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }
}
