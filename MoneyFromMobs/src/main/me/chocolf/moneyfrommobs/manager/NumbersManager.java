package me.chocolf.moneyfrommobs.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.RandomNumberUtils;

public class NumbersManager {
	
	private MoneyFromMobs plugin;
	private double lootingMultiplier;
	private double eventMultiplier = 0;
	

	private HashMap<String, Double> worldMultipliers = new HashMap<>();
	private HashMap<String, Double> permissionGroupMultipliers = new HashMap<>();
	private HashMap<String, Double> playerDeathMultipliers = new HashMap<>();
	
	private HashMap<String, Double> minAmounts = new HashMap<>();
	private HashMap<String, Double> maxAmounts = new HashMap<>();
	private String playerAmount;
	
	private HashMap<String, Double> dropChances = new HashMap<>();
	
	private HashMap<String, Integer> minNumberOfDrops = new HashMap<>();
	private HashMap<String, Integer> maxNumberOfDrops = new HashMap<>();
	
	
	
	public NumbersManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		init();
	}
	
	public void init() {
		FileConfiguration config = plugin.getConfig();
		FileConfiguration MMConfig = plugin.getMMConfig().getConfig();
		loadLootingMultiplier();
		loadWorldMultipliers();
		loadPermissionGroupMultipliers();
		loadPlayerDeathMultipliers();
		playerAmount = config.getString("PLAYER.Amount");
		
		minAmounts.clear();
		maxAmounts.clear();
		dropChances.clear();
		
		// loop through normal config to find enabled mobs. Store their amount, drop chance and number of drops
		for (String mob : config.getKeys(false)){
			ConfigurationSection mobConfigSection = config.getConfigurationSection(mob);
			if (config.getBoolean(mob+".Enabled") && mobConfigSection.contains("DropChance")){
				dropChances.put(mob, config.getDouble(mob+".DropChance"));
				
				String numberOfDrops = config.getString(mob+".NumberOfDrops");
				if (numberOfDrops.contains("-")) {
					String[] numberOfDropsSplit = numberOfDrops.split("-");
					minNumberOfDrops.put(mob, Integer.parseInt(numberOfDropsSplit[0]));
					maxNumberOfDrops.put(mob, Integer.parseInt(numberOfDropsSplit[1]));
				}else {
					minNumberOfDrops.put(mob, Integer.parseInt(numberOfDrops));
					maxNumberOfDrops.put(mob, Integer.parseInt(numberOfDrops));
				}
				if (mobConfigSection.contains("Min") ) {
					minAmounts.put(mob, config.getDouble(mob+".Min"));
					maxAmounts.put(mob, config.getDouble(mob+".Max"));
				}
			}
		}
		// loop through MythicMob config to find enabled MythicMobs. Store their amount, drop chance and number of drops
		for (String mob : MMConfig.getKeys(false)){
			ConfigurationSection mobConfigSection = MMConfig.getConfigurationSection(mob);
			if (MMConfig.getBoolean(mob+".Enabled") && mobConfigSection.contains("DropChance")){
				dropChances.put(mob, MMConfig.getDouble(mob+".DropChance"));
				
				String numberOfDrops = MMConfig.getString(mob+".NumberOfDrops");
				if (numberOfDrops.contains("-")) {
					String[] numberOfDropsSplit = numberOfDrops.split("-");
					minNumberOfDrops.put(mob, Integer.parseInt(numberOfDropsSplit[0]));
					maxNumberOfDrops.put(mob, Integer.parseInt(numberOfDropsSplit[1]));
				}else {
					minNumberOfDrops.put(mob, Integer.parseInt(numberOfDrops));
					maxNumberOfDrops.put(mob, Integer.parseInt(numberOfDrops));
				}
				if (mobConfigSection.contains("Min") ) {
					minAmounts.put(mob, MMConfig.getDouble(mob+".Min"));
					maxAmounts.put(mob, MMConfig.getDouble(mob+".Max"));
				}
			}
		}
	}
	
	public double getAmount(Player p, String entityName) {
		double min = minAmounts.get(entityName);
		double max = maxAmounts.get(entityName);
		double amount = RandomNumberUtils.doubleRandomNumber(min, max);
		double baseAmount = amount;
		
		if ( p!=null ) {
			amount += applyLootingMultiplier(baseAmount, p);
			amount += applyEventMultiplier(baseAmount);
			amount += applyWorldMultiplier(baseAmount, p);
			amount += applyPermissionGroupMultiplier(baseAmount, p);
		}
		return RandomNumberUtils.round(amount, 2);
	}
	
	public int getNumberOfDrops(String entityName) {
		int min = minNumberOfDrops.get(entityName);
		int max = maxNumberOfDrops.get(entityName);
		
		return RandomNumberUtils.intRandomNumber(min, max+1);
	}
	
	
	public double getDropChance(String entityName) {
		return dropChances.get(entityName);
	}
	
	public Map<String, Double> getDropChances(){
		return dropChances;
	}
	
	public double getPlayerAmount(Entity entity) {
		Player p = (Player) entity;
		double playersBalance = plugin.getEcon().getBalance(p);
		String strAmount = this.playerAmount;
		double amount;
		if ( strAmount.contains("%")) {
			strAmount = strAmount.replace("%","");
			amount = playersBalance*(Double.parseDouble(strAmount)/100);
		}else {
			amount = Double.parseDouble(strAmount);
			if (amount > playersBalance) {
				amount = playersBalance;
			}
		}
		amount -= applyPlayerDeathMultipliers(amount, p);
		return RandomNumberUtils.round(amount, 2);
	}
	
	private double applyLootingMultiplier(double amountToAdd, Player p) {
		ItemStack killersWeapon = p.getInventory().getItemInMainHand();
		int lootingLevel = killersWeapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
		amountToAdd *= lootingMultiplier * lootingLevel;
		return amountToAdd;
	}
	
	private double applyEventMultiplier(double amountToAdd) {
		amountToAdd *= eventMultiplier;
		return amountToAdd;
	}
	
	private double applyWorldMultiplier(double amountToAdd, Player p) {
		String worldName = p.getWorld().getName();
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
	
	private double applyPlayerDeathMultipliers(double amount, Player p) {
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
		return amountToAdd;
	}
	
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
	
	public double getEventMultiplier() {
		return eventMultiplier;
	}

	public void setEventMultiplier(double eventMultiplier) {
		this.eventMultiplier = eventMultiplier;
	}
	
	
}
