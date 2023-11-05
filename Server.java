/*
 * Server.java
 */

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

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
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
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
	private BufferedWriter bufferedWriter = null;
	private ArrayList<ClientHandler> clients;

	// save logged in user
	private Map<String, String> session = new HashMap<String, String>();

	// getSession users
	public Map<String, String> getSession() {
		return this.session;
	}

	ServerSocket myService = null;
	String line;

	ClientHandler(Socket client, ServerSocket server, ArrayList<ClientHandler> clients) throws IOException {
		this.client = client;
		this.bufferedReader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
		this.myService = server;
		this.clients = clients;
	}

	// write a single message to client
	public void writeToClient(BufferedWriter br, String message) throws IOException {
		br.write(message);
		br.newLine();
		br.flush();
	}

	// closes the sockets
	public void closeAndExitSocket() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @filename :"file path"
	 * @returns <void> : writes (append) text to a file
	 *          used to save messages to a file (doesn't override content of the
	 *          file)
	 */
	public void writeToFile(String filename, String text) throws IOException {

		File file = new File(filename);
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileChannel channel = raf.getChannel();

		// lock the file so other don't write to file at same time.
		FileLock lock = channel.lock();
		try {
			// //encoding string to utf
			byte[] ptext = (text+"\n").getBytes(ISO_8859_1);
			String value = new String(ptext, UTF_8);

			raf.seek(raf.length());// go to end of line
			raf.writeChars(value + "\n"); // write text to file

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (lock != null)
					lock.release();
				if (raf != null)
					raf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * @filename :"file path"
	 * @returns : read from file and store them in an arrayList
	 */
	public ArrayList<String> readFromFile(String filename) throws IOException {

		ArrayList<String> data = new ArrayList<String>();

		File file = new File(filename);
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileChannel channel = raf.getChannel();

		// prevent other processes from writing to file while reading
		FileLock lock = channel.lock();

		try {
			// reading from file
			String line = "";
			while ((line = raf.readLine()) != null) {
				if (line.trim().isEmpty())
					continue; // don't insert empty lines
				data.add(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (lock != null)
					lock.release();
				if (raf != null)
					raf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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
		// open input and output streams

		try {
			// Get client port and IP
			final String IP_ADDRESS = client.getInetAddress().toString().replace("/", "");

			// message store command
			String msgStoreCMD = "";

			// message send command
			String msgSendCMD = "";
			String msgSendUSER = "";

			// word of the day
			int wordNum = 0;

			// save logged in user
			// Map<String, String> session = new HashMap<String, String>();

			// as long as we receive data, echo that data back to the client.
			while (true) {

				line = bufferedReader.readLine();

				System.out.println("Client CMD: " + line);

				if (line != null && line.contains("SEND") || (msgSendCMD.equals("SEND") && !msgSendUSER.isEmpty())) {
					if (msgSendCMD.equals("SEND")) {
						String msg1 = "200 OK you have a new message from " + msgSendUSER;
						String msg2 = session.keySet().toArray()[0] + ": " + line;
						for (final ClientHandler c : clients) {
							if (c.getSession().containsKey(msgSendUSER)) {
								c.writeToClient(bufferedWriter, msg1);
								c.writeToClient(bufferedWriter, msg2);
								// c.bufferedWriter.write(msg1);
								// //c.bufferedWriter.flush();
								// c.bufferedWriter.write(msg2);
								//c.bufferedWriter.flush();
								break;
							}
						}
						msgSendCMD = "";
						msgSendUSER = "";

					} else {
						String send[] = line.split(" ");
						if (send.length != 2) {
							writeToClient(bufferedWriter, "Invalid send command");
						} else if (userInfo.containsKey(send[1])) {
							msgSendCMD = "SEND";
							msgSendUSER = send[1];
							writeToClient(bufferedWriter, "200 OK");
						} else {
							// do nothing
						}
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
						writeToClient(bufferedWriter, "200 OK");
					} else {
						writeToClient(bufferedWriter, "401 You are not currently logged in, login first.");
					}
				} else if (line != null && line.equals("MSGGET")) {
					ArrayList<String> word = readFromFile("word.txt");
					writeToClient(bufferedWriter, "200 OK");
					writeToClient(bufferedWriter, word.get(wordNum % word.size()));
					wordNum++;
				} else if (line != null && line.contains("LOGIN")) {
					String login[] = line.split(" ");
					if (session.size() > 0) {
						String msg = "409 user " + session.keySet().toArray()[0] + " is already logged in.";
						writeToClient(bufferedWriter, msg);
					} else if (login.length < 3) {
						writeToClient(bufferedWriter, "300 message format error.");
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
							session.put(login[1], IP_ADDRESS);
							writeToClient(bufferedWriter, "200 OK");
						} else {
							writeToClient(bufferedWriter, "201 user is already logged in by another client");
						}

					} else {
						writeToClient(bufferedWriter, "410 Wrong UserID or Password.");
					}
				} else if (line != null && line.equals("SHUTDOWN")) {

					if (session.size() > 0 && session.containsKey("root")) {
						writeToClient(bufferedWriter, "200 OK");

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
							c.writeToClient(bufferedWriter, "210 the server is about to shutdown ......");
							c.closeAndExitSocket();
						}
						closeAndExitSocket();
						myService.close();
						break;
					} else {
						writeToClient(bufferedWriter, "402 User not allowed to execute this command.");
					}
				} else if (line != null && line.equals("LOGOUT")) {
					if (session.size() > 0) {
						// delete entry from text file
						session.clear();
						writeToClient(bufferedWriter, "200 OK");
					} else {
						writeToClient(bufferedWriter, "409 there are no logged in users.");
					}
				} else if (line != null && line.equals("QUIT")) {
					// delete login session file
					writeToClient(bufferedWriter, "200 OK");
					break;
				} else if (line != null && line.equals("WHO")) {

					writeToClient(bufferedWriter, "200 OK");
					writeToClient(bufferedWriter, "The list of the active users:");

					for (final ClientHandler c : clients) {

						final Map<String, String> clientSession = c.getSession();
						if (clientSession.size() > 0) {

							final String username = clientSession.keySet().toArray()[0].toString();
							final String userIP = clientSession.values().toArray()[0].toString();
							final String msg = username + "      " + userIP;

							writeToClient(bufferedWriter, msg);
						}
					}
				} else {
					writeToClient(bufferedWriter, "300 message format error.");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}

		}

	}
}
