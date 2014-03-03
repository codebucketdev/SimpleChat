package de.codebucket.simplechat.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class ServerMain 
{
	private String address;
	private int port;
	
	private String password;
	private boolean needPassword;
	private Server server;

    public ServerMain(String address, int port, String password, boolean needPassword) 
    {
    	this.address = address;
        this.port = port;
        this.password = password;
        this.needPassword = needPassword;
        bootup();
    }
    
    public void bootup()
    {
    	server = new Server(address, port, password, needPassword);
    }

    public static void main(String[] args) 
    {
    	String path = "";
		try 
		{
			String classpath = ClassLoader.getSystemClassLoader().getResource(".").getPath();
			path = URLDecoder.decode(classpath, "UTF-8");
		} 
		catch (UnsupportedEncodingException e1) {}
		
		Logger.createLog(path);
		Logger.log(Level.INFO, "Running SimpleChat Server v0.95");
		Logger.log(Level.INFO, "Working directory: " + path);
		Logger.log(Level.INFO, "Initialising SimpleChat Server..");
		
		String address;
		int port;
		String password;
		boolean needPassword;
		
		if(args.length != 0)
		{
			if(isInteger(args[0]))
			{
				port = Integer.parseInt(args[0]);
				Logger.log(Level.WARNING, "No input for address exist!");
				Logger.log(Level.INFO, "Set address to default (127.0.0.1)");
				address = "127.0.0.1";
				password = "";
				needPassword = false;
			}
			else
			{
				address = args[0];
				if(args.length > 1)
				{
					if(isInteger(args[1]))
					{
						port = Integer.parseInt(args[1]);
					}
					else
					{
						Logger.log(Level.WARNING, "Invalid input for port: " + args[1] + "!");
						Logger.log(Level.INFO, "Set port to default (8192)");
						port = Integer.parseInt("8192");
					}
					
					if(args.length > 2)
					{
						String usage = args[2];
						if(usage.startsWith("/p:"))
						{
							String[] splitted = usage.split(":");
							
							if(splitted.length > 1)
							{
								password = splitted[1];
								needPassword = true;
							}
							else
							{
								password = "";
								needPassword = false;
							}
						}
						else
						{
							password = "";
							needPassword = false;
						}
					}
					else
					{
						password = "";
						needPassword = false;
					}
				}
				else
				{
					Logger.log(Level.WARNING, "No input for port exist!");
					Logger.log(Level.INFO, "Set port to default (8192)");
					port = Integer.parseInt("8192");
					password = "";
					needPassword = false;
				}
			}
		}
		else
		{
			Logger.log(Level.WARNING, "No input for address & port exist!");
			Logger.log(Level.INFO, "Set address to default (127.0.0.1)");
			Logger.log(Level.INFO, "Set port to default (8192)");
			address = "127.0.0.1";
			port = Integer.parseInt("8192");
			password = "";
			needPassword = false;
		}
		
		new ServerMain(address, port, password, needPassword);
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

}
