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
public class CompareIP implements CommandExecutor {

	private DynamicBan plugin;
	private File playerDataFile = null;
	private File playerDataFile1 = null;
	public CompareIP(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dyncompare")) {
			if (!plugin.permissionCheck(cs, "compare")) return true;
			
			if (args.length == 0) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name] [Name]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Compare the ips of two specified players.");
				return true;
			}
			if (args[0].endsWith("*")) {
				args[0] = plugin.findPlayerName(args[0].substring(0, args[0].length() - 1).toLowerCase(), cs);
				if (args[0] == null) {
					return true;
				}
			}
			if (args[1].endsWith("*")) {
				args[1] = plugin.findPlayerName(args[1].substring(0, args[1].length() - 1).toLowerCase(), cs);
				if (args[1] == null) {
					return true;
				}
			}
			
			UUID firstPid = plugin.getUuidAsynch(args[0], plugin.createDelayedCommand(cs, cmd.getName(), args, args[0]));
			if (firstPid == null) return true;
			
			UUID secondPid = plugin.getUuidAsynch(args[1], plugin.createDelayedCommand(cs, cmd.getName(), args, args[1]));
			if (secondPid == null) return true;
			
			if (DynamicBanCache.isImmune(firstPid)) {
				if (plugin.getConfig().getBoolean("config.op_immune_bypass") == true && cs.isOp()) {
					cs.sendMessage(plugin.getTag() + ChatColor.RED + "Since you are OP, you bypassed " + args[0] + "'s immunity.");
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.RED + "Sorry, the first player is immune to your command!");
					return true;
				}
			}
			
			if (DynamicBanCache.isImmune(secondPid)) {
				if (plugin.getConfig().getBoolean("config.op_immune_bypass") == true && cs.isOp()) {
					cs.sendMessage(plugin.getTag() + ChatColor.RED + "Since you are OP, you bypassed " + args[1] + "'s immunity.");
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.RED + "Sorry, the second player is immune to your command!");
					return true;
				}
			}

			playerDataFile = new File("plugins/DynamicBan/playerdata/" + firstPid + "/", "player.dat");
			playerDataFile1 = new File("plugins/DynamicBan/playerdata/" + secondPid + "/", "player.dat");
			YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
			YamlConfiguration playerData1 = YamlConfiguration.loadConfiguration(playerDataFile1);
			if (playerDataFile.exists() && playerDataFile1.exists()) {
				String pname1 = playerData.getString("DisplayName");
				String pname2 = playerData1.getString("DisplayName");
				String IP1 = playerData.getString("IP-Address");
				String IP2 = playerData1.getString("IP-Address");
				cs.sendMessage(ChatColor.GOLD + "<<============ " + ChatColor.BOLD + ChatColor.DARK_AQUA + "DynamicBan v" + plugin.getDescription().getVersion() + ChatColor.GOLD + " ============>>");
				cs.sendMessage(ChatColor.GOLD + "Comparing IP's of: " + ChatColor.AQUA + pname1  + " and " + pname2);
				cs.sendMessage(ChatColor.GOLD + pname1 + "'s IP is: " + IP1);
				cs.sendMessage(ChatColor.GOLD + pname2 + "'s IP is: " + IP2);
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for either or both of the specified players!");
			}
		}
		return true;
	}
}