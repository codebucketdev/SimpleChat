package de.codebucket.simplechat.server;

import java.util.logging.Level;

@SuppressWarnings("unused")
public class ServerMain 
{
	private String address;
	private int port;
	private Server server;

    public ServerMain(String address, int port) 
    {
    	this.address = address;
        this.port = port;
        bootup();
    }
    
    public void bootup()
    {
    	server = new Server(address, port);
    }

    public static void main(String[] args) 
    {
    	String path = ServerMain.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		Logger.createLog(path);
		
		Logger.log(Level.INFO, "Running SimpleChat Server v0.8");
		Logger.log(Level.INFO, "Working directory: " + path);
		Logger.log(Level.INFO, "Initialising SimpleChat Server..");
		
		String address;
		int port;
		
		if(args.length != 0)
		{
			if(isInteger(args[0]))
			{
				port = Integer.parseInt(args[0]);
				Logger.log(Level.WARNING, "No input for address exist!");
				Logger.log(Level.INFO, "Set address to default (127.0.0.1)");
				address = "127.0.0.1";
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
				}
				else
				{
					Logger.log(Level.WARNING, "No input for port exist!");
					Logger.log(Level.INFO, "Set port to default (8192)");
					port = Integer.parseInt("8192");
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
		}
		
		new ServerMain(address, port);
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
