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
import me.chocolf.moneyfrommobs.manager.MobManager;
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
		Player killer = e.getEntity().getKiller();
		// if killer is a player and doesn't have permission "MoneyFromMobs.use"
		if (killer != null && !killer.hasPermission("MoneyFromMobs.use"))
			return;
		DropsManager dropsManager = plugin.getDropsManager();
		MobManager mobManager = plugin.getNumbersManager();
		MultipliersManager multipliersManager = plugin.getMultipliersManager();

		String entityName = dropsManager.getEntityName(entity);
		double amount;

		// checks if money can be dropped
		if (!dropsManager.canDropMoneyHere(entity, entityName, killer))
			return;
		
		if (entityName.equals("PLAYER")) {
			if (entity.hasPermission("MoneyFromMobs.PreventMoneyDropOnDeath"))
				return;
			amount = mobManager.getPlayerAmount(entity);
			amount -= multipliersManager.applyPlayerDeathMultipliers(amount,(Player) entity);
		}
		else {
			amount = multipliersManager.applyMultipliers(mobManager.getAmount(entityName), killer, entity);
		}
		double dropChance = mobManager.getDropChance(entityName);
		int numberOfDrops = mobManager.getNumberOfDrops(entityName);
		
		// calls attempt to drop money event
		AttemptToDropMoneyEvent attemptToDropMoneyEvent = new AttemptToDropMoneyEvent(dropChance, entity, killer);
		Bukkit.getPluginManager().callEvent(attemptToDropMoneyEvent);
		if (attemptToDropMoneyEvent.isCancelled()) return;
		dropChance = attemptToDropMoneyEvent.getDropChance();
		
		// makes random number and compares it to drop chance
		double randomNum = RandomNumberUtils.doubleRandomNumber(0.0, 100.0);
		if (randomNum > dropChance) return;

		// if player has reached max drops per minute send them a message and return.
		if (dropsManager.reachedMaxDropsPerMinute(killer)) {
			String maxDropsReachedMessage = plugin.getMessageManager().getMessage("maxDropsReachedMessage");
			if (!maxDropsReachedMessage.equals(""))
				killer.sendMessage(maxDropsReachedMessage);
			return;
		}

		PickUpManager pickUpManager = plugin.getPickUpManager();
		
		// if money should be dropped as item
		if ( dropsManager.doesMoneyDropOnGround() && amount > 0){
			if (!(entity instanceof Player && !dropsManager.shouldKillerEarnMoney())){
				ItemStack itemToDrop = pickUpManager.getItemToDrop();
				Location location = entity.getLocation();

				// calls drop money event
				DropMoneyEvent dropMoneyEvent = new DropMoneyEvent(itemToDrop,amount, location, killer, entity, numberOfDrops);
				Bukkit.getPluginManager().callEvent(dropMoneyEvent);
				if (dropMoneyEvent.isCancelled())
					return;
				itemToDrop = dropMoneyEvent.getItemToDrop();
				amount = dropMoneyEvent.getAmount();
				location = dropMoneyEvent.getLocation();
				numberOfDrops = dropMoneyEvent.getNumberOfDrops();

				// drops item
				dropsManager.dropItem(itemToDrop, amount, location, numberOfDrops, killer, !entityName.equals("PLAYER"));
			}

		}
		// if money goes straight into players account
		else if (killer!=null){
			if (!(entity instanceof Player && !dropsManager.shouldKillerEarnMoney()))
				pickUpManager.giveMoney(amount, killer);
		}

		// take money from dead player
		if (amount!=0 && entityName.equals("PLAYER") && dropsManager.shouldTakeMoneyFromKilledPlayer()) {
			plugin.getEcon().withdrawPlayer((Player) entity, amount);
			plugin.getMessageManager().sendPlayerMessage(amount, (Player) entity);
		}
	}	
}



