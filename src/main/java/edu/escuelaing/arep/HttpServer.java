package edu.escuelaing.arep;

import org.apache.commons.io.FilenameUtils;

import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP Server class, returns requested resources
 */
public class HttpServer {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructs an HttpServer
     */
    public HttpServer() {
        int port = getPort();
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(1);
        }
        clientSocket = null;
    }

    /**
     * This method reads the default port as specified by the PORT variable in
     * the environment.
     *
     * @return The port variable if set, else 4567 as default
     */
    private static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 4567; // returns default port if heroku-port isn't set (i.e. on localhost)
    }

     /**
     * Runs the http server
     *
     * @throws IOException
     */
    public void run() throws IOException {
        while (true) {
            try {
                System.out.println("Ready to receive");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            Pattern pattern = Pattern.compile("GET (/[^\\s]*)");
            Matcher matcher = null;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("I received: " + inputLine);
                stringBuilder.append(inputLine);
                if (!in.ready()) {
                    matcher = pattern.matcher(stringBuilder.toString());
                    System.out.println(stringBuilder.toString());
                    if (matcher.find()) {
                        String res = matcher.group();
                        System.out.println(res);
                        String fileRequested = matcher.group().substring(5);
                        System.out.println(fileRequested);
                        System.out.println("VALUE: " + fileRequested);
                        handleRequest(fileRequested);
                    }
                    break;
                }
            }

            out.close();
            in.close();
            clientSocket.close();
        }
    }


    /**
     * Sends back a image file 
     * @param header Header of the image file
     * @param filePath Location path of the image in the server
     * @throws IOException
     */
    private void returnImage(String header, String filePath) throws IOException {
        //I have no idea what I am doing.
        //In fact, I don't know why I have to do it this way.
        FileInputStream fileIn = new FileInputStream(filePath);
        OutputStream os = clientSocket.getOutputStream();
        for (char c : header.toCharArray()) {
            os.write(c);
        }
        int a;
        while ((a = fileIn.read()) > -1) {
            os.write(a);
        }
        os.flush();
        fileIn.close();
        os.close();
    }

    /**
     * Sends back a html file 
     * @param header Header of the html file
     * @param file  File to send back
     * @throws IOException
     */
    private void returnFile(String header, File file) throws IOException {
        //Maybe I have an idea of what I'm doing
        out.println(header);
        BufferedReader br = new BufferedReader(new FileReader(file));

        StringBuilder stringBuilder = new StringBuilder();
        String st;
        while ((st = br.readLine()) != null) {
            stringBuilder.append(st);
        }
        out.println(stringBuilder.toString());
        br.close();
    }

    /**
     * Send an html with 404 not found
     * @param fileRequested file not found
     */
    private void returnFileNotFound(String fileRequested){
        out.println("HTTP/1.1 404\r\nAccess-Control-Allow-Origin: *\r\n\r\n" 
                    + "<html>" 
                    + "<body>"
                        + "<h1>404 NOT FOUND ( " + fileRequested + " )</h1>" 
                    + "</body>" 
                    + "</html>");
    }

    /**
     * Handles how to send back a requested resource
     *
     * @param fileRequested File name of the resource to send back
     * @throws IOException
     */
    private void handleRequest(String fileRequested) throws IOException {
        String filePath = "src/main/resources/";
        String ext = FilenameUtils.getExtension(fileRequested);
        boolean isImage = false;
        if (ext.equals("html")) {
            filePath += "web-pages/";
        } else if (ext.equals("png")) {
            filePath += "images/";
            isImage = true;
        }
        filePath += fileRequested;
        File file = new File(filePath);    
    
        if (file.exists() && !file.isDirectory()) {
            String header = generateHeader(isImage, ext, file.length());
            if (isImage) {
                returnImage(header, filePath);
            } else {
                returnFile(header, file);
            }
        } else {
            returnFileNotFound(fileRequested);
        }
    }

    /**
     * Generate header for the browser
     * @param isImage true if request resource is a image
     * @param ext   Extension of the request resource
     * @param length    Request resource length
     * @return Especific header for the browser
     */
    private String generateHeader(boolean isImage, String ext, long length) {
        String header = null;
        if (isImage) {
            header = "HTTP/1.1 200 \r\nAccess-Control-Allow-Origin: *\r\nContent-Type: image/" + ext
                    + "\r\nConnection: close\r\nContent-Length:" + length + "\r\n\r\n";
        } else {
            header = "HTTP/1.1 200 \r\nAccess-Control-Allow-Origin: *\r\nContent-Type: text/html\r\n\r\n";
        }
        return header;
    }

}
