package me.chocolf.moneyfrommobs.runnables;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class NearEntitiesRunnable extends BukkitRunnable{

	private final MoneyFromMobs plugin;
	private final int radius;

	public NearEntitiesRunnable(MoneyFromMobs plugin) {
		this.plugin = plugin;
		this.radius = plugin.getConfig().getInt("PickupMoneyWhenInventoryIsFull.Radius");
	}
	
	public void run() {
		for ( Player p : Bukkit.getOnlinePlayers()) {
			if (p.getInventory().firstEmpty() == -1) {
				// if player doesn't have permission continue
				if (!(p.hasPermission("MoneyFromMobs.use"))) continue;

				for ( Entity entity : p.getNearbyEntities(radius, radius, radius)) {
					if (entity instanceof Item) {
						Item item = (Item) entity;
						ItemStack itemStack = item.getItemStack();
						
						// if item found is not money continue
						if (!plugin.getPickUpManager().isMoneyPickedUp(itemStack)) continue;
						
					    
					    List<String> itemLore = itemStack.getItemMeta().getLore();
						double amount = Double.parseDouble(itemLore.get(1));
					    plugin.getPickUpManager().giveMoney(amount, p);
					    item.remove();
					}
				}
			}
		}
	}
	
	
}
