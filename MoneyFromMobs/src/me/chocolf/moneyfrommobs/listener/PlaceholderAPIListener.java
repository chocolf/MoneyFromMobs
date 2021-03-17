package me.chocolf.moneyfrommobs.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.event.GiveMoneyEvent;

public class PlaceholderAPIListener implements Listener{
	
	private MoneyFromMobs plugin;
	private HashMap<UUID, String> latestPickedUp = new HashMap<>();
	
	
	
	public PlaceholderAPIListener(MoneyFromMobs plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPickUpMoney(GiveMoneyEvent e) {
		String itemName = plugin.getManager().getItemName();
		String strAmount = String.format("%.2f", e.getAmount());
		itemName = itemName.replace("%amount%", strAmount);
		
		
		UUID uuid = e.getPlayer().getUniqueId();
		if (latestPickedUp.containsKey(uuid)) {
			latestPickedUp.remove(uuid);
		}
		latestPickedUp.put(uuid, itemName);
		
	}
	
	public Map<UUID, String> getLatestPickedUp(){
		return latestPickedUp;
	}

}
