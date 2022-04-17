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
	private final double multiplier;
	private final int duration;
	private final String startMessage;
	private final String endMessage;

	public RepeatingMultiplierEvent(MoneyFromMobs plugin) {
		this.plugin = plugin;
		this.multiplier = Double.parseDouble(plugin.getMultipliersConfig().getConfig().getString("RepeatingMultiplierEvent.Multiplier").replace("%",""))/100;
		this.duration = plugin.getMultipliersConfig().getConfig().getInt("RepeatingMultiplierEvent.Duration") * 60;
		FileConfiguration config = plugin.getMultipliersConfig().getConfig();
		this.startMessage = MessageManager.applyColour(config.getString("RepeatingMultiplierEvent.EventStartMessage"));
		this.endMessage = MessageManager.applyColour(config.getString("RepeatingMultiplierEvent.EventEndMessage"));
	}
	
	public void run() {
		MultipliersManager multipliersManager = plugin.getMultipliersManager();
		if (multipliersManager.getCurrentMultiplierEvent() == null){
			multipliersManager.setEventMultiplier(multiplier);
			Bukkit.broadcastMessage(startMessage);

			BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
				multipliersManager.setEventMultiplier(0);
				multipliersManager.setCurrentMultiplierEvent(null);
				Bukkit.broadcastMessage(endMessage);
			}, duration * 20L);
			multipliersManager.setCurrentMultiplierEvent(task);
		}
	}
}
