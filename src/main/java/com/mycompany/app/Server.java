package com.mycompany.app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server extends Thread {

	private DatagramSocket socket;
	private boolean running;
	private String receivedMessage;	
	private MulticastMessageListener listener;
	
	public String getReceivedMessage() {
		return receivedMessage;
	}

	public void setReceivedMessage(String receivedMessage) {
		this.receivedMessage = receivedMessage;
	}

	private byte[] buf = new byte[256];

	public Server(MulticastMessageListener listener) throws SocketException {
		socket = new DatagramSocket(4445);
		this.listener = listener;
	}

	public void run() {
		running = true;

		while (running) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String received = new String(packet.getData(), 0, packet.getLength()).trim();
			System.out.println("Received: " + received);
			if ("end".equalsIgnoreCase(received)) {
				System.out.println("Received end. Server stopping!");
				running = false;
				continue;
			}
			listener.onMessageReceived(received);
		}
		socket.close();
	}
}