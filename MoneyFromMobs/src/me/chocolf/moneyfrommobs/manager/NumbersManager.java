package me.chocolf.moneyfrommobs.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.Utils;

public class NumbersManager {
	
	private MoneyFromMobs plugin;
	private double lootingMultiplier;
	private boolean permissionMultiplierEnabled;
	private HashMap<String, Double> minAmounts = new HashMap<>();
	private HashMap<String, Double> maxAmounts = new HashMap<>();
	private HashMap<String, Double> dropChances = new HashMap<>();
	private HashMap<String, Integer> minNumberOfDrops = new HashMap<>();
	private HashMap<String, Integer> maxNumberOfDrops = new HashMap<>();
	private String playerAmount;
	
	public NumbersManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		init();
	}
	
	public void init() {
		FileConfiguration config = plugin.getConfig();
		FileConfiguration MMConfig = plugin.getMMConfig().getConfig();
		lootingMultiplier = config.getDouble("MoneyAddedPerLevel");
		permissionMultiplierEnabled = config.getBoolean("PermissionMultipliersEnabled");
		minAmounts.clear();
		maxAmounts.clear();
		dropChances.clear();
		playerAmount = config.getString("PLAYER.Amount");
		
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
		if (entityName.equals("PLAYER")) {
			getPlayerAmount(entityName);
		}
		double min = minAmounts.get(entityName);
		double max = maxAmounts.get(entityName);
		double amount = Utils.doubleRandomNumber(min, max);
		
		if ( p!=null ) {
			amount = applyLootingMultiplier(amount, p);
			if (permissionMultiplierEnabled) {
				amount = applyPermissionMultiplier(amount, p);
			}
		}
		
		amount = Utils.round(amount, 2);
		return amount;
	}
	
	public int getNumberOfDrops(String entityName) {
		int min = minNumberOfDrops.get(entityName);
		int max = maxNumberOfDrops.get(entityName);
		
		return Utils.intRandomNumber(min, max+1);
	}
	
	
	public double getDropChance(String entityName) {
		return dropChances.get(entityName);
	}
	
	private double getPlayerAmount(String entityName) {
		Player p = Bukkit.getPlayer(entityName);
		double playersBalance = plugin.getEcon().getBalance(p);
		String strAmount = this.playerAmount;
		double amount;
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
	
	private double applyLootingMultiplier(double amount, Player p) {
		ItemStack killersWeapon = p.getInventory().getItemInMainHand();
		int lootingLevel = killersWeapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
		double lootingmultiplier = this.lootingMultiplier;
		lootingmultiplier = 1+(lootingmultiplier*lootingLevel);
		amount *= lootingmultiplier;
		return amount;
	}
	
	private double applyPermissionMultiplier(double amount, Player p) {
		String permissionPrefix = "moneyfrommobs.multiplier.";
		for (PermissionAttachmentInfo attachmentInfo : p.getEffectivePermissions()) {
			if (attachmentInfo.getPermission().startsWith(permissionPrefix)) {
		    	double permissionMultiplier = Double.parseDouble(attachmentInfo.getPermission().replace(permissionPrefix, ""));
		    	permissionMultiplier /= 100;
		    	permissionMultiplier += 1;
		    	amount *= permissionMultiplier;
		    }
		}
		return amount;
	}
	
	public Map<String, Double> getDropChances(){
		return dropChances;
	}
}
