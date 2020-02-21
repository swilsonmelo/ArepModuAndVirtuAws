package edu.escuelaing.arep;

import java.io.IOException;

public class App {
    
    /**
     * Main method that runs the HTTP Server
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer();
        server.run();
    }

}