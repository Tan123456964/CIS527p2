/*
 * Server.java
 */

import java.io.*;
import java.net.*;
import java.util.*;

// Server Class 
public class Server {

	public static final int SERVER_PORT = 6333;
	public static ServerSocket server = null;

	private static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>(20);

	public static void main(String[] args) {
		try {

			// server is listening on port 1234
			server = new ServerSocket(SERVER_PORT);

			// running infinite loop for getting client request
			while (true) {

				// socket object to receive incoming client requests
				Socket client = server.accept();

				// create a new thread object
				ClientHandler clientThread = new ClientHandler(client, server, clients);

				// add new client to client lists
				clients.add(clientThread);

				// This thread will handle the client separately
				new Thread(clientThread).start();
			}
		} catch (IOException e) {
			// can be logged to a file
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					// can be logged to a file
				}
			}
		}
		System.out.println("Server is closed");
	}
}

// ClientHandler class
class ClientHandler implements Runnable {

	private Socket client = null;
	private BufferedReader bufferedReader = null;
	private PrintWriter printWriter = null;
	private ArrayList<ClientHandler> clients;

	// save logged in user
	private Map<String, String> session = new HashMap<String, String>();

	// getSession users
	public Map<String, String> getSession() {
		return this.session;
	}

	// get clientObject
	public Socket getClientSocket() {
		return this.client;
	}

	ServerSocket myService = null;
	String line;

	ClientHandler(Socket client, ServerSocket server, ArrayList<ClientHandler> clients) throws IOException {
		this.client = client;
		this.bufferedReader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		this.printWriter = new PrintWriter(this.client.getOutputStream(), true);
		this.myService = server;
		this.clients = clients;
	}

	// write a single message to client
	public void writeToClient(String message) throws IOException {
		this.printWriter.println(message);
	}

	// closes the sockets
	public void closeAndExitSocket() throws IOException {
		client.close();
	}

	/**
	 * 
	 * @filename :"file path"
	 * @returns <void> : writes (append) text to a file
	 *          used to save messages to a file (doesn't override content of the
	 *          file)
	 */

