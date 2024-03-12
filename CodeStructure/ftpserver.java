import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

public class ftpserver extends Thread {
    private Socket connectionSocket;
    int port;
    int count = 1;

    public ftpserver(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public void run() {
        if (count == 1)
            System.out.println("User connected" + connectionSocket.getInetAddress());
        count++;

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
            if (count == 1)
                System.out.println("User connected" + connectionSocket.getInetAddress());
            count++;

            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            fromClient = inFromClient.readLine();

            System.out.println("Read line: " + fromClient);
            StringTokenizer tokens = new StringTokenizer(fromClient);

            frstln = tokens.nextToken();
            port = Integer.parseInt(frstln);
            clientCommand = tokens.nextToken();
            System.out.println("GOT COMMAND: " + clientCommand);

            if (clientCommand.equals("list:")) {
                String curDir = System.getProperty("user.dir");
                //System.out.println("Working Directory: " + curDir);

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

                        if (filename.endsWith(".txt"))
                            dataOutToClient.writeUTF(children[i]);
                        // System.out.println(filename);
                        if (i - 1 == children.length - 2) {
                            dataOutToClient.writeUTF("eof");
                            // System.out.println("eof");
                        } // if(i-1)

                    }
                    dataSocket.close();
                    // System.out.println("Data Socket closed");
                }
            }

            if (clientCommand.equals("get:")) {
                String fileName = tokens.nextToken();
                System.out.println("DEBUG: Client requested: " + fileName);
                String curDir = System.getProperty("user.dir");

                Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
                DataOutputStream dataOut = new DataOutputStream(dataSocket.getOutputStream());

                if (new File(curDir, fileName).exists()) { //Check if file exists in server's dir
                    System.out.println("DEBUG: FILE EXISTS");
                    dataOut.writeUTF("200 OK ");

                    BufferedReader fileReader = new BufferedReader(new FileReader(fileName));

                    String fileLine = fileReader.readLine();
                    while (fileLine != null) { //Read lines until eof
                        dataOut.writeUTF(fileLine + "\n"); //readLine() ignores newlines so we have to add them back
                        fileLine = fileReader.readLine();
                    }
                    dataOut.writeUTF("eof");
                    System.out.println("DEBUG: FILE SENT");
                    fileReader.close();
                }else{
                    System.out.println("DEBUG: FILE DOESNT EXIST");
                    dataOut.writeUTF("550 FILE NOT FOUND ");
                }
                dataSocket.close();
            }
        }
    }
}
