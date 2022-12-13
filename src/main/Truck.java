package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;


public class Truck implements Runnable{
	private int id;
	private boolean is_leader;
	private int speed;
	private boolean is_driving;
    private boolean finished = false;

    private Socket clientSocket;
    private BufferedWriter out;
    private BufferedReader in;
    private boolean isConnectedToServer = false;

  public Truck(int id) {
	this.id = id;
	this.setIsleader(false);
	this.setSpeed(50);
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


  public void measureCoordinates() {
	  
  }
  public void measureSpeed() {
	  
  }
    public void measureWheelAngle() {
	  
    }

  	public void sendCoordinates() throws IOException{
//  		String msg1 = this.sendMessage("Truck" + this.getTruckId() + " " + this.getSpeed());
//	    System.out.println(msg1);
	}
  	public void sendSpeed() throws IOException{
  		String message = String.format("%d", this.speed); 
  		this.sendMessage(message);
	}
  	
  	public void readSpeed() throws IOException{
  		String spd = this.readMessage();
  		System.out.println("truck "+this.getTruckId()+ " set speed to " + spd);
	}
  	
  	public void sendWheelAngle(){
//  		String msg1 = this.sendMessage("Truck" + this.getTruckId() + " " + this.getWheelAngle());
//	    System.out.println(msg1);
	}


    void setIsLeader(boolean is_leader) {
	  this.setIsleader(is_leader);
    }

  	void accelerate(int speed){
		this.setSpeed(speed);
	}
  	void decelerate(int speed){
		this.setSpeed(speed);
	}
	
	public boolean isleader() {
		return is_leader;
	}
	
	public void setIsleader(boolean is_leader) {
		this.is_leader = is_leader;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public void startConnection(String endpoint, int port) throws UnknownHostException, IOException {
        clientSocket = new Socket(endpoint, port);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    	System.out.println("Truck " + this.getTruckId() + " connection running on" + Thread.currentThread().getName());

        this.isConnectedToServer = true;
        
        InputHandler inputHandler = new InputHandler();
        Thread t = new Thread(inputHandler);
        t.start();
	    
	    if(this.isleader()) {
	    	Timer timer = new Timer();    
		    timer.scheduleAtFixedRate(this.sendingDataTask(this), 0, 3000);
	    }
	    
//	    Timer readSpeedTimer = new Timer();    
//	    readSpeedTimer.scheduleAtFixedRate(this.readingSpeedTask(this), 0, 1000);
    }

    private TimerTask sendingDataTask(Truck truck) {
		return new TimerTask() {	   
			@Override
			public void run() {
		    	System.out.println("Truck " + truck.getTruckId() + " sending speed on " + Thread.currentThread().getName());
				try {
					truck.sendSpeed();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    };
	}
    
    
    private TimerTask readingSpeedTask(Truck truck) {
		return new TimerTask() {			
			@Override
			public void run() {
				try {
			    	System.out.println("Truck " + truck.getTruckId() + " reading speed on " + Thread.currentThread().getName());
			    	truck.readSpeed();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    };
	}

	public void sendMessage(String msg) throws IOException {
		out.write(msg);
		out.newLine();
        out.flush();
    }
	
	public String readMessage() throws IOException {
		return in.readLine();
	}

    public void stopConnection() throws IOException {
    	finished = true;
		in.close();
        out.close();
        if(!clientSocket.isClosed()) {
        	clientSocket.close();
        }
    }

	public boolean isConnectedToServer() {
		return isConnectedToServer;
	}
	
	class InputHandler implements Runnable{

		@Override
		public void run() {
			while(!finished) {
				try {
					System.out.println("Truck " + getTruckId() + " reading ");
					String message = in.readLine();
										
					// get speed and other data and handle
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
