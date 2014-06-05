package co.cc.dynamicdev.dynamicbanplus.commands;

import java.io.File;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
public class RangeUnbanIP implements CommandExecutor {

	private File playerDataFile = null;
	private DynamicBan plugin;

	public RangeUnbanIP(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynunbanrange")) {
			if (!plugin.permissionCheck(cs, "unban.range")) return true;
			
			if (args.length == 0) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Unban a rangebanned player from the system.");
				return true;
			}
			
			UUID pid = plugin.getUuidAsynch(args[0], plugin.createDelayedCommand(cs, cmd.getName(), args, args[0]));
			if (pid == null) return true;
			
			playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
			YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
			if (playerDataFile.exists()) {
				String playerip = playerData.getString("IP-Address").replace(".", "/");
				String[] IP = playerip.split("/");
				if (DynamicBanCache.getRangeBan(IP[0]+ "/" + "*"+"/" + "*" +"/" + "*") != null || DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + "*" +"/" + "*") != null || DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + IP[2] +"/" + "*") != null) {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " has been rangebanned by DynamicBan, unbanning IP-Range.");
					if (DynamicBanCache.getRangeBan(IP[0]+ "/" + "*"+"/" + "*" +"/" + "*") != null) {
						DynamicBanCache.removeRangeBan(IP[0]+ "/" + "*"+"/" + "*" +"/" + "*");
					}
					if (DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + "*" +"/" + "*") != null) {
						DynamicBanCache.removeRangeBan(IP[0]+ "/" + IP[1] + "/" + "*" +"/" + "*");
					}
					if (DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + IP[2] +"/" + "*") != null) {
						DynamicBanCache.removeRangeBan(IP[0]+ "/" + IP[1] + "/" + IP[2] +"/" + "*");
					}
					if (plugin.getConfig().getBoolean("config.broadcast_on_unban")) {
						String broadcastMessage = plugin.getConfig().getString("broadcast_messages.unban_message")
								.replace("{PLAYER}", playerData.getString("DisplayName"))
								.replace("{SENDER}", cs.getName())
								.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
						plugin.getServer().broadcastMessage(broadcastMessage);
					}
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!");
				}
			}
		}
		return true;
	}
}
