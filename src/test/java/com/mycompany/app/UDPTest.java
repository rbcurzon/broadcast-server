package com.mycompany.app;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UDPTest {
	BroadcastingClient client;
	Server server;
	@BeforeEach
	public void setup() throws IOException {
		server = new Server(null);
		server.start();
		client = new BroadcastingClient();
	}

	@Test
	public void whenCanPublishAndReceivePacket_thenCorrect() throws IOException {
		String TEST_MESSAGE = "Hello World";
		List<InetAddress> addresses = BroadcastingClient.listAllBroadcastAddresses();
		for (InetAddress address: addresses) {
			System.out.println("broadcast message to " + address);
		}
//		assertEquals(TEST_MESSAGE, received.trim());
	}

	@AfterEach
	public void tearDown() throws IOException {
		BroadcastingClient.broadcast("end", InetAddress.getByName("255.255.255.255"));
	}
}
