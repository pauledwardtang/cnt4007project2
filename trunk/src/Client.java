/*
 * Authors: Paul Tang, Mark Ramasco
 * CNT4007 Project 2 
 */
import java.net.*;
import java.io.*;
import java.util.Random;
import java.lang.Math;
import java.lang.Integer;


//*******************************Client*******************************
//*1 socket between authentication server

 public class Client{
	//---********Variables*********---

	//Enumeration for SMTP State machine and menu options
	private enum SMTP_State  { HELO, MAIL, RCPT, DATA, MESSAGE, QUIT, FINISH ,DONE}
	private enum menuOptions { SEND, RECEIVE,GO_BACK, EXIT }
	private enum emailOptions{ SAVE, DELETE, GO_BACK, EXIT }
	
	//IP Addresses for the servers
	String SERVER1_IP = "10.128.83.21";
	String SERVER2_IP = "10.128.83.21";

	private Socket socket, authenticationSocket;
	private String receiver_email_address, sender_email_address;
	private String subject;
	private String message_body;
	private String MIME;
	private Random rand;
	private String temp;
	private boolean authFlag;

	//Readers Writers

	private BufferedReader in;
	private PrintWriter out;

	private SMTP_State   state;
	//private menuOptions  menuChoice;
	private emailOptions emailChoice;

	//---********Constructor*********---
	/*
	 * This constructor is for testing: the message is given in command line arguments
	 * 
	 * */
		Client()
		{
					 
			rand = new Random();
			state = SMTP_State.HELO;
			constructSocket();
		}

	//---********Functions*********---

	/*
	 *    Construct socket. Choose a random server
	 *	  Server 1 = 0, Server 2 = 1
	 * 
	 */
	void constructSocket(){
			try{
				if(Math.round(rand.nextFloat()) == 0) //Set IP to server 1
				{
					socket = new Socket(SERVER1_IP, 25);
					System.out.println("making socket");
					//authenticationSocket = new Socket(SERVER1_IP, 4444);
				}
				else
				{
					socket = new Socket(SERVER1_IP, 25);
					System.out.println("making socket");
					//authenticationSocket = new Socket(SERVER1_IP, 4444);
				}
					
				//For testing purposes
				System.out.println(socket.getRemoteSocketAddress());
				//System.out.println(in.toString());
			
			// Creating readers/writers
			in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			} catch (MalformedURLException e){
				System.out.println("Error: " + e);
			} catch (IOException e){
				System.out.println("Error: " + e);
			}
			
	}


	/*
	 *    Display menu
	 *
	 */
	void displayMenu(){
	}

	 /*
	 *    Gathers the fields necessary to construct an email message using MIME format. Also gathers the user's credentials
	 *
	 */
	void getUserInput(){
			//Buffered writer for reading input from user
			String userName, authentication;
			String retryTemp;
			boolean retry = false;
			boolean showOptions = true;
			boolean authFlag = false;
			boolean hasResponded = false;
			
			try
			{
				do
				{
					BufferedReader writer = new BufferedReader(new InputStreamReader(System.in));
					String replyCode;
					
			//***********Getting username and password********************
					System.out.print("Username: ");
					userName = writer.readLine() + " ";
					
					System.out.print("Password: ");
					authentication = userName + writer.readLine();
					System.out.println();
					
					//Sending Authentication data to server
					System.out.println("AUTH " + authentication);
					out.println("AUTH " + authentication);
					do
					{
						replyCode = in.readLine();
						if(replyCode != null)
						{
							hasResponded = true;
							//System.out.println(replyCode);
							if(replyCode.substring(0,3).equals("777"))
							{
								authFlag = true;
								System.out.println("Authentication successful!");
							}
						}
					}while(hasResponded == false);
					
					if(authFlag == false) //If authentication failed, exit or retry
					{
						System.out.println("Authentication error! Exit(e) or Retry(r)?");
						retryTemp = writer.readLine();
						if(retryTemp.equals("r"))
							retry = true;
						else if(retryTemp.equals("e"))
						{
							retry = false;
							System.exit(0);
						}
					}
					else 		//Authentication worked!
					{
						int choice;
						do
						{
							System.out.println("****************OPTIONS*****************");
							System.out.println("Press (1) to send an email\n" +
											   "Press (2) to retrieve messages\n"+
											   "Press (3) to return to authentication\n"+
											   "Press (4) to exit.");
							choice = Integer.parseInt(writer.readLine());
							switch(choice)
							{
								case 1: //Gather user input for email structure
									System.out.print("To: ");
									receiver_email_address = writer.readLine();
																		 
									System.out.print("From: ");
									sender_email_address = writer.readLine();
									
									System.out.print("Subject: ");
									subject = writer.readLine();
									
									System.out.println("Message Body");
									message_body = writer.readLine();
									
									MIME = ("From: "   + sender_email_address  + "\n"   +
											"To: "     + receiver_email_address+ "\n"   +
											"Subject: "+ subject                       + "\n"   +
											"MIME-version: 1.0\n" +
											"Content-Transfer-Encoding: 7bit\n" +
											"Content-Type: text/plain\r\n\n" +
											message_body);
									state = SMTP_State.HELO;
									System.out.println("Socket Status: \nisConnected? : " + socket.isConnected() 
													+  "\nisClosed?: " + socket.isClosed());
									
									out.println("SEND");
									System.out.println("SEND, ENTERING SERVER COMMUNICATION PHASE");
									sendToServer();
									break;
									
									
								case 2:
									break;
								
								case 3:
									showOptions = false;
									retry = true;
									break;
								
								case 4:
									showOptions = false;
									break;
								
							}	
							
						}while(showOptions); 
					}
					
		//***************Getting email**********************
					
				}while(retry);
				
			}catch(IOException e){
				System.out.println(e);
			}
	}

	 /*
	  *    Send a message to the server
	  *
	  */
	void sendToServer(){
		
	
		String replyCode;
		
		while(state != SMTP_State.DONE)
		try
		{
			replyCode = in.readLine();
			if(replyCode != null)
			{
				System.out.println(replyCode);
				switch(state)
				{
				case HELO:	//Verify the initial TCP connection, send reply
					System.out.println("STATE = HELO");
					if(replyCode.substring(0,3).equals("220"))
					{
						state = SMTP_State.MAIL;
						out.println("HELO" + socket.getLocalAddress());
						System.out.println("HELO" + socket.getLocalAddress());
					}
					else
					{
						System.out.println("Error, connection not established.\n" + replyCode);
						state = SMTP_State.DONE;
					}
					break;
							
				case MAIL: //Wait for HELO response
					System.out.println("STATE = MAIL");
					if(replyCode.substring(0,3).equals("250"))
					{
						state = SMTP_State.RCPT;
						System.out.println("MAIL FROM: " + sender_email_address);
						out.println("MAIL FROM: " + sender_email_address);
					}
					else
					{
						System.out.println("Error in HELO response.\n" + replyCode);
						state = SMTP_State.DONE;
					}
					break;
						
				case RCPT: 				
					System.out.println("STATE = RCPT");
					if(replyCode.substring(0,3).equals("250"))
					{
						state = SMTP_State.DATA;
						System.out.println("RCPT TO: " + receiver_email_address);
						out.println("RCPT TO: " + receiver_email_address);
					}
					else
					{
						System.out.println("Error in MAIL response.\n" + replyCode);
						state = SMTP_State.DONE;
					}
					break;
							
				case DATA: //Wait for recipient ok response before sending data
					System.out.println("STATE = DATA");
					if(replyCode.substring(0,3).equals("250"))
					{
						state = SMTP_State.MESSAGE;
						out.println("DATA");
					}
					else
					{
						System.out.println("Error in RCPT response: \n" + replyCode);
						state = SMTP_State.DONE;
					}
					break;
						
				case MESSAGE: //Send formatted message
					System.out.println("STATE = MESSAGE");
					if(replyCode.substring(0,3).equals("354"))
					{
						state = SMTP_State.QUIT;
						out.println(MIME);
						out.println(".");
						System.out.println(".");
					}
					else
					{
						System.out.println("Error in DATA response: \n" + replyCode);
						state = SMTP_State.DONE;
					}
					break;
					
				case QUIT: //Send formatted message
					System.out.println("STATE = QUIT");
					if(replyCode.substring(0,3).equals("250"))
					{
						state = SMTP_State.FINISH;
						out.println("QUIT");
					}
					else
					{
						System.out.println("Error in MESSAGE response.");
						state = SMTP_State.DONE;
					}
					break;
						
				case FINISH: //Final message 
					System.out.println("STATE = FINISH");
					if(replyCode.substring(0,3).equals("221"))
					{
						state = SMTP_State.DONE;
						out.println("QUIT");
					}
					else
					{
						System.out.println("Error in QUIT response.");
						state = SMTP_State.DONE;
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
				Client test = new Client();
				test.getUserInput();
		}
}
