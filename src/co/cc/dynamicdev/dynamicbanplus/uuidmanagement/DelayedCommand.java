package co.cc.dynamicdev.dynamicbanplus.uuidmanagement;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class DelayedCommand {
	private CommandSender commandSender;
	private String failMessage;
	private String command;
	
	public DelayedCommand(CommandSender cs, String failMessage, String command) {
		commandSender = cs;
		this.failMessage = failMessage;
		this.command = command;
	}
	
	public void fail() {
		commandSender.sendMessage(failMessage);
	}
	
	public void succeed() {
		Bukkit.getServer().dispatchCommand(commandSender, command);
	}
}
