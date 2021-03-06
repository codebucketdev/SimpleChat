package de.codebucket.simplechat.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;
import javax.swing.JButton;

import de.codebucket.simplechat.client.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Login extends JFrame 
{
	private static Login instance;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1417068320371523683L;
	private JPanel contentPane;	
	/**
	 * @wbp.nonvisual location=-109,449
	 */
	private JTextField username;
	private JTextField address;
	private JTextField port;

	/**
	 * Create the frame.
	 */
	public Login() 
	{
		instance = this;
		
		//LOAD OS SYSTEM LOOK AND FEEL STYLE
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		final List<Character> allowed = new ArrayList<>();
		char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz'.-_".toCharArray();
		for(char c : chars)
		{
			allowed.add(Character.valueOf(c));
		}
		
		setType(Type.POPUP);
		setResizable(false);
		setTitle("SimpleChat v0.95");
		setSize(275, 335);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTitle = new JLabel("SimpleChat");
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 26));
		lblTitle.setBounds(10, 11, 249, 32);
		contentPane.add(lblTitle);
		
		JLabel lblAbout = new JLabel("By Codebucket. Version 0.95");
		lblAbout.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblAbout.setBounds(10, 46, 249, 14);
		contentPane.add(lblAbout);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Login", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(10, 71, 249, 224);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblUsername.setBounds(10, 25, 229, 14);
		panel.add(lblUsername);
		
		String[] loginData = readLogin();
		
		username = new JTextField();
		username.addKeyListener(new KeyAdapter() 
		{
		    public void keyTyped(KeyEvent e) 
		    {
		    	Character c = Character.valueOf(e.getKeyChar());
		    	if (!(allowed.contains(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) 
		    	{
		    		e.consume();
		    	}
		    	else
		    	{
		    		if(username.getText().length() > 31)
		    		{
		    			getToolkit().beep();
			    		e.consume();
		    		}
		    	}
		    }
		});
		
		if(loginData.length > 0)
		{
			username.setText(loginData[0]);
		}
		else
		{
			username.setText("");
		}
		username.setBounds(10, 45, 229, 20);
		panel.add(username);
		username.setColumns(10);
		
		JLabel lblAddress = new JLabel("IP Address:");
		lblAddress.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblAddress.setBounds(10, 76, 229, 14);
		panel.add(lblAddress);
		
		address = new JTextField();
		if(loginData.length > 1)
		{
			address.setText(loginData[1]);
		}
		else
		{
			address.setText("");
		}
		address.setColumns(10);
		address.setBounds(10, 95, 229, 20);
		panel.add(address);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblPort.setBounds(10, 126, 229, 14);
		panel.add(lblPort);
		
		port = new JTextField();
		port.addKeyListener(new KeyAdapter() 
		{
		    public void keyTyped(KeyEvent e) 
		    {
		    	char c = e.getKeyChar();
		    	if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) 
		    	{
		    		e.consume();
		    	}
		    	else
		    	{
		    		if(!(port.getText().length() < 10))
		    		{
		    			getToolkit().beep();
			    		e.consume();
		    		}
		    	}
		    }
		});
		
		if(loginData.length > 2)
		{
			if(isInteger(loginData[2])) 
			{
				port.setText(loginData[2]);
			}
			else
			{
				port.setText("");
			}
		}
		else
		{
			port.setText("");
		}
		port.setColumns(10);
		port.setBounds(10, 145, 229, 20);
		panel.add(port);
		
		JButton btnConnect = new JButton("Connect..");
		btnConnect.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				String lUsername = username.getText();
				
				if(lUsername.length() != 0 && lUsername.length() < 32)
				{
					String lAddress = address.getText();
					String lPortString = port.getText();
					
					if(lAddress.length() == 0)
					{
						lAddress = "127.0.0.1";
					}
					
					if(lPortString.length() == 0)
					{
						lPortString = "8192";
					}
					
					if(lPortString.length() < 10)
					{
						if(isInteger(lPortString))
						{
							int lPort = Integer.parseInt(lPortString);
							saveLogin(lUsername, lAddress, lPort);
							login(lUsername, lAddress, lPort);
						}
						else
						{
							getToolkit().beep();
						}
					}
					else
					{
						getToolkit().beep();
					}
				}
				else
				{
					getToolkit().beep();
				}
			}
		});
		btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnConnect.setBounds(10, 176, 229, 37);
		panel.add(btnConnect);
	}
	
	public void login(String username, String address, int port)
	{
		dispose();
        new ClientWindow(username, address, port);
	}
	
	public String[] readLogin()
	{
		File file = new File(getWorkingDirectory() + "/login.txt");
		if(file.exists())
		{
			return FileManager.readFile(file);
		}
		else
		{
			FileManager.createFile(file);
			FileManager.clearFile(file);
			String[] out = { "Guest" + new Random().nextInt(1000), "chat.codebucket.de", "8192" };
			return out;
		}
	}
	
	public void saveLogin(String username, String address, int port)
	{
		File file = new File(getWorkingDirectory() + "/login.txt");
		if(file.exists())
		{
			String[] write = { username, address, String.valueOf(port) };
			FileManager.clearFile(file);
			FileManager.writeFile(file, write);
		}
		else
		{
			String[] write = { username, address, String.valueOf(port) };
			FileManager.createFile(file);
			FileManager.clearFile(file);
			FileManager.writeFile(file, write);
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
	
	/**
	 * Launch the application.
	 */
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
		Logger.log(Level.INFO, "Starting SimpleChat v0.95");
		Logger.log(Level.INFO, "Working directory: " + path);
		Logger.log(Level.INFO, "Initialising SimpleChat Client..");
		
		final Login frame = new Login();
		if(args.length != 0)
		{
			Logger.log(Level.INFO, "Found auto-connect arguments: " + getArgsAmount(args[0]));
			
			String username;
			String address;
			String portString;
			
			String usage = args[0];
			String[] user = usage.split("@");
			
			if(user.length > 1)
			{
				username = user[0];
				String[] connect = user[1].split(":");
				
				if(connect.length > 1)
				{
					address = connect[0];
					
					if(isInteger(connect[1]))
					{
						portString = connect[1];
					}
					else
					{
						Logger.log(Level.WARNING, "Invalid input for port: " + connect[1] + "!");
						Logger.log(Level.INFO, "Set port to default (8192)");
						portString = "8192";
					}
				}
				else
				{
					Logger.log(Level.WARNING, "No input for port exist!");
					Logger.log(Level.INFO, "Set port to default (8192)");
					address = connect[0];
					portString = "8192";
				}
			}
			else
			{
				Logger.log(Level.WARNING, "No input for address & port exist!");
				Logger.log(Level.INFO, "Set address to default (127.0.0.1)");
				Logger.log(Level.INFO, "Set port to default (8192)");
				username = user[0];
				address = "127.0.0.1";
				portString = "8192";
			}
			
			instance.login(username, address, Integer.parseInt(portString));
		}
		else
		{
			EventQueue.invokeLater(new Runnable() 
			{
				public void run() 
				{
					try 
					{
						frame.setVisible(true);
					} 
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	private static int getArgsAmount(String input)
	{
		int output = 0;
		String[] first = input.split("@");
		
		if(first.length != 0)
		{
			output =+ 1;
			if(first.length > 1)
			{
				String[] second = first[1].split(":");
				output += second.length;
			}
		}
		
		return output;
	}
}
