/*
 * Authors: Paul Tang, Mark Ramasco
 * CNT4007 Project 2 
 */
import java.net.*;
import java.io.*;
import java.util.Random;
import java.lang.Math;

//*******************************Client*******************************
//*1 socket between authentication server

 public class Client{
//---********Variables*********---

//Enumeration for SMTP State machine
private enum SMTP_State { HELO, MAIL, RCPT, DATA, MESSAGE, QUIT, FINISH }

//IP Addresses for the servers
String SERVER1_IP = "10.128.82.162";
String SERVER2_IP = "127.0.0.1";

private Socket socket, authenticationSocket;
private String receiver_email_address, sender_email_address;
private String subject;
private String message_body;
private String MIME;
private Random rand;
private String temp;

//Readers Writers

private BufferedReader in;
private PrintWriter out;

private SMTP_State state;


//---********Constructor*********---
/*
 * This constructor is for testing: the message is given in command line arguments
 * 
 * */
	Client()
	{
				 
		rand = new Random();
		state = SMTP_State.HELO;
		//constructSocket();
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
				authenticationSocket = new Socket(SERVER1_IP, 4444);
			}
			else
			{
				socket = new Socket(SERVER1_IP, 25);
				authenticationSocket = new Socket(SERVER1_IP, 4444);
			}
				
		
		// Creating readers/writers
		in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		} catch (MalformedURLException e){
			System.out.println("Error: " + e);
		} catch (IOException e){
			System.out.println("Error: " + e);
		}
		
		//For testing purposes, closing the socket right away (plz delete)
		//socket.close();
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
		String authentication;
		String retryTemp;
		boolean retry = false;
		boolean authFlag = false;
		try
		{
			do
			{
				BufferedReader writer = new BufferedReader(new InputStreamReader(System.in));
				
				System.out.print("Username: ");
				authentication = writer.readLine() + " ";
				
				System.out.print("Password: ");
				authentication.concat(writer.readLine());
				System.out.println();
				
				authFlag = sendAuthentication(authentication);
				if(authFlag == false)
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
				
			}while(retry);
			
		}catch(IOException e){
			System.out.println(e);
		}
}

 /*
  *    Send authentication data to the server
  *
  */
boolean sendAuthentication(String auth)
{
		return false;
}

 /*
  *    Construct an email message using MIME. Alternatively, could push out one line at a time to the server so we can avoid parsing
  * 	a bunch of text.
  *
  */
void construct_EMAIL(String receiver_email_address, String sender_email_address, String  subject, String main_text){
	
	MIME = ("From: "   + sender_email_address  + "\n"   +
			"To: "     + receiver_email_address+ "\n"   +
			"Subject: "+ subject			   + "\n"   +
			"MIME-version: 1.0\n" +
			"Content-Transfer-Encoding: 7bit\n" +
			"Content-Type: text/plain\r\n\n" +
			"Test Message."	
		   );
	
	String replyCode;
	
	while(state != SMTP_State.FINISH)
	try
	{
		replyCode = in.readLine();
		if(replyCode != null)
		{
			System.out.println(replyCode);
			switch(state)
			{
			case HELO:	//Verify the initial TCP connection, send reply
				if(replyCode.substring(0,3).equals("220"))
				{
					state = SMTP_State.MAIL;
					out.println("HELO" + socket.getLocalAddress());
					System.out.println("HELO" + socket.getLocalAddress());
				}
				else
				{
					System.out.println("Error, connection not established.\n" + replyCode);
					state = SMTP_State.FINISH;
				}
				break;
						
			case MAIL: //Wait for HELO response
				if(replyCode.substring(0,3).equals("250"))
				{
					state = SMTP_State.RCPT;
					System.out.println("MAIL FROM: " + sender_email_address);
					out.println("MAIL FROM: " + sender_email_address);
				}
				else
				{
					System.out.println("Error in HELO response.\n" + replyCode);
					state = SMTP_State.FINISH;
				}
				break;
					
			case RCPT: 				
				if(replyCode.substring(0,3).equals("250"))
				{
					state = SMTP_State.DATA;
					System.out.println("RCPT TO: " + receiver_email_address);
					out.println("RCPT TO: " + receiver_email_address);
				}
				else
				{
					System.out.println("Error in MAIL response.\n" + replyCode);
					state = SMTP_State.FINISH;
				}
				break;
						
			case DATA: //Wait for recipient ok response before sending data
				if(replyCode.substring(0,3).equals("250"))
				{
					state = SMTP_State.MESSAGE;
					out.println("DATA");
					System.out.println("DATA");
				}
				else
				{
					System.out.println("Error in RCPT response: \n" + replyCode);
					state = SMTP_State.FINISH;
				}
				break;
					
			case MESSAGE: //Send formatted message
				if(replyCode.substring(0,3).equals("354"))
				{
					state = SMTP_State.QUIT;
					out.println(MIME);
					out.println(".");
					System.out.println(".");
				}
				else
				{
					System.out.println("Error in RCPT response: \n" + replyCode);
					state = SMTP_State.FINISH;
				}
				break;
				
			case QUIT: //Send formatted message
				if(replyCode.substring(0,3).equals("250"))
				{
					state = SMTP_State.FINISH;
					out.println("QUIT");
					System.out.println("QUIT");
				}
				else
				{
					System.out.println("Error in RCPT response.");
					state = SMTP_State.FINISH;
				}
				break;
					
			case FINISH: //Close the socket
				socket.close();
				out.close();
				in.close();
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
		test.construct_EMAIL(args[0], args[1], args[2], args[3]);
}
}
