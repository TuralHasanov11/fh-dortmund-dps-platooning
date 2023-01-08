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
		
		Truck truck1 = new Truck(123);
		Truck truck2 = new Truck(124);
		Truck truck3 = new Truck(125);
		Truck truck4 = new Truck(126);
		Truck truck5 = new Truck(127);
		Truck truck6 = new Truck(128);

		Thread truck1Thread = new Thread(truck1);
		Thread truck2Thread = new Thread(truck2);
		Thread truck3Thread = new Thread(truck3);
		Thread truck4Thread = new Thread(truck4);
		Thread truck5Thread = new Thread(truck5);
		Thread truck6Thread = new Thread(truck6);
		
		truck1Thread.start();
		truck2Thread.start();
		truck3Thread.start();
		truck4Thread.start();
		truck5Thread.start();
		truck6Thread.start();
		
		
		Timer timer1 = new Timer();
	    timer1.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	try {
					truck1.formPlatoon("127.0.0.1", 4444);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    },2000);
	    	    
	    
	    Timer timer2 = new Timer();
	    timer2.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck2.sendEngageRequest(truck1.getPlatoon().getServerEndPoint(), truck1.getPlatoon().getServerPort());
	        }
	    },6000);
	    
	    
	    Timer timer3 = new Timer();
	    timer3.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck1.accelerate(1);
	        }
	    },12000);
	    
	    
	    Timer timer4 = new Timer();
	    timer4.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck1.accelerate(-1);
	        }
	    },18000);
	    
	    
	    Timer timer5 = new Timer();
	    timer5.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck3.sendEngageRequest(truck1.getPlatoon().getServerEndPoint(), truck1.getPlatoon().getServerPort());
	        	truck4.sendEngageRequest(truck1.getPlatoon().getServerEndPoint(), truck1.getPlatoon().getServerPort());
	        }
	    },24000);
	    
	    
	    Timer timer6 = new Timer();
	    timer6.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck2.leave();
	        }
	    },30000);
	    
	    
	    Timer timer7 = new Timer();
	    timer7.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck5.sendEngageRequest(truck1.getPlatoon().getServerEndPoint(), truck1.getPlatoon().getServerPort());
	        }
	    },36000);
	    
	    
	    Timer timer8 = new Timer();
	    timer8.schedule(new TimerTask() {
	        @Override
	        public void run() {
	        	truck6.sendEngageRequest(truck1.getPlatoon().getServerEndPoint(), truck1.getPlatoon().getServerPort());
	        }
	    },42000);
	    
	    
//	    Timer timer9 = new Timer();
//	    timer9.schedule(new TimerTask() {
//	        @Override
//	        public void run() {
//	        	platoon.getTrucks().get(platoon.getTrucks().size()-1);
//	        }
//	    },48000);
	    
	    
	    
	}

}
