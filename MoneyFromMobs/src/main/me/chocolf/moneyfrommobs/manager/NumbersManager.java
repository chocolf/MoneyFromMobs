package me.chocolf.moneyfrommobs.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.RandomNumberUtils;

public class NumbersManager {
	
	private MoneyFromMobs plugin;
	
	private HashMap<String, Double> minAmounts = new HashMap<>();
	private HashMap<String, Double> maxAmounts = new HashMap<>();
	private String playerAmount;
	private boolean randomInteger;
	
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
		playerAmount = config.getString("PLAYER.Amount");
		
		randomInteger = config.getBoolean("RandomIntegerOnly");
		
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
	
	public double getAmount(String entityName) {
		double min = minAmounts.get(entityName);
		double max = maxAmounts.get(entityName);
		if (randomInteger)
			return RandomNumberUtils.intRandomNumber((int) min, (int) max+1);
		else
			return RandomNumberUtils.doubleRandomNumber(min, max);
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
		return amount;
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
	
		
}
