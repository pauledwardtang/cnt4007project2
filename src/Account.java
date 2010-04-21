/*
 * Authors: Paul Tang, Mark Ramasco
 * CNT4007 Project 2 
 */
import java.util.ArrayList;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

//*******************************Account*******************************
/*
 * Accounts on the server.
*/
 public class Account
{
	private   String username;
	private   String password;
	private   ArrayList<Mail> inbox;
	private   int count;
	
	public Account(String username, String password)
	{
		this.username = username;
		this.password = password;
		inbox = new ArrayList<Mail>();
		count = 0;
	}	
	
	public String getUserName()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public ArrayList<Mail> getInbox()
	{
		return inbox;
	}
	
	public void addMail(String message)
	{
		inbox.add(new Mail(count, message));
		count++;
	}
	
	public void deleteMail(int mailNum)
	{
		inbox.remove(mailNum);
	}
	
	private class Mail
	 {
		 public String message;
		 public String subject;
		 public int mailNum;
		 
		 private Mail(int mailNum, String message)
		 {
			 this.mailNum = mailNum;
			 this.message = message;
			 subject = parseSubject();
		 }
		 /*
		  *	Extracts the subject line from the MIME formatted essage header 
		  */
		 private String parseSubject()
		 {
			String str = null;
			BufferedReader reader = new BufferedReader(new StringReader(message));
			try 
			{
			  while ((str = reader.readLine()) != null) 
				if (str.contains("Subject: "))
					return str;

			} catch(IOException e) {
			  e.printStackTrace();
			}
			return str;
		}		
		String getMessage()
		{
			return message;
		}			 
	 }
	public static void main(String[] args)
	 {
		 Account parseTest = new Account("paul", "password");
		 parseTest.addMail("To: Mark\nFrom: Paul\nSubject: Sillyface\nContent-Type: Whocares?");
		 parseTest.addMail("To: Allison\nFrom: Paul\nSubject: Hello :3\nContent-Type: Whocares?");
		 parseTest.addMail("To: Stephen\nFrom: Paul\nSubject: Hey mang\nContent-Type: Whocares?");
		 
		 for(int i = 0; i < parseTest.inbox.size(); i++)
		 {
			 System.out.println(parseTest.inbox.get(i).getMessage()+"\n");
		 }
	 }
}
