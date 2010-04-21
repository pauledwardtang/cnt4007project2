import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private boolean clientInput = true;
	private ReplyState state = ReplyState.AUTH;
	private ArrayList<Account> clients;	
	private enum ReplyState
	{
		AUTH, INIT, NEXT, DATA, QUIT, REDY, FORWARD
	}
	
	/*
	 * Initializes the server with two user accounts and begins listening on port 25
	 */
	public Server()
	{
		clients = new ArrayList<Account>();
		clients.add(new Account("paultang", "sillyface"));
		clients.add(new Account("markramasco", "sillyface"));


			receiveConnection();
	}
	
	private void receiveConnection()
	{

		/*
		 * Always open server socket
		 */
		try {
		    serverSocket = new ServerSocket(25);
		} catch (IOException e) {
		    System.out.println("Could not listen on port: 25");
		    System.exit(-1);
		}
		while(true){
			/*
			 * Client specific socket
			 * open reader write for socket
			 */
			clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(
						clientSocket.getOutputStream(), true);
				System.out.println("Accepted client socket: " + clientSocket.getRemoteSocketAddress().toString());
			} catch (IOException e) {
				System.out.println("Accept failed: 25");
				System.exit(-1);
			}
			
			/*
			 * input commands
			 */
			try {
				//connection accepted message
				while(!(state == ReplyState.QUIT))
				{
					String fromClient = in.readLine();				
					if(fromClient == null){}
							
					else if(fromClient != null)
					{
						if(state == ReplyState.NEXT || state == ReplyState.AUTH )
						{
							String t = messageFSM(fromClient);
						//	out.println(messageFSM(fromClient));
							out.println(t);
							System.out.println(t);
						}
						else if(state == ReplyState.DATA)
						{
							if(fromClient.equals("."))
							{
								out.println(messageFSM(fromClient));
							}		
						}
						else if(state == ReplyState.REDY)
						{
							if(fromClient.equals("SEND"))
							{
								out.println(messageFSM(fromClient));
							}
						}
						System.out.println(fromClient);
					}
					if(clientSocket.isConnected())
					{
						System.out.println("yes");
						System.out.println("state: " +state.toString());
					}
					else
					{
						System.out.println("no");
					}
				}
			} catch (SocketException e) {
				socketClose();
				e.printStackTrace();
			} catch (IOException e) {
				socketClose();
				e.printStackTrace();
			}

			//close sockets
			//close socket
			socketClose();
			state = ReplyState.AUTH;
			System.out.println("Connection closed");
		}
	}
	private void socketClose()
	{
		//close socket
		try {
			clientSocket.close();
			//serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * State Machine to parse commands and reply with proper Reply code
	 */
	private String messageFSM(String message)
	{
		//reply codes
		String OK = "250";
		String DATA = "354";
		String QUIT = "221";
		String WRONG = "503";
		
		//reply code and message
		String reply = null;
		String command = null;
		
		
		switch(state)
		{
		case NEXT:
			//command message parsed from message
			command = message.substring(0, 4);
			
			//initiation reply OK
			if(command.equals("HELO"))
			{
				reply = OK + " Hello " + clientSocket.getRemoteSocketAddress() + ".";
			}

			//Data section, change state machine to take in data till .
			else if(command.equals("DATA"))
			{
				state = ReplyState.DATA;
				reply = DATA + " Enter email.";
			}
			//mail sender, store <sender>
			else if(command.equals("MAIL"))
			{
				reply = OK + " Sender OK.";
			}
			//mail recipient, store <rcpt>
			else if(command.equals("RCPT"))
			{
				reply = OK + " Enter email.";
			}
			//email transaction done, send quit reply
			else if(command.equals("QUIT"))
			{
				reply = QUIT + " " + clientSocket.getLocalAddress() + " Email Sent.";
				state = ReplyState.REDY;
			}
			//incorrect code
			else
			{
				reply = WRONG + " Incorrect command.";
			}
			break;
		case DATA:
			//command message parsed from message
			reply = OK + " Message Accepted.";
			state = ReplyState.NEXT;
			break;
		case AUTH:
			command = message.substring(0, 4);
			if(command.equals("AUTH"))
			{
				String[] authInput; 
				boolean accepted = false;
				//Should split up the message into {op, username, password}
				authInput = message.split("\\s+");  //"\\s" means we are splitting based on whitespace
				System.out.println("------------");
				for (int i = 0 ; i < clients.size(); i++) {
					System.out.println(authInput[i]);
					Account temp = clients.get(i);
					//check usernames
					if(temp.getUserName().equals(authInput[1]) && temp.getPassword().equals(authInput[2]))
					{
						reply = "777 " + "Login Accepted";		
						state = ReplyState.REDY;
						accepted = true;
						break;
					}
				}
				//no login or incorrect password
				if(!accepted)
				{
					reply = "666 " + "Login Failed";
				}
				System.out.println("------------");
				
			}
			else
				reply = "666 " + "Login First";
			break;
		//
		case REDY:
			command = message.substring(0, 4);
			state = ReplyState.NEXT;
			reply = "220 " + clientSocket.getLocalAddress();
			break;
		}
		return reply;
	}
	public static void main(String[] args)
	{
		Server s = new Server();
	}
}
