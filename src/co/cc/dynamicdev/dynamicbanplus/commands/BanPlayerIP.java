package co.cc.dynamicdev.dynamicbanplus.commands;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
public class BanPlayerIP implements CommandExecutor {

	private DynamicBan plugin;
	private File playerDataFile = null;
	public static Permission permission;

	public BanPlayerIP(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynbanip")) {
			if (!plugin.permissionCheck(cs, "ban.ip")) return true;
			
			if (args.length == 0) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name] (Reason)");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "IP-Ban the player specified, with an optional reason");
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
				String iptoban = playerData.getString("IP-Address").replace(".", "/");
				String banReason;
				String broadcastReason;
				String afterBanReason;
				Date today = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy '@' HH:mma");
				String date = sdf.format(today);
				if (args.length == 1) {
					banReason = plugin.getConfig().getString("messages.ip_ban_message")
							.replace("{REASON}", plugin.getConfig().getString("other_messages.default_reason"))
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
				} else {
					banReason = plugin.getConfig().getString("messages.ip_ban_message")
							.replace("{REASON}", plugin.combineSplit(1, args, " "))
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
				}

				if (args.length == 1) {
					broadcastReason = plugin.getConfig().getString("other_messages.default_reason");
				} else {
					broadcastReason = plugin.combineSplit(1, args, " ");
				}

				if (args.length == 1) {
					afterBanReason = "None";
				} else {
					afterBanReason = plugin.combineSplit(1, args, " ");
				}
				DynamicBanCache.addIpBan(iptoban, afterBanReason, cs.getName(), date);
				plugin.getServer().getBanList(BanList.Type.IP).addBan(iptoban.replace("/", "."), broadcastReason, null, cs.getName());
				
				Player targetPlayer = plugin.getPlayer(pid);
				if (targetPlayer != null) {
					targetPlayer.kickPlayer(banReason);
				}
				if (plugin.getConfig().getBoolean("config.broadcast_on_ipban")) {
					String broadcastMessage = plugin.getConfig().getString("broadcast_messages.ip_ban_message")
							.replace("{PLAYER}", args[0])
							.replace("{REASON}", broadcastReason)
							.replace("{SENDER}", cs.getName())
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
					plugin.getServer().broadcastMessage(broadcastMessage);
					return true;
				}
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " has no data stored!");
			}
		}
		return true;
	}
}