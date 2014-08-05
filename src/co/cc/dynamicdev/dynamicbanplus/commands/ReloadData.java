package co.cc.dynamicdev.dynamicbanplus.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.

public class ReloadData implements CommandExecutor {

	private DynamicBan plugin;
	public ReloadData(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynreload")) {
			if (!plugin.permissionCheck(cs, "reload")) return true;
			
			if (args.length > 0) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + "");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Reloads the DynamicBan data.");
				return true;
			}
			plugin.reloadConfig();
			plugin.reload();
			DynamicBanCache.reloadAll(plugin);
			cs.sendMessage(plugin.getTag() + ChatColor.GREEN + "Reload successful!");
			cs.sendMessage(plugin.getTag() + ChatColor.GREEN + "The following were reloaded:");
			cs.sendMessage(plugin.getTag() + ChatColor.GREEN + "DynamicBan Database");
			cs.sendMessage(plugin.getTag() + ChatColor.GREEN + "Configuration");
		}
		return true;
	}
}