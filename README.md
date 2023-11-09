# YAMOTD PROJECT
### AUTHORS
- Tapon Das
- Patrick Imoh
### GROUP INFO
UMich CIS 527 Group: 30
### CONTRIBUTIONS FROM AUTHORS
- Brainstormed on how to approach the second part of the project.
- Decided on whether to use C, C++ or continue with Java, and drew out the structure for the sample program files.
- Engaged in discussions on how to implement multi-socket programming using Java.
- Engaged in research on how to implement the program.
- Reviewed individual codes and merged the better codes where necessary.
- Tested and documented any findings with the aim to resolving them.
- Implemented the LOGIN, MSGGET, MSGSTORE, LOGOUT, SHUTDOWN, QUIT, WHO and SEND operations on both the client and server.
- Researched and implemented the ArrayList, HashMap and writeToFile functions.
- Researched and utilized an easy method to login to the UMich server to test the code.
- Reviewed and reformatted the code to remove unnecessary or uninitialized data.
- Reworked and reviewed commenting and style in the code.
- Reviewed the problem definition to ascertain that all requirements have been met.
- Researched for standardized format and developed the README document.

### FUNCTIONS USED
- writeToClient: used to send messages from the server to the client.
- writeToFile: used to write the word of the day string to the word.txt file without overwriting the original content.
- readFromFile: used to read data from the word.txt file and store them in an array list.
- writeToServer: used to send messages from the client to the server.

### PROJECT DESCRIPTION
The programs are written in java programming language and can be run on either a linux or unix environment. There is a client "Client.java" as well as server "Server.java" program. The communication between the server and clients happen through TCP using the port number 6333. The server receives requests through this socket, acts on those requests, and returns the results to the requester. The clients also creates a socket in the internet domain, send requests to the server IP of a computer specified on the command-line, and receive responses through these sockets from the server. Multiple connections between one or more clients and the server is possible.

It performs the following functions;
- It returns a message of the day on the client console to any user that sends the server a MSGGET message.
- It allows a user, who has been authenticated by the server to send the server a MSGSTORE message to upload one or more messages of the day to the server. These messages are stored in the file "word.txt" and can be returned to other clients that also send the MSGGET messages to the server.
- It allows the "root" user to send a SHUTDOWN message to the server which will cause the server to close any open sessions and sockets, and then terminate.
- It verifies the identity of a user using the LOGIN command, and then allows the user to logout from the session using the LOGOUT command.
- It terminates a client session using the QUIT command.
- It returns the user IDs and IP addresses of all logged in users using the WHO command.
- It allows a logged in user to send a private message to another logged in user using the SEND command.

### HOW TO INSTALL AND RUN THE PROJECT ON THE UMICH NETWORK AND LINUX / UNIX ENVIRONMENT
#### Step 1: Download the tar file
This can be done using any method of your choice.
#### Step 2: Extract the downloaded tar file
```bash
$ tar -xvf das_t_p1.tar
```
#### Step 3: Confirm the contents
01. Server.java
02. Client.java
03. word.txt
04. Makefile
05. README.md 
#### Step 4: Connect to UMich VPN or use a Linux / Unix Environment
To use the UMich VPN option, use the PaloAlto GlobalProtect VPN application and connect using the portal address: "umvpn.umd.umich.edu".
```bash
# Username is UMich ID; e.g. username = 'john' where email is john@umich.edu
# Use your UMich password
# Authenticate with Duo application
```
#### Step 5: Connect to UMich Server
Only for users connecting to the UMich Server. Linux / Unix users ignore this step.
```bash
$ ssh username@login.umd.umich.edu -p 22
# Username is UMich ID; e.g., username = 'john' where email is john@umich.edu
# Use your UMich password
# Authenticate with Duo application
```
#### Step 6: Copy files to server
Only for users connecting to the UMich Server. Linux / Unix users ignore this step.
```bash
$ scp -rv source -P 22 username@login.umd.umich.edu: destination-path
# Username is UMich ID; e.g., username = 'john' where email is john@umich.edu
# Use your UMich password
# Authenticate with Duo application
```
#### Step 7: Run code with makefile
```bash
$ make all               # Creates the Server and Client class files
$ make Server.class      # Creates only the Server class file
$ make Client.class      # Creates only the client class file 
```
#### Step 8: Run the java code
```bash
$ java Server
$ java Client IP         # e.g., java Client 127.0.0.1 
```
#### Step 9: To rebuild the files
```bash
$ make clean             # removes the Server and Client class files
$ make clean all         # removes the Server and Client class files and then rebuilds a new Server and Client class files
```
### HOW TO USE THE YAMOTD PROGRAM
The server begins execution by reading the "word.txt" file, which initially has five (5) messages of the day stored in it. Once executed, the server would wait for connection requests from the client.

