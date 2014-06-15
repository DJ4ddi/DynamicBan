package co.cc.dynamicdev.dynamicbanplus.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
public class WhitelistAddRemove implements CommandExecutor {
	private DynamicBan plugin;

	public WhitelistAddRemove(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (args.length < 2) {
			if (plugin.getPermission().has(cs, "dynamicban.whitelist.add") || plugin.getPermission().has(cs, "dynamicban.whitelist.remove") || cs.isOp()) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [add/remove] [Name/IP/IP-Range]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Add/remove the specified player from the whitelist.");
				return true;
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.RED + "Sorry, you do not have the permission to use that command!");
				return true;
			}
		}
		if (!(args[0].contains("add") || args[0].contains("remove"))) {
			cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Invalid arguments, use /" + alias + " for more information.");
			return true;
		}
		if (args[1].endsWith("*")) {
			args[1] = plugin.findPlayerName(args[0].substring(0, args[1].length() - 1).toLowerCase(), cs);
			if (args[1] == null) {
				return true;
			}
		}
		
		boolean isIp = false;
		UUID pid = null;
		if(args[1].matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
			isIp = true;
		} else {
			pid = plugin.getUuidAsynch(args[1], plugin.createDelayedCommand(cs, cmd.getName(), args, args[1]));
			if (pid == null) return true;
		}
		
		if (args[0].equalsIgnoreCase("add") && DynamicBanCache.isWhitelisted((isIp) ? args[1] : pid)) {
			cs.sendMessage(plugin.getTag() + ChatColor.RED + "That player is already whitelisted!");
			return true;
		}
		if (args[0].equalsIgnoreCase("remove") && !DynamicBanCache.isWhitelisted((isIp) ? args[1] : pid)) {
			cs.sendMessage(plugin.getTag() + ChatColor.RED + "That player is not whitelisted!");
			return true;
		}

		if (args[0].equalsIgnoreCase("add")) {
			if (plugin.getPermission().has(cs, "dynamicban.whitelist.add") || cs.isOp()) {
				DynamicBanCache.addWhitelisted((isIp) ? args[1] : pid, cs.getName());
				cs.sendMessage(plugin.getTag() + ChatColor.GREEN + args[1] + " has been added to the whitelist!");
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.RED + "Sorry, you do not have the permission to use that command!");
			}
		} else if (plugin.getPermission().has(cs, "dynamicban.whitelist.remove")) {
			DynamicBanCache.removeWhitelisted((isIp) ? args[1] : pid);
			cs.sendMessage(plugin.getTag() + ChatColor.GREEN + args[1] + " has been removed from the whitelist!");
		} else {
			cs.sendMessage(plugin.getTag() + ChatColor.RED + "Sorry, you do not have the permission to use that command!");
		}
		return true;
	}
}