package co.cc.dynamicdev.dynamicbanplus.listeners;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;

public class WarnMessageListener extends AbstractListener {	
	private String warnFormat = "";
	private int delay;
	private int timeout;
	private String warningMessage;
	
	public WarnMessageListener(DynamicBan plugin) {
		super(plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void pWarnings(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		if (delay > 0)
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					sendWarnings(p);
				}
			}, delay * 20);
		else
			sendWarnings(p);
	}
	
	private void sendWarnings(Player p) {
		if (p.isOnline()) {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy '@' HH:mm:ss");
			File playerDataFile = new File("plugins/DynamicBan/playerdata/" + plugin.getUuidAsynch(p.getName()) + "/", "player.dat");
			if (playerDataFile.exists()) {
				FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);
				int currentWarns = 0;
				List<String> warns = new ArrayList<String>();
				for (String s : playerData.getConfigurationSection("warns").getKeys(false)) {
					Date date = null;
					try {
						date = sdf.parse(s);
						Calendar warnendtime = Calendar.getInstance();
						if (date != null) {
							warnendtime.setTime(date);
							warnendtime.add(Calendar.HOUR, timeout);
						}
						if (!warnendtime.before(Calendar.getInstance()) || timeout <= 0) {
							currentWarns++;
							warns.add(s + " - " + playerData.getString("warns." + s));
						}
					} catch (ParseException e) {
						plugin.getLogger().severe("Date " + s + " could not be parsed.");
					}
				}
				if (currentWarns != 0) {
					String message = warningMessage.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
					if (!message.matches("(\u00A7([0-9a-fk-or]))*\\{WARNS\\}.*$"))
						p.sendMessage(plugin.getTag() + message.split("(\u00A7([0-9a-fk-or])){0,2}\\{WARNS\\}")[0]
								.replace("{AMOUNT}", String.valueOf(currentWarns)));
					for (String s : warns)
						p.sendMessage(warnFormat + s);
					if (!message.endsWith("{WARNS}"))
						p.sendMessage(message.split("\\{WARNS\\}")[1].replace("{AMOUNT}", String.valueOf(currentWarns)));
				}
			}
		}
	}

	@Override
	public void reload(FileConfiguration config) {
		setEnabled(config.getBoolean("config.warns_on_login"), config);
	}

	@Override
	protected void load(FileConfiguration config) {
		Matcher findFormat = Pattern.compile("(&([0-9a-fk-or])){1,2}(?=\\{WARNS\\})").matcher(config.getString("other_messages.warnings_message"));
		if (findFormat.find())
			warnFormat = findFormat.group()
					.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
		else
			warnFormat = "";
		
		delay = config.getInt("config.warns_on_login_delay");
		timeout = config.getInt("config.warns_timeout");
		warningMessage = config.getString("other_messages.warnings_message");
		
	}
}
