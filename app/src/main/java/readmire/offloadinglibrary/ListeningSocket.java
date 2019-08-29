package readmire.offloadinglibrary;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Handler;

/**
 *
 *    @yasemin
 **/

public class ListeningSocket {
	
	private ServerSocket serverSocket;
	public Activity act;	

	Handler updateConversationHandler;

	Thread serverThread = null;
	

	public static final int SERVERPORT = 9777;

	public void startToListen(Activity act) {
        this.act = act;
		updateConversationHandler = new Handler();

		this.serverThread = new Thread(new ServerThread());
		this.serverThread.start();

	}
	public void stopThread(){		
	
		if(serverThread != null)
		{
			 Thread t1 = serverThread;
			 serverThread = null;
			 t1.interrupt();
		    try {
		    	if(serverSocket!=null)
				      serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		   
		}
		
	}
	class ServerThread implements Runnable {		
		public void run() {
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(SERVERPORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {

				try {
					    socket = serverSocket.accept();

					CommunicationThread commThread = new CommunicationThread(socket);
					new Thread(commThread).start();

				} catch (IOException e) {
					e.printStackTrace();
					
				}
			}
			
		}
	}

	class CommunicationThread implements Runnable {

		private Socket clientSocket;

		private BufferedReader input;

		public CommunicationThread(Socket clientSocket) {

			this.clientSocket = clientSocket;
		}

		public void run() {

			//while (!Thread.currentThread().isInterrupted()) {

				try {
                      System.out.println("Android start listening port 9777 !");
					  long start = System.currentTimeMillis();	            	  
	            	  ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
	            	  ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
	            	  ServerResult sr = (ServerResult)ois.readObject(); 	            	  
	            	  System.out.println("From Server Received class name and method : "+sr.getClassName()+"."+sr.getMethodName());
	   		       
	   		          //call offlaoding factory to get method result
	   		          Object result = OffloadingFactory.getServerResult(sr,act);	   		          
	                  System.out.println(sr.getMethodName()+" is invoked");		            	 
	            	  oos.writeObject(result);	            	  
	            	  oos.flush();
	            	 
	            	  System.out.println("Android response time for a request :"+ (System.currentTimeMillis()-start) + " ms");	            	  
	            	  oos.close(); 
             
					//String read = input.readLine();

					//updateConversationHandler.post(new updateUIThread(read));

				} catch (IOException e) {
					e.printStackTrace();
					
				} catch (ClassNotFoundException e) {
					
					e.printStackTrace();
				}
			//}
		}

	}

	

}
