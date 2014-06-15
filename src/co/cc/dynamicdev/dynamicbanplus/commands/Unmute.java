package co.cc.dynamicdev.dynamicbanplus.commands;

import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

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
public class Unmute implements CommandExecutor {

	private static DynamicBan plugin;
	public static Permission permission = null;

	public Unmute(DynamicBan plugin) {
		Unmute.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynunmute")) {
			if (!plugin.permissionCheck(cs, "unmute")) return true;
			
			if (args.length < 1) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Unmute the player specified.");
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
			
			if (DynamicBanCache.getMute(pid) != null) {
				DynamicBanCache.removeMute(pid);
				Player targetPlayer = plugin.getPlayer(pid);
				if (targetPlayer != null) {
					targetPlayer.getPlayer().sendMessage(plugin.getTag() + plugin.getConfig().getString("messages.unmute_message")
							.replace("{SENDER}", cs.getName())
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2"));
				}
				if (plugin.getConfig().getBoolean("config.broadcast_on_unmute")) {
					plugin.getServer().broadcastMessage(plugin.getConfig().getString("broadcast_messages.unmute_message")
							.replace("{PLAYER}", args[0])
							.replace("{SENDER}", cs.getName())
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2"));
				}
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.RED + "That player is not muted");
			}
		}
		return true;
	}
}
