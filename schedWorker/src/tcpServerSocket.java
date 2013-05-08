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
		   Thread.sleep( n );
		   }
		catch ( InterruptedException e )
		   {
		   System.out.println( "awakened prematurely" );
		   }
	}
	
	public void run() {
		while (true) {
			try {			
				Socket s = listenSocket.accept();
				String host = s.getInetAddress().getHostName();
				removeAll();
				acceptSocketList.add(s);
				millisleep(50);
			}catch (Exception error) {
				System.err.println("Unable to listen to server socket");
			}
		}
	}
	

	public void removeAll() {
		Socket acceptSocket = null;
		if (acceptSocketList == null) {
			System.err.println("acceptSocketList not initialised");
			return;
		} else 
		if (acceptSocketList.isEmpty()) {
			//System.err.println("accept Socket not available yet (1)");
			return;
		}
		else {
			Iterator<Socket> iter = acceptSocketList.iterator();
			if (iter == null) {
				System.err.println("accept Socket not available yet (2)");
				return;
			} else {
				while (iter.hasNext()) {
					acceptSocket = iter.next();
					if (acceptSocket != null) {
						System.err.println("found a accept Socket; removing");
						iter.remove();
					} else {
				}
			  }
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
						if (acceptSocket.isClosed()) {
							System.err.println("found a closed accept Socket,removing it");	
							iter.remove();
                                                        acceptSocket = null;
							continue;
						} else {
							//System.err.println("found a open accept Socket");
							//System.err.println("returning an accept Socket");
							break;
						}
					}
				}
			}
			return acceptSocket;
		}
	}
	
	public String readStringFromStream(InputStream in) 
			throws IOException {
		boolean bStart=false;
		boolean bEnd=false;
		StringBuffer out = new StringBuffer();
		String sentinel="";
		byte[] b = new byte[1024];
		int i=0;
		while (bEnd == false) {
			int n;
			n = in.available();
			if (n == 0) {
				if (bStart == true) bEnd=true;
			        else {
				/* If we dont get any data in the stream, we wait for 10 500 millisecond intervals
                                   before concluding to return empty string to upper layer.
                                   As soon as we do that, we send the XML file for parsing.
                                   A better approach is to send the length of the file in the header and waiting till 
                                   we receive that many.
                                 */
				if (i++ > 10) return "END";
				}
			}
			else  {
				n = in.read(b);
				if ((bStart == false) && (n > 0)) bStart=true;
				if (bEnd == true) {
					break;
				}
				sentinel =(new String(b, n-3, 3));
				out.append(new String(b, 0, n));
				System.out.println("sentinel=" + sentinel);

				if (sentinel.equals("END"))
				break;
			}
		}
		return out.toString();
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
