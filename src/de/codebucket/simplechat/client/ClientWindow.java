package de.codebucket.simplechat.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;


public class ClientWindow extends JFrame implements Runnable 
{
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextArea history;
	private JTextField txtMessage;
	private DefaultCaret caret;
	private Thread run, listen;
	private Client client;

	private boolean running = false;

	public ClientWindow(String username, String address, int port) 
	{
		setResizable(false);
		createWindow();
		client = new Client(username, address, port);
		connect(client);
	}

	private void createWindow() 
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(880, 550);
		setTitle("Connecting... - SimpleChat Client");
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		history = new JTextArea();
		history.setLineWrap(true);
		history.setWrapStyleWord(true);
		history.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		history.setEditable(false);
		caret = (DefaultCaret) history.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		history.setBounds(10, 11, 854, 471);
		contentPane.add(history);
		
		txtMessage = new JTextField();
		txtMessage.setFont(new Font("Tahoma", Font.PLAIN, 13));
		txtMessage.addKeyListener(new KeyAdapter() 
		{
			public void keyPressed(KeyEvent e) 
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER) 
				{
					send(txtMessage.getText(), true);
					txtMessage.setText("");
				}
			}
		});
		txtMessage.setColumns(10);
		txtMessage.setBounds(10, 494, 775, 20);
		contentPane.add(txtMessage);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				send(txtMessage.getText(), true);
				txtMessage.setText("");
			}
		});
		btnSend.setBounds(795, 493, 69, 23);
		contentPane.add(btnSend);
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				int action = JOptionPane.showConfirmDialog(null, "Do you really want to disconnect from the server?", "SimpleChat Client", JOptionPane.YES_NO_OPTION);  
			    if(action == 0)
			    {
			    	String disconnect = "/d/" + client.getID() + "/e/";
					send(disconnect, false);
					close();  
			    }
			}
		});
		
		setVisible(true);
	}

	public void run() 
	{
		listen();
	}

	private void send(String message, boolean text) 
	{
		if (message.equals("")) return;
		if (text) 
		{
			message = client.getName() + ": " + message;
			message = "/m/" + message;
		}
		client.send(message.getBytes());
	}

	public void listen() 
	{
		listen = new Thread("Listen") 
		{
			public void run() 
			{
				while (running)
				{
					String message = client.receive();
					if (message.startsWith("/c/")) 
					{
						client.setID(Integer.parseInt(message.split("/c/|/e/")[1]));
						setTitle(client.getName() + "@" + client.getAddress() + ":" + client.getPort() + " - SimpleChat Client");
						console(Level.INFO, "Connected to Server " + client.getAddress() + ":" + client.getPort() + "!");
					} 
					else if (message.startsWith("/m/")) 
					{
						String text = message.substring(3);
						text = text.split("/e/")[0];
						console(Level.INFO, text);
					} 
					else if (message.startsWith("/n/")) 
					{
						String text = message.substring(3);
						text = text.split("/e/")[0];
						console(Level.INFO, text);
					}
					else if (message.startsWith("/r/$disconnect:")) 
					{
						disconnect(message.split(":")[1], true);
					}
					else if (message.startsWith("/r/$password=?")) 
					{
						JPasswordField pwd = new JPasswordField(10);  
					    int action = JOptionPane.showConfirmDialog(null, pwd,"Enter password to connect",JOptionPane.OK_CANCEL_OPTION);  
					    if(!(action == 0))
					    {
					    	close();  
					    }
					    else 
					    {
					    	send("/r/$password:" + String.valueOf(pwd.getPassword()) + "/c/" + client.getName(), false);
					    }
					}
					else if (message.startsWith("/r/$invalid:password")) 
					{
						disconnect("Invalid or wrong input for password!", false);
					}
					else if (message.startsWith("/r/$invalid:username")) 
					{
						disconnect("A user with the same user name already logged in. Please login with a different user name.", false);
					}
					else if (message.startsWith("/i/")) 
					{
						ping(client.getID());
					}
				}
			}
		};
		listen.start();
	}
	
	private void close()
	{
		running = false;
		client.close();
		
		dispose();
		JFrame frame = new Login();
		frame.setVisible(true);
	}
	
	private void connect(Client client)
	{
		console(Level.INFO, "Trying connect to " + client.getAddress() + ":" + client.getPort() + ", User: " + client.getName());
		boolean connect = client.openConnection(client.getAddress(), client.getPort());
		if (!connect) 
		{
			console(Level.SEVERE, "Failed to connect to server " + client.getAddress() + ":" + client.getPort() + "!");
			setTitle("Not connected - SimpleChat Client");
			JOptionPane.showMessageDialog(null,"Failed to connect to server " + client.getAddress() + ":" + client.getPort() + "!","SimpleChat Client", JOptionPane.ERROR_MESSAGE);
			
			dispose();
			JFrame frame = new Login();
			frame.setVisible(true);
		}
		else
		{
			String connection = "/c/" + client.getName();
			client.send(connection.getBytes());
			running = true;
			run = new Thread(this, "Running");
			run.start();
		}
	}
	
	private void disconnect(String reason, boolean send)
	{
		if(send)
		{
			String disconnect = "/d/" + client.getID() + "/e/";
			send(disconnect, false);
		}
		
		try 
		{
			Thread.sleep(50);
		} 
		catch (InterruptedException e) {}
		
		running = false;
		client.close();
		JOptionPane.showMessageDialog(null,"Lost connection to server: " + reason,"SimpleChat Client", JOptionPane.INFORMATION_MESSAGE);
		
		dispose();
		JFrame frame = new Login();
		frame.setVisible(true);
	}
	
	private void ping(int id)
	{
		String text = "/i/" + id + "/e/";
		send(text, false);
	}

	public void console(Level l, String message) 
	{
		Calendar c = Calendar.getInstance();
		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
		String stamp = "<" + f.format(c.getTime()) + ">";
		
		history.append(stamp + " " + message + "\n\r");
		history.setCaretPosition(history.getDocument().getLength());
		Logger.log(l, message);
	}
}
