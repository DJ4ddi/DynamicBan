package co.cc.dynamicdev.dynamicbanplus.listeners;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;
import co.cc.dynamicdev.dynamicbanplus.DynamicBanCache;

public class MainListener extends AbstractListener {
	private FileConfiguration config;
	
	public MainListener(DynamicBan plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void pipInit(PlayerLoginEvent event) {
		UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
		if (pid == null) {
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your UUID is being fetched. Please try again in a few seconds.");
			return;
		}
		
		DynamicBanCache.setIp(pid, event.getAddress().toString().split("/")[1].split(":")[0].replace(".", "/"));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void pBan(PlayerLoginEvent event) {
		UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
		if (pid == null) return;
		String pip = DynamicBanCache.getIp(pid);
		
		String banReason = DynamicBanCache.getPlayerBan(pid);
		boolean isIpBan = false;
		boolean isTempBan = false;
		if (banReason == null) {
			banReason = DynamicBanCache.getIpBan(pip);
			if (banReason == null) {
				banReason = DynamicBanCache.getTempBan(pid);
				isTempBan = true;
				if (banReason == null) {
					banReason = DynamicBanCache.getTempBan(pip);
					isIpBan = true;
				}
			} else {
				isIpBan = true;
			}
		}
		
		if (banReason != null && !DynamicBanCache.isWhitelisted(pid) && !DynamicBanCache.isWhitelisted(pip)) {
			String banMessage = "";
			if (isTempBan) {
				String[] tempBanData = banReason.split("::");
				Long banTime = Long.valueOf(tempBanData[0]) - System.currentTimeMillis() / 1000;
				if (banTime <= 0) {
					if (isIpBan) {
						plugin.getServer().unbanIP(pip.replace("/", "."));
						DynamicBanCache.removeTempBan(pip);
					} else {
						plugin.getServer().getBanList(BanList.Type.NAME).pardon(event.getPlayer().getName());
						DynamicBanCache.removeTempBan(pid);
					}
					event.allow();
					return;
				} else {
					banReason = tempBanData[1];
					banMessage = config.getString((isIpBan) ? "messages.ip_tempban_message" : "messages.tempban_message")
							.replace("{TIME}", banTime + " seconds");
				}
			} else {
				banMessage = config.getString((isIpBan) ? "messages.ip_ban_message" : "messages.ban_message");
			}
			
			banMessage = banMessage
					.replace("{REASON}", (banReason.equals("None"))? config.getString("other_messages.default_reason") : banReason)
					.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, banMessage);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void pipRangeBan(PlayerLoginEvent event) {
		UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
		if (pid == null) return;
		if (!DynamicBanCache.isWhitelisted(pid) && !DynamicBanCache.isWhitelisted(DynamicBanCache.getIp(pid))) {
			String[] IP = DynamicBanCache.getIp(pid).split("/");
			String banReason = null;
			if (DynamicBanCache.getRangeBan(IP[0]+ "/" + "*"+"/" + "*" +"/" + "*") != null
					&& !DynamicBanCache.isWhitelisted(IP[0]+ "/" + IP[1] + "/" + IP[2] +"/" + "*")
					&& !DynamicBanCache.isWhitelisted(IP[0]+ "/" + IP[1] + "/" + "*" +"/" + "*")) {
				banReason = DynamicBanCache.getRangeBan(IP[0]+ "/" + "*"+"/" + "*" +"/" + "*");
			} 
			if (DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + "*" +"/" + "*") != null
					&& !DynamicBanCache.isWhitelisted(IP[0]+ "/" + IP[1] + "/" + IP[2] +"/" + "*")) {
				banReason = DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + "*" +"/" + "*");
			}
			if (DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + IP[2] +"/" + "*") != null) {
				banReason = DynamicBanCache.getRangeBan(IP[0]+ "/" + IP[1] + "/" + IP[2] +"/" + "*");
			} 
			if (banReason != null) {
				if (banReason.equals("None")) {
					banReason = config.getString("other_messages.default_reason");
				}
				event.disallow(PlayerLoginEvent.Result.KICK_BANNED, config.getString("messages.rangeban_message")
						.replace("{REASON}", banReason)
						.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2"));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	void pStorageInit(final PlayerJoinEvent event) throws IOException {
		UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy '@' HH:mm");
		String date = sdf.format(today);
		String displayname = event.getPlayer().getDisplayName();
		File playerLoggerFile = new File("plugins/DynamicBan/playerdata/" + pid + "/", "player.dat");		
		FileConfiguration playerLogger = YamlConfiguration.loadConfiguration(playerLoggerFile);
		String ip = DynamicBanCache.getIp(pid);
		if (ip != null) {
			ip.replace("/", ".");
		} else {
			event.setJoinMessage(null);
			if (event.getPlayer() != null) {
				event.getPlayer().kickPlayer("Login failed for an unknown reason. Please try again.");
			}
		}
		if (playerLogger.getString("Initial-IP-Address") == null) {
			playerLogger.set("DisplayName", displayname);
			playerLogger.set("Initial-IP-Address", ip);
			playerLogger.set("IP-Address", ip);
			playerLogger.set("Last-Joined", date);
			playerLogger.set("kickedNumber", 0);
			playerLogger.createSection("warns");
			playerLogger.save(playerLoggerFile);
		} else {
			playerLogger.set("DisplayName", displayname);
			playerLogger.set("IP-Address", ip);
			playerLogger.set("Last-Joined", date);
			if (!playerLogger.contains("warns")) {
				playerLogger.createSection("warns");
			}
			playerLogger.save(playerLoggerFile);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void pipLock(PlayerJoinEvent event) throws IOException {
		Player player = event.getPlayer();
		UUID pid = plugin.getUuidAsynch(player.getName());
		String iptocheck = DynamicBanCache.getIp(pid);
		UUID lockedplayer = DynamicBanCache.getIpLock(iptocheck);
		if (lockedplayer != null) {
			if (!lockedplayer.equals(pid)) {
				String lockedipmsg = config.getString("messages.locked_ip_message")
						.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
				player.kickPlayer(lockedipmsg);
				event.setJoinMessage(null);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void pipDuplicate(PlayerJoinEvent event) {
		if (config.getBoolean("config.broadcast_on_same_ip") == true) {
			UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
			String iptocheck = DynamicBanCache.getIp(pid);
			if (iptocheck != null) {
				if (DynamicBanCache.getLoggedIp(iptocheck) == null) {
					DynamicBanCache.addLoggedIp(iptocheck, pid);
				} else {
					UUID olderPlayer = DynamicBanCache.getLoggedIp(iptocheck);
					if (!(pid.equals(olderPlayer))) {
						for (Player broadcastto: Bukkit.getServer().getOnlinePlayers()) {
							if (plugin.getPermission().has(broadcastto, "dynamicban.check") || broadcastto.isOp()) {
								String sameIPMsg = config.getString("other_messages.same_ip_message")
										.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2")
										.replace("{PLAYER}", event.getPlayer().getName())
										.replace("{IP}", iptocheck.replace("/", "."))
										.replace("{OLDERPLAYER}", Bukkit.getPlayer(olderPlayer).getName());
								broadcastto.sendMessage(plugin.getTag() + sameIPMsg);
							}
						}  
						Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, plugin.getTag() + event.getPlayer().getName() + " logged in with the same IP (" + iptocheck.replace("/", ".") + ") as " + olderPlayer);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	void iplimitQuitCheck(PlayerQuitEvent event) {
		if (config.getInt("config.messages_per_ip") > 0) {
			if (DynamicBanCache.getPlayersWithIp(DynamicBanCache.getIp(plugin.getUuidAsynch(event.getPlayer().getName()))) > config.getInt("config.messages_per_ip")) {
				event.setQuitMessage(null);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void onPlayerQuit(PlayerQuitEvent event) {
		final UUID pid = plugin.getUuidAsynch(event.getPlayer().getName());
		plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
				Player p = plugin.getServer().getPlayer(pid);
				if (p == null || !p.isOnline()) {
					DynamicBanCache.removeIp(pid);
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void chatMuteCheck(AsyncPlayerChatEvent event) {
		if (event.getMessage() != null) {
			Player player = event.getPlayer();
			UUID pid = plugin.getUuidAsynch(player.getName());
			String mute = DynamicBanCache.getMute(pid);
			if (mute != null) {
				String[] muteField = mute.split("::");
				long tempTime = Long.valueOf(muteField[0]);
				long now = System.currentTimeMillis() / 1000;
				long diff = tempTime - now;
				if (DynamicBanCache.isImmune(pid)) {
					return;
				}
				if (diff <= 0 ){
					DynamicBanCache.removeMute(pid);
				} else {
					event.setCancelled(true);
					event.setMessage(null);
					String muteReason = muteField[2];
					if (muteReason.equals("None")) {
						muteReason = config.getString("other_messages.default_reason");
					}
					String muteMsg = config.getString("messages.muted_message")
							.replace("{REASON}", muteReason)
							.replace("{SENDER}", muteField[1])
							.replace("{TIME}", diff + " seconds")
							.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
					player.sendMessage(muteMsg);
				}
			}
		}
	}
	
	@Override
	public void reload(FileConfiguration config) {
		setEnabled(true, config);
	}

	@Override
	protected void load(FileConfiguration config) {
		this.config = config;
	}
}
