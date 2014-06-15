package co.cc.dynamicdev.dynamicbanplus.commands;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

@SuppressWarnings("unused")
public class WarnPlayer implements CommandExecutor {

	private DynamicBan plugin;
	private File playerDataFile = null;
	private FileConfiguration playerData = null;

	public WarnPlayer(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (!plugin.permissionCheck(cs, "warn")) return true;
		
		if (args.length == 0) {
			cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name] (Reason)");
			cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Warn the player specified, with an optional reason");
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
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy '@' HH:mm:ss");
		Calendar now = Calendar.getInstance();
		playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
		if (playerDataFile.exists()) {
			YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
			playerData.options().copyDefaults(true);

			int warnNumber = playerData.getInt("warnedNumber");
			String warnReason;
			int currentWarns = 0;
			for (String s : playerData.getConfigurationSection("warns").getKeys(false)) {
				Date date = null;
				try {
					date = sdf.parse(s);
					Calendar warnendtime = Calendar.getInstance();
					if (date != null) {
						warnendtime.setTime(date);
						warnendtime.add(Calendar.HOUR, plugin.getConfig().getInt("config.warns_timeout"));
					}
					if (warnendtime.before(now) && plugin.getConfig().getInt("config.warns_timeout") != 0) {
						playerData.set("warns." + s, null);
					} else {
						currentWarns++;
					}
				} catch (ParseException e) {
					plugin.getLogger().severe("Date " + s + " could not be parsed.");
					playerData.set("warns." + s, null);
				}
			}
			if (args.length == 1) {
				warnReason = plugin.getConfig().getString("other_messages.default_reason");
			} else {
				warnReason = plugin.combineSplit(1, args, " ");
			}

			playerData.set("warns." + sdf.format(now.getTime()), cs.getName() + " - " + warnReason);
			currentWarns++;
			try {
				playerData.save(playerDataFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Player targetPlayer = plugin.getPlayer(pid);
			if (targetPlayer != null) {
				String warnMsg = plugin.getConfig().getString("other_messages.warned_message")
						.replace("{REASON}", warnReason)
						.replace("{SENDER}", cs.getName())
						.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
				targetPlayer.sendMessage(plugin.getTag() + warnMsg);
				targetPlayer.sendMessage(plugin.getTag() + ChatColor.RED + "You have " + currentWarns + " warnings!");
				cs.sendMessage(plugin.getTag() + ChatColor.RED + "You warned " + args[0] + " for " + warnReason);
			}

			if (plugin.getConfig().getBoolean("config.broadcast_on_warn") != false) {
				String broadcastMessage = plugin.getConfig().getString("broadcast_messages.warn_message")
						.replace("{PLAYER}", playerData.getString("DisplayName"))
						.replace("{REASON}", warnReason)
						.replace("{SENDER}", cs.getName())
						.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
				plugin.getServer().broadcastMessage(broadcastMessage);
			}

			for (String s : plugin.getConfig().getConfigurationSection("config.warn_results").getKeys(false)) {
				int warn = Integer.valueOf(s);
				if (currentWarns == warn) {
					String command = plugin.getConfig().getString("config.warn_results." + s)
							.replace("{PLAYER}", playerData.getString("DisplayName"))
							.replace("{REASON}", warnReason);
					try {
						if (!plugin.getServer().dispatchCommand(cs, command)) {
							cs.sendMessage(plugin.getTag() + ChatColor.RED + "The command " + command + " was not found.");
						}
					} catch (CommandException e) {
						cs.sendMessage(plugin.getTag() + ChatColor.RED + "The command " + command + " could not be executed.");
					}
				}
			}
		} else {
			cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!");
		}
		return true;
	}
}