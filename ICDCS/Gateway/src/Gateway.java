import com.sun.net.httpserver.HttpServer;
import handler.FileUploadHandler;
import handler.ServerHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Gateway {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/ebps", new ServerHandler());
        server.createContext("/fileupload", new FileUploadHandler());
        server.setExecutor(null);
        System.out.println("Gateway running...");
        server.start();
    }
}
