package main;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

	public Main() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws InterruptedException, UnknownHostException, IOException {
		
		Platoon platoon = new Platoon();
		Truck truck1 = new Truck(123);
		Truck truck2 = new Truck(124);
		Truck truck3 = new Truck(125);

		Thread platoonThread = new Thread(platoon);
		Thread truck1Thread = new Thread(truck1);
		Thread truck2Thread = new Thread(truck2);
		
		platoonThread.start();	
		truck1Thread.start();
		truck2Thread.start();
		
		
		Timer timer = new Timer();
	    timer.schedule(new TimerTask() {
	        @Override
	        public void run() {
	    		platoon.addLeader(truck1);
	        }
	    },2000);
	    	    
	    
	    Timer timer2 = new Timer();
	    timer2.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck2.sendEngageRequest(platoon.getServerEndPoint(), platoon.getServerPort());
	        }
	    },4000);
	    
	    
	    Timer timer3 = new Timer();
	    timer3.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck1.accelerate(70, 1);
	        }
	    },7000);
	    
	    
	    Timer timer4 = new Timer();
	    timer4.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck1.accelerate(20, -1);
	        }
	    },12000);
	    

	    
	    Timer timer7 = new Timer();
	    timer7.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck2.leave();
	        }
	    },15000);
	    
	    
//	    Timer timerStopPlatoon = new Timer();
//	    timerStopPlatoon.schedule(new TimerTask() {
//	        @Override
//	        public void run() {
//	        	platoon.stop();
//	        }
//	    },10000);
	    
	    
	}

}
