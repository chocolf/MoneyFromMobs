package me.chocolf.moneyfrommobs.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.api.event.GiveMoneyEvent;

public class PlaceholderAPIListener implements Listener{
	
	private HashMap<UUID, Double> latestPickedUp = new HashMap<>();
	
	
	
	public PlaceholderAPIListener(MoneyFromMobs plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPickUpMoney(GiveMoneyEvent e) {		
		UUID uuid = e.getPlayer().getUniqueId();
		if (latestPickedUp.containsKey(uuid))
			latestPickedUp.remove(uuid);
		
		latestPickedUp.put(uuid, e.getAmount());
	}
	
	public Map<UUID, Double> getLatestPickedUp(){
		return latestPickedUp;
	}

}
