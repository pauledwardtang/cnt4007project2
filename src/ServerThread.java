import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

enum ReplyState
{
	AUTH, INIT, NEXT, DATA, QUIT, REDY, FORWARD, RETRIEVE
}
enum SMTP_clientState  { HELO, MAIL, RCPT, DATA, MESSAGE, QUIT, FINISH ,DONE}

public class ServerThread extends Thread {
	private Socket socket = null;
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

	public ServerThread(Socket socket) {
		super("ServerThread");
		this.socket = socket;		
		
		clients = new ArrayList<Account>();
		clients.add(new Account("paultang", "sillyface"));
		clients.add(new Account("markramasco", "sillyface"));
		
		if(socket.getRemoteSocketAddress().toString().equals(SERVER1_IP) 
				   || socket.getRemoteSocketAddress().toString().equals(SERVER2_IP))
				{
					out.println("220 " + socket.getLocalAddress());
					state = ReplyState.NEXT;
				}
				else
				{
					state = ReplyState.AUTH;
				}
	}
	/*
	 * State Machine to parse commands and reply with proper Reply code
	 */
	private String messageFSM(String message,  PrintWriter out, BufferedReader in)
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
				reply = OK + " Hello " + socket.getRemoteSocketAddress() + ".";
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
				reply = QUIT + " " + socket.getLocalAddress() + " Email Sent.";
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
				reply = "220 " + socket.getLocalAddress();
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
		public void run() {

		while(true)
		{
		//************************SMTP*********************************\\
			try 
			{
			    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			   	//connection accepted message
				while(!(state == ReplyState.QUIT))
				{
					String fromClient = in.readLine();				
					if(fromClient == null){}
							
					else if(fromClient != null)
					{
						messageFSM(fromClient, out, in);
						System.out.println(fromClient);
					}
				}
			    //
			} catch (IOException e) {
			    e.printStackTrace();
			}

			
		}
	}
}

