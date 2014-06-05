package co.cc.dynamicdev.dynamicbanplus.commands;

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

public class IPList implements CommandExecutor {

	public int numberOfOnlinePlayers() {
		Player[] onlinePlayers = plugin.getServer().getOnlinePlayers();
		return onlinePlayers.length;
	}
	private DynamicBan plugin;

	public IPList(DynamicBan plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dynlist")) {
			if (cs instanceof Player) {
				cs.sendMessage(ChatColor.BLACK + "[" + ChatColor.DARK_AQUA + "DynamicBan" + ChatColor.BLACK + "]" + ChatColor.WHITE + " : " + ChatColor.RED + "This command can only be used by the console!");
				return true;
			}
			if (numberOfOnlinePlayers() == 0) {
				cs.sendMessage("[DynamicBan] : No players online!");
				return true;
			} else {
				cs.sendMessage("[DynamicBan] : List of every onlineplayer's IP-Address!");
				for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
					cs.sendMessage("Name: " + onlinePlayer.getName() + "| IP-Address: " + DynamicBanCache.getIp(onlinePlayer.getUniqueId()).replace("/", "."));
				}
			}
		}
		return true;
	}
}