The client operates by sending any of the commands; MSGGET, MSGSTORE, SHUTDOWN, LOGIN, LOGOUT, QUIT, WHO and SEND to the server. You should use the carriage-return ("enter" key on your keyboard) after inputting any of these commands.
#### MSGGET
The MSGGET command is used to request a new message of the day from the server. When input, the server would return a "200 OK" if the correct command is entered, as well as one of the messages of the day stored in the "word.txt" file on the next line of the console. It's operation is shown below. 
```bash
c: MSGGET
s: 200 OK
c: Education is not preparation for life; education is life itself
```
#### MSGSTORE
The MSGSTORE command is used to send one message of the day by a logged in user to the server, which is stored in the "word.txt" file. If a user is not logged in, the operation is not possible and an error code with message "401 You are not currently logged in, login first." is displayed. A "200 OK" message is displayed when the correct command: "MSGSTORE" is parsed as well as when a new message is successfully sent to the server. The operation of this command is shown below.
```bash
c: MSGSTORE
s: 401 You are not currently logged in, login first.

c: LOGIN username password
s: 200 OK

c: MSGSTORE
s: 200 OK

c: This is a new message of the day.
s: 200 OK
```
#### SHUTDOWN
The SHUTDOWN command is used to close the connections and sockets on both the client and server, and can only be performed by the "root" user. If no user is logged in or the logged in user isn't "root", the command is rejected. See below for usage.
```bash
c: SHUTDOWN # when logged in user is not "root"
s: 402 User not allowed to execute this command. # result when logged in user is not "root"

c: LOGOUT
s: 200 OK

c: LOGIN root password
s: 200 OK

c: SHUTDOWN1
s: 300 message format error.

c: SHUTDOWN
s: 200 OK
s: 210 the server is about to shutdown ......
```
#### LOGIN
The LOGIN command is used to authenticate a user with the server. The server checks and only authenticates users whose username and password match its records. A "200 OK" messages signifies a successful login whereas "410 Wrong UserID or Password" means that either a wrong username or password has been sent. The usage of this command is demonstrated below.
```bash
c: LOGIN wrong_username wrong_password
s: 410 Wrong UserID or Password.

c: LOGIN mike password
s: 200 OK

c: LOGIN smith password
s: 409 user mike is already logged in.
```
#### LOGOUT
The LOGOUT command is used to terminate the session of a logged in user.
```bash
c: LOGIN username password
s: 200 OK

c: LOGOUT
s: 200 OK

c: LOGOUT
s: 409 there are no logged in users.
```
#### QUIT
The QUIT command is used to terminate the session between the client and the server. If the command is correctly sent and successful, the server returns a "200 OK" message. The operation of the QUIT command is shown below.
```bash
c: QUIT
s: 200 OK
***Client Terminated Successfully***
```
#### WHO
The WHO command is used to list all logged-in users as well as the their IP addresses. If the command is correctly sent and successful, the server returns a "200 OK" message. The operation of the WHO command is shown below.
```bash
c: WHO
s: 200 OK
The list of the active users:
david    127.0.0.1
mary    127.0.0.1
```
#### SEND
The SEND command is used to send a private message from one logged-in user to another logged-in user. If the command is correctly sent and successful, the server returns a "200 OK" message. A "200 OK" messages signifies a successful usage of the command together with a logged-in user, whereas "420 either the user does not exist or is not logged in" means that a user who isn't logged in or doesn't exist has been entered. If the SEND command is used without the username argument, the server returns "Invalid send command". The usage of this command is demonstrated below.
```bash
c: SEND # when no logged in user at the time
s: 402 you are not logged in. Only logged in users are allowed to send messages.

c: SEND
s: Invalid send command

# At David's window
c: SEND john # john isn't logged in
s: 420 either the user does not exist or is not logged in.

c: SEND mary # mary is currently logged in
s: 200 OK
c: Hello mary. How are you doing today?
s: 200 OK

# At Mary's window
s: 200 OK you have a new message from david
david: Hello mary. How are you doing today?
```