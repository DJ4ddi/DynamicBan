package co.cc.dynamicdev.dynamicbanplus.commands;

import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
public class UnbanPlayer implements CommandExecutor {

	private DynamicBan plugin;
	public UnbanPlayer(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynunban")) {
			if (!plugin.permissionCheck(cs, "unban.player")) return true;
			
			if (args.length == 0 || args.length > 1) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Unbans a player's name from the system.");
				return true;
			}
			
			UUID pid = plugin.getUuidAsynch(args[0], plugin.createDelayedCommand(cs, cmd.getName(), args, args[0]));
			if (pid == null) return true;
			
			boolean wasBanned = false;
			BanList bukkitBans = plugin.getServer().getBanList(BanList.Type.NAME);
			if (bukkitBans.isBanned(args[0])) {
				wasBanned = true;
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " is banned by Bukkit, unbanning.");
				bukkitBans.pardon(args[0]);
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " is not banned by Bukkit.");
			}
			if (DynamicBanCache.getPlayerBan(pid) != null) {
				wasBanned = true;
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " has been banned by DynamicBan, unbanning.");
				DynamicBanCache.removePlayerBan(pid);
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " is not banned by DynamicBan.");
			}
			if (DynamicBanCache.getTempBan(pid) != null) {
				wasBanned = true;
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " has been temp-banned by DynamicBan, unbanning.");
				DynamicBanCache.removeTempBan(pid);
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " is not temp-banned by DynamicBan!");
			}
			if (wasBanned) {
				if (plugin.getConfig().getBoolean("config.broadcast_on_unban")) {
					String broadcastMessage = plugin.getConfig().getString("broadcast_messages.unban_message")
							.replace("{PLAYER}", args[0])
							.replace("{SENDER}", cs.getName())
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
					plugin.getServer().broadcastMessage(broadcastMessage);
				}
			}
		}
		return true;
	}
}