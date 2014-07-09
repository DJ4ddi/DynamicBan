package co.cc.dynamicdev.dynamicbanplus.commands;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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
public class TempBanIP implements CommandExecutor {

	private DynamicBan plugin;
	private File playerDataFile = null;
	boolean valid = true;
	public TempBanIP(DynamicBan plugin) {
		this.plugin = plugin;
	}

	public long parseTimeSpec(String time, String unit) {
		long sec;
		try {
			sec = Integer.parseInt(time) * 60;
			valid = true;
		} catch (NumberFormatException ex) {
			valid = false;
			return 0;
		}
		if (unit.endsWith("h")) {
			sec *= 60;
		} else if (unit.endsWith("d")) {
			sec *= (60 * 24);
		} else if (unit.endsWith("w")) {
			sec *= (7 * 60 * 24);
		} else if (unit.endsWith("t")) {
			sec *= (30 * 60 * 24);
		} else if (unit.endsWith("m")) {
			sec *= 1;
		} else if (unit.endsWith("s")) {
			sec /= 60;
		}
		return sec;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dyntempbanip")) {
			if (!plugin.permissionCheck(cs, "tempban.ip")) return true;
			
			if (args.length < 2) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name] [Amount][Unit]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Unit values: s, m, h, d, w, mt");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Split multiple amounts and units with :");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Temporarily bans the specified players IP for the specified time.");
				return true;
			}
			if (!(args[1].contains("s") || args[1].contains("m") || args[1].contains("h") || args[1].contains("d") || args[1].contains("w") || args[1].contains("t"))) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Invalid unit, use /" + alias + " for more information.");
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
			if (args.length > 2) {
				banReason = plugin.combineSplit(2, args, " ");
				if (banReason.contains("::")) {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Please don't use \"::\" in the reason.");
					return true;
				}
				broadcastReason = banReason;
			} else {
				banReason = "None";
				broadcastReason = plugin.getConfig().getString("other_messages.default_reason");
			}
			playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
			if (playerDataFile.exists()) {
				YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
				String iptoban = playerData.getString("IP-Address").replace(".", "/");
				String[] unit= args[1].split(":");
				
				long tempTimeFinal = System.currentTimeMillis() / 1000;
				for (String s : unit) {
					tempTimeFinal += parseTimeSpec(s.replaceAll("[mhdwts]", ""), s);
				}

				SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy '@' HH:mma");
				DynamicBanCache.addTempBan(iptoban, tempTimeFinal + "::" + banReason, cs.getName(), sdf.format(new Date()));
				if (plugin.getConfig().getBoolean("config.enable_bukkit_bans"))
					plugin.getServer().getBanList(BanList.Type.IP).addBan(iptoban.replace("/", "."), broadcastReason, new Date(tempTimeFinal * 1000), cs.getName());
				
				String timeBanned = args[1].replace(":", " ");
				String banMessage = plugin.getConfig().getString("messages.ip_tempban_message")
						.replace("{TIME}", timeBanned).replace("{REASON}", broadcastReason)
						.replace("{SENDER}", cs.getName())
						.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
				
				Player targetPlayer = plugin.getPlayer(pid);
				if (targetPlayer != null) {
					targetPlayer.kickPlayer(banMessage);
				}
				
				if (valid) {
					if (plugin.getConfig().getBoolean("config.broadcast_on_iptempban") != false) {
						String broadcastMessage = plugin.getConfig().getString("broadcast_messages.ip_tempban_message")
								.replace("{PLAYER}", args[0])
								.replace("{TIME}", timeBanned)
								.replace("{REASON}", broadcastReason)
								.replace("{SENDER}", cs.getName())
								.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
						plugin.getServer().broadcastMessage(broadcastMessage);
					}
					return true;
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Invalid time format, use /" + alias + " for more information.");
				}
			} else {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + args[0] + " has no data stored!");
			}
		}
		return true;
	}
}