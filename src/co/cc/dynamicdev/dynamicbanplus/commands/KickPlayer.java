package co.cc.dynamicdev.dynamicbanplus.commands;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
public class KickPlayer implements CommandExecutor {

	private DynamicBan plugin;
	private File playerDataFile = null;
	public KickPlayer(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynkick")) {
			if (!plugin.permissionCheck(cs, "kick")) return true;
			
			if (args.length == 0) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [name] (reason)");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Kick the player specified, with an optional reason");
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
			Player playertokick = plugin.getPlayer(pid);
			int kickNumber = playerData.getInt("kickedNumber");
			String kickReason;
			String broadcastReason;

			broadcastReason = (args.length == 1) ?
					plugin.getConfig().getString("other_messages.default_reason")
					: plugin.combineSplit(1, args, " ");

			kickReason = plugin.getConfig().getString("messages.kick_message")
					.replace("{REASON}", broadcastReason)
					.replace("{SENDER}", cs.getName())
					.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");

			if (playertokick == null) {
				cs.sendMessage(plugin.getTag() + ChatColor.RED + args[0] + " is not online!");
				return true;
			} else {
				playertokick.kickPlayer(kickReason);
				if (plugin.getConfig().getBoolean("config.broadcast_on_kick")) {
					String broadcastMessage = plugin.getConfig().getString("broadcast_messages.kick_message")
							.replace("{PLAYER}", playertokick.getName())
							.replace("{REASON}", broadcastReason)
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2")
							.replace("{SENDER}", cs.getName());
					plugin.getServer().broadcastMessage(broadcastMessage);
				}
				
				playerData.set("kickedNumber", kickNumber + +1);
				try {
					playerData.save(playerDataFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
}