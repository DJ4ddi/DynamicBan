package co.cc.dynamicdev.dynamicbanplus.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import co.cc.dynamicdev.dynamicbanplus.DynamicBan;

public abstract class AbstractListener implements Listener {
	protected final DynamicBan plugin;
	
	protected boolean isEnabled = false;
	
	protected AbstractListener(DynamicBan plugin) {
		this.plugin = plugin;
	}
	
	public abstract void reload(FileConfiguration config);
	protected abstract void load(FileConfiguration config);
	
	protected void setEnabled(boolean enable, FileConfiguration config) {
		if (enable) {
			if (!isEnabled)
				enable();
			load(config);
		} else if (isEnabled) {
			disable();
		}
	}
	
	private void enable() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		isEnabled = true;
	}
	
	private void disable() {
		HandlerList.unregisterAll(this);
		isEnabled = false;
	}
}
