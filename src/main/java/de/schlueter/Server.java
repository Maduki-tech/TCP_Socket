package de.schlueter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Client
 */
public class Server {
  private int clientID;
  Socket clientSocket;
  private List<PrintWriter> writers;

  Server(int clientID, Socket clientSocket, List<PrintWriter> writers) {
    this.clientID = clientID;
    this.clientSocket = clientSocket;
    this.writers = writers;
  }

  public void handleClient() throws Exception {
    InputStream inputStream = clientSocket.getInputStream();
    BufferedReader reader =
        new BufferedReader(new java.io.InputStreamReader(inputStream));
    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
    writers.add(writer);

    int contentLength = readContentLength(reader);

    String body = "";
    String message = "No Message found";
    // Read the body if Content-Length is present
    if (contentLength > 0) {
      body = readBody(reader, contentLength);
      if (body.contains("message")) {
        message = getMessageFromJson(body);
      } else {
        message = body;
      }
    }

    broadcast(message);

    writer.flush();

    writers.remove(writer);

    clientSocket.close();
  }

  private void broadcast(String message) {
    System.out.println("Broadcasting: " + message);
    System.out.println("Number of clients: " + writers.size());
    synchronized (writers) {
      for (PrintWriter writer : writers) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html");
        writer.println();
        writer.println("<h1>"+ message + "</h1>");
        writer.flush();
      }
    }
  }

  private int readContentLength(BufferedReader reader) throws Exception {
    String line;
    int contentLength = -1;
    while (!(line = reader.readLine()).isEmpty()) {
      System.out.println(line);
      if (line.startsWith("Content-Length: ")) {
        contentLength = Integer.parseInt(line.split(":")[1].trim());
      }
    }
    return contentLength;
  }

  private String readBody(BufferedReader reader, int contentLength)
      throws Exception {
    char[] bodyChars = new char[contentLength];
    reader.read(bodyChars, 0, contentLength);
    String body = new String(bodyChars);
    return body;
  }

  private String getMessageFromJson(String body) {
    String message = "No Message found";
    if (body.contains("message")) {
      message =
          body.split(":")[1].trim().split("\"")[1].trim().split("\"")[0].trim();
    }
    return message;
  }
}
