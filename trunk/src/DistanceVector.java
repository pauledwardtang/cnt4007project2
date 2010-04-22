/*
 * Authors: Paul Tang, Mark Ramasco
 * CNT4007 Project 2 Part 2 
 */
import java.util.ArrayList;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

//*******************************DistanceVector*******************************
/*
 * 
*/
 public class DistanceVector
 {
		
		String IP = "10.128.83.134";
		DatagramSocket[] udpVector = new DatagramSocket[5];
		boolean[] neighbor = new boolean[5];
		byte[][] values  = new byte[5][5];
		byte[][] nextHop = new byte[5][5];
		public int index;
			 
		public DistanceVector(String config, String link)
		{
			initializeVectors(getRowToParse(config, link));
			/*
			System.out.println("Vectors: \n");
			for(int i = 0; i < 5; i++)
			{
				if(udpVector[i] != null)
					System.out.print(udpVector[i].getLocalPort() + " ");
				else
					System.out.print("00 ");
			}
			
			System.out.println("\nValues: \n");
			for(int i = 0; i < 5; i++)
			{
				System.out.print(values[index][i] + " ");
			}
			*/
			//System.out.println(udpVector[index].getLocalAddress());
		}
		
		//Initializes the Datagram sockets and values
		private void initializeVectors(String row)
		{
			for(int i = 0; i < 5; i++)
				for(int j = 0; j < 5; j++)
					values[i][j] = 0;
			
			//Set the broadcast socket value
			String[] split = row.substring(0,8).split("\\s");
			try{
				udpVector[index] = new DatagramSocket(Integer.parseInt(split[1]));
			}catch(IOException e){};
			
			String neighbors = row.substring(8);
			split = neighbors.split("\\s");
			String dest;
			//i = link#, i+1 = PORT, i+2 = distance
			for(int i = 0; i < split.length; i+=3)
			{
				int loc = magic(split[i]);
				try
				{
					udpVector[loc] = new DatagramSocket(Integer.parseInt(split[i+1]));
				}catch(IOException e){}
				values[index][loc] = Byte.parseByte(split[i+2]);
				neighbor[loc] = true;
			}
		}
		
		private int magic(String link)
		{
			if(link.equals("A"))
				return 0;
			else if(link.equals("B"))
				return 1;
			else if(link.equals("C"))
				return 2;
			else if(link.equals("D"))
				return 3;
			else if(link.equals("E"))
				return 4;
			else
				return 5;
		}
		//		gets the row!
		private String getRowToParse(String config, String arg)
		{
			String str = null;
			int count = 0;
			BufferedReader reader = new BufferedReader(new StringReader(config));
			try 
			{
			  while ((str = reader.readLine()) != null) 
			  {
				if(str.substring(0,1).equals(arg))
				{
					index = count;//We will use it for indexing
					return str;
				}
				count++;
			}

			} catch(IOException e) {
			  e.printStackTrace();
			}
			return null;
		}
		
		public void printVector(byte[] vector)
		{	
			for(int i = 0; i < 5; i++)
			{
				System.out.print(values[index][i] + " ");
			}
		}
		//Puts the updated DistanceVector onto the socket
		public void broadcast()
		{

			
			//DatagramPacket temp = new DatagramPacket(buf, buf.length, udpVector[index].getLocalAddress(), 
			//										 udpVector[index].getLocalPort());
			
			//temp.setPort(udpVector[index].getLocalPort());
			try{
				udpVector[index].send(new DatagramPacket(values[index], values[index].length, udpVector[index].getLocalAddress(), 
													 udpVector[index].getLocalPort()));
				//udpVector[index].send(temp );
				//(new DatagramSocket(25)).send();
			}catch(IOException e){}
		}
		
		/*
		 * @param loc Receives the vector at index loc and stores it in array
		 * 
		 * */
		public void listen(int neighbor)
		{
			byte[] buf = new byte[5];
			DatagramPacket temp = new DatagramPacket(buf, buf.length, udpVector[index].getLocalAddress(), 
													 udpVector[index].getLocalPort());
			try{
				udpVector[neighbor].receive(temp);
			}catch(IOException e){}			
			values[neighbor] = buf;
			printVector(values[neighbor]);
			
		}
		public boolean isNeighbor(int val)
		{
			return neighbor[val];
		}
		
		
		public static void main(String[] args)
		{
			DistanceVector router = new DistanceVector(
					  "A 25000 B 26025 5 D 25003 4\n"   +
					  "B 26025 A 25000 5 C 25002 3 D 25003 2\n"   +
					  "C 25002 B 25001 3 E 25004 3\n"   +
					  "D 25003 A 25000 4 B 25001 2 E 25004 1\n" +
					  "E 25004 C 25002 3 D 25003 1", args[0]);
			
			while(true)
			{
				/*
				router.broadcast();
				if(router.index == 0)
					router.listen(1);
				else
					router.listen(0);
				*/
				
				
				int min;
				//Infinite vals where??
				for(int i = 0; i < 5; i++)
				{
					min = 100; 
					router.listen(i);
					for(int j = 0; j < 5; j++)
					{
						if(router.isNeighbor(i))
						{
							if(min > router.values[router.index][j] + router.values[j][i])
							{
								min = router.values[router.index][j] + router.values[j][i];
								router.broadcast();
							}
								
						}
					}
				}
				
			}
		}
 }
 
 
