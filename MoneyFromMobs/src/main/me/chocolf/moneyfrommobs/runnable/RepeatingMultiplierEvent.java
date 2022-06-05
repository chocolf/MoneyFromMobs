package me.chocolf.moneyfrommobs.runnable;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.MessageManager;
import me.chocolf.moneyfrommobs.manager.MultipliersManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


public class RepeatingMultiplierEvent extends BukkitRunnable{

	private final MoneyFromMobs plugin;

	public RepeatingMultiplierEvent(MoneyFromMobs plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		MultipliersManager multipliersManager = plugin.getMultipliersManager();
		if (multipliersManager.getCurrentMultiplierEvent() == null){
			multipliersManager.setEventMultiplier(multipliersManager.getRepeatingMultiplier());
			Bukkit.broadcastMessage(multipliersManager.getRepeatingStartMessage());

			BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
				multipliersManager.setEventMultiplier(0);
				multipliersManager.setCurrentMultiplierEvent(null);
				Bukkit.broadcastMessage(multipliersManager.getRepeatingEndMessage());
			}, multipliersManager.getRepeatingDuration() * 20L);
			multipliersManager.setCurrentMultiplierEvent(task);
		}
	}



}
