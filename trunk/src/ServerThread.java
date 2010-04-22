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
	AUTH, INIT, NEXT, DATA, QUIT, REDY, FORWARD, RETRIEVE, EXIT, PRNT, POP3
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
	String SERVER1_IP = "10.128.83.134";
	String SERVER2_IP = "10.128.82.93";
	private SMTP_clientState clientState = SMTP_clientState.HELO;
	String sender = null;
	String recip = null;
	String MIME = "";
	String user = null;

	public ServerThread(Socket socket, ArrayList<Account> clients) {
		super("ServerThread");
		this.socket = socket;		
		this.clients = clients;

		
		System.out.println("Connected to: " + socket.getRemoteSocketAddress().toString());
		if(socket.getRemoteSocketAddress().toString().contains(SERVER1_IP))
				{
					state = ReplyState.NEXT;
					PrintWriter out;
					try {
						out = new PrintWriter(socket.getOutputStream(), true);
						out.println("220 " + socket.getLocalAddress());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
				String[] temp;
				temp = message.split(": ");
				sender = temp[1];
			}
			//mail recipient, store <rcpt>
			else if(command.equals("RCPT"))
			{
				String[] temp;
				temp = message.split(": ");
				recip = temp[1];
				reply = OK + " Enter email.";
			}
			//email transaction done, send quit reply
			else if(command.equals("QUIT"))
			{
				reply = QUIT + " " + socket.getLocalAddress() + " Email Sent.";
				state = ReplyState.FORWARD;
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
				//System.out.println(MIME);
				state = ReplyState.NEXT;
				out.println(reply);
			}		
			else
			{
				MIME += " " +  message + "\n";
			}
			break;
		case AUTH:
			command = message.substring(0, 4);
			if(command.equals("AUTH"))
			{
				String[] authInput; 
				boolean accepted = false;
				//Should split up the message into {op, username, password}
				authInput = message.split("\\s");  //"\\s" means we are splitting based on whitespace
				for (int i = 0 ; i < clients.size(); i++) {
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
			else if(command.equals("POP3"))
			{
				String[] pop; 
				pop = message.split("\\s");
				user = pop[1];
			    state = ReplyState.POP3;
				out.println("+OK " + "POP3 Started");
			}
			else if(command.equals("PRNT")) //Print out mailbox of given client
			{
				String username;
				String[] temp;
				temp = message.split(": ");
				username = temp[1];
				System.out.println(username);
				System.out.println("Ready to print");
				//Find the client and print out his/her inbox
	 			for (int i = 0 ; i < clients.size(); i++)
	 			{
	 				Account tempUsr = clients.get(i);
	 				System.out.println("USER IS: " + username);
	 				if(tempUsr.getUserName().equals(username))
	 				{
	 					ArrayList<Account.Mail> inbox = tempUsr.getInbox();
	 					 for(int j = 0; j < tempUsr.getInbox().size(); j++)
	 					 {
	 						 System.out.println(inbox.get(j).getMessage()+"\n");
	 					 }
	 					 break;
	 				}
	 			}
			}
			break;
		case FORWARD:
			boolean forwardEn = true;
 			for (int i = 0 ; i < clients.size(); i++)
 			{
 				Account temp = clients.get(i);
 				System.out.println("recip is:  " + recip);
 				if(temp.getUserName().equals(recip))
 				{
 					System.out.println("Local Email");
 					temp.addMail(MIME);
 					forwardEn = false;
 					break;
 				}
 			}
 			if(forwardEn)
 			{
 				try
 				{
 					System.out.println("Non Local Email");
	 				Socket temp = new Socket(SERVER1_IP, 25);
	 				PrintWriter outWriter = new PrintWriter(temp.getOutputStream(), true);
				    BufferedReader inReader = new BufferedReader(new InputStreamReader(temp.getInputStream()));
	 				//new ServerThread(temp, clients).start();
	 				//out.println("SEND");
	 				sendToServer(temp, outWriter, inReader);
	 				//temp.close();
	 				
 				}catch(IOException e){
 					System.out.println(e);
 				}
 			}
 			clientState = SMTP_clientState.HELO;
 			state = ReplyState.REDY;	
			break;
		case POP3:
			command = message.substring(0, 4);
			if(command.equals("list"))
			{
				for (int i = 0 ; i < clients.size(); i++)
	 			{
	 				Account tempUsr = clients.get(i);
	 				if(tempUsr.getUserName().equals(user))
	 				{
	 					ArrayList<Account.Mail> inbox = tempUsr.getInbox();
	 					 for(int j = 0; j < tempUsr.getInbox().size(); j++)
	 					 {
	 						 out.println(j + " " + inbox.get(j).subject);
	 					 }
	 				}
	 			}
			}
			else if(command.equals("retr"))
			{
				String[] index;
				index = message.split("\\s");
				for (int i = 0 ; i < clients.size(); i++)
	 			{
	 				Account tempUsr = clients.get(i);
	 				if(tempUsr.getUserName().equals(user))
	 				{
						ArrayList<Account.Mail> inbox = tempUsr.getInbox();
						out.println(inbox.get(Integer.parseInt(index[1])).message);
					}
				}
				
			}
			else if(command.equals("dele"))
			{
				String[] index;
				index = message.split("\\s");
				for (int i = 0 ; i < clients.size(); i++)
	 			{
	 				Account tempUsr = clients.get(i);
	 				if(tempUsr.getUserName().equals(user))
	 				{
						ArrayList<Account.Mail> inbox = tempUsr.getInbox();
						inbox.remove(index[1]);
						break;
					}
				}
			}
		    else if(command.equals("quit"))
			{
				out.println("+OK " + "POP signing off");
				state = ReplyState.REDY;
			}
		break;
	}
		return reply;
	}
	/*
	  *    Send a message to the server
	  *
	  */
	void sendToServer(Socket clientSocket, PrintWriter out, BufferedReader in){
		
	
		String replyCode;
		
		while(clientState != SMTP_clientState.DONE)
		try
		{
		    replyCode = in.readLine();
			System.out.println(replyCode);
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
					System.out.println(fromClient);		
					if(fromClient == null){}
							
					else if(fromClient != null)
					{
						messageFSM(fromClient, out, in);
					    System.out.println("-----" + state + "-----");
					}
				}
			    //
			} catch (IOException e) {
			    e.printStackTrace();
			}
			

		}
	}
}

