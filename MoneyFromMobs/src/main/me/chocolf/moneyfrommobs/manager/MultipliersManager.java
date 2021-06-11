package me.chocolf.moneyfrommobs.manager;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.RandomNumberUtils;
import me.lorinth.rpgmobs.LorinthsRpgMobs;

public class MultipliersManager {
	
	private MoneyFromMobs plugin;
	private double lootingMultiplier;
	private double eventMultiplier = 0;
	private double lorinthsRpgMobsMultiplier;	

	private HashMap<String, Double> worldMultipliers = new HashMap<>();
	private HashMap<String, Double> permissionGroupMultipliers = new HashMap<>();
	private HashMap<String, Double> playerDeathMultipliers = new HashMap<>();
	
	public MultipliersManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		init();
	}

	public void init() {
		loadLootingMultiplier();
		loadWorldMultipliers();
		loadPermissionGroupMultipliers();
		loadPlayerDeathMultipliers();
		loadLorinthsRpgMobs();
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
		amountToAdd *= eventMultiplier;
		return amountToAdd;
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
		if (this.lorinthsRpgMobsMultiplier == 0) return 0;
		if (LorinthsRpgMobs.GetLevelOfEntity(entity) != null) {
			int level = LorinthsRpgMobs.GetLevelOfEntity(entity) - 1;
			return amountToAdd * lorinthsRpgMobsMultiplier * level;
		}
		return 0;
	}
	
	// load multipliers
	
	private void loadLootingMultiplier() {
		FileConfiguration config = plugin.getConfig();
		String strLootingMultiplier = config.getString("LootingMultiplier").replace("%", "");
		lootingMultiplier =  Double.parseDouble(strLootingMultiplier)/100;
	}
	
	private void loadPermissionGroupMultipliers() {
		permissionGroupMultipliers.clear();
		if (plugin.getEcon() == null)
			return;
		List<String> permissiongroupMultipliers = plugin.getConfig().getStringList("PermissionGroupMultipliers");
		for (String permissionGroup : permissiongroupMultipliers) {
			String[] splitList = permissionGroup.split(" ");
			String permissionGroupName = splitList[0];
			
			if (permissionGroupName.equalsIgnoreCase("NONE") )
				return;
			
			double permissionGroupMultiplier = Double.parseDouble(splitList[1].replace("%", "") )/100;
			permissionGroupMultipliers.put(permissionGroupName, permissionGroupMultiplier);
		}
	}
	
	private void loadWorldMultipliers() {
		worldMultipliers.clear();
		List<String> worldmultipliers = plugin.getConfig().getStringList("WorldMultipliers");
		for (String world : worldmultipliers) {
			String[] splitList = world.split(" ");
			String worldName = splitList[0];
			
			if (worldName.equalsIgnoreCase("NONE"))
				return;
			
			Double worldMultiplier = Double.parseDouble(splitList[1].replace("%", "") )/100;
			worldMultipliers.put(worldName,  worldMultiplier);
		}
	}
	
	private void loadPlayerDeathMultipliers() {
		playerDeathMultipliers.clear();
		
		if (plugin.getEcon() == null)
			return;
		
		List<String> playerdeathMultipliers = plugin.getConfig().getStringList("PlayerDeathMultipliers");
		for (String permissionGroup : playerdeathMultipliers) {
			String[] splitList = permissionGroup.split(" ");
			String permissionGroupName = splitList[0];
			
			if (permissionGroupName.equalsIgnoreCase("NONE") )
				return;
			
			double playerDeathMultiplier = Double.parseDouble(splitList[1].replace("%", "") )/100;
			playerDeathMultipliers.put(permissionGroupName, playerDeathMultiplier);
		}
	}
	
	private void loadLorinthsRpgMobs() {
		if (Bukkit.getPluginManager().getPlugin("LorinthsRpgMobs") != null) {
			FileConfiguration config = plugin.getConfig();
			String strLorinthsRpgMobsMultiplier = config.getString("LorinthsRpgMobsMultiplier").replace("%", "");
			lorinthsRpgMobsMultiplier =  Double.parseDouble(strLorinthsRpgMobsMultiplier)/100;
		}
		else lorinthsRpgMobsMultiplier = 0;
	}

	public void setEventMultiplier(double eventMultiplier) {
		this.eventMultiplier = eventMultiplier;
	}
}
