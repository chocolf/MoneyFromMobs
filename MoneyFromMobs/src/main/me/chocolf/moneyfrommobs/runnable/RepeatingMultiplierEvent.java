package me.chocolf.moneyfrommobs.runnable;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.MultipliersManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

			// send multiplier event started message
			Bukkit.getConsoleSender().sendMessage(multipliersManager.getRepeatingStartMessage());
			for (Player p : Bukkit.getServer().getOnlinePlayers()){
				if (!p.hasMetadata("MfmMuteMessages")) {
					p.sendMessage(multipliersManager.getRepeatingStartMessage());
				}
			}

			// run task later to set multiplier back to 0 and send message to players
			BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
				multipliersManager.setEventMultiplier(0);
				multipliersManager.setCurrentMultiplierEvent(null);

				Bukkit.getConsoleSender().sendMessage(multipliersManager.getRepeatingEndMessage());
				for (Player p : Bukkit.getServer().getOnlinePlayers()){
					if (!p.hasMetadata("MfmMuteMessages"))
						p.sendMessage(multipliersManager.getRepeatingEndMessage());
				}
			}, multipliersManager.getRepeatingDuration() * 20L);
			multipliersManager.setCurrentMultiplierEvent(task);
		}
	}
}
