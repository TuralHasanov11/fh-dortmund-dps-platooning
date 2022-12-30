package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.json.JSONObject;

enum RequestTypes{
	REQUEST_ENGAGE_REQUESTED,
	REQUEST_ENGAGE_ACCEPTED,
	REQUEST_ENGAGE_REJECTED,
	REQUEST_LEAVE,
	REQUEST_EMERGENCY_BREAKE,
	REQUEST_SEND_PLATOONING_DATA
}

enum ResponseTypes{
	RESPONSE_ENGAGE_ACCEPTED,
	RESPONSE_ENGAGE_REJECTED,
	RESPONSE_LEAVE,
	RESPONSE_EMERGENCY_BREAKE
}


public class PlatoonServer implements Runnable{
	
	private ServerSocket serverSocket;
    private boolean is_running;
    public ArrayList<EchoClientHandler> clients;
    public EchoClientHandler clientToEngage;
    private ExecutorService pool;
    private int serverPort;
    private String serverEndPoint;
    private Platoon platoon;
    
    public PlatoonServer(String endpoint, int port, Platoon platoon) {
    	this.setServerEndPoint(endpoint);
    	this.setServerPort(port);
    	this.platoon = platoon;
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
        
        System.out.println("Platoon Server running on " + Thread.currentThread().getName());
		this.platoon.setState(PlatoonState.JOINABLE);

        
        while (true)
        {
        	EchoClientHandler handler;
			try {
				handler = new EchoClientHandler(serverSocket.accept());
				if(clients.isEmpty()) {
					clients.add(handler);
				}
	        	pool.execute(handler);
			} catch (IOException e) {
				Thread.currentThread().interrupt();
			}
        	
        }
    }

    public void shutdown() throws IOException {
    	pool.shutdown();
        if(!serverSocket.isClosed()) {
        	serverSocket.close();
        }
    }
    
    public boolean isRunning() {
    	return this.is_running;
    }
    
    public EchoClientHandler leaderClient() {
		return clients.get(0);
    }
    

    public String getServerEndPoint() {
		return this.serverEndPoint;
	}

	public void setServerEndPoint(String serverEndPoint) {
		this.serverEndPoint = serverEndPoint;
	}
	
	public void setServerPort(int port) {
		this.serverPort = port;
	}
	
	public int getServerPort() {
		return this.serverPort;
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
				Thread.currentThread().interrupt();
			}
            
    		   
            String inputLine;
            JSONObject data;
            String type;
            JSONObject response;
            try {
				while ((inputLine = in.readLine()) != null) {
					data = new JSONObject(inputLine);
					type = (String) data.get("type");

					if(type.equals(RequestTypes.REQUEST_ENGAGE_REQUESTED.name())) {
					     // send this information to leader truck to make decision
						 clientToEngage = this;
						 leaderClient().sendMessage(inputLine);	
					}
					else if(type.equals(RequestTypes.REQUEST_ENGAGE_ACCEPTED.name())) {
						// add truck to Platoon Clients
						clients.add(clientToEngage);
						// send response to engaging truck
						response = new JSONObject();
						response.put("type", ResponseTypes.RESPONSE_ENGAGE_ACCEPTED);
						clientToEngage.sendMessage(response.toString());
					}
					else if(type.equals(RequestTypes.REQUEST_ENGAGE_REJECTED.name())) {
					     // send response to engaging truck
						response = new JSONObject();
						response.put("type", ResponseTypes.RESPONSE_ENGAGE_REJECTED);
						clientToEngage.sendMessage(response.toString());						
					}
					else if(type.equals(RequestTypes.REQUEST_LEAVE.name())){
						response = new JSONObject();
						response.put("type", ResponseTypes.RESPONSE_LEAVE);
						response.put("truck_id", data.get("truck_id"));
						broadcast(response.toString());
						clients.remove(this);
					}
					else if(type.equals(RequestTypes.REQUEST_EMERGENCY_BREAKE.name())){					
						List<EchoClientHandler> clientsToBroadcast = clients.subList(clients.indexOf(this), clients.size() - 1);
						response = new JSONObject();
						response.put("type", ResponseTypes.RESPONSE_EMERGENCY_BREAKE);
						response.put("truck_id", data.get("truck_id"));
						leaderClient().sendMessage(response.toString());	
						broadcast(response.toString(), clientsToBroadcast);
					}
					else if(type.equals(RequestTypes.REQUEST_SEND_PLATOONING_DATA.name())){
						broadcast(inputLine);
					}
				}
			} catch (IOException e) {
				Thread.currentThread().interrupt();
			}
    	}

        
        public void sendMessage(String message) throws IOException {
        	out.write(message);
        	out.newLine();
			out.flush();
        }
        
        public void shutdown(){
        	try {
				in.close();
				out.close();
	        	if(!clientSocket.isClosed()) {
	        		clientSocket.close();
	        	}
			} catch (IOException e) {
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
        
        public void broadcast(String message, List<EchoClientHandler> clientsToBroadcast) throws IOException {
        	for (EchoClientHandler echoClient : clientsToBroadcast) {
    			if(echoClient != null) {
    				echoClient.sendMessage(message);
    			}
    		}
        }
    }
}
