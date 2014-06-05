package co.cc.dynamicdev.dynamicbanplus.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

public class LockIP implements CommandExecutor {
	private DynamicBan plugin;
	
	public LockIP(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynlockip")) {
			if (!plugin.permissionCheck(cs, "lockip")) return true;
			
			if (args.length < 2) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [IP] [Name]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "IP-Ban the player specified, with an optional reason");
				return true;
			}
			if(!(args[0].contains("1") ||args[0].contains("2") || args[0].contains("3") || args[0].contains("4") || args[0].contains("5") || args[0].contains("6") || args[0].contains("7") || args[0].contains("8") || args[0].contains("9") || args[0].contains("0")) && (!(args[0].contains(".")))){
				cs.sendMessage(plugin.getTag() + ChatColor.RED + "Please use a valid IP!");
				return true;
			}
			if(args[0].contains(":")){
				cs.sendMessage(plugin.getTag() + ChatColor.RED + "Please do not use a port!");
				return true;
			}
			if (args[1].endsWith("*")) {
				args[1] = plugin.findPlayerName(args[1].substring(0, args[1].length() - 1).toLowerCase(), cs);
				if (args[1] == null) {
					return true;
				}
			}
			
			UUID pid = plugin.getUuidAsynch(args[0], plugin.createDelayedCommand(cs, cmd.getName(), args, args[0]));
			if (pid == null) return true;
			
			DynamicBanCache.addIpLock(args[0].replace(".", "/"), pid);
			cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Locked IP " + args[0] +  " to "  + pid);
		}
		return true;
	}
}
