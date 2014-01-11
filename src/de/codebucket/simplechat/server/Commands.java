package de.codebucket.simplechat.server;

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
		if (cmd.equals("stop")) 
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
		else if (cmd.equals("kick")) 
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
		else if (cmd.equals("kickall")) 
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
		else if (cmd.equals("ban")) 
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
								s.bannedUsers.add(c.name);
								Logger.log(Level.INFO, "Banned client " + c.name + ": " + getText(args, 1));
								s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
							}
							else
							{
								s.bannedUsers.add(c.name);
								Logger.log(Level.INFO, "Banned client " + c.name + ": Banned from server by operator.");
								s.send("/r/$disconnect:Banned from server by operator.", c.address, c.port);
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
								s.bannedUsers.add(c.name);
								Logger.log(Level.INFO, "Banned client " + c.name + ": " + getText(args, 1));
								s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
							}
							else
							{
								s.bannedUsers.add(c.name);
								Logger.log(Level.INFO, "Banned client " + c.name + ": Banned from server by operator.");
								s.send("/r/$disconnect:Banned from server by operator.", c.address, c.port);
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
								s.bannedUsers.add(args[0]);
								Logger.log(Level.INFO, "Banned client " + args[0] + ": " + getText(args, 1));
							}
							else
							{
								s.bannedUsers.add(args[0]);
								Logger.log(Level.INFO, "Banned client " + args[0] + ": Banned from server by operator.");
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
		else if (cmd.equals("ban-ip")) 
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
								s.bannedAddresses.add(c.address.getHostAddress());
								Logger.log(Level.INFO, "Banned address " + c.address.getHostAddress() + ": " + getText(args, 1));
								s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
							}
							else
							{
								s.bannedAddresses.add(c.address.getHostAddress());
								Logger.log(Level.INFO, "Banned address " + c.address.getHostAddress() + ": Banned from server by operator.");
								s.send("/r/$disconnect:Banned from server by operator.", c.address, c.port);
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
								s.bannedAddresses.add(c.address.getHostAddress());
								Logger.log(Level.INFO, "Banned address " + c.address.getHostAddress() + ": " + getText(args, 1));
								s.send("/r/$disconnect:" + getText(args, 1), c.address, c.port);
							}
							else
							{
								s.bannedAddresses.add(c.address.getHostAddress());
								Logger.log(Level.INFO, "Banned address " + c.address.getHostAddress() + ": Banned from server by operator.");
								s.send("/r/$disconnect:Banned from server by operator.", c.address, c.port);
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
								s.bannedAddresses.add(args[0]);
								Logger.log(Level.INFO, "Banned address " + args[0] + ": " + getText(args, 1));
							}
							else
							{
								s.bannedAddresses.add(args[0]);
								Logger.log(Level.INFO, "Banned address " + args[0] + ": Banned from server by operator.");
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
		else if (cmd.equals("unban")) 
		{
			if(args.length != 0)
			{
				if(s.bannedUser(args[0]) == true)
				{
					for (int i = 0; i < s.bannedUsers.size(); i++) 
					{
						String us = s.bannedUsers.get(i);
						if(us.equals(args[0]))
						{
							s.bannedUsers.remove(i);
							Logger.log(Level.INFO, "Unbanned client " + args[0] + " by operator.");
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
		else if (cmd.equals("unban-ip")) 
		{
			if(args.length != 0)
			{
				if(s.bannedAddress(args[0]) == true)
				{
					for (int i = 0; i < s.bannedAddresses.size(); i++) 
					{
						String us = s.bannedAddresses.get(i);
						if(us.equals(args[0]))
						{
							s.bannedAddresses.remove(i);
							Logger.log(Level.INFO, "Unbanned address " + args[0] + " by operator.");
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
		else if (cmd.equals("list")) 
		{
			Logger.log(Level.INFO, "Clients:");
			Logger.log(Level.INFO, "========");
			for (int i = 0; i < s.clients.size(); i++) 
			{
				ServerClient c = s.clients.get(i);
				Logger.log(Level.INFO, c.name + "(" + c.getID() + "): " + c.address.toString() + ":" + c.port);
			}
			Logger.log(Level.INFO, "========");
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
}
