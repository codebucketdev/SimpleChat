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

import de.codebucket.simplechat.server.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
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
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (InstantiationException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (UnsupportedLookAndFeelException e) 
		{
			e.printStackTrace();
		}
		
		setType(Type.POPUP);
		setResizable(false);
		setTitle("SimpleChat v0.8");
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
		
		JLabel lblAbout = new JLabel("By Codebucket. Version 0.8.");
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
		
		username = new JTextField();
		username.setBounds(10, 45, 229, 20);
		panel.add(username);
		username.setColumns(10);
		
		JLabel lblAddress = new JLabel("IP Address:");
		lblAddress.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblAddress.setBounds(10, 76, 229, 14);
		panel.add(lblAddress);
		
		address = new JTextField();
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
		    		getToolkit().beep();
		    		e.consume();
		    	}
		    	else
		    	{
		    		if(port.getText().length() > 9)
		    		{
		    			getToolkit().beep();
			    		e.consume();
		    		}
		    	}
		    }
		});
		port.setColumns(10);
		port.setBounds(10, 145, 229, 20);
		panel.add(port);
		
		JButton btnConnect = new JButton("Connect..");
		btnConnect.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				String lUsername = username.getText();
				
				if(lUsername.length() != 0)
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
					
					if(isInteger(lPortString))
					{
						int lPort = Integer.parseInt(lPortString);
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
		String path = Login.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		Logger.createLog(path);
		
		Logger.log(Level.INFO, "Starting SimpleChat v0.8");
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
