package com.mycompany.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class App extends JFrame implements MulticastMessageListener {

	private static final long serialVersionUID = 1L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					App frame = new App();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JPanel appMain;
	private Server server;
	private JTextArea messages;
	private JTextField messageField;
	private JButton exitButton;
	private JPanel messagePanel;
	private JTextArea status;
	private CardLayout cardLayout;
	private String user;
	private JPanel contentPane;
	private JPanel loginPanel;

	private JTextField usernameField;

	private java.util.concurrent.ScheduledExecutorService scheduler;

	private final long HEARTBEAT_INTERVAL_SECONDS = 3;

	private final long TIMEOUT_THRESHOLD_SECONDS = 10;

	private java.util.Map<String, Long> lastSeenTimes = new java.util.concurrent.ConcurrentHashMap<>();

	private java.util.Set<String> onlineUsers = new java.util.HashSet<>();

	private int MIN_USERNAME_LENGTH = 5;

	/**
	 * Create the frame.
	 *
	 * @throws IOException
	 */
	public App()  {
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				try {
					String leaveMessage = "[STATUS]LEAVE:" + user;
					List<InetAddress> addresses = BroadcastingClient.listAllBroadcastAddresses();
					for (InetAddress address : addresses) {
						BroadcastingClient.broadcast(leaveMessage, address);
					}
					Thread.sleep(50);
				} catch (Exception e) {
					System.err.println("Error sending leave signal.");
				} finally {
					if (server != null) {
						List<InetAddress> addresses;
						try {
							addresses = BroadcastingClient.listAllBroadcastAddresses();
							for (InetAddress address : addresses) {
								BroadcastingClient.broadcast("end", address);
							}
						} catch (SocketException e) {
							System.err.println("Error getting broadcast address.");
							e.printStackTrace();
						} catch (IOException e1) {
							System.err.println("Error broadcasting end.");
						}
					}
					System.exit(0);
				}
			}
		});

		setBounds(100, 100, 450, 300);
		try {
			server = new Server(this);
		} catch (SocketException e) {
			System.out.println("Error instantiating server: ");
			e.printStackTrace();
		}
		appMain = new JPanel();
		cardLayout = new CardLayout();
		contentPane = new JPanel();
		contentPane.setLayout(cardLayout);

		messages = new JTextArea();
		messages.setEditable(false);
		messages.setFocusable(false);
		messages.setPreferredSize(new Dimension(300, 200));

		messageField = new JTextField();
		messageField.setPreferredSize(new Dimension(200, 30));
		messageField.setFocusable(true);
		messageField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String msgFieldText = messageField.getText();
				if (msgFieldText == null || msgFieldText.trim().isEmpty()) {
					return;
				}
				String message = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + user + ": "
						+ msgFieldText;
				try {
					List<InetAddress> addresses = BroadcastingClient.listAllBroadcastAddresses();
					for (InetAddress address : addresses) {
						BroadcastingClient.broadcast(message, address);
					}

				} catch (IOException e) {
					System.out.println("Error broadcasting message: ");
					e.printStackTrace();
				}
				messageField.setText("");
			}
		});

		exitButton = new JButton();
		exitButton.setText("Exit");
		exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					for (InetAddress address : BroadcastingClient.listAllBroadcastAddresses()) {
						BroadcastingClient.broadcast("end", address);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		messagePanel = new JPanel();
		messagePanel.setLayout(new FlowLayout());
		messagePanel.add(messageField);
		messagePanel.add(exitButton);

		appMain.add(messages, BorderLayout.CENTER);

		status = new JTextArea();
		status.setEditable(false);
		status.setPreferredSize(new Dimension(100, 180));

		appMain.add(status, BorderLayout.WEST);
		appMain.add(messagePanel, BorderLayout.SOUTH);

		usernameField = new JTextField();
		usernameField.setPreferredSize(new Dimension(150, 20));
		usernameField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				user = usernameField.getText().strip();
					System.out.println("Username must be at least 5 characters long");
				if (user.length() < 5) {
					System.out.println("Username length should greater than 4");
					return;
				}
				server.start();
				String joinMessage = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + user + ": "
						+ user + " joined.";
				System.out.println(joinMessage);
				try {
					for (InetAddress address : BroadcastingClient.listAllBroadcastAddresses()) {
						BroadcastingClient.broadcast(joinMessage, address);
					}
				} catch (IOException e1) {
					System.out.println("Failed to broadcast join status.");
					e1.printStackTrace();
				}

				whenLogin();

				System.out.println(user + " logged in!");
				System.out.println("Server running.");
				cardLayout.show(contentPane, "mainCard");
			}
		});

		loginPanel = new JPanel();
		loginPanel.add(usernameField);

		contentPane.add(loginPanel, "loginCard");
		contentPane.add(appMain, "mainCard");
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
	}

	private void handleStatusMessage(String message) {
		String content = message.substring("[STATUS]".length()).trim();

		String[] parts = content.split(":", 2);
		if (parts.length < 2) {
			return;
		}
		String action = parts[0];
		String user = parts[1];

		switch (action) {
		case "JOIN":
		case "ALIVE":
			lastSeenTimes.put(user, System.currentTimeMillis());

			if (!user.equals(this.user)) {
				onlineUsers.add(user);
			}
			break;
		case "LEAVE":
			onlineUsers.remove(user);
			lastSeenTimes.remove(user);
			break;
		}
		updateStatusDisplay();
	}

	@Override
	public void onMessageReceived(String fullMessage) {
		String message = fullMessage.trim();

		if (message.startsWith("[STATUS]")) {
			handleStatusMessage(message);
		} else {
			SwingUtilities.invokeLater(() -> {
				messages.append(message + "\n");
			});
		}
	}

	private void runTimeoutCleanup() {
		long currentTime = System.currentTimeMillis();
		long timeoutTime = TIMEOUT_THRESHOLD_SECONDS * 1000;

		java.util.Iterator<String> userIterator = onlineUsers.iterator();

		boolean listChanged = false;

		while (userIterator.hasNext()) {
			String user = userIterator.next();

			if (user.equals(this.user)) {
				continue;
			}

			Long lastSeen = lastSeenTimes.get(user);

			if (lastSeen == null || (currentTime - lastSeen > timeoutTime)) {
				System.out.println("Timeout: User " + user + " removed from online list.");
				userIterator.remove();
				lastSeenTimes.remove(user);
				listChanged = true;
			}
		}

		if (listChanged) {
			updateStatusDisplay();
		}
	}

	private void updateStatusDisplay() {
		SwingUtilities.invokeLater(() -> {
			status.setText("--- Online Users ---\n");
			for (String user : onlineUsers) {
				status.append(user + "\n");
			}
		});
	}

	private void whenLogin() {
		scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

		Runnable heartbeatSender = () -> {
			try {
				String aliveMessage = "[STATUS]ALIVE:" + user;
				List<InetAddress> addresses = BroadcastingClient.listAllBroadcastAddresses();
				for (InetAddress address : addresses) {
					BroadcastingClient.broadcast(aliveMessage, address);
				}
				lastSeenTimes.put(user, System.currentTimeMillis());
			} catch (IOException e1) {
				System.err.println("Error sending ALIVE signal.");
			}
		};

		scheduler.scheduleAtFixedRate(heartbeatSender, 0, HEARTBEAT_INTERVAL_SECONDS,
				java.util.concurrent.TimeUnit.SECONDS);

		scheduler.scheduleAtFixedRate(this::runTimeoutCleanup, 0, HEARTBEAT_INTERVAL_SECONDS,
				java.util.concurrent.TimeUnit.SECONDS);
	}

}
