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
	    		platoon.addTruck(truck1);
	        }
	    },2000);
	    
	    Timer timer1 = new Timer();
	    timer.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	platoon.addTruck(truck2);
	        }
	    },4000);
		
	}

}
