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
		String serverInput = null;

		InputStreamReader inputStreamReader = null;
		OutputStreamWriter outputStreamWriter = null;

		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;

		// Check the number of command line parameters
		// if (args.length < 1) {
		// 	System.out.println("Usage: Client <Server IP Address>");
		// 	System.exit(1);
		// }

		// try to open a socket on SERVER_PORT
		// try to open input and output streams
		try {
			clientSocket = new Socket("localhost", SERVER_PORT);

			inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
			outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream());

			bufferedReader = new BufferedReader(inputStreamReader);
			bufferedWriter = new BufferedWriter(outputStreamWriter);

			Scanner scanner = new Scanner(System.in);

			// if everything has been initialized then we want to write some data
			// to the socket we have opened a connection to

			if (clientSocket != null) {

				while (true) {
					// get user input
					System.out.print("Enter command: ");
					userInput = scanner.nextLine();

					// send a request to server
					writeToServer(bufferedWriter, userInput);

					// read and print server response 
					serverInput = bufferedReader.readLine();
					System.out.println(serverInput);

					if (serverInput != null && (serverInput.equals("200 OK") || serverInput.contains("200 OK"))) {

						if (userInput != null && (userInput.equals("QUIT") || userInput.equals("SHUTDOWN"))) {
							scanner.close();
							break;
						} 
						else if (userInput != null && userInput.equals("MSGGET")) {
							// read word of the day
							serverInput = bufferedReader.readLine();
							System.out.println(serverInput);
						} 
						else if (userInput.equals("MSGSTORE")) {

							System.out.print("Enter a new message: ");

							String msg = scanner.nextLine();
							writeToServer(bufferedWriter, msg);

							serverInput = bufferedReader.readLine();
							System.out.println(serverInput);
						} 
						else if(userInput != null && userInput.equals("WHO")){
							String msg = scanner.nextLine();
							msg.replace('\n','\n');
							System.out.println(msg);
						}
						else {
							// do nothing
						}
					}
				}
			}

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: hostname");
		} finally {
			try {
				if (clientSocket != null)
					clientSocket.close();
				if (inputStreamReader != null)
					inputStreamReader.close();
				if (outputStreamWriter != null)
					outputStreamWriter.close();
				if (bufferedReader != null)
					bufferedReader.close();
				if (bufferedWriter != null)
					bufferedWriter.close();

			} catch (Exception error) {
				error.printStackTrace();
			}
		}

		System.out.println("***Client Terminated Successfully.***");
	}
}