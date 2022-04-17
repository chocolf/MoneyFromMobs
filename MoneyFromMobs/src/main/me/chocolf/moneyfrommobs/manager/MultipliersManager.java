package me.chocolf.moneyfrommobs.manager;

import java.util.HashMap;

import io.lumine.mythic.bukkit.BukkitAPIHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.infernal_mobs;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.RandomNumberUtils;
import me.lokka30.levelledmobs.LevelInterface;
import me.lokka30.levelledmobs.LevelledMobs;
import me.lorinth.rpgmobs.LorinthsRpgMobs;
import org.bukkit.scheduler.BukkitTask;

public class MultipliersManager {
	
	private final MoneyFromMobs plugin;
	private static LevelInterface levelledMobs;
	private static infernal_mobs infernalMobs;
	private double lootingMultiplier;
	private double eventMultiplier = 0;
	private double lorinthsRpgMobsMultiplier = 0;	
	private double mythicMobsLevelsMultiplier = 0;
	private double levelledMobsMultiplier = 0;
	private double infernalMobsMultiplier = 0;
	private BukkitTask currentMultiplierEvent;
	
	private final HashMap<String, Double> worldMultipliers = new HashMap<>();
	private final HashMap<String, Double> permissionGroupMultipliers = new HashMap<>();
	private final HashMap<String, Double> playerDeathMultipliers = new HashMap<>();
	
