package co.cc.dynamicdev.dynamicbanplus.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

public class MessageLimitListener extends AbstractListener {	
	private int messagesPerIp;
	
	public MessageLimitListener(DynamicBan plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void pipMessageLimit(PlayerJoinEvent event) {
			if (DynamicBanCache.getPlayersWithIp(DynamicBanCache.getIp(plugin.getUuidAsynch(event.getPlayer().getName()))) > messagesPerIp)
				event.setJoinMessage(null);
	}

	@Override
	public void reload(FileConfiguration config) {
		setEnabled(config.getInt("config.messages_per_ip") > 0, config);
	}

	@Override
	protected void load(FileConfiguration config) {
		messagesPerIp = config.getInt("config.messages_per_ip");
	}
}