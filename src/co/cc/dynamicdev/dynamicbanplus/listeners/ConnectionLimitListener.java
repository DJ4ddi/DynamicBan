package co.cc.dynamicdev.dynamicbanplus.listeners;

import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

public class ConnectionLimitListener extends AbstractListener {
	private int connectionsPerIp;
	private String kickMessage;
	
	public ConnectionLimitListener(DynamicBan plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.LOW)
	void pipLimit(PlayerLoginEvent event) {
		UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
		if (pid == null) return;
		if (!DynamicBanCache.isWhitelisted(pid) && !DynamicBanCache.isWhitelisted(DynamicBanCache.getIp(pid)))
			if (DynamicBanCache.getPlayersWithIp(DynamicBanCache.getIp(pid)) > connectionsPerIp)
				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage
						.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2"));
	}

	@Override
	public void reload(FileConfiguration config) {
		setEnabled(config.getInt("config.connections_per_ip") > 0, config);
	}

	@Override
	protected void load(FileConfiguration config) {
		connectionsPerIp = config.getInt("config.connections_per_ip");
		kickMessage = config.getString("messages.ip_connections_message");
	}
}
