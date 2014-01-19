package de.codebucket.simplechat.server;

import java.util.List;
import java.util.logging.Level;

public class Commands 
{
	public enum Executor
	{
		CLIENT,
		SERVER;
	}
	
	public static void dispatchCommand(Server s, String cmd, String[] args, Executor exec)
	{
		if (cmd.equalsIgnoreCase("stop")) 
		{
			if(args.length == 0)
			{
				s.stop("Server shutdown.");
			}
			else
			{
				s.stop(getText(args, 0));
			}
		}
		else if (cmd.equalsIgnoreCase("reload"))
		{
			Logger.log(Level.INFO, "Console: Reloading server..");
			s.saveBanned();
			s.loadBanned();
			s.loadMotd();
			Logger.log(Level.INFO, "Console: Reload complete.");
		}
		else if (cmd.equalsIgnoreCase("kick")) 
		{
			if(args.length != 0)
			{
				if(isInteger(args[0]))
				{
					ServerClient c = s.getClientByID(Integer.parseInt(args[0]));
					
					if(c != null)
					{
						if(args.length > 1)
						{
							Logger.log(Level.INFO, "Kicked client " + c.name + ": " + getText(args, 1));
							s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
						}
						else
						{
							Logger.log(Level.INFO, "Kicked client " + c.name + ": Kicked by operator.");
							s.send("/r/$disconnect:Kicked by operator.", c.address, c.port);
						}
					}
					else
					{
						Logger.log(Level.INFO, "Client with ID " + args[0] + " is not online!");
					}
				}
				else
				{
					ServerClient c = s.getClientByName(args[0]);
					
					if(c != null)
					{
						if(args.length > 1)
						{
							Logger.log(Level.INFO, "Kicked client " + c.name + ": " + getText(args, 1));
							s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
						}
						else
						{
							Logger.log(Level.INFO, "Kicked client " + c.name + ": Kicked by operator.");
							s.send("/r/$disconnect:Kicked by operator.", c.address, c.port);
						}
					}
					else
					{
						Logger.log(Level.INFO, "Client " + args[0] + " is not online!");
					}
				}
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: kick <username|ID> [reason]");
			}
		} 
		else if (cmd.equalsIgnoreCase("kickall")) 
		{
			if(args.length == 0)
			{
				Logger.log(Level.INFO, "Kicked all clients: Kicked by operator.");
				s.sendToAll("/r/$disconnect:Kicked by operator.");
			}
			else
			{
				Logger.log(Level.INFO, "Kicked all clients: " + getText(args, 1));
				s.sendToAll("/r/$disconnect:" + getText(args, 0));
			}
		} 
		else if (cmd.equalsIgnoreCase("ban")) 
		{
			if(args.length != 0)
			{
				if(isInteger(args[0]))
				{
					ServerClient c = s.getClientByID(Integer.parseInt(args[0]));
					
					if(c != null)
					{
						if(s.bannedUser(c.name) == false)
						{
							if(args.length > 1)
							{
								s.bannedUsers.add(c.name + "=" + getText(args, 1));
								Logger.log(Level.INFO, "Banned client " + c.name + ": " + getText(args, 1));
								s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
								s.saveBanned();
							}
							else
							{
								s.bannedUsers.add(c.name + "=Banned from server by operator.");
								Logger.log(Level.INFO, "Banned client " + c.name + ": Banned from server by operator.");
								s.send("/r/$disconnect:Banned from server by operator.", c.address, c.port);
								s.saveBanned();
							}
						}
						else
						{
							Logger.log(Level.INFO, "Client " + c.name + " already banned!");
						}
					}
					else
					{
						Logger.log(Level.INFO, "Client with ID " + args[0] + " is not online!");
					}
				}
				else
				{
					ServerClient c = s.getClientByName(args[0]);
					
					if(c != null)
					{
						if(s.bannedUser(c.name) == false)
						{
							if(args.length > 1)
							{
								s.bannedUsers.add(c.name + "=" + getText(args, 1));
								Logger.log(Level.INFO, "Banned client " + c.name + ": " + getText(args, 1));
								s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
								s.saveBanned();
							}
							else
							{
								s.bannedUsers.add(c.name + "=Banned from server by operator.");
								Logger.log(Level.INFO, "Banned client " + c.name + ": Banned from server by operator.");
								s.send("/r/$disconnect:Banned from server by operator.", c.address, c.port);
								s.saveBanned();
							}
						}
						else
						{
							Logger.log(Level.INFO, "Client " + c.name + " already banned!");
						}
					}
					else
					{
						if(s.bannedUser(args[0]) == false)
						{
							if(args.length > 1)
							{
								s.bannedUsers.add(args[0] + "=" + getText(args, 1));
								Logger.log(Level.INFO, "Banned client " + args[0] + ": " + getText(args, 1));
								s.saveBanned();
							}
							else
							{
								s.bannedUsers.add(args[0] + "=Banned from server by operator.");
								Logger.log(Level.INFO, "Banned client " + args[0] + ": Banned from server by operator.");
								s.saveBanned();
							}
						}
						else
						{
							Logger.log(Level.INFO, "Client " + args[0] + " already banned!");
						}
					}
				}
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: ban <username|ID> [reason]");
			}
		} 
		else if (cmd.equalsIgnoreCase("ban-ip")) 
		{
			if(args.length != 0)
			{
				if(isInteger(args[0]))
				{
					ServerClient c = s.getClientByID(Integer.parseInt(args[0]));
					
					if(c != null)
					{
						if(s.bannedAddress(c.address.getHostAddress()) == false)
						{
							if(args.length > 1)
							{
								s.bannedAddresses.add(c.address.getHostAddress() + "=" + getText(args, 1));
								Logger.log(Level.INFO, "Banned address " + c.address.getHostAddress() + ": " + getText(args, 1));
								s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
								s.saveBanned();
							}
							else
							{
								s.bannedAddresses.add(c.address.getHostAddress() + "=Banned from server by operator.");
								Logger.log(Level.INFO, "Banned address " + c.address.getHostAddress() + ": Banned from server by operator.");
								s.send("/r/$disconnect:Banned from server by operator.", c.address, c.port);
								s.saveBanned();
							}
						}
						else
						{
							Logger.log(Level.INFO, "Address " + c.address.getHostAddress() + " already banned!");
						}
					}
					else
					{
						Logger.log(Level.INFO, "Client with ID " + args[0] + " is not online!");
					}
				}
				else
				{
					ServerClient c = s.getClientByName(args[0]);
					
					if(c != null)
					{
						if(s.bannedAddress(c.address.getHostAddress()) == false)
						{
							if(args.length > 1)
							{
								s.bannedAddresses.add(c.address.getHostAddress() + "=" + getText(args, 1));
								Logger.log(Level.INFO, "Banned address " + c.address.getHostAddress() + ": " + getText(args, 1));
								s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
								s.saveBanned();
							}
							else
							{
								s.bannedAddresses.add(c.address.getHostAddress() + "=Banned from server by operator.");
								Logger.log(Level.INFO, "Banned address " + c.address.getHostAddress() + ": Banned from server by operator.");
								s.send("/r/$disconnect:Banned from server by operator.", c.address, c.port);
								s.saveBanned();
							}
						}
						else
						{
							Logger.log(Level.INFO, "Address " + c.address.getHostAddress() + " already banned!");
						}
					}
					else
					{
						if(s.bannedAddress(args[0]) == false)
						{
							if(args.length > 1)
							{
								s.bannedAddresses.add(args[0] + "=" + getText(args, 1));
								Logger.log(Level.INFO, "Banned address " + args[0] + ": " + getText(args, 1));
								s.saveBanned();
							}
							else
							{
								s.bannedAddresses.add(args[0] + "=Banned from server by operator.");
								Logger.log(Level.INFO, "Banned address " + args[0] + ": Banned from server by operator.");
								s.saveBanned();
							}
						}
						else
						{
							Logger.log(Level.INFO, "Address " + args[0] + " already banned!");
						}
					}
				}
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: ban-ip <username|ID|address> [reason]");
			}
		} 		
		else if (cmd.equalsIgnoreCase("unban")) 
		{
			if(args.length != 0)
			{
				if(s.bannedUser(args[0]) == true)
				{
					for (int i = 0; i < s.bannedUsers.size(); i++) 
					{
						String us = s.bannedUsers.get(i);
						String[] check = us.split("=");
						if(check[0].equals(args[0]))
						{
							s.bannedUsers.remove(i);
							Logger.log(Level.INFO, "Unbanned client " + args[0] + " by operator.");
							s.saveBanned();
							break;
						}
					}
				}
				else
				{
					Logger.log(Level.INFO, "Client " + args[0] + " is not banned!");
				}
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: unban <username>");
			}
		} 
		else if (cmd.equalsIgnoreCase("unban-ip")) 
		{
			if(args.length != 0)
			{
				if(s.bannedAddress(args[0]) == true)
				{
					for (int i = 0; i < s.bannedAddresses.size(); i++) 
					{
						String us = s.bannedAddresses.get(i);
						String[] check = us.split("=");
						if(check[0].equals(args[0]))
						{
							s.bannedAddresses.remove(i);
							Logger.log(Level.INFO, "Unbanned address " + args[0] + " by operator.");
							s.saveBanned();
							break;
						}
					}
				}
				else
				{
					Logger.log(Level.INFO, "Address " + args[0] + " is not banned!");
				}
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: unban-ip <address>");
			}
		} 	
		else if (cmd.equalsIgnoreCase("list")) 
		{
			Logger.log(Level.INFO, "There are " + s.clients.size() + "/128 clients online:");
			Logger.log(Level.INFO, getClientList(s.clients, 0));
		}
		else if (cmd.equalsIgnoreCase("pw")) 
		{
			if(args.length != 0) 
			{
				if(args[0].equalsIgnoreCase("on"))
				{
					if(s.needPassword == false)
					{
						s.needPassword = true;
						Logger.log(Level.INFO, "Turned on password authentication.");
					}
					else
					{
						Logger.log(Level.INFO, "Password authentication already turned on!");
					}
				}
				else if(args[0].equalsIgnoreCase("off"))
				{
					if(s.needPassword == true)
					{
						s.needPassword = false;
						Logger.log(Level.INFO, "Turned off password authentication.");
					}
					else
					{
						Logger.log(Level.INFO, "Password authentication already turned off!");
					}
				}
				else if(args[0].equalsIgnoreCase("set"))
				{
					if(args.length > 1)
					{
						String pw = args[1];
						s.password = pw;
						Logger.log(Level.INFO, "Set authentication password to \"" + pw + "\"");
					}
					else
					{
						Logger.log(Level.INFO, "Not enough arguments. Usage: pw <on|off|set> [password]");
					}
				}
				else
				{
					Logger.log(Level.INFO, "Bad usage. Usage: pw <on|off|set> [password]");
				}
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: pw <on|off|set> [password]");
			}
		}
		else if (cmd.equalsIgnoreCase("say"))
		{
			String msg = getText(args, 0);
			if(msg.length() != 0) 
			{
				s.sendToAll("/m/Console: " + msg);
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: say <message>");
			}
		}
		else if (cmd.equalsIgnoreCase("me"))
		{
			String msg = getText(args, 0);
			if(msg.length() != 0) 
			{
				s.sendToAll("/m/Console " + msg);
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: me <message>");
			}
		}
		else if (cmd.equalsIgnoreCase("tell") || cmd.equalsIgnoreCase("msg"))
		{
			if(args.length > 1)
			{
				if(isInteger(args[0]))
				{
					ServerClient c = s.getClientByID(Integer.parseInt(args[0]));
					
					if(c != null)
					{
						Logger.log(Level.INFO, "[Console -> " + c.name + "] " + getText(args, 1));
						s.send("/n/Console whispers you: " +  getText(args, 1), c.address, c.port);
					}
					else
					{
						Logger.log(Level.INFO, "Client with ID " + args[0] + " is not online!");
					}
				}
				else
				{
					ServerClient c = s.getClientByName(args[0]);
					
					if(c != null)
					{
						Logger.log(Level.INFO, "[Console -> " + c.name + "] " + getText(args, 1));
						s.send("/n/Console whispers you: " +  getText(args, 1), c.address, c.port);
					}
					else
					{
						Logger.log(Level.INFO, "Client " + args[0] + " is not online!");
					}
				}
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: tell <username|ID> <message>");
			}
		}
		else if (cmd.equalsIgnoreCase("poke"))
		{
			if(args.length > 1)
			{
				if(isInteger(args[0]))
				{
					ServerClient c = s.getClientByID(Integer.parseInt(args[0]));
					
					if(c != null)
					{
						Logger.log(Level.INFO, "Poked client " + c.name + ": " + getText(args, 1));
						s.send("/r/$poke=Console/m/" + getText(args, 1), c.address, c.port);
					}
					else
					{
						Logger.log(Level.INFO, "Client with ID " + args[0] + " is not online!");
					}
				}
				else
				{
					ServerClient c = s.getClientByName(args[0]);
					
					if(c != null)
					{
						Logger.log(Level.INFO, "Poked client " + c.name + ": " + getText(args, 1));
						s.send("/r/$poke=Console/m/" + getText(args, 1), c.address, c.port);
					}
					else
					{
						Logger.log(Level.INFO, "Client " + args[0] + " is not online!");
					}
				}
			}
			else
			{
				Logger.log(Level.INFO, "Bad usage. Usage: poke <username|ID> <message>");
			}
		}
		else if (cmd.equalsIgnoreCase("help"))
		{
			String[] send = { "Available commands for console: ", "stop: Stops the server.", "refresh: Loads the ban list and Motd new.", "say: Writes a message in console.", 
					"kick: Removes a user from the server.", "kickall: Removes all from the server.", "ban: Bans a user from the server.", "ban-ip: Bans an IP address from the server.", 
					"unban: Unbans a user.", "unban-ip: Unbans an IP address.", "pw: Turns the password authentication on or off, or set a new password.", "list: displays the list of all players with ID who are online.", 
					"me: Writes a status message.", "tell: Writes a user a private message.", "poke: Pokes a user with a message." };
			
			for(String msg : send)
			{
				Logger.log(Level.INFO, msg);
			}
		}
		else
		{
			Logger.log(Level.INFO, "Unknown command. Type \"/help\" for help.");
		}
	}
	
