package com.mycompany.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class Frame extends JFrame implements MulticastMessageListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	MulticastApp multicastApp;
	JTextArea messages;
	JTextField messageField ;
	JButton sendButton;
	JPanel messagePanel ;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame frame = new Frame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 */
	public Frame() throws IOException {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();

		multicastApp = new MulticastApp("230.0.0.1", 4447, this);
		multicastApp.listen();

		messages = new JTextArea();
		messages.setEditable(false);
		messages.setPreferredSize(new Dimension(300, 200));

		messageField = new JTextField();
		messageField.setPreferredSize(new Dimension(200, 30));

		sendButton = new JButton();
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String message = messageField.getText();
				try {
					multicastApp.sendMessage(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				messageField.setText("");
			}
		});

		sendButton.setText("Send");
		messagePanel = new JPanel();
		messagePanel.setLayout(new FlowLayout());
		messagePanel.add(messageField);
		messagePanel.add(sendButton);

		contentPane.add(messages, BorderLayout.CENTER);
		contentPane.add(messagePanel, BorderLayout.SOUTH);

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

	}

	@Override
	public void onMessageReceived(String message) {
		SwingUtilities.invokeLater(() -> {
			messages.append("Other: " + message + "\n");
		});
		
		if("end".equalsIgnoreCase(message.trim())) {
			System.out.println("Multicast frame received shutdown command.");
			multicastApp.shutdown();
		};
		
	}
}
