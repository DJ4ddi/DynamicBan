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
public class PlayerDetails implements CommandExecutor {

	private DynamicBan plugin;
	private File playerDataFile = null;
	public PlayerDetails(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynplayer")) {
			if (!plugin.permissionCheck(cs, "player.details")) return true;
			
			if (args.length == 0) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Used to display all logged details of a specified player.");
				return true;
			}
			if (args[0].endsWith("*")) {
				args[0] = plugin.findPlayerName(args[0].substring(0, args[0].length() - 1).toLowerCase(), cs);
				if (args[0] == null) {
					return true;
				}
			}
			
			UUID pid = plugin.getUuidAsynch(args[0], plugin.createDelayedCommand(cs, cmd.getName(), args, args[0]));
			if (pid == null) return true;
			
			if (DynamicBanCache.isImmune(pid)) {
				if (plugin.getConfig().getBoolean("config.op_immune_bypass") == true && cs.isOp()) {
					cs.sendMessage(plugin.getTag() + ChatColor.RED + "Since you are OP, you bypassed " + args[0] + "'s immunity.");
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.RED + "Sorry, that player is immune to your command!");
					return true;
				}
			}
			
			playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
			YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
			if (playerDataFile.exists()) {
				String displayName = playerData.getString("DisplayName");
				if (plugin.getServer().getBannedPlayers().contains(displayName)) {
				} else {
					if (plugin.getServer().getBannedPlayers().contains(args[0])) {
					} else {
						if (plugin.getServer().getBannedPlayers().contains(args[0].toLowerCase())) {
						} else {
						}
						String initialPlayerIP = playerData.getString("Initial-IP-Address");
						String lastJoined = playerData.getString("Last-Joined");
						String ip = DynamicBanCache.getIp(pid);
						cs.sendMessage(ChatColor.GOLD + "<<============ " + ChatColor.BOLD + ChatColor.DARK_AQUA + "DynamicBan v" + plugin.getDescription().getVersion() + ChatColor.GOLD + " ============>>");
						cs.sendMessage(ChatColor.GOLD + "Displaying details for: " + ChatColor.AQUA + displayName);
						if (ip != null) {
							cs.sendMessage(ChatColor.GOLD + "IP-Address: " + ChatColor.AQUA + ip);
						} else {
							cs.sendMessage(ChatColor.GOLD + "The player is not online.");
						}
						cs.sendMessage(ChatColor.GOLD + "Initial-IP-Address: " + ChatColor.AQUA + initialPlayerIP);
						cs.sendMessage(ChatColor.GOLD + "Last seen on: " + ChatColor.AQUA + lastJoined);
					}
				}
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!");
			}
		}
		return true;
	}
}