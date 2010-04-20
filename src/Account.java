/*
 * Authors: Paul Tang, Mark Ramasco
 * CNT4007 Project 2 
 */


//*******************************Account*******************************
/*
 * Accounts on the server.
*/
 public class Account
{
	private   String username;
	private   String password;
	
	public Account(String name, String password)
	{
		this.name = name;
		this.password = password;
	}	
	
	String getUserName()
	{
		return username;
	}
	
	String getPassword()
	{
		return password;
	}
	
}
