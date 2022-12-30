package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import main.Truck.InputHandler;


enum PlatoonState
{
    STOPPED,
    AVAILABLE,
    ENGAGING,
    JOINABLE,
    PLATOONING
};

class V2VRange{
	public float radius;
	
	public V2VRange(int radius) {
		this.radius = radius;
	}
};

public class Platoon implements Runnable{
    public ArrayList<Truck> trucks;
    private Truck leader_truck;
    private Enum<PlatoonState> state;
    private PlatoonServer server;
    private int serverPort = 4444;
    private String serverEndPoint = "127.0.0.1";
	private Thread serverThread;
	private V2VRange v2vRange;
 

    public Platoon() {
    	trucks = new ArrayList<Truck>();
    	setState(PlatoonState.AVAILABLE);
    	setV2vRange(new V2VRange(100));
	}
    
    public ArrayList<Truck> getTrucks() {
		return trucks;
	}
    
	public V2VRange getV2vRange() {
		return v2vRange;
	}

	public void setV2vRange(V2VRange v2vRange) {
		this.v2vRange = v2vRange;
	}
    
    public String getServerEndPoint() {
		return serverEndPoint;
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
	
    public void setState(PlatoonState state)
	{
	    this.state = state;
	}
	
	public void setLeader(Truck truck)
	{
	    this.leader_truck = truck;
	    this.leader_truck.setLeader(true);
	}
    
    public void addLeader(Truck truck) {
    	AddTruckHandler addTruckHandler = new AddTruckHandler(truck);
    	Thread t = new Thread(addTruckHandler);
    	t.start();
    }
    
    @Override
    public void run() {
    	System.out.println("Platoon running on " + Thread.currentThread().getName());
		this.server = new PlatoonServer(serverEndPoint, serverPort, this);
		this.serverThread = new Thread(server);
		serverThread.start();
		
  	  	while(true) {
  	  		if(this.state == PlatoonState.JOINABLE) {
  	  		}else if(this.state == PlatoonState.ENGAGING) {
	  		} 
	  		if(this.state.equals(PlatoonState.STOPPED)) {
	  			System.out.println("STOPPPPED");
	  		}
  	  	}
	}
    
    
	public void stop()
	{
	    try {
	    	for (Truck truck : trucks) {
	    		truck.stopConnection();
    		} 	
			this.server.shutdown();
		    this.trucks.clear();
		    System.out.println(trucks);
	    	this.setState(PlatoonState.STOPPED);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRunning() {
		return this.state == PlatoonState.AVAILABLE && this.server.isRunning(); 
	}
	

	class AddTruckHandler implements Runnable{
		
		Truck truck;

		public AddTruckHandler(Truck truck) {
			this.truck = truck;
		}

		@Override
		public void run() {
	    	if(trucks.isEmpty()) {
	    		leader_truck = truck;
	    		leader_truck.setLeader(true);
	    	}
	    	trucks.add(truck);
	    	
	    	try {
				truck.startConnection(serverEndPoint, serverPort);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
