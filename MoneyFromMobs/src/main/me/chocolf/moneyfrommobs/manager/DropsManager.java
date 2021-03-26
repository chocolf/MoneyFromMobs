package me.chocolf.moneyfrommobs.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import io.lumine.xikage.mythicmobs.MythicMobs;
import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.Utils;
import me.chocolf.moneyfrommobs.util.VersionUtils;

public class DropsManager {
	
	private MoneyFromMobs plugin;
	private HashSet<String> disabledWorlds = new HashSet<>();
	private HashSet<String> onlyOnKillMobs = new HashSet<>();
	private boolean canDropIfNatural;
	private boolean canDropIfSpawner;
	private boolean canDropIfSpawnEgg;
	private boolean canDropIfSplitSlimes;
	private boolean dropMoneyOnGround;
	
	public DropsManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		loadDropMoneyOnGround();
		loadDisabledWorlds();
		loadSpawnReasonBooleans();
		loadOnlyOnKill();
	}
	
	private void loadDropMoneyOnGround() {
		dropMoneyOnGround = plugin.getConfig().getBoolean("MoneyDropsOnGround.Enabled");
	}

	public void loadOnlyOnKill() {
		this.onlyOnKillMobs.clear();
		FileConfiguration config = plugin.getConfig();
		FileConfiguration MMConfig = plugin.getMMConfig().getConfig();
		for (String mob : config.getKeys(false)){
			//ConfigurationSection mobConfigSection = config.getConfigurationSection(mob);
			if (config.getBoolean(mob+".Enabled") && config.getBoolean(mob+".OnlyOnKill")){
				onlyOnKillMobs.add(mob);
			}
		}
		for (String mob : MMConfig.getKeys(false)){
			//ConfigurationSection mobConfigSection = config.getConfigurationSection(mob);
			if (MMConfig.getBoolean(mob+".Enabled") && MMConfig.getBoolean(mob+".OnlyOnKill")){
				onlyOnKillMobs.add(mob);
			}
		}
	}

	public void loadDisabledWorlds() {
		this.disabledWorlds.clear();
		@SuppressWarnings("unchecked")
		List<String> disabledWorldsInConfig = (List<String>) this.plugin.getConfig().getList("DisabledWorlds");
		for (String world : disabledWorldsInConfig)
		  this.disabledWorlds.add(world); 
	}
	
	public void loadSpawnReasonBooleans() {
	    FileConfiguration config = this.plugin.getConfig();
	    this.canDropIfNatural = config.getBoolean("MoneyDropsFromNaturalMobs");
	    this.canDropIfSpawner = config.getBoolean("MoneyDropsFromSpawnerMobs");
	    this.canDropIfSpawnEgg = config.getBoolean("MoneyDropsFromSpawnEggMobs");
	    this.canDropIfSplitSlimes = config.getBoolean("MoneyDropsFromSplitSlimes");
	}
	
	public boolean canDropMoneyHere(Entity entity, String entityName, Player p) {
		if (!isEntityEnabled(entityName))
			return false;
		
		if (onlyOnKill(p,entityName))
			return false;
			
		if (canDropInWorld(entity.getWorld().getName()))
			return false;
		
		return canDropWithSpawnReason(entity);
	}
	
	private boolean onlyOnKill(Player p, String entityName) {		
		return (p==null && onlyOnKillMobs.contains(entityName));
	}

	public void dropItem(ItemStack item, Double amount, Location location, int numberOfDrops) {
		if (amount == 0) return;
		amount = amount/numberOfDrops;
		for ( int i=0; i<numberOfDrops;i++ ) {
			ItemMeta meta = item.getItemMeta();
			List<String> lore = new ArrayList<>();
			lore.add(String.valueOf(Utils.intRandomNumber(1000000,9999999) + "mfm"));
						
			// adds lore so when picked up plugin knows how much money to give
			lore.add(String.valueOf(amount));
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			Item itemDropped = location.getWorld().dropItemNaturally(location, item );
			String strAmount = String.format("%.2f", amount);
			
			// removes decimal place
			if (plugin.getConfig().getBoolean("MoneyDropsOnGround.DisableDecimal") && strAmount.contains(".00"))
				strAmount = String.format("%.0f", amount);
			
			
			itemDropped.setCustomName(plugin.getPickUpManager().getItemName().replace("%amount%", strAmount));
			itemDropped.setCustomNameVisible(true);
		}
	}
	
	private boolean canDropWithSpawnReason(Entity entity) {
		if (VersionUtils.getVersionNumber() > 13 ){
			try {
				String spawnReason = entity.getPersistentDataContainer().get(new NamespacedKey(plugin, "MfMSpawnReason"), PersistentDataType.STRING);
				if (spawnReason != null)
					return isSpawnReasonEnabled(spawnReason);
			}
			catch(Exception e) {
				return true;
			}
		}
		else if (entity.hasMetadata("MfMSpawnReason")){
			try {
				String spawnReason = entity.getMetadata("MfMSpawnReason").get(0).value().toString();
				return isSpawnReasonEnabled(spawnReason);
			}
			catch(Exception e) {
				return true;
			}
		}
		// if mob was in a rose stacker stack
		// REMEMBER TO REMOVE ONCE ROSE STACKER CALLS CREATURESPAWNEVENT
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("RoseStacker")) {
			String spawnReason = PersistentDataUtils.getEntitySpawnReason((LivingEntity) entity).toString();
			if (!isSpawnReasonEnabled(spawnReason)) return false;
		}
		return true;
	}

	private boolean canDropInWorld(String worldName) {
		return getDisabledWorlds().contains(worldName);
	}

	private boolean isEntityEnabled(String entityName) {
		return plugin.getNumbersManager().getDropChances().containsKey(entityName);
	}
	
	public String getEntityName(Entity entity) {
		FileConfiguration mfmMMConfig = plugin.getMMConfig().getConfig();
		if (mfmMMConfig.getBoolean("Enabled") && plugin.getServer().getPluginManager().isPluginEnabled("MythicMobs") && MythicMobs.inst().getAPIHelper().isMythicMob(entity)) {
			String mythicMobName = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity).getType().getInternalName();
			if (this.isEntityEnabled(mythicMobName)) {
				return mythicMobName;
			}
			else {
				return entity.getType().toString();
			}
		}
		else {
			return entity.getType().toString();
		}		
	}
	
	public Set<String> getDisabledWorlds() {
		return disabledWorlds;
	}
	
	public boolean isSpawnReasonEnabled(String spawnReason) {
		switch (spawnReason) {
		case "NATURAL":
			return canDropIfNatural;
		case "SPAWNER":
			return canDropIfSpawner;
		case "SPAWNER_EGG":
			return canDropIfSpawnEgg;
		case "SLIME_SPLIT":
			return canDropIfSplitSlimes;
		default:
			return true;
		}
	}

	public boolean doesMoneyDropOnGround() {
		return dropMoneyOnGround;
	}

	
	
	
}