	public static void performCommand(Server s, String cmd, String[] args, Executor exec, ServerClient user)
	{
		if (cmd.equalsIgnoreCase("list")) 
		{
			String[] send = {"/n/There are " + s.clients.size() + "/128 clients online:", "/n/" + getClientList(s.clients, 0)};
			s.sendMass(send, user.address, user.port);
		}
		else if (cmd.equalsIgnoreCase("me"))
		{
			String msg = getText(args, 0);
			if(msg.length() != 0) 
			{
				s.sendToAll("/m/" + user.name + " " + msg);
			}
			else
			{
				s.send("/n/Bad usage. Usage: /me <message>", user.address, user.port);
			}
		}
		else if (cmd.equalsIgnoreCase("tell") || cmd.equalsIgnoreCase("msg"))
		{
			if(args.length > 1)
			{
				if(isInteger(args[0]))
				{
					ServerClient c = s.getClientByID(Integer.parseInt(args[0]));
					
					if(c != null)
					{
						s.send("/n/[" + user.name + " -> " + c.name + "] " + getText(args, 1), user.address, user.port);
						s.send("/n/" + user.name + " whispers you: " +  getText(args, 1), c.address, c.port);
					}
					else
					{
						s.send("/n/Client with ID " + args[0] + " is not online!", user.address, user.port);
					}
				}
				else
				{
					ServerClient c = s.getClientByName(args[0]);
					
					if(c != null)
					{
						s.send("/n/[" + user.name + " -> " + c.name + "] " + getText(args, 1), user.address, user.port);
						s.send("/n/" + user.name + " whispers you: " +  getText(args, 1), c.address, c.port);
					}
					else
					{
						s.send("/n/Client " + args[0] + " is not online!", user.address, user.port);
					}
				}
			}
			else
			{
				s.send("/n/Bad usage. Usage: /tell <username|ID> <message>", user.address, user.port);
			}
		}
		else if (cmd.equalsIgnoreCase("poke"))
		{
			if(args.length > 1)
			{
				if(isInteger(args[0]))
				{
					ServerClient c = s.getClientByID(Integer.parseInt(args[0]));
					
					if(c != null)
					{
						s.send("/n/Poked client " + c.name + ": " + getText(args, 1), user.address, user.port);
						s.send("/r/$poke="+user.name+"/m/" + getText(args, 1), c.address, c.port);
					}
					else
					{
						s.send("/n/Client with ID " + args[0] + " is not online!", user.address, user.port);
					}
				}
				else
				{
					ServerClient c = s.getClientByName(args[0]);
					
					if(c != null)
					{
						s.send("/n/Poked client " + c.name + ": " + getText(args, 1), user.address, user.port);
						s.send("/r/$poke="+user.name+"/m/" + getText(args, 1), c.address, c.port);
					}
					else
					{
						s.send("/n/Client " + args[0] + " is not online!", user.address, user.port);
					}
				}
			}
			else
			{
				s.send("/n/Bad usage. Usage: /poke <username|ID> <message>", user.address, user.port);
			}
			
		}
		else if (cmd.equalsIgnoreCase("help"))
		{
			String[] send = { "/n/Available commands for client: ", "/n//list: displays the list of all players with ID who are online.", "/n//me: Writes a status message.", "/n//tell: Writes a user a private message.", "/n//poke: Pokes a user with a message." };
			s.sendMass(send, user.address, user.port);
		}
		else
		{
			s.send("/n/Unknown command. Type \"/help\" for help.", user.address, user.port);
		}
	}
	
	private static boolean isInteger(String integer)
	{
		try
		{
			Integer.parseInt(integer);
			return true;
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	
	private static String getText(String[] args, int start)
	{
		String text = "";
	    
	    for(int i = 0; i < args.length; i++) 
		{
	    	if(i >= start)
	    	{
		    	if(text.length() == 0)
		    	{
		    		text = (text + args[i]);
		    	}
		    	else
		    	{
		    		text = (text + " " + args[i]);
		    	}
	    	}
	    }
	    
	    return text;
	}
	
	private static String getClientList(List<ServerClient> clients, int start)
	{
		String text = "";
	    
	    for(int i = 0; i < clients.size(); i++) 
		{
	    	if(i >= start)
	    	{
		    	if(text.length() == 0)
		    	{
		    		text = (text + clients.get(i).name + " (ID" + clients.get(i).getID() + ")");
		    	}
		    	else
		    	{
		    		text = (text + ", " + clients.get(i).name + " (ID" + clients.get(i).getID() + ")");
		    	}
	    	}
	    }
	    
	    return text;
	}
}
