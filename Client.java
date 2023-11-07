/* 
 * Client.java
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

	// write a message to server.
	public static void writeToServer(BufferedWriter br, String message) throws IOException {
		br.write(message);
		br.newLine();
		br.flush();
	}

	public static final int SERVER_PORT = 6333;

	public static void main(String[] args) {
		Socket clientSocket = null;
		String userInput = null;
		BufferedWriter bufferedWriter = null;

		// Check the number of command line parameters
		if (args.length < 1) {
		System.out.println("Usage: Client <Server IP Address>");
		System.exit(1);
		}

		// try to open a socket on SERVER_PORT
		// try to open input and output streams
		try {
			clientSocket = new Socket(args[0], SERVER_PORT);
			ServerMessageHandler serverMessageHandler = new ServerMessageHandler(clientSocket);
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			Scanner scanner = new Scanner(System.in);

			// start the single thread
			new Thread(serverMessageHandler).start();

			// if everything has been initialized then we want to write some data
			// to the socket we have opened a connection to

			// get user input
			System.out.print("Enter command: ");

			while (true) {
				
				userInput = scanner.nextLine();

				// send a request to server
				writeToServer(bufferedWriter, userInput);

				// if we encounter quit command
				if (userInput.equals("QUIT")) {
					scanner.close();
					break;
				}
			}

		} catch (UnknownHostException e) {
			// do nothing
		} catch (IOException e) {
			// do nothing
		} finally {

			try {
				if (clientSocket != null)
					clientSocket.close();
				if (bufferedWriter != null)
					bufferedWriter.close();

			} catch (Exception error) {
				error.printStackTrace();
			}
		}

		System.out.println("***Client Terminated Successfully.***");
	}
}

// handles only input from server
class ServerMessageHandler implements Runnable {

	private Socket server = null;
	private BufferedReader bufferedReader = null;
	private String serverInput = null;

	ServerMessageHandler(Socket server) throws IOException {
		this.server = server;
		this.bufferedReader = new BufferedReader(new InputStreamReader(this.server.getInputStream()));
	}

	@Override
	public void run() {

		try {
			while (true) {

				//Close client if server not available 
				if(server.getInputStream().available() < 0){
					break;
				}

				// print any messages you receive from server 
				serverInput = bufferedReader.readLine();
				if(serverInput != null){
					System.out.println(serverInput);
				}

				// get out of infinite loop if server is not sending any feedback
				if(serverInput == null){
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}