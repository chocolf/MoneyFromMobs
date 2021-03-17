package me.chocolf.moneyfrommobs.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;

import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import io.lumine.xikage.mythicmobs.MythicMobs;
import me.chocolf.moneyfrommobs.MfmManager;
import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.event.AttemptToDropMoneyEvent;
import me.chocolf.moneyfrommobs.event.DropMoneyEvent;
import me.chocolf.moneyfrommobs.util.Utils;

public class DeathListeners implements Listener{
	
	private MoneyFromMobs plugin;
	
	public DeathListeners(MoneyFromMobs plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}	
	
	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent e) {
		String spawnReason = e.getSpawnReason().name();
	    LivingEntity entity = e.getEntity();
	    entity.setMetadata("MfMSpawnReason", (MetadataValue)new FixedMetadataValue(this.plugin, spawnReason));
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		MfmManager manager = plugin.getManager();
		FileConfiguration config = plugin.getConfig();
		FileConfiguration mfmMMConfig = plugin.getMMConfig().getConfig();
		Entity entity = e.getEntity();
		String entityName = entity.getType().toString();
		String mythicMobName = getMMName(entity);
		Player p = null;
		double amount;
		double dropChance;
		int numberOfDrops;
		
		// checks if money can drop
		if (!canDropMoneyHere(entity)) return;

		// if killer is a player set p = to killer
		if ((e.getEntity().getKiller() instanceof Player)) {
			p = e.getEntity().getKiller();
			// if player doesn't have permission return
			if (!(p.hasPermission("MoneyFromMobs.use")))
				return;
		}
		
		// if mythic mob died
		if (mythicMobName!=null) {
			if (p==null && mfmMMConfig.getBoolean(mythicMobName+".OnlyOnKill")) return;
			
			amount = getMMAmount(mythicMobName, p);
			dropChance = mfmMMConfig.getDouble(mythicMobName+".DropChance");
			numberOfDrops = getMMNumberOfDrops(mythicMobName);
		}
		
		// if normal mob/player died
		else if (config.getBoolean(entityName + ".Enabled")) {
			if (p==null && config.getBoolean(entityName+".OnlyOnKill")) return;
			
			if (entityName.equals("PLAYER")){
				amount = getPlayerAmount(entity);
			}
			else amount = getAmount(entity, p);
			
			dropChance = config.getDouble(entityName + ".DropChance");
			numberOfDrops = getNumberOfDrops(entityName);
			
		}
		// if mob that isnt in the config died
		else return;
		
		// calls attempt to drop money event
		AttemptToDropMoneyEvent attemptToDropMoneyEvent = new AttemptToDropMoneyEvent(dropChance, entity, p);
		Bukkit.getPluginManager().callEvent(attemptToDropMoneyEvent);
		if (attemptToDropMoneyEvent.isCancelled()) return;
		dropChance = attemptToDropMoneyEvent.getDropChance();
		
		// makes random number for drop chance
		double randomNum = Utils.doubleRandomNumber(0.0, 100.0);
		
		if (randomNum < dropChance) {
			ItemStack itemToDrop = manager.getItemToDrop();
			Location location = entity.getLocation();
			
			// if drop money on ground
			if ( config.getBoolean("MoneyDropsOnGround.Enabled") ){
				// call drop money event
				DropMoneyEvent dropMoneyEvent = new DropMoneyEvent(itemToDrop,amount, location, p, entity, numberOfDrops);
				Bukkit.getPluginManager().callEvent(dropMoneyEvent);
				if (dropMoneyEvent.isCancelled()) return;
				itemToDrop = dropMoneyEvent.getItemToDrop();
				amount = dropMoneyEvent.getAmount();
				location = dropMoneyEvent.getLocation();
				numberOfDrops = dropMoneyEvent.getNumberOfDrops();
				
				manager.dropItem(itemToDrop, amount, location, numberOfDrops);
				return;
			}
			// if money goes straight into players account
			else {
				if (p==null) return;
				manager.giveMoney(amount, p);
			}
			
			if (entity instanceof Player) {
				if (amount == 0) return;
				plugin.getEcon().withdrawPlayer((Player) entity, amount);
				entity.sendMessage(Utils.applyColour(config.getString("PLAYER.Message") ).replace("%amount%", String.format("%.2f", amount)) );
			}
		}
	}
	
	// METHODS
	
	private boolean canDropMoneyHere(Entity entity) {
		MfmManager manager = plugin.getManager();
		// checks if entity that died is in a disabled world
		if (manager.getDisabledWorlds().contains(entity.getWorld().getName()))
			return false;
		
		
		// checks how entity spawned and if its disabled
		if (entity.hasMetadata("MfMSpawnReason")) {
			try {
				String spawnReason = entity.getMetadata("MfMSpawnReason").get(0).value().toString();
				if (!manager.canDrop(spawnReason)) return false;
			}
			catch(Exception e) {
				return true;
			}
			
		}
		else if (Bukkit.getServer().getPluginManager().isPluginEnabled("RoseStacker")) {
			String spawnReason = PersistentDataUtils.getEntitySpawnReason((LivingEntity) entity).toString();
			if (!manager.canDrop(spawnReason)) return false;
		}
		return true;
	}

	// gets amount to give if entity is a MythicMob
	private double getMMAmount(String entityName, Player p) {
		FileConfiguration config = plugin.getMMConfig().getConfig();
		Double min = Double.valueOf(config.getString(entityName + ".Min"));
		Double max = Double.valueOf(config.getString(entityName + ".Max"));
		double amount;
		amount = Utils.doubleRandomNumber(min, max);
		
		if ( p!=null) {
			// times amount by looting multiplier
			ItemStack killersWeapon = p.getInventory().getItemInMainHand();
			int lootingLevel = killersWeapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
			double lootingMultiplier = plugin.getManager().getLootingMultiplier();
			lootingMultiplier = 1+(lootingMultiplier*lootingLevel);
			amount *= lootingMultiplier;
			
			// times by permission multiplier
			if (plugin.getManager().isPermissionMultiplierEnabled()) {
				String permissionPrefix = "moneyfrommobs.multiplier.";
				for (PermissionAttachmentInfo attachmentInfo : p.getEffectivePermissions()) {
					if (attachmentInfo.getPermission().startsWith(permissionPrefix)) {
				    	double permissionMultiplier = Double.parseDouble(attachmentInfo.getPermission().replace("moneyfrommobs.multiplier.", ""));
				    	permissionMultiplier /= 100;
				    	permissionMultiplier += 1;
				    	amount *= permissionMultiplier;
				    }
				}
			}
		}
		
		amount = Utils.round(amount, 2);
		return amount;
	}
	
	// gets amount to give if entity is a player
	public double getPlayerAmount(Entity entity) {
		FileConfiguration config = plugin.getConfig();
		double amount;
		double playersBalance = plugin.getEcon().getBalance((Player) entity);
		String strAmount = config.getString("PLAYER.Amount");
		if ( strAmount.contains("%")) {
			strAmount = strAmount.replace("%","");
			amount = playersBalance*(Double.valueOf(strAmount)/100);
		}else {
			amount = Double.valueOf(strAmount);
			if (amount > playersBalance) {
				amount = playersBalance;
			}
		}
		amount = Utils.round(amount, 2);
		return amount;
	}
	
	// gets amount to give if entity is a vanilla mob
	public double getAmount(Entity entity, Player p) {
		FileConfiguration config = plugin.getConfig();
		String entityName = String.valueOf(entity.getType());
		double amount;
		
		Double min = config.getDouble(entityName + ".Min");
		Double max = config.getDouble(entityName + ".Max");
		
		amount = Utils.doubleRandomNumber(min, max);
		if ( p!=null) {
			// times amount by looting multiplier
			ItemStack killersWeapon = p.getInventory().getItemInMainHand();
			int lootingLevel = killersWeapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
			double lootingMultiplier = plugin.getManager().getLootingMultiplier();
			lootingMultiplier = 1+(lootingMultiplier*lootingLevel);
			amount *= lootingMultiplier;
			
			// times by permission multiplier
			if (plugin.getManager().isPermissionMultiplierEnabled()) {
				String permissionPrefix = "moneyfrommobs.multiplier.";
				for (PermissionAttachmentInfo attachmentInfo : p.getEffectivePermissions()) {
					if (attachmentInfo.getPermission().startsWith(permissionPrefix)) {
				    	double permissionMultiplier = Double.parseDouble(attachmentInfo.getPermission().replace(permissionPrefix, ""));
				    	permissionMultiplier /= 100;
				    	permissionMultiplier += 1;
				    	amount *= permissionMultiplier;
				    }
				}
			}
			
		}
		
		amount = Utils.round(amount, 2);
		return amount;
	}
	
	// gets how many items should be dropped from the vanilla mob that died
	public int getNumberOfDrops(String entityName) {
		int numberOfDrops = 1;
		String amountOfDrops = plugin.getConfig().getString(entityName+ ".NumberOfDrops");
		if (amountOfDrops.contains("-")) {
			String[] dropsList = amountOfDrops.split("-");
			numberOfDrops = Utils.intRandomNumber(Integer.valueOf(dropsList[0]), Integer.valueOf(dropsList[1])+1 );
		}
		else numberOfDrops = Integer.valueOf(amountOfDrops);
		
		return numberOfDrops;
	}
	
	// gets how many items should be dropped from the MythicMob that died
	public int getMMNumberOfDrops(String entityName) {
		int numberOfDrops = 1;
		String amountOfDrops = plugin.getMMConfig().getConfig().getString(entityName+ ".NumberOfDrops");
		if (amountOfDrops.contains("-")) {
			String[] dropsList = amountOfDrops.split("-");
			numberOfDrops = Utils.intRandomNumber(Integer.valueOf(dropsList[0]), Integer.valueOf(dropsList[1])+1 );
		}
		else numberOfDrops = Integer.valueOf(amountOfDrops);
		
		return numberOfDrops;
	}
	
	// gets name of MythicMob that died
	public String getMMName(Entity entity) {
		FileConfiguration mfmMMConfig = plugin.getMMConfig().getConfig();
		String mythicMobName = null;
		if (mfmMMConfig.getBoolean("Enabled") && plugin.getServer().getPluginManager().isPluginEnabled("MythicMobs") && MythicMobs.inst().getAPIHelper().isMythicMob(entity)) {
			mythicMobName = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity).getType().getInternalName();
			if (mfmMMConfig.contains(mythicMobName) && mfmMMConfig.getBoolean(mythicMobName+".Enabled")) 
				return mythicMobName;
		}
		return null;
	}
}