	public static void writeToFile(String filename, String text) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename), true));
		writer.write(text);
		writer.newLine();
		writer.close();
	}

	/**
	 * 
	 * @filename :"file path"
	 * @returns : read from file and store them in an arrayList
	 */
	public static ArrayList<String> readFromFile(String filename) throws IOException {

		ArrayList<String> data = new ArrayList<String>();

		File file = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line = "";

		while ((line = br.readLine()) != null) {
			if (line.trim().isEmpty())
				continue; // don't insert empty lines
			data.add(line);
		}

		br.close();
		return data;
	}

	@Override
	public void run() {

		// list of usernames and passwords
		Map<String, String> userInfo = new HashMap<String, String>();
		userInfo.put("root", "root01");
		userInfo.put("john", "john01");
		userInfo.put("david", "david01");
		userInfo.put("mary", "mary01");

		// creates a socket object from the ServerSocket to listen and accept
		// connections.

		try {
			// message store command
			String msgStoreCMD = "";

			// message send command
			String msgSendCMD = "";
			String msgSendUSER = "";

			// word of the day
			int wordNum = 0;

			// as long as we receive data, echo that data back to the client.
			while (true) {

				line = bufferedReader.readLine();

				if (line != null && ((line.contains("SEND") && msgStoreCMD.isEmpty()) ||
						(msgSendCMD.equals("SEND") && !msgSendUSER.isEmpty() && msgStoreCMD.isEmpty()))) {

					if (session.size() > 0) {
						if (msgSendCMD.equals("SEND")) {
							String userName = session.keySet().toArray()[0].toString();
							String msg1 = "200 OK you have a new message from " + userName;
							String msg2 = userName + ": " + line;
							for (final ClientHandler c : clients) {
								if (c.getSession().containsKey(msgSendUSER)) {
									c.writeToClient(msg1);
									c.writeToClient(msg2);
									break;
								}
							}
							writeToClient("200 OK");
							msgSendCMD = "";
							msgSendUSER = "";

						} else {
							String send[] = line.split(" ");
							if (send.length != 2) {
								writeToClient("Invalid send command");
							} else {
								boolean isUserLoggedIn = false;
								for (final ClientHandler c : clients) {
									final Map<String, String> session = c.getSession();
									if (session.containsKey(send[1])) {
										isUserLoggedIn = true;
										break;
									}
								}
								if (isUserLoggedIn) {
									msgSendCMD = "SEND";
									msgSendUSER = send[1];
									writeToClient("200 OK");

								} else {
									writeToClient("420 either the user does not exist or is not logged in.");
								}
							}
						}
					} else {
						writeToClient("402 you are not logged in. Only logged in users are allowed to send messages.");
					}
				}

				else if (line != null && (line.equals("MSGSTORE") || msgStoreCMD.equals("MSGSTORE"))) {
					if (session.size() == 1) {
						if (msgStoreCMD.equals("MSGSTORE")) {
							writeToFile("word.txt", line);
							msgStoreCMD = "";
						} else {
							msgStoreCMD = line;
						}
						writeToClient("200 OK");
					} else {
						writeToClient("401 You are not currently logged in, login first.");
					}
				} else if (line != null && line.equals("MSGGET")) {
					ArrayList<String> word = readFromFile("word.txt");
					writeToClient("200 OK");
					writeToClient(word.get(wordNum % word.size()));
					wordNum++;
				} else if (line != null && line.contains("LOGIN")) {
					String login[] = line.split(" ");
					if (session.size() > 0) {
						String msg = "409 user " + session.keySet().toArray()[0] + " is already logged in.";
						writeToClient(msg);
					} else if (login.length < 3) {
						writeToClient("300 message format error.");
					} else if (userInfo.containsKey(login[1]) && userInfo.get(login[1]).equals(login[2])) {

						boolean isUserLoggedIn = false;

						for (final ClientHandler c : clients) {
							String clientname = "";
							if (c.getSession().size() > 0) {
								clientname = c.getSession().keySet().toArray()[0].toString();
							}
							if (clientname.equals(login[1])) {
								isUserLoggedIn = true;
								break;
							}
						}
						if (!isUserLoggedIn) {
							// login[0]= CMD, login[1]= uername, login[2]=password
							session.put(login[1], login[2]);
							writeToClient("200 OK");
						} else {
							writeToClient("201 user is already logged in by another client");
						}

					} else {
						writeToClient("410 Wrong UserID or Password.");
					}
				} else if (line != null && line.equals("SHUTDOWN")) {

					if (session.size() > 0 && session.containsKey("root")) {
						writeToClient("200 OK");

						// close all running sockets
						for (final ClientHandler c : clients) {
							// Don't close current client yet
							String clientname = "";
							if (c.getSession().size() > 0) {
								clientname = c.getSession().keySet().toArray()[0].toString();
							}
							if (session.containsKey(clientname)) {
								continue;
							}
							c.writeToClient("210 the server is about to shutdown ......");
							c.closeAndExitSocket();
						}
						writeToClient("210 the server is about to shutdown ......");
						closeAndExitSocket();
						myService.close();

						break;
					} else {
						writeToClient("402 User not allowed to execute this command.");
					}
				} else if (line != null && line.equals("LOGOUT")) {
					if (session.size() > 0) {
						// delete entry from text file
						session.clear();
						writeToClient("200 OK");
					} else {
						writeToClient("409 there are no logged in users.");
					}
				} else if (line != null && line.equals("QUIT")) {
					writeToClient("200 OK");
					session.clear();
					break;
				} else if (line != null && line.equals("WHO")) {

					writeToClient("200 OK");
					writeToClient("The list of the active users:");

					for (final ClientHandler c : clients) {

						final Map<String, String> clientSession = c.getSession();
						if (clientSession.size() > 0) {

							final String USERNAME = clientSession.keySet().toArray()[0].toString();
							final String IP_ADDRESS = c.getClientSocket().getInetAddress().toString().replace("/", "");
							final String msg = USERNAME + "      " + IP_ADDRESS;

							writeToClient(msg);
						}
					}
				} else {
					writeToClient("300 message format error.");
				}
			}

		} catch (IOException e) {
			// can be logged to a file
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (Exception e) {
					// can be logged to a file
				}
			}
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					// can be logged to a file
				}
			}
			if (printWriter != null) {
				try {
					printWriter.close();
				} catch (Exception e) {
					// can be logged to a file
				}
			}

		}

	}
}
