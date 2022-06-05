package me.chocolf.moneyfrommobs.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.MessageManager;
import me.chocolf.moneyfrommobs.util.UpdateChecker;

public class OnJoinListener implements Listener{
	
	
	public OnJoinListener(MoneyFromMobs plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (p.isOp() && UpdateChecker.checkForUpdate()) {
			p.sendMessage(MessageManager.applyColour("&bUpdate Available for MoneyFromMobs:"));
			p.sendMessage(MessageManager.applyColour("&bhttps://www.spigotmc.org/resources/money-from-mobs-1-9-1-16-4.79137/"));
		}
	}
}
