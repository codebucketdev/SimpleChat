package de.codebucket.simplechat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FileManager 
{
	public static String[] readFile(File file)
	{
		List<String> list = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(file)))
		{
		    for(String line; (line = br.readLine()) != null; ) 
		    {
		        list.add(line);
		    }
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
	    {
	    	e.printStackTrace();
	    }
		
		return list.toArray(new String[list.size()]);
	}
	
	public static void clearFile(File file)
	{
		try 
		{
			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			writer.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static boolean createFile(File file)
	{
		try 
		{
			return file.createNewFile();
		} 
		catch (IOException e) {}
		return false;
	}
	
	public static void writeFile(File file, String[] i)
	{
	    BufferedWriter buffwriter = null;
	    FileWriter filewriter = null;
	    
	    try 
	    {
	    	filewriter = new FileWriter(file, true);
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
	    	e.printStackTrace();
	    }
	}
}
