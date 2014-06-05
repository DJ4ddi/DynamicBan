package co.cc.dynamicdev.dynamicbanplus.listeners;

import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

public class CommandMuteListener extends AbstractListener {
	private List<String> blockedCommands;
	private String defaultMuteReason;
	private String blockedMessage;
	
	public CommandMuteListener(DynamicBan plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void commandMuteCheck(PlayerCommandPreprocessEvent event){
		UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
		if (DynamicBanCache.isImmune(pid)) return;
		String mute = DynamicBanCache.getMute(pid);
		if (mute != null) {
			String[] muteField = mute.split("::");
			long tempTime = Long.valueOf(muteField[0]);
			long now = System.currentTimeMillis() / 1000;
			long diff = tempTime - now;
			if (diff > 0)
				for (String i : blockedCommands)
					if (event.getMessage().startsWith("/"+i)) {
						String muteReason = muteField[2];
						if (muteReason.equals("None"))
							muteReason = defaultMuteReason;
						String muteMsg = blockedMessage
								.replace("{REASON}", muteReason)
								.replace("{SENDER}", muteField[1])
								.replace("{TIME}", diff + " seconds")
								.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
						event.getPlayer().sendMessage(muteMsg);
						event.setCancelled(true);
					}
		}
	}

	@Override
	public void reload(FileConfiguration config) {
		setEnabled(config.getStringList("config.mute.blocked_commands").size() > 0, config);
	}

	@Override
	protected void load(FileConfiguration config) {
		blockedCommands = config.getStringList("config.mute.blocked_commands");
		defaultMuteReason = config.getString("other_messages.default_reason");
		blockedMessage = config.getString("messages.muted_command_blocked");
	}
}