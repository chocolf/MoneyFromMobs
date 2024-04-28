package me.chocolf.moneyfrommobs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.managers.DropsManager;
import me.chocolf.moneyfrommobs.managers.MessageManager;
import me.chocolf.moneyfrommobs.managers.MultipliersManager;
import me.chocolf.moneyfrommobs.managers.MobManager;
import me.chocolf.moneyfrommobs.managers.PickUpManager;

public class ReloadCommand implements CommandExecutor{
	
	private final MoneyFromMobs plugin;
	
	
	public ReloadCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmreload").setExecutor(this);		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PickUpManager pickUpManager = plugin.getPickUpManager();
		DropsManager dropsManager = plugin.getDropsManager();
		MobManager mobManager = plugin.getNumbersManager();
		MessageManager messageManager = plugin.getMessageManager();
		MultipliersManager multipliersManager = plugin.getMultipliersManager();
		
		// reloads configs
		plugin.reloadConfig();
		plugin.getMMConfig().reloadConfig();
		plugin.getMultipliersConfig().reloadConfig();

		// reload Repeating Multiplier Events if something was changed in the config
		multipliersManager.reloadRepeatingMultiplierEvent();

		// Reload values stored in managers
		messageManager.loadMessage();
		pickUpManager.init();
		dropsManager.init();
		mobManager.init();
		multipliersManager.init();
		
		// reloads bukkit runnable if user is not using paper
		if (!plugin.isUsingPaper()) {
			if (plugin.getInventoryIsFullRunnable() != null) {
				Bukkit.getScheduler().cancelTask(plugin.getInventoryIsFullRunnable().getTaskId());
			}
			plugin.loadInventoryIsFullRunnable();
		}


		
		// sends message saying it reloaded correctly
		sender.sendMessage(messageManager.getMessage("reloadMessage"));
	
		return true;
	}
	
}