	public MultipliersManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		init();
	}

	public void init() {
		FileConfiguration config = plugin.getMultipliersConfig().getConfig();
		loadLootingMultiplier(config);
		loadWorldMultipliers(config);
		loadPermissionGroupMultipliers(config);
		loadPlayerDeathMultipliers(config);
		loadLorinthsRpgMobsMultiplier(config);
		loadMythicMobsLevelsMultiplier(config);
		loadLevelledMobsMultiplier(config);
		loadInfernalMobsMultiplier(config);
	}
	
	public double applyMultipliers(double amount, Player p, Entity entity) {
		double baseAmount = amount;
		
		if ( p!=null ) {
			amount += applyLootingMultiplier(baseAmount, p);
			amount += applyPermissionGroupMultiplier(baseAmount, p);
		}
		amount += applyEventMultiplier(baseAmount);
		amount += applyWorldMultiplier(baseAmount, entity);
		amount += applyLorinthsRpgMobsMultiplier(baseAmount, entity);
		amount += applyMythicMobsLevelsMultiplier(baseAmount, entity);
		amount += applyLevelledMobsMultiplier(baseAmount, entity);
		amount += applyInfernalMobsMultiplier(baseAmount, entity);
		
		return RandomNumberUtils.round(amount, 2);
	}
	
	public double applyPlayerDeathMultipliers(double amount, Player p) {
		if (this.playerDeathMultipliers.isEmpty())
			return 0;
		
		String[] playerGroups = plugin.getPerms().getPlayerGroups(p);
		if (playerGroups.length == 0)
			return 0;
		
		double amountToAdd = 0;
		for (String groupName : playerGroups) {
			if (!playerDeathMultipliers.containsKey(groupName))
				continue;
			
			double groupMultiplier = playerDeathMultipliers.get(groupName);
			amountToAdd += amount * groupMultiplier;
		}
		return RandomNumberUtils.round(amountToAdd, 2);
	}
	
	private double applyLootingMultiplier(double amountToAdd, Player p) {
		ItemStack killersWeapon = p.getInventory().getItemInMainHand();
		int lootingLevel = killersWeapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
		return amountToAdd * lootingMultiplier * lootingLevel;
	}
	
	private double applyEventMultiplier(double amountToAdd) {
		return amountToAdd * eventMultiplier;
	}
	
	private double applyWorldMultiplier(double amountToAdd, Entity entity) {
		String worldName = entity.getWorld().getName();
		if (worldMultipliers.isEmpty() || !worldMultipliers.containsKey(worldName))
			return 0;
		
		double worldMultiplier = worldMultipliers.get(worldName);
		amountToAdd *= worldMultiplier;
		return amountToAdd;
	}
	
	private double applyPermissionGroupMultiplier(double amount, Player p) {
		if (permissionGroupMultipliers.isEmpty())
			return 0;
		
		String[] playerGroups = plugin.getPerms().getPlayerGroups(p);
		if (playerGroups.length == 0)
			return 0;
		double amountToAdd = 0;
		for (String groupName : playerGroups) {
			if (!permissionGroupMultipliers.containsKey(groupName))
				continue;
			
			double groupMultiplier = permissionGroupMultipliers.get(groupName);
			amountToAdd += amount * groupMultiplier;
		}
		return amountToAdd;
	}
	
	private double applyLorinthsRpgMobsMultiplier(double amountToAdd, Entity entity) {
		if (lorinthsRpgMobsMultiplier == 0) return 0;
		if (LorinthsRpgMobs.GetLevelOfEntity(entity) != null) {
			int level = LorinthsRpgMobs.GetLevelOfEntity(entity) - 1;
			return amountToAdd * lorinthsRpgMobsMultiplier * level;
		}
		return 0;
	}
	
	private double applyMythicMobsLevelsMultiplier(double amountToAdd, Entity entity) {
		if (mythicMobsLevelsMultiplier == 0) return 0;
		BukkitAPIHelper MythicMobsAPI = MythicBukkit.inst().getAPIHelper();
		if ( MythicMobsAPI.isMythicMob(entity)) {
			double level = MythicMobsAPI.getMythicMobInstance(entity).getLevel() - 1;
			return amountToAdd * mythicMobsLevelsMultiplier * level;
		}
		return 0;
	}
	
	private double applyLevelledMobsMultiplier(double amountToAdd, Entity entity) {
		if (levelledMobsMultiplier == 0) return 0;
		if ( levelledMobs.isLevelled(( LivingEntity) entity) ) {
			int level = levelledMobs.getLevelOfMob((LivingEntity) entity) - 1;
			return amountToAdd * levelledMobsMultiplier * level;
		}
		return 0;
	}
	
	private double applyInfernalMobsMultiplier(double amountToAdd, Entity entity) {
		if (infernalMobsMultiplier == 0) return 0;
		if ( infernalMobs.findMobAbilities(entity.getUniqueId())!= null ) {
			return amountToAdd * infernalMobsMultiplier;
		}
		return 0;
	}
			
	// load multipliers
	
	private void loadLootingMultiplier(FileConfiguration config) {
		String strLootingMultiplier = config.getString("LootingMultiplier").replace("%", "");
		lootingMultiplier =  Double.parseDouble(strLootingMultiplier)/100;
	}
	
	private void loadPermissionGroupMultipliers(FileConfiguration config) {
		permissionGroupMultipliers.clear();
		if (plugin.getPerms() == null)
			return;

		for (String permissionGroup : config.getStringList("PermissionGroupMultipliers")) {
			String[] splitList = permissionGroup.split(" ");
			String permissionGroupName = splitList[0];
			
			if (permissionGroupName.equalsIgnoreCase("NONE") )
				return;
			
			double permissionGroupMultiplier = Double.parseDouble(splitList[1].replace("%", "") )/100;
			permissionGroupMultipliers.put(permissionGroupName, permissionGroupMultiplier);
		}
	}
	
	private void loadWorldMultipliers(FileConfiguration config) {
		worldMultipliers.clear();
		for (String world : config.getStringList("WorldMultipliers")) {
			String[] splitList = world.split(" ");
			String worldName = splitList[0];
			
			if (worldName.equalsIgnoreCase("NONE"))
				return;
			
			Double worldMultiplier = Double.parseDouble(splitList[1].replace("%", "") )/100;
			worldMultipliers.put(worldName,  worldMultiplier);
		}
	}
	
	private void loadPlayerDeathMultipliers(FileConfiguration config) {
		playerDeathMultipliers.clear();
		
		if (plugin.getPerms() == null)
			return;

		for (String permissionGroup : config.getStringList("PlayerDeathMultipliers")) {
			String[] splitList = permissionGroup.split(" ");
			String permissionGroupName = splitList[0];
			
			if (permissionGroupName.equalsIgnoreCase("NONE") )
				return;
			
			double playerDeathMultiplier = Double.parseDouble(splitList[1].replace("%", "") )/100;
			playerDeathMultipliers.put(permissionGroupName, playerDeathMultiplier);
		}
	}
	
	private void loadLorinthsRpgMobsMultiplier(FileConfiguration config) {
		if (Bukkit.getPluginManager().getPlugin("LorinthsRpgMobs") != null) {
			String strLorinthsRpgMobsMultiplier = config.getString("LorinthsRpgMobsMultiplier").replace("%", "");
			lorinthsRpgMobsMultiplier =  Double.parseDouble(strLorinthsRpgMobsMultiplier)/100;
		}
	}
	
	private void loadMythicMobsLevelsMultiplier(FileConfiguration config) {
		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			String strMythicMobsLevelsMultiplier = config.getString("MythicMobsLevelsMultiplier").replace("%", "");
			mythicMobsLevelsMultiplier = Double.parseDouble(strMythicMobsLevelsMultiplier)/100;
		}	
	}
	
	private void loadLevelledMobsMultiplier(FileConfiguration config) {
		if (Bukkit.getPluginManager().getPlugin("LevelledMobs") != null) {
			levelledMobs = ((LevelledMobs) Bukkit.getPluginManager().getPlugin("LevelledMobs")).levelInterface;
			String strLevelledMobsMultiplier = config.getString("LevelledMobsMultiplier").replace("%", "");
			levelledMobsMultiplier = Double.parseDouble(strLevelledMobsMultiplier)/100;
		}
	}
	
	private void loadInfernalMobsMultiplier(FileConfiguration config) {
		if (Bukkit.getPluginManager().getPlugin("InfernalMobs") != null) {
			infernalMobs = (infernal_mobs) Bukkit.getPluginManager().getPlugin("InfernalMobs");
			String strInfernalMobsMultiplier = config.getString("InfernalMobsMultiplier").replace("%", "");
			infernalMobsMultiplier = Double.parseDouble(strInfernalMobsMultiplier)/100;
		}	
	}

	public void setEventMultiplier(double eventMultiplier) {
		this.eventMultiplier = eventMultiplier;
	}
	
	public double getEventMultiplier() {
		return eventMultiplier;
	}

	public BukkitTask getCurrentMultiplierEvent(){return currentMultiplierEvent;}
	public void setCurrentMultiplierEvent(BukkitTask task){currentMultiplierEvent = task;}
}
