package co.cc.dynamicdev.dynamicbanplus.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

public class UnlockIP implements CommandExecutor {
	private DynamicBan plugin;

	public UnlockIP(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynunlockip")) {
			if (!plugin.permissionCheck(cs, "unlockip")) return true;
			
			if (args.length < 1) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [IP]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Unlock the specified IP.");
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
			String iptocheck = args[0].replace(".", "/");
			if(DynamicBanCache.getIpLock(iptocheck) != null) {
				DynamicBanCache.removeIpLock(iptocheck);
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Unlocked IP " + args[0]);
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "That IP is not locked!");
			}
		}
		return true;
	}


}
