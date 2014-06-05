package co.cc.dynamicdev.dynamicbanplus.uuidmanagement;

import java.util.ArrayList;
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
 * @author James Crasta
 * @author Adrian Haberecht
 *
 */
public class UUIDCache implements Listener {
	private static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private Map<String, UUID> cache = new CaseInsensitiveConcurrentHashMap<UUID>();
	private Map<String, DelayedCommand> commandQueue = new ConcurrentHashMap<String, DelayedCommand>();
	
	private DynamicBan plugin;
	private boolean alwaysFetchUuids;

	public UUIDCache(DynamicBan plugin, boolean alwaysFetchUuids) {
		Validate.notNull(plugin);
		this.plugin = plugin;
		this.alwaysFetchUuids = alwaysFetchUuids;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Get the UUID from the cache for the player named 'name'.
	 * 
	 * If the id does not exist in our database, then we will queue a fetch to
	 * get it, return null and queue the command that will be executed once
	 * the fetch is finished.
	 * 
	 * The asynchronous fetch will be repeated up to 3 times with a 10 second
	 * delay.
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
	 * Get the UUID from the cache for the player named 'name', with blocking get.
	 *
	 * If the player named is not in the cache, then we will fetch the UUID in
	 * a blocking fashion. Note that this will block the thread until the fetch
	 * is complete, so only use this in a thread or in special circumstances.
	 * 
	 * The synchronous fetch will not be repeated.
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
					commandQueue.put(name, command);
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
		cache.put(p.getName(), p.getUniqueId());
	}

	private void asyncFetch(final ArrayList<String> names) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				for (int i = 0; i < 3; i++) {
					if (syncFetch(names)) break;
					try {
						Thread.sleep(5000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private boolean syncFetch(ArrayList<String> names) {
		final UUIDFetcher fetcher = new UUIDFetcher(names);
		try {
			Map<String, UUID> newEntries = fetcher.call();
			cache.putAll(newEntries);
			executeDelayedCommand(names, newEntries.isEmpty());
			scheduleCleanup(newEntries);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void scheduleCleanup(final Map<String, UUID> entries) {
		plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
			public void run() {
				for (Entry<String, UUID> e : entries.entrySet())
					if (cache.containsKey(e.getKey()) && plugin.getServer().getPlayer(e.getValue()) == null)
						cache.remove(e.getKey());
			}
		}, 12000L);
	}

	private ArrayList<String> nameList(String name) {
		ArrayList<String> names = new ArrayList<String>();
		names.add(name);
		return names;
	}

	private void executeDelayedCommand(ArrayList<String> names, boolean fail) {
		DelayedCommand command;
		for (String s : names) {
			command = commandQueue.get(s);
			if (command != null) {
				if (fail) command.fail();
				else command.succeed();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerLogin(PlayerLoginEvent event) {
		if (alwaysFetchUuids) ensurePlayerUUID(event.getPlayer().getName());
		else ensurePlayerUUID(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerQuit(PlayerQuitEvent event) {
		cache.remove(event.getPlayer().getName());
	}
}
