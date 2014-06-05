package co.cc.dynamicdev.dynamicbanplus.commands;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
public class PurgeData implements CommandExecutor {

	private File playerDataFile = null;
	private FileConfiguration playerData = null;
	private File playerDataDir = null;
	public DynamicBan plugin;

	public PurgeData(DynamicBan plugin) {
		this.plugin = plugin;
	}
	
	UUID pid;

	public void reloadPlayerData() {
		if (playerDataFile == null) {
			playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
		}
		YamlConfiguration.loadConfiguration(playerDataFile);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynpurge")) {
			if (!plugin.permissionCheck(cs, "purge")) return true;
			
			if (args.length < 2) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Usage: /" + cmd.getAliases().toString() + " [Name] [Type]");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Type values: data, kicks, warns");
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Deletes the value of a players data, or all data.");
				return true;
			}
			if (!(args[1].contains("data") || args[1].contains("kicks") || args[1].contains("warns"))) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Invalid type, use /" + alias + " for more information.");
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
			
			playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
			playerData = YamlConfiguration.loadConfiguration(playerDataFile);
			String playerip = playerData.getString("IP-Address");
			if (playerDataFile.exists()  && DynamicBanCache.getPlayerBan(pid) != null || DynamicBanCache.getTempBan(pid) != null) {
				cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "To prevent errors, please unban this player before you purge!");
				return true;
			} else {
				if (playerip != null) {
					if (DynamicBanCache.getIpBan(playerip.replace(".", "/")) != null || DynamicBanCache.getTempBan(playerip.replace(".", "/")) != null) {
						cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "To prevent errors, please unban this player before you purge!");
						return true; 
					}
				}
			}
			if (args[1].equalsIgnoreCase("data")) {
				playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
				playerDataDir = new File("plugins/DynamicBan/playerdata/" + pid);
				if (playerDataFile.exists()) {
					playerDataFile.delete();
					playerDataDir.delete();
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Purged player " + args[0] + "'s data!");
				} else {
					cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!");
				}
			} else {
				if (args[1].equalsIgnoreCase("kicks")) {
					playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
					playerData = YamlConfiguration.loadConfiguration(playerDataFile);
					YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
					if (playerDataFile.exists()) {
						cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Purged player " + playerData.getString("DisplayName") + "'s kicks!");
						playerData.set("kickedNumber", "0");
						try {
							playerData.save(playerDataFile);
							reloadPlayerData();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!");
					}
				} else {
					if (args[1].equalsIgnoreCase("warns")) {
						playerDataFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");
						YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
						if (playerDataFile.exists()) {
							cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "Purged player " + playerData.getString("DisplayName") + "'s warns!");
							playerData.options().copyDefaults(true);
							for (String s : playerData.getConfigurationSection("warns").getKeys(false)) {
								playerData.set("warns." + s, null);
							}
							try {
								playerData.save(playerDataFile);
								reloadPlayerData();
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							cs.sendMessage(plugin.getTag() + ChatColor.AQUA + "No data exists for the specified player!");
						}
					}
				}
			}
		}
		return true;
	}
}