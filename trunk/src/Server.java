import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;


public class Server {

	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out;
	BufferedReader in;
	boolean clientInput = true;
	ReplyState state = ReplyState.NEXT;
	
	private enum ReplyState
	{
		INIT, NEXT, DATA, QUIT
	}
	void receiveConnection()
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
			out.println("220 " + clientSocket.getLocalAddress() );
			while(!(state == ReplyState.QUIT))
			{
				String fromClient = in.readLine();				
				if(fromClient == null){}
						
				else if(fromClient != null)
				{
					if(state == ReplyState.NEXT)
					{
						out.println(messageFSM(fromClient));
					}
					else if(state == ReplyState.DATA)
					{
						if(fromClient.equals("."))
						{
							out.println(messageFSM(fromClient));
						}		
					}
					System.out.println(fromClient);
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
		System.out.println("Connection closed");
	}
	void socketClose()
	{
		//close socket
		try {
			clientSocket.close();
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * State Machine to parse commands and reply with proper Reply code
	 */
	public String messageFSM(String message)
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
				reply = QUIT + " " + clientSocket.getLocalAddress() + " closing connection.";
				state = ReplyState.QUIT;
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
		}
		return reply;
	}
	public static void main(String[] args)
	{
		Server s = new Server();
		s.receiveConnection();
	}
}
