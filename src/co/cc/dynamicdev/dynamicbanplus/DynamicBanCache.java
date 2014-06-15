package co.cc.dynamicdev.dynamicbanplus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class DynamicBanCache {
	private static final String path = "plugins" + File.separator + "DynamicBan" + File.separator + "data";
	
	private static Map<UUID, String> bannedplayers = new HashMap<UUID, String>();
	private static final File bannedplayerfilepath = new File(path, "banned-players.dat");
	private static FileConfiguration bannedplayerfile = YamlConfiguration.loadConfiguration(bannedplayerfilepath);

	private static Map<String, String> bannedips = new HashMap<String, String>();
	private static final File bannedipfilepath = new File(path, "banned-ips.dat");
	private static FileConfiguration bannedipfile = YamlConfiguration.loadConfiguration(bannedipfilepath);

	private static Map<String, String> tempbans = new HashMap<String, String>();
	private static final File tempbanfilepath = new File(path, "temp-bans.dat");
	private static FileConfiguration tempbanfile = YamlConfiguration.loadConfiguration(tempbanfilepath);

	private static Map<String, String> executors = new HashMap<String, String>();
	private static final File executorfilepath = new File(path, "banned-by.dat");
	private static FileConfiguration executorfile = YamlConfiguration.loadConfiguration(executorfilepath);

	private static Map<String, String> timestamps = new HashMap<String, String>();
	private static final File timestampfilepath = new File(path, "ban-time.dat");
	private static FileConfiguration timestampfile = YamlConfiguration.loadConfiguration(timestampfilepath);

	private static Map<String, String> bannedranges = new HashMap<String, String>();
	private static final File bannedrangefilepath = new File(path, "range-bans.dat");
	private static FileConfiguration bannedrangefile = YamlConfiguration.loadConfiguration(bannedrangefilepath);

	private static ArrayList<UUID> immuneplayers = new ArrayList<UUID>();
	private static ArrayList<String> whitelist = new ArrayList<String>();
	private static final File immuneplayerfilepath = new File(path, "immune-players.dat");
	private static FileConfiguration immuneplayerfile = YamlConfiguration.loadConfiguration(immuneplayerfilepath);

	private static Map<UUID, String> mutedplayers = new HashMap<UUID, String>();
	private static final File mutedplayerfilepath = new File(path, "muted-players.dat");
	private static FileConfiguration mutedplayerfile = YamlConfiguration.loadConfiguration(mutedplayerfilepath);

	private static Map<String, UUID> lockedips = new HashMap<String, UUID>();
	private static final File lockedipfilepath = new File(path, "locked-ips.dat");
	private static FileConfiguration lockedipfile = YamlConfiguration.loadConfiguration(lockedipfilepath);

	private static Map<UUID, String> currentips = new HashMap<UUID, String>();
	
	public static void loadAll() {
		for (String s : bannedplayerfile.getKeys(false)) {
			UUID p = UUID.fromString(s);
			if (!bannedplayers.containsKey(p))
				bannedplayers.put(p, bannedplayerfile.getString(s));
		}
		
		for (String s : bannedipfile.getKeys(false))
			if (!bannedips.containsKey(s))
				bannedips.put(s, bannedipfile.getString(s));
		
		for (String s : tempbanfile.getKeys(false))
			if (!tempbans.containsKey(s))
				tempbans.put(s, tempbanfile.getString(s));
		
		for (String s : executorfile.getKeys(false))
			if (!executors.containsKey(s))
				executors.put(s, executorfile.getString(s));
		
		for (String s : timestampfile.getKeys(false))
			if (!timestamps.containsKey(s))
				timestamps.put(s, timestampfile.getString(s));
		
		for (String s : mutedplayerfile.getKeys(false)) {
			UUID p = UUID.fromString(s);
			if (!mutedplayers.containsKey(p))
				mutedplayers.put(p, mutedplayerfile.getString(s));
		}
		
		for (String s : lockedipfile.getKeys(false))
			if (!lockedips.containsKey(s))
				lockedips.put(s, UUID.fromString(lockedipfile.getString(s)));
		
		for (String s : bannedrangefile.getKeys(false))
			if (!bannedranges.containsKey(s))
				bannedranges.put(s, bannedrangefile.getString(s));
		
		if (!immuneplayerfile.contains("immune"))
			immuneplayerfile.createSection("immune");
		for (String s : immuneplayerfile.getConfigurationSection("immune").getKeys(false)) {
			UUID p = UUID.fromString(s);
			if (!immuneplayers.contains(p))
				immuneplayers.add(p);
		}
		
		if (!immuneplayerfile.contains("whitelist"))
			immuneplayerfile.createSection("whitelist");
		for (String s : immuneplayerfile.getConfigurationSection("whitelist").getKeys(false))
			if (!whitelist.contains(s))
				whitelist.add(s);
		
		for (Player p : Bukkit.getServer().getOnlinePlayers())
			currentips.put(p.getUniqueId(), p.getAddress().toString().split("/")[1].split(":")[0].replace(".", "/"));
	}

	public static void reloadAll() {
		bannedplayers.clear();
		bannedips.clear();
		tempbans.clear();
		bannedranges.clear();
		executors.clear();
		timestamps.clear();
		mutedplayers.clear();
		lockedips.clear();
		immuneplayers.clear();
		whitelist.clear();
		currentips.clear();
		loadAll();
	}

	public static String getPlayerBan(UUID p) {
		return bannedplayers.get(p);
	}

	public static String getIpBan(String ip) {
		return bannedips.get(ip);
	}

	public static String getTempBan(String ip) {
		return (tempbans.containsKey(ip)) ? tempbans.get(ip) : null;
	}
	
	public static String getTempBan(UUID id) {
		return getTempBan(id.toString());
	}

	public static String getExecutor(String nameip) {
		return executors.get(nameip);
	}
	
	public static String getExecutor(UUID pid) {
		return executors.get(pid.toString());
	}

	public static String getTime(String nameip) {
		return timestamps.get(nameip);
	}
	
	public static String getTime(UUID pid) {
		return timestamps.get(pid.toString());
	}

	public static String getMute(UUID p) {
		return (mutedplayers.containsKey(p)) ? mutedplayers.get(p) : null;
	}

	public static UUID getIpLock(String ip) {
		return lockedips.get(ip);
	}

	public static String getRangeBan(String range) {
		return bannedranges.get(range);
	}

	public static boolean isImmune(UUID pid) {
		return (immuneplayers.contains(pid)) ? true : false;
	}
	
	public static boolean isWhitelisted(Object o) {
		return (whitelist.contains(o.toString()));
	}
	
	public static String getIp(UUID pid) {
		return currentips.get(pid);
	}
	
	public static int getPlayersWithIp(String ip) {
		int count = 0;
		for (String s : currentips.values())
			if (s.equals(ip))
				count++;
		return count;
	}
	
	public static UUID getOlderPlayerWithIp(String ip, UUID pid) {
		for (Entry<UUID, String> e : currentips.entrySet())
			if (e.getValue().equals(ip) && e.getKey() != pid)
				return e.getKey();
		return null;
	}
	
	public static void setIp(UUID p, String ip) {
		currentips.put(p, ip);
	}
	
	public static void removeIp(UUID pid) {
		currentips.remove(pid);
	}
	
	public static void addBan(String key, String reason, String executor, String date, FileConfiguration file, File path) {
		if (!executors.containsKey(key))
			executors.put(key, executor);
		if (!timestamps.containsKey(key))
			timestamps.put(key, date);

		file.set(key, reason);
		saveToFile(file, path);
		if (!executorfile.contains(key)) {
			executorfile.set(key, executor);
			saveToFile(executorfile, executorfilepath);
		}
		if (!timestampfile.contains(key)) {
			timestampfile.set(key, date);
			saveToFile(timestampfile, timestampfilepath);
		}
	}

	public static void addPlayerBan(UUID p, String reason, String executor, String date) {
		bannedplayers.put(p, reason);
		addBan(p.toString(), reason, executor, date, bannedplayerfile, bannedplayerfilepath);
	}

	public static void addIpBan(String ip, String reason, String executor, String date) {
		bannedips.put(ip, reason);
		addBan(ip, reason, executor,date, bannedipfile, bannedipfilepath);
	}

	public static void addTempBan(String nameip, String time, String executor, String date) {
		tempbans.put(nameip, time);
		addBan(nameip, time, executor, date, tempbanfile, tempbanfilepath);
	}
	
	public static void addTempBan(UUID id, String time, String executor, String date) {
		addTempBan(id.toString(), time, executor, date);
	}

	public static void addRangeBan(String range, String reason, String executor, String date) {
		bannedranges.put(range, reason);
		addBan(range, reason, executor, date, bannedrangefile, bannedrangefilepath);
	}

	public static void addMute(UUID p, String time) {
		mutedplayers.put(p, time);

		mutedplayerfile.set(p.toString(), time);
		saveToFile(mutedplayerfile, mutedplayerfilepath);
	}

	public static void addIpLock(String ip, UUID pid) {
		lockedips.put(ip, pid);

		lockedipfile.set(ip, pid);
		saveToFile(lockedipfile, lockedipfilepath);
	}

	public static void addImmunity(UUID pid, String executor) {
		if (!immuneplayers.contains(pid))
			immuneplayers.add(pid);

		immuneplayerfile.set("immune." + pid, executor);
		saveToFile(immuneplayerfile, immuneplayerfilepath);
	}
	
	public static void addWhitelisted(Object o, String executor) {
		String uuidIp = o.toString();
		if (!whitelist.contains(uuidIp))
			whitelist.add(uuidIp);
		
		immuneplayerfile.set("whitelist." + uuidIp, executor);
		saveToFile(immuneplayerfile, immuneplayerfilepath);
	}
	
	public static void removeBan(String key, FileConfiguration file, File path) {
		executors.remove(key);
		timestamps.remove(key);
		if (file.contains(key)) {
			file.set(key, null);
			saveToFile(file, path);
		}
		if (executorfile.contains(key)) {
			executorfile.set(key, null);
			saveToFile(executorfile, executorfilepath);
		}
		if (timestampfile.contains(key)) {
			timestampfile.set(key, null);
			saveToFile(timestampfile, timestampfilepath);
		}
	}

	public static void removePlayerBan(UUID pid) {
		bannedplayers.remove(pid);
		removeBan(pid.toString(), bannedplayerfile, bannedplayerfilepath);
	}

	public static void removeIpBan(String ip) {
		bannedips.remove(ip);
		removeBan(ip, bannedipfile, bannedipfilepath);
	}

	public static void removeTempBan(String ip) {
		tempbans.remove(ip);
		removeBan(ip, tempbanfile, tempbanfilepath);
	}
	
	public static void removeTempBan(UUID id) {
		removeTempBan(id.toString());
	}

	public static void removeRangeBan(String range) {
		bannedranges.remove(range);
		removeBan(range, bannedrangefile, bannedrangefilepath);
	}
	
	private static void removeFromFile(String key, FileConfiguration file, File path) {
		if (file.contains(key)) {
			file.set(key, null);
			saveToFile(file, path);
		}
	}

	public static void removeMute(UUID pid) {
		mutedplayers.remove(pid);
		removeFromFile(pid.toString(), mutedplayerfile, mutedplayerfilepath);
	}

	public static void removeIpLock(String ip) {
		lockedips.remove(ip);
		removeFromFile(ip, lockedipfile, lockedipfilepath);
	}

	public static void removeImmunity(UUID pid) {
		immuneplayers.remove(pid);
		removeFromFile("immune." + pid.toString(), immuneplayerfile, immuneplayerfilepath);
	}
	
	public static void removeWhitelisted(Object o) {
		whitelist.remove(o.toString());
		removeFromFile("whitelist." + o.toString(), immuneplayerfile, immuneplayerfilepath);
	}
	
	private static void saveToFile(FileConfiguration file, File path) {
		try {
			file.save(path);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
