import java.net.*;
import java.io.*;

public class FTPMulti {

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = null;
    boolean listening = true;
    FTPServer w;

    try {
      serverSocket = new ServerSocket(1200);
      System.out.println(InetAddress.getLocalHost());
      System.out.println(InetAddress.getLocalHost().getHostName());
    } catch (IOException e) {
      System.err.println("Could not listen on port: 1200.");
      System.exit(-1);
    }

    while(listening){
      w = new FTPServer(serverSocket.accept());
      Thread t = new Thread(w);
      t.start();
    }
  }
}

