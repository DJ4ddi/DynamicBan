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
public class UnbanPlayerIP implements CommandExecutor {

	private DynamicBan plugin;
	private File playerDataFile = null;
	public UnbanPlayerIP(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynunbanip")) {
			if (!plugin.permissionCheck(cs, "unban.ip")) return true;
			
			if (args.length == 0) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Unban's a player's IP from the system.");
				return true;
			}
			
			boolean isIp = false;
			UUID pid = null;
			if(args[0].matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
				isIp = true;
			} else {
				pid = plugin.getUuidAsynch(args[0], plugin.createDelayedCommand(cs, cmd.getName(), args, args[0]));
				if (pid == null) return true;
			}
			
			boolean wasBanned = false;
			playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
			if (playerDataFile.exists() || isIp) {
				String pip;
				String pname;
				if (isIp) {
					pip = args[0];
					pname = args[0];
				} else if (playerDataFile.exists()) {
					YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
					pname = playerData.getString("DisplayName");
					pip = playerData.getString("IP-Address").replace(".", "/");
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!");
					return true;
				}

				if (plugin.getServer().getIPBans().contains(pip.replace("/", "."))) {
					wasBanned = true;
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + pname + " is ip-banned by Bukkit, unbanning.");
					plugin.getServer().unbanIP(pip.replace("/", "."));
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + pname + " is not ip-banned by Bukkit.");
				}
				if (DynamicBanCache.getIpBan(pip) != null) {
					wasBanned = true;
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + pname + " has been ip-banned by DynamicBan, unbanning.");
					DynamicBanCache.removeIpBan(pip);
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + pname + " is not ip-banned by DynamicBan.");
				}
				if (DynamicBanCache.getTempBan(pip) != null) {
					wasBanned = true;
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + pname + " has been temporarily ip-banned by DynamicBan, unbanning.");
					DynamicBanCache.removeTempBan(pip);
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + pname + " is not temporarily ip-banned by DynamicBan.");
				}
				if (wasBanned) {
					if (plugin.getConfig().getBoolean("config.broadcast_on_unban")) {
						String broadcastMessage = plugin.getConfig().getString("broadcast_messages.unban_message")
								.replace("{PLAYER}", pname)
								.replace("{SENDER}", cs.getName())
								.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
						plugin.getServer().broadcastMessage(broadcastMessage);
					}
				}
			}
		}
		return true;
	}
}