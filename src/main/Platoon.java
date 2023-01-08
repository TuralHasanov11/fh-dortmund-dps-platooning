package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import main.Truck.InputHandler;


enum PlatoonState
{
    STOPPED,
    ENGAGING,
    JOINABLE,
};

class V2VRange{
	public float radius;
	
	public V2VRange(int radius) {
		this.radius = radius;
	}
};

public class Platoon{
    private Enum<PlatoonState> state;
    private PlatoonServer server;
    private int serverPort;
    private String serverEndPoint;
	private Thread serverThread;
	private V2VRange v2vRange;
 

    public Platoon(String endpoint, int port) {
    	serverEndPoint = endpoint;
    	serverPort = port;
    	setState(PlatoonState.JOINABLE);
    	setV2vRange(new V2VRange(100));
    	startServer();
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
	

    public void startServer() {
		server = new PlatoonServer(serverEndPoint, serverPort);
		this.serverThread = new Thread(server);
		serverThread.start();
    }
        
	public boolean isRunning() {
		return this.state == PlatoonState.JOINABLE && this.server.isRunning(); 
	}
}
