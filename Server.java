/*
 * Server.java
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;

// Server Class 
public class Server {

	public static final int SERVER_PORT = 6333;
	public static ServerSocket server = null;

	// Delete file
	public static void deleteAfile(String filename) {
		File file = new File(filename);
		// create a file if doesn't exists
		if (file.exists()) {
			file.delete();
		}
	}

	// Create file
	public static void createAfile(String filename) throws IOException {
		File file = new File(filename);
		// create a file if doesn't exists
		if (!file.exists()) {
			file.createNewFile();
		} else {
			System.out.println(filename + " already exists.");
		}
	}

	public static void main(String[] args) {
		try {
			// Create user.txt file
			createAfile("user.txt");

			// server is listening on port 1234
			server = new ServerSocket(SERVER_PORT);

			// running infinite loop for getting
			// client request
			while (true) {

				// socket object to receive incoming client
				// requests
				Socket client = server.accept();

				// create a new thread object
				ClientHandler clientSock = new ClientHandler(client, server);

				// This thread will handle the client
				// separately
				new Thread(clientSock).start();
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
			// delete user txt file
			deleteAfile("user.txt");
		}
		System.out.println("Server is closed");
	}
}

// ClientHandler class
class ClientHandler implements Runnable {

	Socket serviceSocket = null;
	ServerSocket myService = null;
	String line;
	InputStreamReader inputStreamReader = null;
	OutputStreamWriter outputStreamWriter = null;
	BufferedReader bufferedReader = null;
	BufferedWriter bufferedWriter = null;

	public ClientHandler(Socket socket, ServerSocket server) {
		this.serviceSocket = socket;
		this.myService = server;
	}

	// write a single message to client
	public void writeToClient(BufferedWriter br, String message) throws IOException {
		br.write(message);
		br.newLine();
		br.flush();
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
		// create a file channel
		FileOutputStream fos = new FileOutputStream(file, true);
		FileChannel channel = fos.getChannel();

		// lock the file so other don't write to file at same time.
		FileLock lock = channel.lock();

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.write(text);
		bw.newLine();

		// Close the files
		bw.close();
		fos.close();

		// Release the lock - if it is not null!
		if (lock != null) {
			lock.release();
		}
		// close the lock - if it is not null!
		if (channel != null) {
			channel.close();
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
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();

		// prevent other processes from writing to file while reading
		FileLock lock = channel.lock();

		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		Charset charset = Charset.forName("US-ASCII");

		while(channel.read(byteBuffer) > 0){
			byteBuffer.array();
			System.out.print(charset.decode(byteBuffer));
		}
		//System.out.println("bytebuffer:"+byteBuffer);

		fis.close();
		lock.release();
		channel.close();

		// try {
		// 	// reading from file
		// 	// BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		// 	// String line = "";

		// 	// while ((line = br.readLine()) != null) {
		// 	// 	if (line.trim().isEmpty())
		// 	// 		continue; // don't insert empty lines
		// 	// 	data.add(line);
		// 	// }
		// 	// br.close();

		// } finally {

		// 	// closing the files
		// 	//br.close();
		// 	fis.close();

		// 	// Release the lock - if it is not null!
		// 	if (lock != null) {
		// 		lock.release();
		// 	}

		// 	// close the lock - if it is not null!
		// 	if (channel != null) {
		// 		channel.close();
		// 	}

		// }

		return data;
	}

	/**
	 * @filename e.g, username.txt
	 * @entry specific line in text file
	 * @returns <void> : removes a user from file
	 */
	public void modifyExistingFile(String filename, String entry) throws IOException {

		File file = new File(filename);
		// create a file channel
		FileOutputStream fos = new FileOutputStream(file);
		FileChannel channel = fos.getChannel();

		// lock the file so other don't write to file at same time.
		FileLock lock = channel.lock();

		// ***** READING CONTENT OF FILE ****
		final ArrayList<String> data = this.readFromFile(filename);

		// ***** WRITING TO FILE *****

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		// remove entry from file
		data.removeIf(e -> e.equals(entry));

		for (final String s : data) {
			bw.write(s);
			bw.newLine();
		}

		// closing files
		bw.close();
		fos.close();

		// Release the lock - if it is not null!
		if (lock != null) {
			lock.release();
		}
		// close the lock - if it is not null!
		if (channel != null) {
			channel.close();
		}
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

			// File input & output
			inputStreamReader = new InputStreamReader(serviceSocket.getInputStream());
			outputStreamWriter = new OutputStreamWriter(serviceSocket.getOutputStream());

			bufferedReader = new BufferedReader(inputStreamReader);
			bufferedWriter = new BufferedWriter(outputStreamWriter);

			// Get client port and IP
			final String IP_ADDRESS = serviceSocket.getInetAddress().toString();
			final String PORT_NUMBER = Integer.toString(serviceSocket.getPort());

			// message store command
			String msgStoreCMD = "";

			// word of the day
			int wordNum = 0;

			// save logged in user
			Map<String, String> session = new HashMap<String, String>();

			// as long as we receive data, echo that data back to the client.
			while (true) {

				line = bufferedReader.readLine();

				System.out.println("Client CMD: " + line);

				if (line != null && (line.equals("MSGSTORE") || msgStoreCMD.equals("MSGSTORE"))) {
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
						session.put(login[1], login[2]);
						// insert entry to text file
						final String userEntry = login[1] + "|" + PORT_NUMBER + "|" + IP_ADDRESS;
						// read user .txt file
						final ArrayList<String> data = this.readFromFile("user.txt");
						Boolean isUserLoggedin = false;
						for (final String info : data) {
							if (info.contains(login[1])) {
								isUserLoggedin = true;
								break;
							}
						}
						if (!isUserLoggedin) {
							this.writeToFile("user.txt", userEntry);
							writeToClient(bufferedWriter, "200 OK");
						} else {
							writeToClient(bufferedWriter, "404 user already logged in.");

						}

					} else {
						writeToClient(bufferedWriter, "410 Wrong UserID or Password.");
					}
				} else if (line != null && line.equals("SHUTDOWN")) {

					if (session.size() > 0 && session.containsKey("root")) {
						writeToClient(bufferedWriter, "200 OK");
						myService.close();
						break;
					} else {
						writeToClient(bufferedWriter, "402 User not allowed to execute this command.");
					}
				} else if (line != null && line.equals("LOGOUT")) {
					if (session.size() > 0) {
						// delete entry from text file
						final String username = session.keySet().toArray()[0].toString();
						final String userEntry = username + "|" + PORT_NUMBER + "|" + IP_ADDRESS;
						this.modifyExistingFile("user.txt", userEntry);
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
					final ArrayList<String> data = this.readFromFile("user.txt");
					String who = "";

					for (final String s : data) {
						who += s + "\n";
					}
					writeToClient(bufferedWriter, "200 OK");
					writeToClient(bufferedWriter, who);
				}

				/**
				 * else if(line != null && line.equals("SEND")){....}
				 */
				else {
					writeToClient(bufferedWriter, "300 message format error.");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serviceSocket != null) {
				try {
					serviceSocket.close();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
			if (outputStreamWriter != null) {
				try {
					outputStreamWriter.close();
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
