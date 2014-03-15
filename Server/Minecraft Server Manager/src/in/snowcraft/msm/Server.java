package in.snowcraft.msm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class Server extends Thread{
	
	public Manager plugin;

	BufferedReader in; 
	PrintWriter out;
	Socket clientSocket;
	ServerSocket serverSocket;
	
	public Server(int port, Manager plugin) { //TODO: Add support for closing specific connections
		this.plugin = plugin;
		try{
			serverSocket = new ServerSocket(port);
		}catch(IOException ex){
			
		}
	}
	
	public void run(){
		while (true) {
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ClientServiceThread thread = new ClientServiceThread(clientSocket, plugin);
			thread.start();
		}
	}
}

class ClientServiceThread extends Thread {
	
	public Manager plugin;
	
	int failedAttempts = 0; //TODO: Add failed attempts
	Socket clientSocket;
	ServerState state = ServerState.login;
	PrintWriter out;
	
	public ClientServiceThread(Socket socket, Manager plugin){
		clientSocket = socket;
		this.plugin = plugin;
	}
	
	public void run(){
		plugin.getLogger().info(clientSocket.getInetAddress().getHostName() + " connected.");
		try { //Socket Timeout Code
			clientSocket.setSoTimeout(15 * 1000); //Socket Timeout Code
		} catch (SocketException e) { //Socket Timeout Code
			e.printStackTrace(); //Socket Timeout Code
		} //Socket Timeout Code
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			String line;
			plugin.getLogger().info("Pre-While loop");
			try {//Socket Timeout Code
				while ((line = in.readLine()) != null) {
					plugin.getLogger().info(line);
				    switch (state) {
				        case login:
							plugin.getLogger().info(line);
				        	plugin.getLogger().info("Starting login state.");
				            processLogin(line);
				            break;
				        case command:
				        	plugin.getLogger().info("Starting command state.");
				            processCommand(line);
				            break;
				    }
				}
			} catch (SocketTimeoutException se) { //Socket Timeout Code
				out.write("Connection has timmed out"); //Socket Timeout Code
				out.flush(); //Socket Timeout Code
				plugin.getLogger().info("Connection has timed out");
				clientSocket.close(); //Socket Timeout Code
			} //Socket Timeout Code
			plugin.getLogger().info("While loop has been exited.");
			out.close();
		}catch(Exception ex){
			plugin.getLogger().info("Exception: " + ex.toString());
		}
	}
	
	void processLogin(String line) throws IOException { //TODO: Add timeout
		if(line.startsWith("login:")){
			try{
				String[] args = line.split(";");
				if(args.length != 2){
					plugin.getLogger().info("Too many/few args. " + args.length);
					for(String arg : args){
						plugin.getLogger().info(arg);
					}
					clientSocket.close();
				} else {
					plugin.getLogger().info("Correct # of args: " + args.length);
					String username = args[0].split(":")[1];
					String password = args[1];
					plugin.getLogger().info("Username: " + username + " Password: " + password +" Expected Username: "+ plugin.getConfig().getString("username") + " Expected Username: " + plugin.getConfig().getString("password"));
					if(username.equals(plugin.getConfig().getString("username")) && password.equals(plugin.getConfig().getString("password"))){
						plugin.getLogger().info("Going to command state");
						out.println("login:success");
						out.flush();
						state = ServerState.command;
						plugin.getLogger().info(clientSocket.getInetAddress().getHostName() + " has successfully logged in!");
					} else {
						plugin.getLogger().info("Login has failed.");
						clientSocket.close();
					}
				}
			} catch (Exception ex){
				String error = new String();
				ex.printStackTrace(new PrintWriter(error));
				plugin.getLogger().info("Exception has been caught: " + error);
				clientSocket.close();
			}	
		} else {
			plugin.getLogger().info("Line did not start with login:");		
		}
	}
	
	void processCommand(String line) throws IOException {
	    if (line.equalsIgnoreCase("list")) {
	    	String players = "List: ";
	    	for(Player player : Bukkit.getServer().getOnlinePlayers()){
	    		players = players.concat(player.getName() + ", ");
	    	}
	    	out.write(players);
	    	out.flush();
	    	plugin.getLogger().info(players);
	    } else if (line.equalsIgnoreCase("quit")) {
	    	plugin.getLogger().info(clientSocket.getInetAddress().getHostName() +  "has disconnected.");
	        clientSocket.close();
	    } else {
	        plugin.getLogger().info("Unrecognized command: " + line);
	    }
	}
}