package de.schlueter;

import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class App {
  public static List<PrintWriter> writers = Collections.synchronizedList(new ArrayList<>());
  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(8080)) {
      System.out.println("Server started on port 8080");
      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket);
        new Thread(() -> {
          try {

            Server client = new Server(1, clientSocket, writers);
            client.handleClient();

          } catch (Exception e) {
            e.printStackTrace();
          }
        }).start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
