package me.chocolf.moneyfrommobs.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.PickUpManager;

public class PaperListeners implements Listener{
	
	private MoneyFromMobs plugin;
	
	public PaperListeners(MoneyFromMobs plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onAttemptToPickUp(PlayerAttemptPickupItemEvent e) {
		PickUpManager manager = plugin.getPickUpManager();
		// gets item picked up
		Item item = e.getItem();
		ItemStack itemStack = item.getItemStack();
		// return if item picked up isnt money
		if (!manager.isMoneyPickedUp(itemStack)) return;
		
		e.setCancelled(true);
		Player p = e.getPlayer();
		// returns if player doesn't have permission to pickup money
		if ( !p.hasPermission("MoneyFromMobs.use") ) return;
		
	    List<String> itemLore = itemStack.getItemMeta().getLore();
	    double amount = Double.parseDouble(itemLore.get(1));
	    manager.giveMoney(amount, p);
	    item.remove();
	}
}
