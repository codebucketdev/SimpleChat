package de.codebucket.simplechat.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;

public class Logger 
{
	private static File logFile;
	
	public static void log(Level level, String msg)
	{
		Calendar c = Calendar.getInstance();
		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
		String stamp = f.format(c.getTime());
		String prefix = "[" + level.toString().toUpperCase() + "]";
		String output = stamp + " " + prefix + " " + msg;
		
		System.out.println(output);
		
		//String[] i = {output};
		//writeLog(i);
	}
	
	public static void createLog(String path)
	{
		File logDir = new File(path + "/logs");
		if(!logDir.exists())
		{
			logDir.mkdir();
		}
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		String timestamp = f.format(c.getTime());
		logFile = new File(path + "/logs/client_" + timestamp + ".log");
	}
	
	public static void writeLog(String[] i)
	{
	    File log = logFile;
	    BufferedWriter buffwriter = null;
	    FileWriter filewriter = null;
	    
	    try 
	    {
	      filewriter = new FileWriter(log, true);
	      buffwriter = new BufferedWriter(filewriter);

	      for (String s : i)
	      {
	        buffwriter.write(s);
	        buffwriter.newLine();
	      }

	      buffwriter.flush();
	    }
	    catch (IOException e)
	    {
	    	
	    }
	}
}
