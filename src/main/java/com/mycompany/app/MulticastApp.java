package com.mycompany.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastApp implements MulticastMessageListener {
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private volatile boolean running = true;
	private MulticastMessageListener listener;
	
	public MulticastApp(String multicastAddress, int port, MulticastMessageListener listener) throws IOException {
		this.group = InetAddress.getByName(multicastAddress);
		this.port = port;
		this.socket = new MulticastSocket(port);
		this.socket.joinGroup(group);
		this.listener = listener;
	}

	public void sendMessage(String message) throws IOException {
		byte[] buffer = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
		socket.send(packet);
	}

	public void listen() {
		byte[] buffer = new byte[1000];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		Thread t1 = new Thread(() -> {

				while (running) {
					try {
						socket.receive(packet);
						String received = new String(packet.getData(), 0, packet.getLength());
						listener.onMessageReceived(received);
					} catch (IOException e) {
						System.out.println("IOException: " + e.getMessage());
					} finally {
						packet.setLength(buffer.length);
					}
				}
			});
		t1.setDaemon(true);
		t1.start();
	}

	public void shutdown() {
		running = false;
		if (socket != null && !socket.isClosed()) {
			try {
				socket.leaveGroup(group);
				socket.close();
			} catch (IOException e) {
				// Log error during shutdown
			}
		}
	}

	@Override
	public void onMessageReceived(String message) {
		System.out.println(socket.getLocalPort() + ": " + message);
		
		if ("end".equalsIgnoreCase(message.trim())) {
			System.out.println("App shutting down!");
			shutdown();
		}
	}
}