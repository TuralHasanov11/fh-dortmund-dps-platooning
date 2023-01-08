package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.json.*;

import com.aparapi.Kernel;
import com.aparapi.Range;
import java.util.Random;

enum TruckState{
    JOINABLE,
    LEAVING,
    PLATOONING,
    EMERGENCY_BREAKING
}


public class Truck implements Runnable{
	private int id;
	private boolean is_leader;
	private boolean is_driving;
	private int speed = 0;
	private int acceleration = 0;
	private int wheel_angle = 0;
    private boolean finished = false;
    private Enum<TruckState> mode; 
	private int max_deceleration = 10;
	
	
	private Platoon platoon;
    
    private InputHandler inputHandler;
    private Timer sendDataTimer;

    private Socket clientSocket;
    private BufferedWriter out;
    private BufferedReader in;
    

	  public Truck(int id) {
		this.id = id;
		setMode(TruckState.JOINABLE);
		this.setLeader(false);
	  }
	  
	  void setMode(TruckState mode) {
		  this.mode = mode;
	   }
	  
	  @Override
		public void run() {
			this.drive();
		}
	  
  
	  public void drive() {
		  this.is_driving = true;
	  }
	  
	  public int getTruckId() {
		  return this.id;
	  }


  	public void accelerate(int acceleration){
  		System.out.println("Truck " + getTruckId() + " set acceleration to " + 
  							acceleration + " " +Thread.currentThread().getName());
		this.acceleration = acceleration;
	}
  	
  	public boolean isLeader() {
		return is_leader;
	}
	
	public void setLeader(boolean is_leader) {
		this.is_leader = is_leader;
	}
	
	public void setVehicleData(JSONObject data) {
		new Thread(()->{
			accelerate((int) data.get("acceleration"));
			setWheelAngle((int) data.get("wheel_angle"));       
		}).start();
	}
	
	public JSONObject getCoordinates() {
		JSONObject data = new JSONObject();
		
//   	 	Kernel kernel = new Kernel(){
//            @Override public void run() {
//               int gid = getGlobalId();
//            }
//         };
//         
//         kernel.execute(Range.create(1));
//         
//         kernel.dispose();
         
		data.put("lat", Math.random() * (90 - (-90)) + (-90));
		data.put("long", Math.random() * (180 - (-180)) + (-180));
		return data;
	}
	
	
	public double getAcceleration() {
		return this.acceleration;
	}
	
	public void setWheelAngle(int wheel_angle) {
		this.wheel_angle = wheel_angle;
	}
	
	
	public double getWheelAngle() {
		return this.wheel_angle;
	}
	
