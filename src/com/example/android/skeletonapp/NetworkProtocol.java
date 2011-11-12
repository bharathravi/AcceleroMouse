package com.example.android.skeletonapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkProtocol {
	
	private String ip;
	private InetAddress ipAddress;
	private int port;
	public int xRes;
	public int yRes;
	private long lastSendTime;	
	private DatagramSocket clientSocket;
	private boolean init = false;
	public float yMeters;
	public float xMeters;
	
	// We don't want to flood the server with updates, it increases latency.
	// Instead, we send a position update only every "throttle" microseconds.
	public float throttle = 80000000;
	
	public NetworkProtocol(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public NetworkProtocol() {
	}

	public void init() {
		try {
			BufferedReader inFromUser =
		    new BufferedReader(new InputStreamReader(System.in));
		    clientSocket = new DatagramSocket();		      		      		
		    
		    ipAddress = InetAddress.getByName(ip);
		    byte[] sendData = new byte[1024];
		    byte[] receiveData = new byte[1024];		     
		      
		    sendData = "init".getBytes();
		    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
		    		ipAddress, port);
		    clientSocket.send(sendPacket);
		      
		      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		      clientSocket.receive(receivePacket);
		      String line = new String(receivePacket.getData());		     
		      System.out.println(line.length());
		      System.out.println(line.indexOf('\n'));
		      
			int xpos = line.indexOf('x');
			int space = line.indexOf(' ');
			int Xpos = line.indexOf('X');
			int end = line.indexOf('\n');
			xRes = Integer.parseInt(line.substring(0, xpos));			
			yRes = Integer.parseInt(line.substring(xpos + 1, space));
			xMeters = Integer.parseInt(line.substring(space + 1, Xpos))/1000f;
			yMeters = Integer.parseInt(line.substring(Xpos + 1, end))/1000f;
			
			init = true;
		} catch (UnknownHostException e) {
			init = false;
			e.printStackTrace();
		} catch (IOException e) {
			init = false;
			e.printStackTrace();
		}
	}
	
	public void sendUpdate(int x, int y, int click) {
		if (!init)
			return;
		
		// Throttle update sends
		if (lastSendTime == 0 ) {
			lastSendTime = System.nanoTime();
		} else{ 
			long now = System.nanoTime();
			// To prevent sending too fast, we only send every "resolution" nanoseconds.
			if (now - lastSendTime < throttle ) {
				return;
			}		
		}		
		
		// BufferedReader inFromUser = new BufferedReader( new
		// InputStreamReader(System.in));
		try {
            lastSendTime = System.nanoTime();
			String sentence = x + " " + y + " " + click;
			byte[] sendData = sentence.getBytes();
		    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
		    clientSocket.send(sendPacket);

			//outToServer.writeBytes(x + " " + y + " " + isClick + 'e');
			// modifiedSentence = inFromServer.readLine();
			// System.out.println("FROM SERVER: " + modifiedSentence);
			// clientSocket.close();
		} catch (UnknownHostException e) {
			System.out.println("hi");
		} catch (IOException e) {
			System.out.println("hi");
			e.printStackTrace();
		}

	}
}
