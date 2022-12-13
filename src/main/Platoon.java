package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import main.Truck.InputHandler;


enum PlatoonState
{
    STOPPED,
    AVAILABLE,
    ENGAGING
};

public class Platoon implements Runnable{
    public ArrayList<Truck> trucks;
    private Truck leader_truck;
    private Enum<PlatoonState> state;
    private PlatoonServer server;
    private int serverPort = 4444;
    private String serverEndPoint = "127.0.0.1";
	private Thread serverThread;
 

    public Platoon() {
    	this.trucks = new ArrayList<Truck>();
	}
    
    public void addTruck(Truck truck) {
    	AddTruckHandler addTruckHandler = new AddTruckHandler(truck);
    	Thread t = new Thread(addTruckHandler);
    	t.start();
    }
    
    @Override
    public void run() {
    	System.out.println("Platoon running on " + Thread.currentThread().getName());
		this.setState(PlatoonState.AVAILABLE);
  	  	
//  	  	new Thread(()->{
//			try {
//				this.startServer();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}).start();serverEndPoint,
		this.server = new PlatoonServer(serverEndPoint, serverPort);
		this.serverThread = new Thread(server);
		serverThread.start();
  	  	
  	  	while(true) {
  	  		if(this.state == PlatoonState.AVAILABLE) {
  	  		}
  	  		if(this.state == PlatoonState.ENGAGING) {
	  		}
  	  		if(this.state == PlatoonState.STOPPED) {
	  	  		Thread.currentThread().interrupt();
  	  		}
  	  	}
		
	}
    
    public void setState(PlatoonState state)
	{
	    this.state = state;
	}
	
	public void setLeader(Truck truck)
	{
	    this.leader_truck = truck;
	    this.leader_truck.setIsleader(true);
	}

	public void platoonComplete()
	{
	    this.setState(PlatoonState.STOPPED);
	    System.out.println("Platoon completed");
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
	    		leader_truck.setIsleader(true);
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
