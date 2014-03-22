package in.snowcraft.msm;

import java.io.File;

import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Manager extends JavaPlugin{
	
	Server server;
	boolean isRunning;
		
	@Override
	public void onEnable(){
		getLogger().info("Enabled");
		File dataDirectory = getDataFolder();
		if(!dataDirectory.exists()){
			dataDirectory.mkdirs();
		}
		saveDefaultConfig();
		server = new Server(getConfig().getInt("port"), this);
		server.start();
	}
	
	@Override
	public void onDisable(){
		getLogger().info("Disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
		if(StringUtils.startsWithIgnoreCase(cmd.getName(), "msm")){
			if(args[0].equalsIgnoreCase("help") || args[0] == null){
				sender.sendMessage("/msm help: Shows this message\n/msm reload: Reloads config.yml\n/msm password [password]: Sets the login password\n/msm username [username]: Sets the login username\n/msm stop: Stops the login server\n/msm start: Starts the login server. Only works if the server is stopped.");
			} else if(args[0].equalsIgnoreCase("reload")){
				reloadConfig();
			} else if(args[0].equalsIgnoreCase("password")){ //TODO: Add password confirmation if not default
				String hashedPassword = HashString.sha256(args[1].getBytes());
				getConfig().set("password", hashedPassword);
				saveConfig();
				reloadConfig();
				sender.sendMessage("Password has been changed");
				return true;
			} else if(args[0].equalsIgnoreCase("username")){
				getConfig().set("username", args[1]);
				saveConfig();
				reloadConfig();
				sender.sendMessage("Username has been set to " + args[1]);
				return true;
			} else if(args[0].equalsIgnoreCase("start")){ //TODO: Make this work.
				return true;
			} else if(args[0].equalsIgnoreCase("stop")){
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public int getPort(){
		return getConfig().getInt("port");
	}
	
}