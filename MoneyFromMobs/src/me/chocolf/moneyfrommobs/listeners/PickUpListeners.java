package me.chocolf.moneyfrommobs.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MfmManager;
import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class PickUpListeners implements Listener{
private MoneyFromMobs plugin;
	
	public PickUpListeners(MoneyFromMobs plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler
	public void onPickup(EntityPickupItemEvent e) {
		MfmManager manager = plugin.getManager();
		// gets item picked up
		Item item = e.getItem();
		ItemStack itemStack = item.getItemStack();
		if (!manager.checkIfMoney(itemStack)) return;
		
		
		e.setCancelled(true);
		// stops mobs from picking up money
		if (!(e.getEntity() instanceof Player) ) {
			return;
		}
		Player p = (Player) e.getEntity();
		// returns if player doesn't have permission to pickup money
		if ( !p.hasPermission("MoneyFromMobs.use") ) return;
		
	    item.remove();
	    List<String> itemLore = itemStack.getItemMeta().getLore();
	    double amount = Double.valueOf(itemLore.get(1));
	    manager.giveMoney(amount, p);
	}
	
	@EventHandler
	public void onHopperPickup(InventoryPickupItemEvent e) {
		ItemStack item = e.getItem().getItemStack();
		if (!plugin.getManager().checkIfMoney(item)) return;
		e.setCancelled(true);
	}
}
