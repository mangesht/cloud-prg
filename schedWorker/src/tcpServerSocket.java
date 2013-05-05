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
				millisleep(50);
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
		if (acceptSocketList == null) {
			System.err.println("acceptSocketList not initialised");
			return null;
		} else 
		if (acceptSocketList.isEmpty()) {
			//System.err.println("accept Socket not available yet (1)");
			return null;
		}
		else {
			Iterator<Socket> iter = acceptSocketList.iterator();
			if (iter == null) {
				System.err.println("accept Socket not available yet (2)");
				return null;
			} else {
				while (iter.hasNext()) {
					acceptSocket = iter.next();
					if (acceptSocket == null) {
						System.err.println("did not find accept Socket");					
						millisleep(50);
					} else {
						System.err.println("found an accept Socket");					
						iter.remove();
						break;
					}
				}
			}
			System.err.println("returning an accept Socket");					
			return acceptSocket;
		}
	}
	
	public String readStringFromStream(InputStream inputSockStream) 
			throws IOException {
				  String taskresponse = "";
			      int availLen;
			      int readLen;
			      int pos = 0;
			      byte b[] = new byte[1024];
			      int c = 1024;
				  boolean continueReading = true;
				  boolean readStarted = false;
				  System.out.println("readStringFromStream");
				  c = 0;
				  while (c < 1024) {
					  b[c] = 0;
					  c++;
				  }
				  
				  while (continueReading) {
				      boolean endOfBuffer;
				      
				      endOfBuffer = false;
				      
					  availLen = inputSockStream.available();
					  if (availLen == 0) {
						  if (readStarted == true) {
							  continueReading = false;
							  continue;
						  }
					  }
					  else {
						  //System.out.println("available length=" + availLen);
					  }
					  
					  if (pos + availLen > 1024) {
						  readLen = 1024 - pos;
						  endOfBuffer = true;
					 } else {
						  readLen = availLen;
					  }
					  //System.out.println("readLen=" + readLen + " pos = " + pos);
					  inputSockStream.read(b, pos, readLen);


					  if (endOfBuffer == true) {
						  //System.out.println("fullbuffer taskresponse length=" 
						//		  	+ taskresponse.length());
						  c = 0;
						  while (c < 1024) {
							  if (b[c] == 0) break;
							  readStarted = true;
							  taskresponse += (char) b[c];
							  //System.out.println("(1)taskresponse=" + taskresponse);
							  b[c] = 0;
							  c++;
						  }
						  //System.out.println("(2)taskresponse=" + taskresponse);
						  pos = 0;
					  } else {
						  //System.out.println("partial buffer ");
						  
						  if ((pos == 0) && (readLen == 0)) {
							  if (readStarted == true) {
								  System.out.println("(2)readLen=" + readLen + " pos = " + pos);
								  continueReading = false;
								  continue;
							  }
						  }
						  c = 0;
						  while (c < 1024) {
							  if (b[c] == 0) break;
							  readStarted = true;
							  taskresponse += (char) b[c];
							  c++;
						  }
						  //System.out.println("(3)taskresponse=" + taskresponse);
						  pos += readLen;
					  }
					  
				  }
				  System.out.println("readStringFromStream len=" + taskresponse.length());
				  return taskresponse;
	}
	
		
	public String readString(Socket s)  throws IOException {
		InputStream in = s.getInputStream();
		String readString = readStringFromStream(in);
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