	public double getSpeed() {
		return speed;
	}
	
  	
  	void emergencyBreak(){
  		new Thread(()->{
			try {
				JSONObject data = new JSONObject();
				data.put("type", RequestTypes.REQUEST_EMERGENCY_BREAKE);
				data.put("truck_id", getTruckId());
				data.put("acceleration", max_deceleration);
		        sendMessage(data.toString());	        		 
			} catch (IOException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}).start();
	}
	
	
	public void startConnection(String endpoint, int port) throws UnknownHostException, IOException {
        clientSocket = new Socket(endpoint, port);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        
        inputHandler = new InputHandler();
        Thread t = new Thread(inputHandler);
        t.start();
	    if(isLeader()) {
	    	sendDataTimer = new Timer();    
	    	sendDataTimer.scheduleAtFixedRate(this.sendingDataTask(), 0, 2000);
	    }
    }

    private TimerTask sendingDataTask() {
		return new TimerTask() {	   
			@Override
			public void run() {
				try {
					JSONObject data = new JSONObject();
					data.put("type", RequestTypes.REQUEST_SEND_PLATOONING_DATA);
			  		data.put("speed", getSpeed());
			  		data.put("acceleration", getAcceleration());
			  		data.put("coordinates", getCoordinates());
			  		data.put("wheel_angle", getWheelAngle());
			  		sendMessage(data.toString());
				} catch (Exception e) {
					this.cancel();
				}
			}
	    };
	}
   

	public void sendMessage(String msg) throws IOException {
		out.write(msg);
		out.newLine();
        out.flush();
    }

    public boolean stopConnection() throws IOException {
    	if(isLeader()) {
    		sendDataTimer.cancel();
    	}
    	inputHandler.stop();
    	in.close();
        out.close();
        if(!clientSocket.isClosed()) {
        	clientSocket.close();
        }
        this.mode = TruckState.JOINABLE;
        return true;
    }


	public void sendEngageRequest(String endpoint, int port) {
		new Thread(()->{
			try {
				if(this.mode != TruckState.JOINABLE) {
					throw new Exception("Truck " + this.getTruckId() + " Not in Joinable Mode");
				}
				
				clientSocket = new Socket(endpoint, port);
		        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		        
		        inputHandler = new InputHandler();
		        Thread t = new Thread(inputHandler);
		        t.start();
		        
		        JSONObject data = new JSONObject();
				data.put("type", RequestTypes.REQUEST_ENGAGE_REQUESTED);
		  		data.put("truck_id", this.getTruckId());
		  		sendMessage(data.toString());
		      
		        		 
			} catch (Exception e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}).start();
	}
	
	public void sendLeavingRequest() {
		new Thread(()->{
			try {
				JSONObject data = new JSONObject();
				data.put("type", RequestTypes.REQUEST_LEAVE);
		  		data.put("truck_id", getTruckId());
		        sendMessage(data.toString());
		        
		        stopConnection();
			} catch (IOException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}).start();
	}
	
	
	public void leave() {
		this.mode = TruckState.LEAVING;
		this.sendLeavingRequest();
	}
	
	private void decideTruckEngage(int id) {
		new Thread(()->{
			JSONObject data;
			data = new JSONObject();
			Random randomGenerator = new Random(); // randomGenerator.nextBoolean()
			
			if(true) {
				System.out.println("Accepted");
				data.put("type", RequestTypes.REQUEST_ENGAGE_ACCEPTED);
			}else {
				System.out.println("Rejected");
				data.put("type", RequestTypes.REQUEST_ENGAGE_REJECTED);
			}
	  		data.put("truck_id", id);

	  		try {
				sendMessage(data.toString());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	public Platoon getPlatoon() {
		return platoon;
	}
	
	public void formPlatoon(String endpoint, int port) throws UnknownHostException, IOException {
		is_leader = true;
		platoon = new Platoon(endpoint, port);
		startConnection(endpoint, port);
	}
	
	class InputHandler implements Runnable{

		@Override
		public void run() {
			
            JSONObject data;
            String type;
            String inputLine;
            
			while(!finished) {
				try {
					inputLine = in.readLine();
					data = new JSONObject(inputLine);
					type = (String) data.get("type");
					
					
					System.out.println("Truck " + getTruckId() + " reads " + inputLine + 
										" on " + Thread.currentThread().getName());
					
					if(type.equals(RequestTypes.REQUEST_ENGAGE_REQUESTED.name())) {
					     // get Engaging truck id and make decision
						 if(isLeader()) {
							 decideTruckEngage((int) data.get("truck_id"));
						 }
					}
					else if(type.equals(ResponseTypes.RESPONSE_ENGAGE_ACCEPTED.name())) {
					    setMode(TruckState.PLATOONING);
					}
					else if(type.equals(ResponseTypes.RESPONSE_ENGAGE_REJECTED.name())) {
					     // stop connection
						stopConnection();
					}
					else if(type.equals(ResponseTypes.RESPONSE_EMERGENCY_BREAKE.name())) {
			        	accelerate((int) data.get("acceleration"));
			        	stopConnection();
					}
					else if(type.equals(RequestTypes.REQUEST_SEND_PLATOONING_DATA.name())){
						if(!isLeader()) {
							// do some actions based on data coming from leader truck
							setVehicleData(data);
						}
					}
				} catch (Exception e) {
					
					Thread.currentThread().interrupt();
				}
			}
		}
		
		public void stop() {
	        finished = true;
	    }
		
	}

}
