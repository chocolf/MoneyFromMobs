package me.chocolf.moneyfrommobs.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MfmManager;
import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class PaperListeners implements Listener{
	
	private MoneyFromMobs plugin;
	
	public PaperListeners(MoneyFromMobs plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onAttemptToPickUp(PlayerAttemptPickupItemEvent e) {
		MfmManager manager = plugin.getManager();
		// gets item picked up
		Item item = e.getItem();
		ItemStack itemStack = item.getItemStack();
		if (!manager.checkIfMoney(itemStack)) return;
		
		
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
