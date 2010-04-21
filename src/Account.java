/*
 * Authors: Paul Tang, Mark Ramasco
 * CNT4007 Project 2 
 */
import java.util.ArrayList;


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
			 subject = parse(message);
		 }
		 //Fist latersc
		 private String parse(String message)
		 {
			 return message;
		 }
		 
	 }
}
