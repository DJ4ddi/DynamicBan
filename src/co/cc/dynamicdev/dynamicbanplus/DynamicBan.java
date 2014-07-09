package co.cc.dynamicdev.dynamicbanplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import co.cc.dynamicdev.dynamicbanplus.commands.*;
import co.cc.dynamicdev.dynamicbanplus.listeners.*;
import co.cc.dynamicdev.dynamicbanplus.uuidmanagement.DelayedCommand;
import co.cc.dynamicdev.dynamicbanplus.uuidmanagement.UUIDCache;

//Author: xDrapor
//The DynamicDev Team 
//DynamicBan - Comprehensive IP banning.
public class DynamicBan extends JavaPlugin implements Listener {
	private static Permission permission = null;
	private String version;
	protected DynamicLogger log;
	private UUIDCache uuidCache;
	private static File configfile = new File("plugins/DynamicBan/config.yml");
	
	private int serverVersion = 0;

	private String tag = "";
	
	private AbstractListener[] listeners = new AbstractListener[7];

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}
	
	private void updateCheck() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(configfile);
		if(config.getBoolean("config.check_for_updates") == false) {
			System.out.println("[DynamicBan] Update checks disabled in the config.");
			return;
		}
		Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[DynamicBan] Checking the server for update info...");
		try {
			version = getDescription().getVersion();

			int updateVer;
			int curVer;
			int updateHot = 0;
			int curHot = 0;
			int updateBuild;

			URLConnection yc = new URL("https://raw.github.com/DJ4ddi/DynamicBan/master/version.txt").openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));


			String updateVersion = in.readLine().replace(".", "");

			if (Character.isLetter(updateVersion.charAt(updateVersion.length() - 1))) {
				updateHot = Character.getNumericValue(updateVersion.charAt(updateVersion.length() - 1));
				updateVer = Integer.parseInt(updateVersion.substring(0, updateVersion.length() - 1));
			} else {
				updateVer = Integer.parseInt(updateVersion);
			}

			if (Character.isLetter(version.charAt(version.length() - 1))) {
				String tversion = version.replace(".", "");
				curHot = Character.getNumericValue(tversion.charAt(tversion.length() - 1));
				curVer = Integer.parseInt(tversion.substring(0, tversion.length() - 1));
			} else {
				curVer = Integer.parseInt(version.replace(".", ""));
			}

			boolean updateAvailable = false;
			if (updateVer > curVer || updateVer == curVer && updateHot > curHot) {
				Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[DynamicBan] Update available! Check BukkitDev.");
				updateAvailable = true;
			} else {
				Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[DynamicBan] No update available.");
			}

			if (updateAvailable) {
				if (serverVersion != 0) {
					updateBuild = Integer.parseInt(in.readLine());
					if (updateBuild > serverVersion) {
						Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[DynamicBan] It is recommended to update Bukkit to version " + updateBuild);
					} else {
						Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[DynamicBan] The update should be compatible.");
					}
				} else {
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.WARNING, "[DynamicBan] The server version couldn't be parsed.");
				}
			}
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "[DynamicBan] Error performing update check!");
		}
	}

	@Override
	public void onEnable() {
		this.log = new DynamicLogger(this);
		
		try {
			Matcher m = Pattern
					.compile("-b(\\d+)jnks", Pattern.CASE_INSENSITIVE)
					.matcher(getServer().getVersion());
			if (m.find())
				serverVersion = Integer.parseInt(m.group(1));
			else throw new Exception();
		} catch (Exception e) {
			System.out.println("[DynamicBan] Server version couldn't be parsed");
		}
		
		uuidCache = new UUIDCache(this, getServer().getOnlineMode(), serverVersion < 3043 && serverVersion != 0);
		
		getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				updateCheck();
			}
		});
		
		FileConfiguration config = getConfig();
		config.options().copyHeader(true);
		config.options().copyDefaults(true);
		
		if (!config.contains("config.convert_to_uuid"))
			config.set("config.convert_to_uuid", true);
		
		if (!config.contains("config.warn_results")) {
			config.set("config.warn_results.3", "dk {PLAYER} {REASON}");
			config.set("config.warn_results.5", "dtb {PLAYER} 15m {REASON}");
			config.set("config.warn_results.7", "dtb {PLAYER} 30m {REASON}");
			config.set("config.warn_results.9", "dtb {PLAYER} 60m {REASON}");
			config.set("config.warn_results.10", "db {PLAYER} {REASON}");
		}
		
		if (config.getBoolean("config.convert_to_uuid")) {
			convertDatabase();
			config.set("config.convert_to_uuid", false);
		}
		
		saveConfig();
		
		DynamicBanCache.loadAll();
		setupPermissions();
		
		listeners[0] = new MainListener(this);
		listeners[1] = new CommandMuteListener(this);
		listeners[2] = new ConnectionLimitListener(this);
		listeners[3] = new DNSBLListener(this);
		listeners[4] = new IpDuplicateListener(this);
		listeners[5] = new MessageLimitListener(this);
		listeners[6] = new WarnMessageListener(this);
		
		reload();
		getCommand("dynplayer").setExecutor(new PlayerDetails(this));
		getCommand("dynkick").setExecutor(new KickPlayer(this));
		getCommand("dynban").setExecutor(new BanPlayer(this));
		getCommand("dynbanip").setExecutor(new BanPlayerIP(this));
		getCommand("dynunban").setExecutor(new UnbanPlayer(this));
		getCommand("dynunbanip").setExecutor(new UnbanPlayerIP(this));
		getCommand("dynstanding").setExecutor(new PlayerStanding(this));
		getCommand("dyntempban").setExecutor(new TempBan(this));
		getCommand("dyntempbanip").setExecutor(new TempBanIP(this));
		getCommand("dynimmune").setExecutor(new ImmuneAddRemove(this));
		getCommand("dynwarn").setExecutor(new WarnPlayer(this));
		getCommand("dynpurge").setExecutor(new PurgeData(this));
		getCommand("dynlist").setExecutor(new IPList(this));
		getCommand("dynreload").setExecutor(new ReloadData(this));
		getCommand("dyncompare").setExecutor(new CompareIP(this));
		getCommand("dynmute").setExecutor(new Mute(this));
		getCommand("dynunmute").setExecutor(new Unmute(this));
		getCommand("dynlockip").setExecutor(new LockIP(this));
		getCommand("dynunlockip").setExecutor(new UnlockIP(this));
		getCommand("dynrangeban").setExecutor(new RangeBanIP(this));
		getCommand("dynunbanrange").setExecutor(new RangeUnbanIP(this));
		getCommand("dynwhitelist").setExecutor(new WhitelistAddRemove(this));		
		
		System.out.println("[DynaminBan] has been enabled (v" + getDescription().getVersion() + ")");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((JavaPlugin) this);
		System.out.println("[DynaminBan] has been disabled (v" + getDescription().getVersion() + ")");
	}
	
	public void reload() {
		FileConfiguration config = getConfig();
		
		tag = config.getString("config.plugin_tag").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
		for (AbstractListener l : listeners)
			l.reload(config);
	}
	
	public String findPlayerName(String input, CommandSender cs) {
		List<String> results = new ArrayList<String>();
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			String pname = p.getName();
			if (pname.toLowerCase().startsWith(input)) {
				if (!results.contains(pname)) {
					results.add(pname);
				}
			}
		}
		if (results.size() == 1) {
			return results.get(0);
		} else {
			if (results.size() == 0) {
				cs.sendMessage(tag + ChatColor.AQUA + "There is no player using that name.");
			} else {
				if (results.size() > 1) {
					String resultString = results.toString();
					cs.sendMessage(tag + ChatColor.AQUA + "There are multiple players using that name: " + resultString.substring(1, resultString.length() - 1));
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public Player getPlayer(UUID pid) {
		if (getServer().getOnlineMode())
			return getServer().getPlayer(pid);
		String name = uuidCache.getName(pid);
		return (name == null) ? null : getServer().getPlayer(name);
	}
	
	public UUID getUuidAsynch(String name, DelayedCommand command) {
		return uuidCache.getIdAsynch(name, command);
	}
	
	public UUID getUuidAsynch(String name) {
		return uuidCache.getIdAsynch(name);
	}
	
	public DelayedCommand createDelayedCommand(CommandSender cs, String command, String[] args, String fetchedName) {
		StringBuilder commandString = new StringBuilder(command);
		for (String s : args)
			commandString.append(" ").append(s);
		return new DelayedCommand(cs, tag + ChatColor.RED + "The player " + fetchedName + " does not exist.", commandString.toString());
	}
	
	public String getTag() {
		return tag;
	}
	
	public Permission getPermission() {
		return permission;
	}
	
	public boolean permissionCheck(CommandSender cs, String p) {
		if (cs instanceof Player)
			if (!(permission.has(cs, "dynamicban." + p) || cs.isOp())) {
				cs.sendMessage(tag + ChatColor.RED + "Sorry, you do not have the permission to use that command!");
				return false;
			}
		return true;
	}

	public String combineSplit(int startIndex, String[] string, String seperator) {
		StringBuilder builder = new StringBuilder();

		for (int i = startIndex; i < string.length; i++) {
			builder.append(string[i]);
			builder.append(seperator);
		}

		builder.deleteCharAt(builder.length() - seperator.length());
		return builder.toString();
	}
	
	private void convertDatabase() {
		System.out.println("[DynamicBan] Data conversion started.");
		
		System.out.println("Removing old player data...");
		String path = "plugins" + File.separator + "DynamicBan" + File.separator + "playerdata";
		for (File f : new File(path).listFiles()) {
			for (File fi : f.listFiles())
				fi.delete();
			f.delete();
		}
		
		System.out.println("Clearing Bukkit ban lists...");
		clearBanList(getServer().getBanList(BanList.Type.IP));
		clearBanList(getServer().getBanList(BanList.Type.NAME));
		
		System.out.println("Converting data...");
		path = "plugins" + File.separator + "DynamicBan" + File.separator + "data";
		
		new File(path, "ip-log.dat").delete();
		
		File f = new File(path, "immune-players .dat");
		File fi = new File(path, "immune-players.dat");
		if (!fi.exists())
			f.renameTo(fi);
		else if (f.exists())
			f.delete();
		FileConfiguration immunePlayers = YamlConfiguration.loadConfiguration(fi);
		
		List<String> invalidNames = new LinkedList<String>();
		try {
			processFile(path, "banned-players.dat", false, invalidNames);
			processFile(path, "muted-players.dat", false, invalidNames);
			processFile(path, "temp-bans.dat", true, invalidNames);
			processFile(path, "banned-by.dat", true, invalidNames);
			processFile(path, "ban-time.dat", true, invalidNames);
			processConfigurationSection(immunePlayers.getConfigurationSection("immune"), false, invalidNames);
			processConfigurationSection(immunePlayers.getConfigurationSection("whitelist"), true, invalidNames);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			immunePlayers.save(fi);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!invalidNames.isEmpty())
			System.out.println("The following names could not be converted: " + invalidNames.toString());
		System.out.println("[DynamicBan] Conversion complete.");
	}
	
	private void clearBanList(BanList b) {
		for (BanEntry e : b.getBanEntries())
			b.pardon(e.getTarget());
	}
	
	private void processFile(String path, String name, boolean mixedFile, List<String> invalidNames) throws IOException {
		File f = new File(path, name);
		FileConfiguration c = YamlConfiguration.loadConfiguration(f);
		processConfigurationSection(c, mixedFile, invalidNames);
		c.save(f);
	}
	
	private void processConfigurationSection(ConfigurationSection c, boolean mixedFile, List<String> invalidNames) {
		for (String s : convertFile(c, mixedFile))
			if (!invalidNames.contains(s))
				invalidNames.add(s);
	}
	
	private List<String> convertFile(ConfigurationSection data, boolean mixedFile) {
		List<String> invalidNames = new LinkedList<String>();
		if (data != null) {
			for (String s : data.getKeys(false)) {
				if (mixedFile && s.matches("\\d{1,3}/\\d{1,3}/\\d{1,3}/\\d{1,3}")) continue;
				UUID pid = uuidCache.getIdSynch(s);
				if (pid != null)
					data.set(pid.toString(), data.get(s));
				else
					invalidNames.add(s);
				data.set(s, null);
			}
		}
		return invalidNames;
	}
}
