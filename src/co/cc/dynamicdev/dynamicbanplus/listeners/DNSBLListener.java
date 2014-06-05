package co.cc.dynamicdev.dynamicbanplus.listeners;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import co.cc.dynamicdev.dynamicbanplus.DNSBL;
import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

public class DNSBLListener extends AbstractListener {
	private DNSBL dnsbl;
	
	private String result;
	private String reason;
	private String broadcast;
	
	public DNSBLListener(DynamicBan plugin) {
		super(plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void pidDnsbl(PlayerLoginEvent event) {
		if (!result.equalsIgnoreCase("none")) {
			UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
			if (pid == null) return;
			final String pip = DynamicBanCache.getIp(pid);
			final String pname = event.getPlayer().getName();
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					dnsblLookup(pip, pname);
				}
			});
		}
	}
	
	private void dnsblLookup(String pip, String pname) {
		if (dnsbl.isBlacklisted(pip.replace("/", "."))) {
			if (result.equals("kick") || result.equals("ban") || result.equals("ipban"))
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), result + " " + pname + " " + reason);
			else
				if (result.equals("notify")) {
					for (Player broadcastto: plugin.getServer().getOnlinePlayers())
						if (plugin.getPermission().has(broadcastto, "dynamicban.check") || broadcastto.isOp())
							broadcastto.sendMessage(plugin.getTag() + broadcast
									.replace("{PLAYER}", pname)
									.replace("{IP}", pip)
									.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2"));
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, plugin.getTag() + pname + "'s ip (" + pip + ") is blacklisted.");
				}
		}
	}

	@Override
	public void reload(FileConfiguration config) {
		setEnabled(!config.getString("config.dnsbl_result").equalsIgnoreCase("none"), config);
	}

	@Override
	protected void load(FileConfiguration config) {
		if (dnsbl == null) {
			try {
				dnsbl = new DNSBL();
			} catch (NamingException e) {
				System.out.println("[DynamicBan] Error initializing DNSBL lookups!");
			}
		}
		dnsbl.clearServices();
		for (String service : config.getStringList("config.dnsbl_services"))
			dnsbl.addService(service);
		
		result = config.getString("config.dnsbl_result");
		reason = config.getString("other_messages.dnsbl_reason");
		broadcast = config.getString("other_messages.dnsbl_ip_message");
	}
}
