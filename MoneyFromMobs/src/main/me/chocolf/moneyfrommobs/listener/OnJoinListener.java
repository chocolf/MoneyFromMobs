package me.chocolf.moneyfrommobs.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.UpdateChecker;
import me.chocolf.moneyfrommobs.util.Utils;

public class OnJoinListener implements Listener{
	
	private MoneyFromMobs plugin;
	
	public OnJoinListener(MoneyFromMobs plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (!p.isOp()) return;
		if (!plugin.getConfig().getBoolean("UpdateNotification")) return;
		
		if (UpdateChecker.checkForUpdate()) {
			p.sendMessage(Utils.applyColour("&bUpdate Available for MoneyFromMobs:"));
			p.sendMessage(Utils.applyColour("&bhttps://www.spigotmc.org/resources/money-from-mobs-1-9-1-16-4.79137/"));
		}
	}
}
