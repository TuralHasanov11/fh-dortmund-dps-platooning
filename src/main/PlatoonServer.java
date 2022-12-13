package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlatoonServer implements Runnable{
	
	private ServerSocket serverSocket;
    private boolean is_running;
    public ArrayList<EchoClientHandler> clients;
    private ExecutorService pool;
    private int serverPort;
    private String serverEndPoint;
    
    public PlatoonServer(String endpoint, int port) {
    	this.serverEndPoint = endpoint;
    	this.serverPort = port;
    }
    
    @Override
    public void run(){
        try {
			serverSocket = new ServerSocket(serverPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
        this.pool = Executors.newCachedThreadPool();
        this.clients = new ArrayList<EchoClientHandler>();
        this.is_running = true;
        while (true)
        {
        	EchoClientHandler handler;
			try {
				handler = new EchoClientHandler(serverSocket.accept());
				this.clients.add(handler);
//	        	handler.start();
	        	pool.execute(handler);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        }
    }

    public void stop() throws IOException {
    	pool.shutdown();
        if(!serverSocket.isClosed()) {
        	serverSocket.close();
        }
    }
    
    public boolean isRunning() {
    	return this.is_running;
    }
    

    private class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
        private BufferedWriter out;


        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        
    	@Override
    	public void run() {
    		try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            String inputLine;
            try {
				while ((inputLine = in.readLine()) != null) {
					this.broadcast(inputLine);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

        
        public void broadcast(String message) throws IOException {
        	for (EchoClientHandler echoClient : clients) {
				if(echoClient != null) {
					echoClient.sendMessage(message);
				}
			}
        }
        
        public void sendMessage(String message) throws IOException {
        	this.out.write(message);
        	this.out.newLine();
			this.out.flush();
        }
        
        public void shutdown() throws IOException {
        	in.close();
        	out.close();
        	if(!clientSocket.isClosed()) {
        		clientSocket.close();
        	}
        }
    }


}
