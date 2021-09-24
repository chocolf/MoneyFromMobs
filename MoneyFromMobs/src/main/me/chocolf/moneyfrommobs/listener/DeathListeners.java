package me.chocolf.moneyfrommobs.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.api.event.AttemptToDropMoneyEvent;
import me.chocolf.moneyfrommobs.api.event.DropMoneyEvent;
import me.chocolf.moneyfrommobs.manager.DropsManager;
import me.chocolf.moneyfrommobs.manager.MultipliersManager;
import me.chocolf.moneyfrommobs.manager.NumbersManager;
import me.chocolf.moneyfrommobs.manager.PickUpManager;
import me.chocolf.moneyfrommobs.util.RandomNumberUtils;

public class DeathListeners implements Listener{
	
	private final MoneyFromMobs plugin;
	
	public DeathListeners(MoneyFromMobs plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}	
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		Player p = null;

		// if killer is a player set p = to killer
		if (entity.getKiller() != null) {
			p = e.getEntity().getKiller();
			// if player doesn't have permission return
			if (!(p.hasPermission("MoneyFromMobs.use")))
				return;
		}
		
		DropsManager dropsManager = plugin.getDropsManager();
		NumbersManager numbersManager = plugin.getNumbersManager();
		MultipliersManager multipliersManager = plugin.getMultipliersManager();

		String entityName = dropsManager.getEntityName(entity);
		double amount;
		
		if (!dropsManager.canDropMoneyHere(entity, entityName, p))
			return;
		
		if (entityName.equals("PLAYER")) {
			if (entity.hasPermission("MoneyFromMobs.PreventMoneyDropOnDeath"))
				return;
			amount = numbersManager.getPlayerAmount(entity);
			amount -= multipliersManager.applyPlayerDeathMultipliers(amount,(Player) entity);
		}
		else {
			amount = numbersManager.getAmount(entityName);
			amount = multipliersManager.applyMultipliers(amount, p, entity);
		}
		double dropChance = numbersManager.getDropChance(entityName);
		int numberOfDrops = numbersManager.getNumberOfDrops(entityName);
		
		// calls attempt to drop money event
		AttemptToDropMoneyEvent attemptToDropMoneyEvent = new AttemptToDropMoneyEvent(dropChance, entity, p);
		Bukkit.getPluginManager().callEvent(attemptToDropMoneyEvent);
		if (attemptToDropMoneyEvent.isCancelled()) return;
		dropChance = attemptToDropMoneyEvent.getDropChance();
		
		// makes random number and compares it to drop chance
		double randomNum = RandomNumberUtils.doubleRandomNumber(0.0, 100.0);
		if (randomNum > dropChance) return;
		
		if (dropsManager.reachedMaxDropsPerMinute(p)) {
			String maxDropsReachedMessage = plugin.getMessageManager().getMessage("maxDropsReachedMessage");
			if (!maxDropsReachedMessage.equals("")) {
				p.sendMessage(maxDropsReachedMessage);
			}
			return;
		}
			
		
		PickUpManager pickUpManager = plugin.getPickUpManager();
		
		// if drop money on ground
		if ( dropsManager.doesMoneyDropOnGround() ){
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
			dropsManager.dropItem(itemToDrop, amount, location, numberOfDrops, p);
		}
		// if money goes straight into players account
		else {
			if (p==null) return;
			pickUpManager.giveMoney(amount, p);
		}
		
		if (entity instanceof Player) {
			if (amount == 0) return;
			if (dropsManager.shouldTakeMoneyFromKilledPlayer()) {
				plugin.getEcon().withdrawPlayer((Player) entity, amount);
				plugin.getMessageManager().sendPlayerMessage(amount, (Player) entity);
			}
			
		}
	}	
}



