package me.chocolf.moneyfrommobs.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.RandomNumberUtils;
import me.chocolf.moneyfrommobs.util.VersionUtils;

public class DropsManager {
	
	private final MoneyFromMobs plugin;
	
	private final HashSet<String> disabledWorlds = new HashSet<>();
	private final HashSet<String> onlyOnKillMobs = new HashSet<>();
	private boolean disableDecimal;
	private boolean canDropIfNatural;
	private boolean canDropIfSpawner;
	private boolean canDropIfSpawnEgg;
	private boolean canDropIfSplitSlimes;
	private boolean dropMoneyOnGround;
	private boolean divideMoneyBetweenDrops;
	private boolean takeMoneyFromKilledPlayer;
	private boolean babyMobsCanDropMoney;
	private boolean roseStackerSupport;
	private boolean autoRemoveDrop;
	private int timeUntilRemove;
	private boolean moneyDropsInCreative;
	private boolean killerEarnsMoney;

	private final HashMap<String, Integer> numberOfDropsThisMinute = new HashMap<>();
	private int maxDropsPerMinute;
	
	public DropsManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		// so it can be run on using /mfmreload
		init();
	}
	
	public void init() {
		FileConfiguration config = plugin.getConfig();
		loadDisabledWorlds(config);
		loadSpawnReasonBooleans(config);
		loadOnlyOnKill(config);
		disableDecimal = config.getBoolean("MoneyDropsOnGround.DisableDecimal");
		babyMobsCanDropMoney = config.getBoolean("MoneyDropsFromBabyMobs");
		maxDropsPerMinute = config.getInt("MaxDropsPerMinute");
		divideMoneyBetweenDrops = config.getBoolean("DivideMoneyBetweenDrops");
		takeMoneyFromKilledPlayer = config.getBoolean("PLAYER.TakeMoneyFromKilledPlayer");
		killerEarnsMoney = config.getBoolean("PLAYER.MoneyDrops");
		roseStackerSupport = Bukkit.getPluginManager().isPluginEnabled("RoseStacker");
		autoRemoveDrop = config.getBoolean("AutoRemoveMoney.Enabled");
		timeUntilRemove = config.getInt("AutoRemoveMoney.TimeUntilRemove");
		moneyDropsInCreative = config.getBoolean("CreativeModeDropsMoney");
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
		disabledWorlds.addAll(disabledWorldsInConfig);
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

		if (disableDecimal)
			amount = RandomNumberUtils.round(amount, 0);
		
		for ( int i=0; i<numberOfDrops;i++ ) {
			// first line of lore is random numbers + mfm so items don't stack
			ItemMeta meta = item.getItemMeta();
			List<String> lore = new ArrayList<>();
			lore.add(RandomNumberUtils.intRandomNumber(1000000,9999999) + "mfm");
						
			// second line of lore is amount to give on pickup
			lore.add(String.valueOf(amount));
			
			// third line of lore is player who killed the mob
			if (p != null)
				lore.add(p.getName());
			
			meta.setLore(lore);
			item.setItemMeta(meta);

			String strAmount = String.format("%.2f", amount);
			// removes decimal place
			if (disableDecimal)
				strAmount = String.format("%.0f", amount);

			final String finalAmount = strAmount;
			Item itemDropped;

			if (VersionUtils.getVersionNumber() > 15){
				itemDropped = location.getWorld().dropItemNaturally(location, item, itemdropped ->{
					itemdropped.setCustomNameVisible(true);
					itemdropped.setCustomName(plugin.getPickUpManager().getItemName().replace("%amount%", finalAmount));
				});
			}
			else {
				itemDropped = location.getWorld().dropItemNaturally(location, item);
				itemDropped.setCustomNameVisible(true);
				itemDropped.setCustomName(plugin.getPickUpManager().getItemName().replace("%amount%", finalAmount));
			}
			// used so Upgradable Hoppers plugin does not pick up money
			itemDropped.setMetadata("NO_PICKUP", new FixedMetadataValue(this.plugin, "no_pickup"));

			// schedules task to remove drop in certain amount of time if enabled
			if (autoRemoveDrop) {
				Bukkit.getScheduler().runTaskLater(plugin, itemDropped::remove, timeUntilRemove * 20L);
			}
		}
	}
	
	public boolean canDropMoneyHere(Entity entity, String entityName, Player p) {
		if (!isEntityEnabled(entityName))
			return false;
		
		if (onlyOnKill(p,entityName))
			return false;
			
		if (canDropInWorld(entity.getWorld().getName()))
			return false;
		
		if (!babyMobsCanDropMoney && entity instanceof Ageable && !((Ageable) entity).isAdult())
			return false;

		if (p != null && !moneyDropsInCreative && p.getGameMode() == GameMode.CREATIVE)
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
			Bukkit.getScheduler().runTaskLater(plugin, () -> numberOfDropsThisMinute.remove(playerName), 1200L);
			return false;
		}
	}

	private boolean onlyOnKill(Player p, String entityName) {
		// if mob died without killing a player and OnlyOnKill is false in config
		return p==null && onlyOnKillMobs.contains(entityName);
	}

	private boolean canDropInWorld(String worldName) {
		return disabledWorlds.contains(worldName);
	}

	private boolean isEntityEnabled(String entityName) {
		return plugin.getNumbersManager().getDropChances().containsKey(entityName);
	}
	
	public String getEntityName(Entity entity) {
		if (plugin.getServer().getPluginManager().isPluginEnabled("MythicMobs") && MythicBukkit.inst().getAPIHelper().isMythicMob(entity)) {
			String mythicMobName = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(entity).getType().getInternalName();
			if (this.isEntityEnabled(mythicMobName)) 
				return mythicMobName;
		}
		else if (entity.hasMetadata("NPC")){
			if (this.isEntityEnabled(entity.getName()))
				return entity.getName();
		}
		return entity.getType().toString();	
	}

	private boolean canDropWithSpawnReason(Entity entity) {
		String spawnReason = getSpawnReason(entity);
		if (spawnReason != null)
			return isSpawnReasonEnabled(spawnReason);
		return true;
	}

	private boolean isSpawnReasonEnabled(String spawnReason) {
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

	public String getSpawnReason(Entity entity){
		if (VersionUtils.getVersionNumber() > 13 ){
			try {
				String spawnReason = entity.getPersistentDataContainer().get(new NamespacedKey(plugin, "MfMSpawnReason"), PersistentDataType.STRING);
				if (spawnReason != null)
					return spawnReason;
			}
			catch(Exception e) {
				return null;
			}
		}
		else if (entity.hasMetadata("MfMSpawnReason")){
			try {
				return entity.getMetadata("MfMSpawnReason").get(0).value().toString();
			}
			catch(Exception e) {
				return null;
			}
		}
		if (roseStackerSupport) {
			return PersistentDataUtils.getEntitySpawnReason(entity).toString();
		}
		return null;
	}

	public boolean doesMoneyDropOnGround() {
		return dropMoneyOnGround;
	}
	public boolean shouldTakeMoneyFromKilledPlayer() {
		return takeMoneyFromKilledPlayer;
	}
	public boolean shouldDisableDecimal() {
		return disableDecimal;
	}

	public boolean shouldKillerEarnMoney() {
		return killerEarnsMoney;
	}
}
