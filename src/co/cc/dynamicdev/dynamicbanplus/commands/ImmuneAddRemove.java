package co.cc.dynamicdev.dynamicbanplus.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
public class ImmuneAddRemove implements CommandExecutor {

	private DynamicBan plugin;
	
	public ImmuneAddRemove(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (args.length < 2) {
			if (plugin.getPermission().has(cs, "dynamicban.immune.add") || plugin.getPermission().has(cs, "dynamicban.immune.remove") || cs.isOp()) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [add/remove] [name]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Add/remove the specified player from the immune list.");
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
			args[1] = plugin.findPlayerName(args[1].substring(0, args[1].length() - 1).toLowerCase(), cs);
			if (args[1] == null) {
				return true;
			}
		}
		
		UUID pid = plugin.getUuidAsynch(args[1], plugin.createDelayedCommand(cs, cmd.getName(), args, args[1]));
		if (pid == null) return true;
		
		if (args[0].equalsIgnoreCase("add") && DynamicBanCache.isImmune(pid)) {
			cs.sendMessage(plugin.getTag() + ChatColor.RED + "That player is already immune!");
			return true;
		}
		if (args[0].equalsIgnoreCase("remove") && !DynamicBanCache.isImmune(pid)) {
			cs.sendMessage(plugin.getTag() + ChatColor.RED + "That player is not immune!");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("dynimmune")) {
			if (cs instanceof Player) {
				if (plugin.getPermission().has(cs, "dynamicban.immune.add") || cs.isOp()) {
					if (args[0].equalsIgnoreCase("add")) {
						DynamicBanCache.addImmunity(pid, cs.getName().toLowerCase());
						cs.sendMessage(plugin.getTag() + ChatColor.GREEN + args[1] + " has been added to the list of immune players!");
						return true;
					} else if (args[0].equalsIgnoreCase("remove")) {
						if(plugin.getPermission().has(cs, "dynamicban.immune.remove")){
							DynamicBanCache.removeImmunity(pid);
							cs.sendMessage(plugin.getTag() + ChatColor.GREEN + args[1] + " has been removed from the of immune players!");
							return true;
						} else {
							cs.sendMessage(plugin.getTag() + ChatColor.RED + "Sorry, you do not have the permission to use that command!");
						}
					}
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.RED + "Sorry, you do not have the permission to use that command!");
				}
			} else {
				if (args[0].equalsIgnoreCase("add")) {
					DynamicBanCache.addImmunity(pid, cs.getName().toLowerCase());
					cs.sendMessage(plugin.getTag() + ChatColor.GREEN + args[1] + " has been added to the list of immune players!");
					return true;
				} else if (args[0].equalsIgnoreCase("remove")) {
					DynamicBanCache.removeImmunity(pid);
					cs.sendMessage(plugin.getTag() + ChatColor.GREEN + args[1] + " has been removed from the list of immune players!");
					return true;
				}
			}
		}
		return true;
	}
}