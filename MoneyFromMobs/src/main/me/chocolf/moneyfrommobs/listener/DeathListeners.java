package me.chocolf.moneyfrommobs.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.event.AttemptToDropMoneyEvent;
import me.chocolf.moneyfrommobs.event.DropMoneyEvent;
import me.chocolf.moneyfrommobs.manager.DropsManager;
import me.chocolf.moneyfrommobs.manager.NumbersManager;
import me.chocolf.moneyfrommobs.manager.PickUpManager;
import me.chocolf.moneyfrommobs.util.Utils;

public class DeathListeners implements Listener{
	
	private MoneyFromMobs plugin;
	
	public DeathListeners(MoneyFromMobs plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}	
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		Entity entity = e.getEntity();
		Player p = null;

		// if killer is a player set p = to killer
		if ((e.getEntity().getKiller() instanceof Player)) {
			p = e.getEntity().getKiller();
			// if player doesn't have permission return
			if (!(p.hasPermission("MoneyFromMobs.use")))
				return;
		}
		
		DropsManager dropsManager = plugin.getDropsManager();
		NumbersManager numbersManager = plugin.getNumbersManager();
		String entityName = dropsManager.getEntityName(entity);
		double amount;
		double dropChance;
		int numberOfDrops;
		
		if (!dropsManager.canDropMoneyHere(entity, entityName, p))
			return;
		
		if (entityName.equals("PLAYER"))
			amount = numbersManager.getPlayerAmount(entity);
		else
			amount = numbersManager.getAmount(p, entityName);
		
		dropChance = numbersManager.getDropChance(entityName);
		numberOfDrops = numbersManager.getNumberOfDrops(entityName);
		
		// calls attempt to drop money event
		AttemptToDropMoneyEvent attemptToDropMoneyEvent = new AttemptToDropMoneyEvent(dropChance, entity, p);
		Bukkit.getPluginManager().callEvent(attemptToDropMoneyEvent);
		if (attemptToDropMoneyEvent.isCancelled()) return;
		dropChance = attemptToDropMoneyEvent.getDropChance();
		
		// makes random number for drop chance
		double randomNum = Utils.doubleRandomNumber(0.0, 100.0);
		
		if (randomNum > dropChance) return;
		PickUpManager pickUpManager = plugin.getPickUpManager();
		FileConfiguration config = plugin.getConfig();
		
		// if drop money on ground
		if ( config.getBoolean("MoneyDropsOnGround.Enabled") ){
			ItemStack itemToDrop = pickUpManager.getItemToDrop();
			Location location = entity.getLocation();
			
			// calls drop money event
			DropMoneyEvent dropMoneyEvent = new DropMoneyEvent(itemToDrop,amount, location, p, entity, numberOfDrops);
			Bukkit.getPluginManager().callEvent(dropMoneyEvent);
			if (dropMoneyEvent.isCancelled()) return;
			itemToDrop = dropMoneyEvent.getItemToDrop();
			amount = dropMoneyEvent.getAmount();
			location = dropMoneyEvent.getLocation();
			numberOfDrops = dropMoneyEvent.getNumberOfDrops();
			
			// drops item
			dropsManager.dropItem(itemToDrop, amount, location, numberOfDrops);
		}
		// if money goes straight into players account
		else {
			if (p==null) return;
			pickUpManager.giveMoney(amount, p);
		}
		
		if (entity instanceof Player) {
			if (amount == 0) return;
			plugin.getEcon().withdrawPlayer((Player) entity, amount);
			entity.sendMessage(Utils.applyColour(config.getString("PLAYER.Message") ).replace("%amount%", String.format("%.2f", amount)) );
		}
	}	
}



