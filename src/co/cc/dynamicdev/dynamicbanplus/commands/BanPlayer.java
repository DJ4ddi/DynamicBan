package co.cc.dynamicdev.dynamicbanplus.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
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

public class BanPlayer implements CommandExecutor {

	private static DynamicBan plugin;
	public static Permission permission = null;
	public BanPlayer(DynamicBan plugin) {
		BanPlayer.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynban")) {
			if (!plugin.permissionCheck(cs, "ban.player")) return true;
				
			if (args.length == 0) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name] (Reason)");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Ban the player specified, with an optional reason");
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
			
			String banReason;
			String broadcastReason;
			String afterBanReason;
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy '@' HH:mma");
			String date = sdf.format(today);

			if (args.length == 1) {
				broadcastReason = plugin.getConfig().getString("other_messages.default_reason");
				afterBanReason = "None";
			} else {
				broadcastReason = plugin.combineSplit(1, args, " ");
				afterBanReason = broadcastReason;
			}
			
			banReason = plugin.getConfig().getString("messages.ban_message")
					.replace("{REASON}", broadcastReason)
					.replace("{SENDER}", cs.getName())
					.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
			
			DynamicBanCache.addPlayerBan(pid, afterBanReason, cs.getName(), date);
			if (plugin.getConfig().getBoolean("config.enable_bukkit_bans"))
				Bukkit.getBanList(BanList.Type.NAME).addBan(args[0], afterBanReason, null, cs.getName());
			
			Player targetPlayer = plugin.getPlayer(pid);
			if (targetPlayer != null) {
				targetPlayer.kickPlayer(banReason);
			}
			if (plugin.getConfig().getBoolean("config.broadcast_on_ban")) {
				String broadcastMessage = plugin.getConfig().getString("broadcast_messages.ban_message")
						.replace("{PLAYER}", args[0])
						.replace("{REASON}", broadcastReason)
						.replace("{SENDER}", cs.getName())
						.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
				plugin.getServer().broadcastMessage(broadcastMessage);
			}
		}
		return true;
	}
}