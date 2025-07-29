package processing.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.zip.*;

/**
 * A simple HTTP server that serves files from a zip archive.
 * Replaces the previous custom WebServer implementation.
 */
public class ReferenceServer {
    private final HttpServer server;
    private final ZipFile zip;
    private final Map<String, ZipEntry> entries;
    private final int port;

    /** mapping of file extensions to content-types */
    static final Map<String, String> contentTypes = new ConcurrentHashMap<>();

    static {
        contentTypes.put("", "content/unknown");
        contentTypes.put(".css", "text/css");
        contentTypes.put(".csv", "text/csv");
        contentTypes.put(".eot", "application/vnd.ms-fontobject");
        contentTypes.put(".gif", "image/gif");
        contentTypes.put(".html", "text/html");
        contentTypes.put(".ico", "image/x-icon");
        contentTypes.put(".jpeg", "image/jpeg");
        contentTypes.put(".jpg", "image/jpeg");
        contentTypes.put(".js", "text/javascript");
        contentTypes.put(".json", "application/json");
        contentTypes.put(".md", "text/markdown");
        contentTypes.put(".mdx", "text/mdx");
        contentTypes.put(".mtl", "text/plain");
        contentTypes.put(".obj", "text/plain");
        contentTypes.put(".otf", "font/otf");
        contentTypes.put(".pde", "text/plain");
        contentTypes.put(".png", "image/png");
        contentTypes.put(".svg", "image/svg+xml");
        contentTypes.put(".tsv", "text/tab-separated-values");
        contentTypes.put(".ttf", "font/ttf");
        contentTypes.put(".txt", "text/plain");
        contentTypes.put(".vlw", "application/octet-stream");
        contentTypes.put(".woff", "font/woff");
        contentTypes.put(".woff2", "font/woff2");
        contentTypes.put(".xml", "application/xml");
        contentTypes.put(".yml", "text/yaml");
        contentTypes.put(".zip", "application/zip");
    }

    /**
     * Creates a new reference server that serves files from the specified zip file.
     *
     * @param zipFile The zip file containing reference documentation
     * @param port The port to serve on
     * @throws IOException If there is an error starting the server
     */
    public ReferenceServer(File zipFile, int port) throws IOException {
        this.zip = new ZipFile(zipFile);
        this.port = port;

        // Index all entries in the zip file
        entries = new HashMap<>();
        Enumeration<? extends ZipEntry> en = zip.entries();
        while (en.hasMoreElements()) {
            ZipEntry entry = en.nextElement();
            entries.put(entry.getName(), entry);
        }

        // Create and configure the server
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new ReferenceHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        Messages.log("Reference server started on port " + port);
    }

    /**
     * Gets the base URL for the server.
     *
     * @return The base URL
     */
    public String getPrefix() {
        return "http://localhost:" + port + "/";
    }

    /**
     * Stops the server.
     */
    public void stop() {
        server.stop(0);
        try {
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handler for reference documentation requests.
     */
    class ReferenceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Remove leading slash to match zip entry paths
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // Handle empty paths or directory requests
            if (path.isEmpty() || path.endsWith("/")) {
                path = path + "index.html";
            }

            ZipEntry entry = entries.get(path);

            if (entry != null) {
                // Determine content type
                String contentType = "application/octet-stream";
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex > 0) {
                    String extension = path.substring(dotIndex);
                    contentType = contentTypes.getOrDefault(extension, "application/octet-stream");
                }

                // Send the file
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Content-Length", String.valueOf(entry.getSize()));
                exchange.sendResponseHeaders(200, entry.getSize());

                try (OutputStream os = exchange.getResponseBody();
                     InputStream is = zip.getInputStream(entry)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                Messages.log("Serving: " + path + " (" + contentType + ")");
            } else {
                // Send 404
                String response = "<html><body><h1>404 Not Found</h1><p>The requested resource was not found.</p></body></html>";
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

                Messages.log("404 Not Found: " + path);
            }
        }
    }

    /**
     * A main() method for testing.
     */
    static public void main(String[] args) {
        try {
            new ReferenceServer(new File(args[0]), 8053);
            System.out.println("Server running at http://localhost:8053/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}