package me.chocolf.moneyfrommobs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.chocolf.moneyfrommobs.MfmManager;
import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.utils.Utils;

public class ReloadCommand implements CommandExecutor{
	
	private MoneyFromMobs plugin;
	
	
	public ReloadCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmreload").setExecutor(this);		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("mfmreload")) {
			if (sender.hasPermission("MoneyFromMobs.reload")) {
				MfmManager manager = plugin.getManager();
				// reloads config
				plugin.reloadConfig();
				plugin.getMMConfig().reloadConfig();
				
				// reloads things
				manager.loadItem();
				manager.loadMessage();
				manager.loadParticlesAndSound();
				manager.loadMultipliers();
				manager.loadCanDropBooleans();
				manager.loadDisabledWorlds();
				
				// reloads bukkit runnable if user is not using paper
				if (!plugin.checkIfPaper()) {
					if (plugin.getInventoryIsFullRunnable() != null) {
						Bukkit.getScheduler().cancelTask(plugin.getInventoryIsFullRunnable().getTaskId());
					}
					plugin.loadInventoryIsFullRunnable();
					
				}
				// sends message saying it loaded correctly
				sender.sendMessage(Utils.applyColour("&9Money From Mobs was reloaded!"));
				return true;
			}else {
				sender.sendMessage(Utils.applyColour("&cYou don't have permission to use this command!"));
				return true;
			}
		}
		return false;
	}
	
}
