package de.codebucket.simplechat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;

import de.codebucket.simplechat.client.Logger;

public class Server implements Runnable 
{
	private List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<Integer> clientResponse = new ArrayList<Integer>();

	private DatagramSocket socket;
	private String address;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
	private final int MAX_ATTEMPTS = 5;

	public Server(String address, int port) 
	{
		this.address = address;
		this.port = port;
		try 
		{
			socket = new DatagramSocket(port);
		} 
		catch (SocketException e) 
		{
			e.printStackTrace();
			return;
		}
		run = new Thread(this, "Server");
		run.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() 
		{
			 
		    @SuppressWarnings("deprecation")
			public void run() 
		    {
		    	sendToAll("/r/$disconnect:Server shutdown.");
				Logger.log(Level.INFO, "Closing listening thread");
				running = false;
				manage.stop();
				receive.stop();
				
				Logger.log(Level.INFO, "Stopping SimpleChat Server..");
		    }
		}));
	}

	@SuppressWarnings("resource")
	public void run() 
	{
		running = true;
		Logger.log(Level.INFO, "Listening on " + address + ":" + port);
		manageClients();
		receive();
		Scanner scanner = new Scanner(System.in);
		while (running) 
		{
			String cmd = null;
			
			try
			{
				cmd = scanner.nextLine();
			}
			catch(NoSuchElementException e) {}
			
			if(cmd != null)
			{
				if (!cmd.startsWith("/")) 
				{
					if(cmd.length() != 0) sendToAll("/m/Console: " + cmd + "/e/");
					continue;
				}
				
				cmd = cmd.substring(1);
				if (cmd.equals("stop")) 
				{
					System.exit(0);
				} 
				else if (cmd.equals("kickall")) 
				{
					sendToAll("/r/$disconnect:Kicked by operator.");
				} 
				else if (cmd.equals("list")) 
				{
					Logger.log(Level.INFO, "Clients:");
					Logger.log(Level.INFO, "========");
					for (int i = 0; i < clients.size(); i++) 
					{
						ServerClient c = clients.get(i);
						Logger.log(Level.INFO, c.name + "(" + c.getID() + "): " + c.address.toString() + ":" + c.port);
					}
					Logger.log(Level.INFO, "========");
				}
			}
		}
	}

	private void manageClients() 
	{
		manage = new Thread("Manage") 
		{
			public void run() 
			{
				while (running) 
				{
					sendToAll("/i/server");
					try 
					{
						Thread.sleep(2000);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
					for (int i = 0; i < clients.size(); i++) 
					{
						ServerClient c = clients.get(i);
						if (!clientResponse.contains(c.getID())) 
						{
							if (c.attempt >= MAX_ATTEMPTS) 
							{
								disconnect(c.getID(), false);
							} else 
							{
								c.attempt++;
							}
						} 
						else 
						{
							clientResponse.remove(new Integer(c.getID()));
							c.attempt = 0;
						}
					}
				}
			}
		};
		manage.start();
	}

	private void receive() 
	{
		receive = new Thread("Receive")
		{
			public void run() 
			{
				while (running) 
				{
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try 
					{
						socket.receive(packet);
					} 
					catch (IOException e) 
					{
						Logger.log(Level.SEVERE, "Error while receiving data from client " + packet.getAddress() + ":" + packet.getPort() + "!");
					}
					process(packet);
				}
			}
		};
		receive.start();
	}

	private void sendToAll(String message) 
	{
		if (message.startsWith("/m/")) 
		{
			String text = message.substring(3);
			text = text.split("/e/")[0];
			Logger.log(Level.INFO, text);
		}
		for (int i = 0; i < clients.size(); i++) 
		{
			ServerClient client = clients.get(i);
			send(message.getBytes(), client.address, client.port);
		}
	}

	private void send(final byte[] data, final InetAddress address, final int port) 
	{
		send = new Thread("Send") 
		{
			public void run() 
			{
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				try 
				{
					socket.send(packet);
				} 
				catch (IOException e) 
				{
					Logger.log(Level.SEVERE, "Error while sending data to client " + packet.getAddress() + ":" + packet.getPort() + "!");
				}
			}
		};
		send.start();
	}

	private void send(String message, InetAddress address, int port) 
	{
		message += "/e/";
		send(message.getBytes(), address, port);
	}

	private void process(DatagramPacket packet) 
	{
		String string = new String(packet.getData(), 0, packet.getLength());
		if (string.startsWith("/c/")) 
		{
			int id = UniqueIdentifier.getIdentifier();
			clients.add(new ServerClient(string.substring(3, string.length()), packet.getAddress(), packet.getPort(), id));
			Logger.log(Level.INFO, "Client " + string.substring(3, string.length()) + " (" + id + ") @ " + packet.getAddress() + ":" + packet.getPort() + " connected.");
			String ID = "/c/" + id;
			send(ID, packet.getAddress(), packet.getPort());
			sendToAll("/n/Client " + string.substring(3, string.length()) + " connected.");
		} 
		else if (string.startsWith("/m/")) 
		{
			sendToAll(string);
		} 
		else if (string.startsWith("/d/")) 
		{
			String id = string.split("/d/|/e/")[1];
			disconnect(Integer.parseInt(id), true);
		} 
		else if (string.startsWith("/i/")) 
		{
			clientResponse.add(Integer.parseInt(string.split("/i/|/e/")[1]));
		} 
		else if (string.startsWith("/r/$password:")) 
		{
			//TODO PASSWORT AUTH
		}
		else 
		{
			Logger.log(Level.INFO, string);
		}
	}

	private void disconnect(int id, boolean status) 
	{
		ServerClient c = null;
		for (int i = 0; i < clients.size(); i++)
		{
			if (clients.get(i).getID() == id)
			{
				c = clients.get(i);
				clients.remove(i);
				break;
			}
		}
		String message = "";
		if (status) 
		{
			message = "Client " + c.name + " (" + c.getID() + ") @ " + c.address.toString() + ":" + c.port + " disconnected.";
			sendToAll("/n/Client " + c.name + " (" + c.getID() + ") disconnected.");
		} 
		else 
		{
			message = "Client " + c.name + " (" + c.getID() + ") @ " + c.address.toString() + ":" + c.port + " timed out.";
			sendToAll("/n/Client " + c.name + " (" + c.getID() + ") timed out.");
		}
		Logger.log(Level.INFO, message);
	}

}
