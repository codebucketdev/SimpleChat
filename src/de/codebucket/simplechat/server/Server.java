package de.codebucket.simplechat.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;

import de.codebucket.simplechat.server.Logger;
import de.codebucket.simplechat.server.Commands.Executor;

public class Server implements Runnable 
{
	public List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<Integer> clientResponse = new ArrayList<Integer>();

	public DatagramSocket socket;
	public String address;
	public int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
	private final int MAX_ATTEMPTS = 5;
	
	public String[] motd;
	public String password;
	public boolean needPassword;
	public List<String> bannedUsers = new ArrayList<String>();
	public List<String> bannedAddresses = new ArrayList<String>();
	private String shutdownReason = "Server shutdown.";

	public Server(String address, int port, String password, boolean needPassword) 
	{
		this.address = address;
		this.port = port;
		this.password = password;
		this.needPassword = needPassword;
		loadBanned();
		loadMotd();
		
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
		    	sendToAll("/r/$disconnect:" + shutdownReason);
		    	Logger.log(Level.WARNING, "DSCT: Socket closed!");
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
		manageClients();
		receive();
		
		Logger.log(Level.INFO, "Listening on " + address + ":" + port);
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
				if(cmd.length() != 0 && !cmd.startsWith(" "))
				{
					String[] args = cmd.split(" ");
					String command = args[0];
				    final List<String> list =  new ArrayList<String>();
				    Collections.addAll(list, args); 
				    list.remove(args[0]);
				    args = list.toArray(new String[list.size()]);
					Commands.dispatchCommand(this, command, args, Executor.SERVER);
				}
				else
				{
					Commands.dispatchCommand(this, "", new String[0], Executor.SERVER);
				}
			}
		}
	}
	
	public void stop(String reason)
	{
		this.shutdownReason = reason;
		System.exit(0);
	}
	
	public void loadBanned()
	{
		File file = new File(getWorkingDirectory() + "/banned.txt");
		if(file.exists())
		{
			String[] lines = FileManager.readFile(file);
			
			for(String l : lines)
			{
				if(l.startsWith("username:"))
				{
					bannedUsers.add(l.split(":")[1]);
				}
				else if(l.startsWith("address:"))
				{
					bannedAddresses.add(l.split(":")[1]);
				}
			}
		}
		else
		{
			FileManager.createFile(file);
			FileManager.clearFile(file);
		}
	}
	
	public void saveBanned()
	{
		File file = new File(getWorkingDirectory() + "/banned.txt");
		FileManager.clearFile(file);
		
		List<String> lines = new ArrayList<>();
		for(String username : bannedUsers)
		{
			lines.add("username:"+username);
		}
		for(String address : bannedAddresses)
		{
			lines.add("address:"+address);
		}
		
		String[] write = lines.toArray(new String[lines.size()]);
		FileManager.writeFile(file, write);
	}
	
	public void loadMotd()
	{
		File file = new File(getWorkingDirectory() + "/motd.txt");
		if(file.exists())
		{
			motd = FileManager.readFile(file);
		}
		else
		{
			FileManager.createFile(file);
			FileManager.clearFile(file);
		}
	}
	
	public String getWorkingDirectory()
	{
		String path = "";
		try 
		{
			String classpath = ClassLoader.getSystemClassLoader().getResource(".").getPath();
			path = URLDecoder.decode(classpath, "UTF-8");
		} 
		catch (UnsupportedEncodingException e1) {}
		return path;
	}
	
	public ServerClient getClientByName(String name)
	{
		for (int i = 0; i < clients.size(); i++)
		{
			if (clients.get(i).name.equals(name))
			{
				return clients.get(i);
			}
		}
		
		return null;
	}
	
	public ServerClient getClientByID(int id)
	{
		for (int i = 0; i < clients.size(); i++)
		{
			if (clients.get(i).getID() == id)
			{
				return clients.get(i);
			}
		}
		
		return null;
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

	public void sendToAll(String message) 
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

	public void send(final byte[] data, final InetAddress address, final int port) 
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

	public void send(String message, InetAddress address, int port) 
	{
		message += "/e/";
		send(message.getBytes(), address, port);
	}
	
	public void sendMassToAll(final String[] messages, final InetAddress address, final int port)
	{
		Thread t = new Thread(new Runnable()
		{
	        public void run()
	        {
	        	for(String msg : messages)
	        	{
	        		if (msg.startsWith("/m/")) 
	        		{
	        			String text = msg.substring(3);
	        			text = text.split("/e/")[0];
	        			Logger.log(Level.INFO, text);
	        		}
	        		
	        		for (int i = 0; i < clients.size(); i++) 
	        		{
	        			ServerClient client = clients.get(i);
	        			send(msg.getBytes(), client.address, client.port);
	        		}
	        		
	        		try 
	        		{
						Thread.sleep(5);
					} 
	        		catch (InterruptedException e) 
	        		{
						e.printStackTrace();
					}
	        	}
	        }
		});
		t.start();
	}
	
	public void sendMass(final String[] messages, final InetAddress address, final int port)
	{
		Thread t = new Thread(new Runnable()
		{
	        public void run()
	        {
	        	for(String msg : messages)
	        	{
	        		send(msg, address, port);
	        		try 
	        		{
						Thread.sleep(5);
					} 
	        		catch (InterruptedException e) 
	        		{
						e.printStackTrace();
					}
	        	}
	        }
		});
		t.start();
	}
	
	public String[] convertText(String[] messages)
	{
		List<String> lines = new ArrayList<>();
		for(String msg : messages)
		{
			lines.add("/n/" + msg);
		}
		
		return lines.toArray(new String[lines.size()]);
	}

	private void process(DatagramPacket packet) 
	{
		String string = new String(packet.getData(), 0, packet.getLength());
		try 
		{
			string = URLDecoder.decode(string, "UTF-8");
		} 
		catch (UnsupportedEncodingException e1) {}
		if (string.startsWith("/c/")) 
		{
			String username = string.substring(3, string.length());
			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			
			if(bannedAddress(address.getHostAddress()) == false)
			{
				if(bannedUser(username) == false)
				{
					if(!multipleUser(username))
					{
						if(needPassword == false)
						{					
							int id = UniqueIdentifier.getIdentifier();
							clients.add(new ServerClient(username, address, port, id));
							Logger.log(Level.INFO, "Client " + username + " (ID" + id + ") @ " + address + ":" + port + " connected.");
							send("/c/" + id, address, port);
							
							try 
							{
								Thread.sleep(50);
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
							
							sendToAll("/n/Client " + username + " (ID" + id + ") connected.");
							sendMass(convertText(motd), address, port);
						}
						else
						{
							send("/r/$password=?", packet.getAddress(), packet.getPort());
						}
					}
					else
					{
						send("/r/$invalid:username", address, port);
					}
				}
				else
				{
					send("/r/$banned:username=" + getBanReason(username, false), address, port);
				}
			}
			else
			{
				send("/r/$banned:address=" + getBanReason(address.getHostAddress(), true), address, port);
			}
				
		} 
		else if (string.startsWith("/m/")) 
		{
			String text = string.substring(3);
			String[] users = text.split(":");
			if(check(users[0]))
			{
				sendToAll(string);
			}
		} 
		else if (string.startsWith("/b/"))
		{
			String text = string.substring(3);
			String[] splitted = text.split("=");
			if(check(splitted[0]))
			{
				if(splitted.length > 1)
				{
					String cmd = splitted[1];
					ServerClient c = getClientByName(splitted[0]);
					if(cmd.length() != 0)
					{
						String[] args = cmd.split(" ");
						String command = args[0];
					    final List<String> list =  new ArrayList<String>();
					    Collections.addAll(list, args); 
					    list.remove(args[0]);
					    args = list.toArray(new String[list.size()]);
						Commands.performCommand(this, command, args, Executor.CLIENT, c);
						Logger.log(Level.INFO, c.name + " issued server command: /" + splitted[1]);
					}
					else
					{
						Commands.performCommand(this, "", new String[0], Executor.CLIENT, c);
					}
				}
				else
				{
					ServerClient c = getClientByName(splitted[0]);
					Commands.performCommand(this, "", new String[0], Executor.CLIENT, c);
				}
			}
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
			String[] request = string.split("/c/");
			
			if(request.length > 1)
			{
				String pass = request[0];
				
				if(pass.split(":").length > 1)
				{
					String pw = pass.split(":")[1];
					
					if(pw.equals(password))
					{
						String username = request[1];
						InetAddress address = packet.getAddress();
						int port = packet.getPort();
						
						int id = UniqueIdentifier.getIdentifier();
						clients.add(new ServerClient(username, address, port, id));
						Logger.log(Level.INFO, "Client " + username + " (ID" + id + ") @ " + address + ":" + port + " connected.");
						send("/c/" + id, address, port);
						
						try 
						{
							Thread.sleep(50);
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
						
						sendToAll("/n/Client " + username + " (ID" + id + ") connected.");
						sendMass(convertText(motd), address, port);
					}
					else
					{
						send("/r/$invalid:password", packet.getAddress(), packet.getPort());
					}
				}
				else
				{
					send("/r/$invalid:password", packet.getAddress(), packet.getPort());
				}
			}
		}
		else 
		{
			Logger.log(Level.INFO, string);
		}
	}
	
	public boolean check(String username)
	{
		ServerClient c = getClientByName(username);
		
		if(c != null)
		{
			return true;
		}
		
		return false;
	}
	
	public boolean multipleUser(String username)
	{
		for (int i = 0; i < clients.size(); i++)
		{
			if (clients.get(i).name.equals(username))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean bannedUser(String username)
	{
		for(String u : bannedUsers)
		{
			String[] check = u.split("=");
			String[] user = check[0].split("username:");
			if(user[0].equals(username))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean bannedAddress(String address)
	{
		for(String a : bannedAddresses)
		{
			String[] check = a.split("=");
			String[] addr = check[0].split("address:");
			if(addr[0].equals(address))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public String getBanReason(String search, boolean address)
	{
		if(address == false)
		{
			for(String u : bannedUsers)
			{
				String[] check = u.split("=");
				String[] user = check[0].split("username:");
				if(user[0].equals(search))
				{
					return check[1];
				}
			}
		}
		else
		{
			for(String a : bannedAddresses)
			{
				String[] check = a.split("=");
				String[] addr = check[0].split("address:");
				if(addr[0].equals(search))
				{
					return check[1];
				}
			}
		}
		
		return "Banned from server by operator";
	}

	public void disconnect(int id, boolean status) 
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
			message = "Client " + c.name + " (ID" + c.getID() + ") @ " + c.address.toString() + ":" + c.port + " disconnected.";
			sendToAll("/n/Client " + c.name + " (ID" + c.getID() + ") disconnected.");
		} 
		else 
		{
			message = "Client " + c.name + " (ID" + c.getID() + ") @ " + c.address.toString() + ":" + c.port + " timed out.";
			sendToAll("/n/Client " + c.name + " (ID" + c.getID() + ") timed out.");
		}
		Logger.log(Level.INFO, message);
	}

}
