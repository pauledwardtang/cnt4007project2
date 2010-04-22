import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class MultiServer {

	
	/**
	 *  Runs the main server
	 */
    public static void main(String[] args) throws IOException {
		ArrayList<Account> clients = new ArrayList<Account>();
		clients.add(new Account("paultang", "sillyface"));
		clients.add(new Account("markramasco", "sillyface"));
        ServerSocket serverSocket = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(25);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 25.");
            System.exit(-1);
        }

        while (listening)
	    new ServerThread(serverSocket.accept(), clients).start();

        serverSocket.close();
    }
}
