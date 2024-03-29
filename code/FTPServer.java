import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

public class FTPServer extends Thread {
  private Socket connectionSocket;
  int port;
  int count = 1;

  public FTPServer(Socket connectionSocket) {
    this.connectionSocket = connectionSocket;
  }

  public void run() {
    if (count == 1) {
      System.out.println("User connected" + connectionSocket.getInetAddress());
      count++;
    }

    try {
      processRequest();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private void processRequest() throws Exception {
    String fromClient;
    String clientCommand;
    byte[] data;
    String frstln;

    while (true) {
      if (count == 1) {
        System.out.println("User connected" + connectionSocket.getInetAddress());
        count++;
      }

      DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
      BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      fromClient = inFromClient.readLine();

      StringTokenizer tokens;
      try {
        tokens = new StringTokenizer(fromClient);
      } catch(Exception e) {
        System.out.println("User disconnected" + connectionSocket.getInetAddress());
        break;
      }

      frstln = tokens.nextToken();
      port = Integer.parseInt(frstln);
      clientCommand = tokens.nextToken();

      if (clientCommand.equals("list:")) {
        String curDir = System.getProperty("user.dir");

        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
        DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
        File dir = new File(curDir);

        String[] children = dir.list();
        if (children == null) {
          //Either dir does not exist or is not a directory
          System.out.println("Error: No Children");
        } else {
          for (int i = 0; i < children.length; i++) {
            // Get filename of file or directory
            String filename = children[i];

            if (filename.endsWith(".txt")) {
              dataOutToClient.writeUTF(children[i]);
            }

            if (i - 1 == children.length - 2) {
              dataOutToClient.writeUTF("eof");
            }
          }
          
          dataSocket.close();
        }
      }

      if (clientCommand.equals("get:") || clientCommand.equals("retr:")) {
        String fileName = tokens.nextToken();
        String curDir = System.getProperty("user.dir");

        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
        DataOutputStream dataOut = new DataOutputStream(dataSocket.getOutputStream());

        if (new File(curDir, fileName).exists()) { //Check if file exists in server's dir
          dataOut.writeUTF("200 OK ");

          BufferedReader fileReader = new BufferedReader(new FileReader(fileName));

          String fileLine = fileReader.readLine();
          while (fileLine != null) { //Read lines until eof
            dataOut.writeUTF(fileLine + "\n"); //readLine() ignores newlines so we have to add them back
            fileLine = fileReader.readLine();
          }

          dataOut.writeUTF("eof");
          fileReader.close();
        } else {
          dataOut.writeUTF("550 FILE NOT FOUND ");
        }

        dataSocket.close();
      }




      if (clientCommand.equals("stor:")) {
        String fileName = tokens.nextToken();
        String curDir = System.getProperty("user.dir");

        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
        DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

        // Send confirmation to client
        dataOutToClient.writeUTF("200 OK ");

        // Create a BufferedReader to read file data line by line from the client
        BufferedReader dataInFromClient = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

        // Create the file on the server
        File fileToReceive = new File(curDir, fileName);
        FileWriter fileWriter = new FileWriter(fileToReceive);

        String line;
        while ((line = dataInFromClient.readLine()) != null) {
          if (line.equals("eof")) { // Check for end of file marker
            break;
          }

          fileWriter.write(line + "\n"); // Write each line to the file
        }

        fileWriter.close();
        dataInFromClient.close(); // Close the reader

        fileWriter.close();
        dataInFromClient.close(); // Close the input stream as well
        dataSocket.close();

      }
    }
  }
}
