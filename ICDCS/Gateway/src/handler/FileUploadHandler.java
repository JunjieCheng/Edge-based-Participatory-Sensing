package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class FileUploadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) {
        for (Map.Entry<String, List<String>> header : httpExchange.getRequestHeaders().entrySet()) {
            System.out.println(header.getKey() + ": " + header.getValue().get(0));
        }

        DiskFileItemFactory d = new DiskFileItemFactory();

        try {
            ServletFileUpload up = new ServletFileUpload(d);
            List<FileItem> result = up.parseRequest(new RequestContext() {

                @Override
                public String getCharacterEncoding() {
                    return "UTF-8";
                }

                @Override
                public int getContentLength() {
                    return Integer.parseInt(httpExchange.getRequestHeaders().getFirst("Content-length"));
                }

                @Override
                public String getContentType() {
                    return httpExchange.getRequestHeaders().getFirst("Content-type");
                }

                @Override
                public InputStream getInputStream() {
                    return httpExchange.getRequestBody();
                }

            });

            httpExchange.getResponseHeaders().add("Content-type", "text/plain");
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream os = httpExchange.getResponseBody();

            for (FileItem fi : result) {
                File uploadedFile = new File("./src/data/images/" + fi.getName());
                fi.write(uploadedFile);
                System.out.println("File-Item: " + fi.getFieldName() + " = " + fi.getName());
            }

            os.write("File Received".getBytes());
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
