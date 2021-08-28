package me.chocolf.moneyfrommobs.manager;

import java.util.ArrayList;
import java.util.HashMap;
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
import me.chocolf.moneyfrommobs.util.RandomNumberUtils;
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
	private boolean removeDropInMinute;
	private boolean divideMoneyBetweenDrops;
	private boolean takeMoneyFromKilledPlayer;
	
	private HashMap<String, Integer> numberOfDropsThisMinute = new HashMap<>();
	private int maxDropsPerMinute;
	
	public DropsManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		init();
		
	}
	
	public void init() {
		FileConfiguration config = plugin.getConfig();
		loadDropMoneyOnGround(config);
		loadDisabledWorlds(config);
		loadSpawnReasonBooleans(config);
		loadOnlyOnKill(config);
		maxDropsPerMinute = config.getInt("MaxDropsPerMinute");
		removeDropInMinute = config.getBoolean("RemoveMoneyAfter60Seconds");
		divideMoneyBetweenDrops = config.getBoolean("DivideMoneyBetweenDrops");
		takeMoneyFromKilledPlayer = config.getBoolean("PLAYER.TakeMoneyFromKilledPlayer");
	}
	
	private void loadDropMoneyOnGround(FileConfiguration config) {
		dropMoneyOnGround = config.getBoolean("MoneyDropsOnGround.Enabled");
	}

	private void loadOnlyOnKill(FileConfiguration config) {
		this.onlyOnKillMobs.clear();
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

	private void loadDisabledWorlds(FileConfiguration config) {
		disabledWorlds.clear();
		@SuppressWarnings("unchecked")
		List<String> disabledWorldsInConfig = (List<String>) config.getList("DisabledWorlds");
		for (String world : disabledWorldsInConfig)
		  disabledWorlds.add(world); 
	}
	
	private void loadSpawnReasonBooleans(FileConfiguration config) {
	    canDropIfNatural = config.getBoolean("MoneyDropsFromNaturalMobs");
	    canDropIfSpawner = config.getBoolean("MoneyDropsFromSpawnerMobs");
	    canDropIfSpawnEgg = config.getBoolean("MoneyDropsFromSpawnEggMobs");
	    canDropIfSplitSlimes = config.getBoolean("MoneyDropsFromSplitSlimes");
	}
	
	public void dropItem(ItemStack item, Double amount, Location location, int numberOfDrops, Player p) {
		if (amount == 0) return;
		if (divideMoneyBetweenDrops)
			amount = amount/numberOfDrops;
		
		for ( int i=0; i<numberOfDrops;i++ ) {
			
			// first line of lore is random numbers + mfm so items don't stack
			ItemMeta meta = item.getItemMeta();
			List<String> lore = new ArrayList<>();
			lore.add(String.valueOf(RandomNumberUtils.intRandomNumber(1000000,9999999) + "mfm"));
						
			// second line of lore is amount to give on pickup
			lore.add(String.valueOf(amount));
			
			// third line of lore is player who killed the mob
			if (p != null)
				lore.add(p.getName());
			
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			Item itemDropped = location.getWorld().dropItemNaturally(location, item );
			String strAmount = String.format("%.2f", amount);
			
			// removes decimal place
			if (plugin.getConfig().getBoolean("MoneyDropsOnGround.DisableDecimal") && strAmount.contains(".00"))
				strAmount = String.format("%.0f", amount);
			
			// schedules task to remove drop in 1 minute if enabled
			if (removeDropInMinute) {
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				    @Override
				    public void run() {
				    	itemDropped.remove();
				    }
				}, 1200L);
			}
			
			itemDropped.setCustomName(plugin.getPickUpManager().getItemName().replace("%amount%", strAmount));
			itemDropped.setCustomNameVisible(true);
		}
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
	
	public boolean reachedMaxDropsPerMinute(Player p ) {
		if (maxDropsPerMinute == 0) return false;
		if (p==null) return false;
		
		String playerName = p.getName();
		
		if (numberOfDropsThisMinute.containsKey(playerName)) {
			numberOfDropsThisMinute.replace(playerName, numberOfDropsThisMinute.get(playerName)+1);
			return numberOfDropsThisMinute.get(playerName) > maxDropsPerMinute;
		}
		else {
			numberOfDropsThisMinute.put(playerName, 1);
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			    @Override
			    public void run() {
			    	numberOfDropsThisMinute.remove(playerName);
			    }
			}, 1200L);
			return false;
		}
	}

	private boolean onlyOnKill(Player p, String entityName) {
		return p==null && onlyOnKillMobs.contains(entityName);
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
		if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) {
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
		if (plugin.getServer().getPluginManager().isPluginEnabled("MythicMobs") && MythicMobs.inst().getAPIHelper().isMythicMob(entity)) {
			String mythicMobName = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity).getType().getInternalName();
			if (this.isEntityEnabled(mythicMobName)) 
				return mythicMobName;
		}
		return entity.getType().toString();	
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

	public boolean shouldTakeMoneyFromKilledPlayer() {
		return takeMoneyFromKilledPlayer;
	}

	
	
	
}
