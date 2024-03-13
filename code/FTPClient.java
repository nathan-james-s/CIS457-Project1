import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

class FTPClient {

  public static void main(String argv[]) throws Exception {
		String sentence;
		String modifiedSentence;
		boolean isOpen = true;
		int number = 1;
		int port1 = 1221;
		int port = 1200;
		String statusCode;
		boolean clientgo = true;

    String welcome_str = "Welcome to the simple FTP App\n" +
      "Commands  \n" + 
      "connect servername port# connects to a specified server \n" + 
      "list: lists files on server \n" + 
      "get: fileName.txt downloads that text file to your current directory \n" + 
      "stor: fileName.txt Stores the file on the server \n" + 
      "close terminates the connection to the server \n" + 
      "retr: downloads specified file from server";
		System.out.println(welcome_str);
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		sentence = inFromUser.readLine();
		StringTokenizer connectionTokens = new StringTokenizer(sentence);

		if (sentence.startsWith("connect")) {
			String serverName = connectionTokens.nextToken(); // pass the connect command
			serverName = connectionTokens.nextToken();
			port1 = Integer.parseInt(connectionTokens.nextToken());
			System.out.println("You are connected to " + serverName);
			Socket ControlSocket = new Socket(serverName, port1);
			while (isOpen && clientgo) {

				DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
				DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
				String userInput = inFromUser.readLine(); // Added this as a variable separate from sentence variable, which is now used for connection only
				StringTokenizer inputTokens = new StringTokenizer(userInput);

				if (userInput.equals("list:")) {
					port += 2;
					//System.out.println("PORT: " + port);
					ServerSocket welcomeData = new ServerSocket(port);

					System.out.println("\n \n \nThe files on this server are:");
					outToServer.writeBytes(port + " " + userInput + " " + '\n');

					Socket dataSocket = welcomeData.accept();
					DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
					while (true) {
						modifiedSentence = inData.readUTF();
						if (modifiedSentence.equals("eof"))
							break;
						System.out.println("	" + modifiedSentence);
					}

					welcomeData.close();
					dataSocket.close();
					System.out.println("\nWhat would you like to do next: \nget: file.txt ||  stor: file.txt  || close");

				} else if (userInput.startsWith("get: ") || userInput.startsWith("retr: ")) {
					port += 2;
					//System.out.println("PORT: " + port);

					String fileName = inputTokens.nextToken();
					fileName = inputTokens.nextToken();

					ServerSocket welcomeData = new ServerSocket(port);

					outToServer.writeBytes(port + " " + userInput + " " + '\n');

					Socket dataSocket = welcomeData.accept();
					DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));

					String status = inData.readUTF();
					if (status.equals("200 OK ")) {
						System.out.println("\nDownloading file...");

						File file = new File("client" + fileName); // appending client to filename to keep track since client and server run out of the same folder for now
						FileWriter fileWriter = new FileWriter(file);

						while (true) {
							String fileData = inData.readUTF();
							if (fileData.equals("eof")) { // if EOF stop reading
								break;
							} else { // else write to file
								fileWriter.write(fileData);
							}
						}

						System.out.println("Successfully downloaded " + fileName + "\n");
						fileWriter.close();
						welcomeData.close();

					} else if(status.equals("550 FILE NOT FOUND ")){
						System.out.println("\nThat file does not exist on the server.\nUse the list command to see available files.\n");
					} else{
						System.out.println("An unknown error has occured.\nPlease try again.");
					}
				} else if(userInput.equals("close")){
					clientgo = false;
				}

			}

			//System.out.print("No server exists with that name, or server not listening on that port try agian");
		} else {
			System.out.println("Error: Please use the connect <servername> <port> command");
		}
	}
}
