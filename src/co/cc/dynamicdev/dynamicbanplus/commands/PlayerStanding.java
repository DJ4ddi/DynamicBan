package co.cc.dynamicdev.dynamicbanplus.commands;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
public class PlayerStanding implements CommandExecutor {

	private DynamicBan plugin;
	private File playerDataFile = null;
	public PlayerStanding(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynstanding")) {
			if (!plugin.permissionCheck(cs, "player.standing")) return true;
			
			if (args.length == 0 || args.length > 2) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name] (Page)");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Displays statistics of bans, kicks and warnings of a specified player.");
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
			
			if (args.length == 2) {
				if (args[1].equals("2")) {
					printPage2(cs, pid);
				}
				if (args[1].equals("1")) {
					printPage1(cs, pid);
				}
			} else {
				printPage1(cs, pid);
			}
		}
		return true;
	}

	public void printPage1(CommandSender cs, UUID pid) {
		playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
		YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
		if (playerDataFile.exists()) {
			String playerIP = playerData.getString("IP-Address");
			cs.sendMessage(ChatColor.GOLD + "<<============ " + ChatColor.BOLD + ChatColor.DARK_AQUA + "DynamicBan v" + plugin.getDescription().getVersion() + ChatColor.GOLD + " ============>>");
			String banreason = DynamicBanCache.getPlayerBan(pid);
			if (banreason != null) {
				cs.sendMessage(ChatColor.GOLD + "Banned by DynamicBan: " + ChatColor.AQUA + "Yes");
				cs.sendMessage(ChatColor.GOLD + "--> Banned for: " + banreason);
				cs.sendMessage(ChatColor.GOLD + "--> Banned by: " + DynamicBanCache.getExecutor(pid));
				cs.sendMessage(ChatColor.GOLD + "--> Time of ban: " + DynamicBanCache.getTime(pid));
			} else {
				cs.sendMessage(ChatColor.GOLD + "Banned by DynamicBan: " + ChatColor.AQUA + "No");
			}
			String iptocheck = playerIP;
			banreason = DynamicBanCache.getIpBan(iptocheck);
			if (banreason != null) {
				cs.sendMessage(ChatColor.GOLD + "IP Banned by DynamicBan: " + ChatColor.AQUA + "Yes");
				cs.sendMessage(ChatColor.GOLD + "--> Banned for: " + banreason);
				cs.sendMessage(ChatColor.GOLD + "--> Banned by: " + DynamicBanCache.getExecutor(iptocheck));
				cs.sendMessage(ChatColor.GOLD + "--> Time of ban: " + DynamicBanCache.getTime(iptocheck));
			} else {
				cs.sendMessage(ChatColor.GOLD + "IP-Banned by DynamicBan: " + ChatColor.AQUA + "No");
			}
			String tempBan = DynamicBanCache.getTempBan(pid);
			long tempTime;
			if (tempBan != null) {
				tempTime = Long.valueOf(tempBan.split("::")[0]);
				if (tempTime != 0) {
					long now = System.currentTimeMillis() / 1000;
					long diff = tempTime - now;
					if (diff > 0) {
						cs.sendMessage(ChatColor.GOLD + "TempBanned by DynamicBan: " + ChatColor.AQUA + "Yes");
						cs.sendMessage(ChatColor.GOLD + "--> Banned for: " + tempBan.split("::")[1]);
						cs.sendMessage(ChatColor.GOLD + "--> Banned by: " + DynamicBanCache.getExecutor(pid));
						cs.sendMessage(ChatColor.GOLD + "--> Ban-Length: " + diff + " second(s)");
					}
				} else {
					cs.sendMessage(ChatColor.GOLD + "TempBanned by DynamicBan: " + ChatColor.AQUA + "No");
				}
			} else {
				cs.sendMessage(ChatColor.GOLD + "TempBanned by DynamicBan: " + ChatColor.AQUA + "No");
			}
			tempBan = DynamicBanCache.getTempBan(iptocheck);
			if (tempBan != null) {
				tempTime = Long.valueOf(tempBan.split("::")[0]);
				if (tempTime != 0) {
					long now = System.currentTimeMillis() / 1000;
					long diff = tempTime - now;
					if (diff > 0) {
						cs.sendMessage(ChatColor.GOLD + "TempIPBanned by DynamicBan: " + ChatColor.AQUA + "Yes");
						cs.sendMessage(ChatColor.GOLD + "--> Banned for: " + tempBan.split("::")[1]);
						cs.sendMessage(ChatColor.GOLD + "--> Banned by: " + DynamicBanCache.getExecutor(iptocheck));
						cs.sendMessage(ChatColor.GOLD + "--> Ban-Length: " + diff + " second(s)");
					}
				} else {
					cs.sendMessage(ChatColor.GOLD + "TempIPBanned by DynamicBan: " + ChatColor.AQUA + "No");
				}
			} else {
				cs.sendMessage(ChatColor.GOLD + "TempIPBanned by DynamicBan: " + ChatColor.AQUA + "No");
			}
			if (plugin.getServer().getOfflinePlayer(pid).isBanned()) {
				cs.sendMessage(ChatColor.GOLD + "Banned by Bukkit: " + ChatColor.AQUA + "Yes");
			} else {
				cs.sendMessage(ChatColor.GOLD + "Banned by Bukkit: " + ChatColor.AQUA + "No");
			}
			if (plugin.getServer().getIPBans().contains(playerIP.replace("/", "."))) {
				cs.sendMessage(ChatColor.GOLD + "IP-Banned by Bukkit: " + ChatColor.AQUA + "Yes");
			} else {
				cs.sendMessage(ChatColor.GOLD + "IP-Banned by Bukkit: " + ChatColor.AQUA + "No");
			}
			tempBan = DynamicBanCache.getMute(pid);
			if (tempBan != null) {
				tempTime = Long.valueOf(tempBan.split("::")[0]);
				if (tempTime != 0) {
					long now = System.currentTimeMillis() / 1000;
					long diff = tempTime - now;
					if (diff > 0) {
						cs.sendMessage(ChatColor.GOLD + "Muted by DynamicBan: " + ChatColor.AQUA + "Yes");
						cs.sendMessage(ChatColor.GOLD + "--> Muted for: " + tempBan.split("::")[2]);
						cs.sendMessage(ChatColor.GOLD + "--> Muted by: " + tempBan.split("::")[1]);
						cs.sendMessage(ChatColor.GOLD + "--> Mute-Length: " + diff + " second(s)");
					}
				} else {
					cs.sendMessage(ChatColor.GOLD + "Muted by DynamicBan: " + ChatColor.AQUA + "No");
				}
			} else {
				cs.sendMessage(ChatColor.GOLD + "Muted by DynamicBan: " + ChatColor.AQUA + "No");
			}
			cs.sendMessage(ChatColor.GOLD + "Page 1 of 2");
		} else {
			cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!");
		}
	}

	public void printPage2(CommandSender cs, UUID pid) {
		playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
		YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
		if (playerDataFile.exists()) {
			playerData.options().copyDefaults(true);
			cs.sendMessage(ChatColor.GOLD + "<<============ " + ChatColor.BOLD + ChatColor.DARK_AQUA + "DynamicBan v" + plugin.getDescription().getVersion() + ChatColor.GOLD + " ============>>");
			String[] IP = playerData.getString("IP-Address").replace(".", "/").split("/");
			cs.sendMessage(ChatColor.GOLD + "Times kicked: " + ChatColor.AQUA + playerData.getString("kickedNumber"));
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy '@' HH:mm:ss");
			int currentWarns = 0;
			boolean sent = false;
			for (String s : playerData.getConfigurationSection("warns").getKeys(false)) {
				Date date = null;
				try {
					date = sdf.parse(s);
					Calendar warnendtime = Calendar.getInstance();
					if (date != null) {
						warnendtime.setTime(date);
						warnendtime.add(Calendar.HOUR, plugin.getConfig().getInt("config.warns_timeout"));
					}
					if (!warnendtime.before(Calendar.getInstance()) || plugin.getConfig().getInt("config.warns_timeout") == 0) {
						if (!sent) {
							cs.sendMessage(ChatColor.GOLD + "Warnings:");
							sent = true;
						}
						currentWarns++;
						cs.sendMessage(ChatColor.AQUA + s + " - " + playerData.getString("warns." + s));
					}
				} catch (ParseException e) {
					plugin.getLogger().severe("Date " + s + " could not be parsed.");
				}
			}
			if (currentWarns == 0) {
				cs.sendMessage(ChatColor.GOLD + "Warnings: " + ChatColor.AQUA + "None");
			}
			if (DynamicBanCache.getRangeBan(IP[0]+ "/" + "*"+"/" + "*" +"/" + "*") != null) {
				cs.sendMessage(ChatColor.GOLD + "Range-Banned by DynamicBan: " + ChatColor.AQUA + "Yes");
				cs.sendMessage(ChatColor.GOLD + "--> Level: 3");
			} else {
				if (DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + "*" +"/" + "*") != null) {
					cs.sendMessage(ChatColor.GOLD + "Range-Banned by DynamicBan: " + ChatColor.AQUA + "Yes");
					cs.sendMessage(ChatColor.GOLD + "--> Level: 2");
				} else {
					if (DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + IP[2] +"/" + "*") != null) {
						cs.sendMessage(ChatColor.GOLD + "Range-Banned by DynamicBan: " + ChatColor.AQUA + "Yes");
						cs.sendMessage(ChatColor.GOLD + "--> Level: 1");
					} else {
						cs.sendMessage(ChatColor.GOLD + "Range-Banned by DynamicBan: " + ChatColor.AQUA + "No");
					}
				}
			}
			cs.sendMessage(ChatColor.GOLD + "Page 2 of 2");
		} else {
			cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!"); 
		}
	}
}