package de.codebucket.simplechat.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.logging.Level;

public class Client 
{
	private DatagramSocket socket;

	private String name, address;
	private int port;
	private InetAddress ip;
	private Thread send;
	private int ID = -1;
	private int attempt = 0;

	public Client(String name, String address, int port) 
	{
		this.name = name;
		this.address = address;
		this.port = port;
	}

	public String getName() 
	{
		return name;
	}

	public String getAddress() 
	{
		return address;
	}

	public int getPort() 
	{
		return port;
	}
	
	public int getAttempts()
	{
		return attempt;
	}
	
	public void setAttempts(int attempts)
	{
		attempt = attempts;
	}
	
	public void resetAttempts()
	{
		attempt = 0;
	}

	public boolean openConnection(String address, int port) 
	{
		try 
		{
			socket = new DatagramSocket();
			ip = InetAddress.getByName(address);
		} 
		catch (UnknownHostException e) 
		{
			Logger.log(Level.SEVERE, "Error while connecting to server " + getAddress() + ":" + getPort() + "!");
			return false;
		} 
		catch (SocketException e) 
		{
			Logger.log(Level.SEVERE, "Error while connecting to server " + getAddress() + ":" + getPort() + "!");
			return false;
		}
		return true;
	}

	public String receive() 
	{
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try 
		{
			socket.receive(packet);
		} 
		catch (IOException e) 
		{
			Logger.log(Level.SEVERE, "Error while receiving data from server " + getAddress() + ":" + getPort() + "!");
		}
		String message = new String(packet.getData(), 0, packet.getLength());
		try 
		{
			message = URLDecoder.decode(message, "UTF-8");
		} 
		catch (UnsupportedEncodingException e1) {}
		return message;
	}

	public void send(final byte[] data) 
	{
		send = new Thread("Send") 
		{
			public void run() 
			{
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try 
				{
					socket.send(packet);
				} 
				catch (IOException e) 
				{
					Logger.log(Level.SEVERE, "Error while sending data to server " + getAddress() + ":" + getPort() + "!");
				}
			}
		};
		send.start();
	}

	public void close() 
	{
		new Thread() 
		{
			public void run()
			{
				synchronized (socket) 
				{
					socket.close();
				}
			}
		}.start();
	}

	public void setID(int ID) 
	{
		this.ID = ID;
	}

	public int getID() 
	{
		return ID;
	}

}