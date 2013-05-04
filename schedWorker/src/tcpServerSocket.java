import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import com.amazonaws.services.sqs.model.Message;

import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class tcpServerSocket extends Thread implements Runnable {
	int serverPort;
	ServerSocket listenSocket = null;
	List<Socket> acceptSocketList = new ArrayList<Socket>();
	
	public tcpServerSocket(int serverPort) {
		this.serverPort = serverPort;
		try {
			listenSocket = new ServerSocket(serverPort);
		} catch (Exception error) {
			System.err.println("Unable to create server socket");
		}
	}

	public ServerSocket getListeningSocket() {
		return listenSocket;
	}
	
	private void millisleep(int n) {
		try
		   {
		   // Sleep at least n milliseconds.
		   // 1 millisecond = 1/1000 of a second.
		   Thread.sleep( n );
		   }
		catch ( InterruptedException e )
		   {
		   System.out.println( "awakened prematurely" );
	
		   // If you want to simulate the interrupt happening
		   // just after awakening, use the following line
		   // so that our NEXT sleep or wait
		   // will be interrupted immediately.
		   // Thread.currentThread().interrupt();
		   // Or have have same other thread awaken us:
		   // Thread us;
		   // ...
		   // us = Thread.currentThread();
		   // ...
		   // us.interrupt();
		   }
	}
	
	public void run() {
		while (true) {
			try {			
				Socket s = listenSocket.accept();
				String host = s.getInetAddress().getHostName();
				acceptSocketList.add(s);
				millisleep(500);
			}catch (Exception error) {
				System.err.println("Unable to listen to server socket");
			}
		}
	}
	
	public boolean isAcceptSocketAvailable() {
		if (acceptSocketList.isEmpty()) return false;
		else return true;
	}
	
	
	public Socket retrieveAcceptSocket() {
		Socket acceptSocket = null;
		if (acceptSocketList.isEmpty()) {
			return null;
		}
		else {
			Iterator<Socket> iter = acceptSocketList.iterator();
			while (iter.hasNext()) {
				acceptSocket = iter.next();
				iter.remove();
				millisleep(500);
				break;
			}
			return acceptSocket;
		}
	}
	
	private String getRawRequest(InputStream in) throws IOException {
		byte buf[] = new byte[1024];
		int pos = 0;
		boolean continueReading=true; 

		while (pos < 1024) {
			buf[pos++] = (byte) 0;
		}
		
		pos = 0;
		while (continueReading) {
			int len;
			len = in.available();
			if (len > 0) {
				if (pos+len > 1024) {
					len = (pos + len) - 1024; 
				}
				in.read(buf, pos, len);
				pos += len;
			}
			else {
				if (pos > 0) continueReading = false;
				else millisleep(500);
			}
		}
		
		return (new String(buf, 0, pos));
	}
	
	public String readString(Socket s)  throws IOException {
		InputStream in = s.getInputStream();
		String readString = getRawRequest(in);
		return readString;
	}
	
	public void closeAcceptSocket(Socket s) {
		try {
		s.close();
		} catch (Exception error) {
		   System.err.println("Error in closing socket");
		}
	}
}