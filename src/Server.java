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
	private ReplyState state;
	private int currentRecipient;
	private ArrayList<Account> clients;	
	String SERVER1_IP = "10.128.83.19";
	String SERVER2_IP = "10.128.83.21";
	private SMTP_clientState clientState = SMTP_clientState.HELO;
	String sender = null;
	String recip = null;
	String MIME = null;

	private enum ReplyState
	{
		AUTH, INIT, NEXT, DATA, QUIT, REDY, FORWARD, RETRIEVE
	}
	private enum SMTP_clientState  { HELO, MAIL, RCPT, DATA, MESSAGE, QUIT, FINISH ,DONE}
	/*
	 * Initializes the server with two user accounts and begins listening on port 25
	 */
	public Server()
	{
		clients = new ArrayList<Account>();
		clients.add(new Account("paultang", "sillyface"));
		clients.add(new Account("markramasco", "sillyface"));
		clients.add(new Account("server1", "pass123"));


			receiveConnection();
	}
	
	private void receiveConnection()
	{

		/*
		 * Always open server socket
		 */
		try {
		    serverSocket = new ServerSocket(4444);
		} catch (IOException e) {
		    System.out.println("Could not listen on port: 4444");
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
				if(clientSocket.getRemoteSocketAddress().toString().equals(SERVER1_IP) 
				   || clientSocket.getRemoteSocketAddress().toString().equals(SERVER2_IP))
				{
					out.println("220 " + clientSocket.getLocalAddress());
					state = ReplyState.NEXT;
				}
				else
				{
					state = ReplyState.AUTH;
				}
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
						messageFSM(fromClient);
						System.out.println(fromClient);
					}
					if(clientSocket.isConnected())
					{
						//System.out.println("yes");
						//System.out.println("state: " +state.toString());
					}
					else
					{
						//System.out.println("no");
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
				String[] sender;
				sender = message.split(": ");
			}
			//mail recipient, store <rcpt>
			else if(command.equals("RCPT"))
			{
				String[] recip;
				recip = message.split(": ");
				//String recipient = recip[1];
				for (int i = 0 ; i < clients.size(); i++)
				{
					Account temp = clients.get(i);
					if(temp.getUserName().equals(recip[1]))
					{
						
					}
				}
				System.out.println(recip[1]);
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
			out.println(reply);
			break;
		case DATA:

			if(message.equals("."))
			{
				reply = OK + " Message Accepted.";
				System.out.println(MIME);
				state = ReplyState.NEXT;
				out.println(reply);
			}		
			else
			{
				MIME += message;
			}
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
			{
				reply = "666 " + "Login First";
			}
			out.println(reply);
			break;
		//
		case REDY:
			command = message.substring(0, 4);
			if(command.equals("SEND"))
			{
				reply = "220 " + clientSocket.getLocalAddress();
				out.println(reply);
				state = ReplyState.NEXT;
			}
			else if(command.equals("RECV"))
			{
				
			}
			else if(command.equals("AUTH"))
			{
				state = ReplyState.AUTH;
			}
			break;
		}
		return reply;
	}
	 /*
	  *    Send a message to the server
	  *
	  */
	void sendToServer(){
		
	
		String replyCode;
		
		while(clientState != SMTP_clientState.DONE)
		try
		{
			replyCode = in.readLine();
			if(replyCode != null)
			{
				System.out.println(replyCode);
				switch(clientState)
				{
				case HELO:	//Verify the initial TCP connection, send reply
					System.out.println("clientState = HELO");
					if(replyCode.substring(0,3).equals("220"))
					{
						clientState = SMTP_clientState.MAIL;
						out.println("HELO" + clientSocket.getLocalAddress());
						System.out.println("HELO" + clientSocket.getLocalAddress());
					}
					else
					{
						System.out.println("Error, connection not established.\n" + replyCode);
						clientState = SMTP_clientState.DONE;
					}
					break;
							
				case MAIL: //Wait for HELO response
					System.out.println("clientState = MAIL");
					if(replyCode.substring(0,3).equals("250"))
					{
						clientState = SMTP_clientState.RCPT;
						System.out.println("MAIL FROM: " + sender);
						out.println("MAIL FROM: " + sender);
					}
					else
					{
						System.out.println("Error in HELO response.\n" + replyCode);
						clientState = SMTP_clientState.DONE;
					}
					break;
						
				case RCPT: 				
					System.out.println("clientState = RCPT");
					if(replyCode.substring(0,3).equals("250"))
					{
						clientState = SMTP_clientState.DATA;
						System.out.println("RCPT TO: " + recip);
						out.println("RCPT TO: " + recip);
					}
					else
					{
						System.out.println("Error in MAIL response.\n" + replyCode);
						clientState = SMTP_clientState.DONE;
					}
					break;
							
				case DATA: //Wait for recipient ok response before sending data
					System.out.println("clientState = DATA");
					if(replyCode.substring(0,3).equals("250"))
					{
						clientState = SMTP_clientState.MESSAGE;
						out.println("DATA");
					}
					else
					{
						System.out.println("Error in RCPT response: \n" + replyCode);
						clientState = SMTP_clientState.DONE;
					}
					break;
						
				case MESSAGE: //Send formatted message
					System.out.println("clientState = MESSAGE");
					if(replyCode.substring(0,3).equals("354"))
					{
						clientState = SMTP_clientState.QUIT;
						out.println(MIME);
						out.println(".");
						System.out.println(".");
					}
					else
					{
						System.out.println("Error in DATA response: \n" + replyCode);
						clientState = SMTP_clientState.DONE;
					}
					break;
					
				case QUIT: //Send formatted message
					System.out.println("clientState = QUIT");
					if(replyCode.substring(0,3).equals("250"))
					{
						clientState = SMTP_clientState.FINISH;
						out.println("QUIT");
					}
					else
					{
						System.out.println("Error in MESSAGE response.");
						clientState = SMTP_clientState.DONE;
					}
					break;
						
				case FINISH: //Final message 
					System.out.println("clientState = FINISH");
					if(replyCode.substring(0,3).equals("221"))
					{
						clientState = SMTP_clientState.DONE;
						out.println("QUIT");
					}
					else
					{
						System.out.println("Error in QUIT response.");
						clientState = SMTP_clientState.DONE;
					}
					break;
				case DONE: //When to close ports?
					break;
					
				}
			}
		}catch(IOException e){
			System.out.println(e);
		}	
	}
	public static void main(String[] args)
	{
		Server s = new Server();
	}
}
