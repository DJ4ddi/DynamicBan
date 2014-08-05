package co.cc.dynamicdev.dynamicbanplus.uuidmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;

/**
 * A cache of username->UUID mappings that automatically cleans itself.
 *
 * This cache is meant to be used in plugins such that plugins can look up the
 * UUID of a player by using the name of the player.
 *
 * For the most part, when the plugin asks the cache for the UUID of an online
 * player, it should have it available immediately because the cache registers
 * itself for the player join/quit events and does background fetches.
 *
 * @author Adrian Haberecht
 *
 */
public class UUIDCache implements Listener {
	private static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private Map<String, UUID> cache = new CaseInsensitiveConcurrentHashMap<UUID>();
	private Map<UUID, String> reverseCache = null;
	private Map<DelayedCommand, String> commandQueue = new ConcurrentHashMap<DelayedCommand, String>();
	
	private DynamicBan plugin;
	private boolean alwaysFetchUuids;

	public UUIDCache(DynamicBan plugin, boolean onlineMode, boolean compatibilityMode) {
		Validate.notNull(plugin);
		this.plugin = plugin;
		alwaysFetchUuids = !onlineMode || compatibilityMode;
		if (!onlineMode)
			reverseCache = new HashMap<UUID, String>();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Get the associated name for the given UUID from the cache.
	 * 
	 * @param uuid The UUID to search for.
	 * @return The associated name if the UUID is stored, null otherwise.
	 */
	public String getName(UUID uuid) {
		if (reverseCache != null)
			return reverseCache.get(uuid);
		return null;
	}
	
	/**
	 * Get the UUID from the cache for the player with the given name.
	 * 
	 * If the id is not in the cache, then queue a fetch to get it, return null
	 * and queue the command that will be executed once the fetch is finished.
	 * 
	 * @param name The player name to search for.
	 * @return The UUID if it is already stored, null otherwise.
	 */
	public UUID getIdAsynch(String name) {
		return getId(name, null, false);
	}
	
	/**
	 * @see UUIDCache#getIdAsynch(String)
	 * @param command The command to be executed after the fetch is finished.
	 * @see DelayedCommand#DelayedCommand(org.bukkit.command.CommandSender, String, String)
	 */
	public UUID getIdAsynch(String name, DelayedCommand command) {
		return getId(name, command, false);
	}
	
	/**
	 * Get the UUID from the cache for the player with the given name, with
	 * blocking get.
	 *
	 * If the id is not in the cache, then fetch the UUID in a blocking
	 * fashion. Note that this will block the thread until the fetch is
	 * complete, so do not call this from the main thread.
	 *
	 * @param name The player name to search for.
	 * @return The UUID of the player.
	 */
	public UUID getIdSynch(String name) {
		return getId(name, null, true);
	}
	
	private UUID getId(String name, DelayedCommand command, boolean synch) {
		Validate.notEmpty(name);
		UUID uuid = cache.get(name);
		if (uuid == null) {
			if (synch) {
				syncFetch(nameList(name));
				return cache.get(name);
			} else {
				if (command != null)
					commandQueue.put(command, name);
				ensurePlayerUUID(name);
			}
		} else if (uuid.equals(ZERO_UUID)) {
			uuid = null;
		}
		return uuid;
	}
	
	private void ensurePlayerUUID(String name) {
		if (cache.containsKey(name)) return;
		cache.put(name, ZERO_UUID);
		asyncFetch(nameList(name));
	}
	
	private void ensurePlayerUUID(Player p) {
		if (cache.containsKey(p.getName())) return;
		put(p.getName(), p.getUniqueId());
	}

	private void asyncFetch(final ArrayList<String> names) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				syncFetch(names);
			}
		});
	}

	private void syncFetch(ArrayList<String> names) {
		final UUIDFetcher fetcher = new UUIDFetcher(names);
		Map<String, UUID> newEntries = null;
		try {
			newEntries = fetcher.call();
			if (plugin.getConfig().getBoolean("config.allow_offline_players"))
				for (String s : names)
					if (newEntries.get(s) == null)
						newEntries.put(s, offlineFetch(s));
		} catch (Exception e) {
			if (plugin.getConfig().getBoolean("config.allow_offline_players"))
				newEntries = offlineFetch(names);
		}
		
		if (newEntries != null) {
			for (Entry<String, UUID> e : newEntries.entrySet())
				put(e.getKey(), e.getValue());			
			executeDelayedCommand(names, newEntries.isEmpty());
			scheduleCleanup(newEntries);
		}
	}

	private Map<String, UUID> offlineFetch(ArrayList<String> names) {
		Map<String, UUID> newEntries = new HashMap<String, UUID>();
		for (String s : names)
			newEntries.put(s, offlineFetch(s));
		return newEntries;
	}
	
	@SuppressWarnings("deprecation")
	private UUID offlineFetch(String name) {
		return plugin.getServer().getOfflinePlayer(name).getUniqueId();
	}

	private void scheduleCleanup(final Map<String, UUID> entries) {
		plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
			public void run() {
				for (Entry<String, UUID> e : entries.entrySet())
					if (cache.containsKey(e.getKey()) && plugin.getPlayer(e.getValue()) == null) {
						remove(e.getKey());
					}
			}
		}, 12000L);
	}

	private ArrayList<String> nameList(String name) {
		ArrayList<String> names = new ArrayList<String>();
		names.add(name);
		return names;
	}

	private void executeDelayedCommand(ArrayList<String> names, boolean fail) {
		for (String s : names)
			for (Entry<DelayedCommand, String> e : commandQueue.entrySet())
				if (e.getValue().equalsIgnoreCase(s)) {
					DelayedCommand c = e.getKey();
					if (fail) c.fail();
					else c.succeed();
					commandQueue.remove(c);
				}
	}
	
	/* Replaces the cache.put(String key, UUID value) method */
	private void put(String key, UUID value) {
		cache.put(key, value);
		if (reverseCache != null)
			reverseCache.put(value, key);
	}
	
	/* Replaces the cache.remove(String key) method */
	private void remove(String key) {
		if (reverseCache != null)
			reverseCache.remove(cache.remove(key));
		else cache.remove(key);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerLogin(PlayerLoginEvent event) {
		if (alwaysFetchUuids) ensurePlayerUUID(event.getPlayer().getName());
		else ensurePlayerUUID(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerQuit(PlayerQuitEvent event) {
		remove(event.getPlayer().getName());
	}
}
