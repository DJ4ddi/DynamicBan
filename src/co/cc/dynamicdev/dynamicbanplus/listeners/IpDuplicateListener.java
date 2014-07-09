package co.cc.dynamicdev.dynamicbanplus.listeners;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

public class IpDuplicateListener extends AbstractListener {
	private String sameIpMessage;

	public IpDuplicateListener(DynamicBan plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void pipDuplicate(PlayerJoinEvent event) {
		UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
		String iptocheck = DynamicBanCache.getIp(pid);
		UUID olderPlayer = DynamicBanCache.getOlderPlayerWithIp(iptocheck, pid);
		if (olderPlayer != null) {
			String olderPlayerName = plugin.getPlayer(olderPlayer).getName();
			for (Player broadcastto: Bukkit.getServer().getOnlinePlayers()) {
				if (plugin.getPermission().has(broadcastto, "dynamicban.check") || broadcastto.isOp()) {
					String sameIPMsg = sameIpMessage
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2")
							.replace("{PLAYER}", event.getPlayer().getName())
							.replace("{IP}", iptocheck.replace("/", "."))
							.replace("{OLDERPLAYER}", olderPlayerName);
					broadcastto.sendMessage(plugin.getTag() + sameIPMsg);
				}
			}  
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, plugin.getTag() + event.getPlayer().getName() + " logged in with the same IP (" + iptocheck.replace("/", ".") + ") as " + olderPlayerName);
		}
	}

	@Override
	public void reload(FileConfiguration config) {
		setEnabled(config.getBoolean("config.broadcast_on_same_ip"), config);

	}

	@Override
	protected void load(FileConfiguration config) {
		sameIpMessage = config.getString("other_messages.same_ip_message");
	}

}
