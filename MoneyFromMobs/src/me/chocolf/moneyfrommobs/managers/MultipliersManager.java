package me.chocolf.moneyfrommobs.managers;

import java.util.HashMap;

import io.lumine.mythic.bukkit.BukkitAPIHelper;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.hotmail.com.jacob_vejvoda.infernal_mobs.infernal_mobs;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.utils.RandomNumberUtils;
import org.bukkit.scheduler.BukkitTask;

public class MultipliersManager {
	
	private final MoneyFromMobs plugin;
	private static infernal_mobs infernalMobsAPI;
	private LevelledMobsManager levelledMobsManager;
	private static GuildsAPI guildsAPI;
	private double lootingMultiplier;
	private double eventMultiplier = 0;
	private double mythicMobsLevelsMultiplier = 0;
	private double levelledMobsMultiplier = 0;
	private double infernalMobsMultiplier = 0;
	private double guildsMultiplier = 0;
	private BukkitTask currentMultiplierEvent;
	private long currentEventEndTime;
	
	private final HashMap<String, Double> worldMultipliers = new HashMap<>();
	private final HashMap<String, Double> permissionGroupMultipliers = new HashMap<>();
	private final HashMap<String, Double> playerDeathMultipliers = new HashMap<>();

	private double repeatingMultiplier;
	private int repeatingDuration;
	private int repeatingDelay;
	private int repeatingInitialDelay;
	private String repeatingStartMessage;
	private String repeatingEndMessage;

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
		loadMythicMobsLevelsMultiplier(config);
		loadLevelledMobsMultiplier(config);
		loadInfernalMobsMultiplier(config);
		loadGuildsMultiplier(config);
		reloadRepeatingMultiplierEventValues(config);
	}

	private void reloadRepeatingMultiplierEventValues(FileConfiguration config) {
		this.repeatingMultiplier = Double.parseDouble(config.getString("RepeatingMultiplierEvent.Multiplier").replace("%",""))/100;
		this.repeatingDuration = config.getInt("RepeatingMultiplierEvent.Duration") * 60;
		this.repeatingDelay = config.getInt("RepeatingMultiplierEvent.RepeatDelay") * 60 * 20;
		this.repeatingInitialDelay = config.getInt("RepeatingMultiplierEvent.InitialDelay") * 60 * 20;
		this.repeatingStartMessage = MessageManager.applyColour(config.getString("RepeatingMultiplierEvent.EventStartMessage"));
		this.repeatingEndMessage = MessageManager.applyColour(config.getString("RepeatingMultiplierEvent.EventEndMessage"));
	}

	public double applyMultipliers(double amount, Player p, Entity entityKilled) {
		double baseAmount = amount;
		
		if ( p!=null ) {
			amount += applyLootingMultiplier(baseAmount, p.getInventory().getItemInMainHand());
			amount += applyPermissionGroupMultiplier(baseAmount, p);
		}
		amount += applyEventMultiplier(baseAmount);
		amount += applyWorldMultiplier(baseAmount, entityKilled.getWorld().getName());
		amount += applyMythicMobsLevelsMultiplier(baseAmount, entityKilled);
		amount += applyLevelledMobsMultiplier(baseAmount, entityKilled);
		amount += applyInfernalMobsMultiplier(baseAmount, entityKilled);
		amount += applyGuildsMultiplier(baseAmount, p);
		
		return RandomNumberUtils.round(amount, 2);
	}
	
	public double applyPlayerDeathMultipliers(double baseAmount, Player p) {
		if (this.playerDeathMultipliers.isEmpty())
			return 0;
		if (!p.isOnline())
			return 0;
		
		String[] playerGroups = plugin.getPerms().getPlayerGroups(p);
		if (playerGroups.length == 0)
			return 0;
		
		double amountToAdd = 0;
		for (String groupName : playerGroups) {
			if (!playerDeathMultipliers.containsKey(groupName))
				continue;
			
			double groupMultiplier = playerDeathMultipliers.get(groupName);
			amountToAdd += baseAmount * groupMultiplier;
		}
		return RandomNumberUtils.round(amountToAdd, 2);
	}
	
	private double applyLootingMultiplier(double baseAmount, ItemStack killersWeapon) {
		int lootingLevel = killersWeapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS );
		return baseAmount * lootingMultiplier * lootingLevel;
	}
	
	private double applyEventMultiplier(double baseAmount) {
		return baseAmount * eventMultiplier;
	}
	
	private double applyWorldMultiplier(double baseAmount, String worldName) {
		if (worldMultipliers.isEmpty() || !worldMultipliers.containsKey(worldName))
			return 0;
		
		double worldMultiplier = worldMultipliers.get(worldName);
		baseAmount *= worldMultiplier;
		return baseAmount;
	}
	
	private double applyPermissionGroupMultiplier(double baseAmount, Player p) {
		if (permissionGroupMultipliers.isEmpty())
			return 0;
		if (!p.isOnline())
			return 0;
		
		String[] playerGroups = plugin.getPerms().getPlayerGroups(p);
		if (playerGroups.length == 0)
			return 0;
		double amountToAdd = 0;
		for (String groupName : playerGroups) {
			if (!permissionGroupMultipliers.containsKey(groupName))
				continue;
			
			double groupMultiplier = permissionGroupMultipliers.get(groupName);
			amountToAdd += baseAmount * groupMultiplier;
		}
		return amountToAdd;
	}
	
	private double applyMythicMobsLevelsMultiplier(double baseAmount, Entity entity) {
		if (mythicMobsLevelsMultiplier == 0)
			return 0;
		BukkitAPIHelper MythicMobsAPI = MythicBukkit.inst().getAPIHelper();
		if ( MythicMobsAPI.isMythicMob(entity)) {
			double level = MythicMobsAPI.getMythicMobInstance(entity).getLevel() - 1;
			return baseAmount * mythicMobsLevelsMultiplier * level;
		}
		return 0;
	}
	
	private double applyLevelledMobsMultiplier(double baseAmount, Entity entity) {
		if (levelledMobsMultiplier == 0)
			return 0;

		int level = levelledMobsManager.getLevelledMobsMobLevel(entity);
		if (level > 0){
			return baseAmount * levelledMobsMultiplier * (level-1);
		}
		return 0;
	}
	
	private double applyInfernalMobsMultiplier(double amountToAdd, Entity entity) {
		if (infernalMobsMultiplier == 0)
			return 0;
		if ( infernalMobsAPI.findMobAbilities(entity.getUniqueId())!= null ) {
			return amountToAdd * infernalMobsMultiplier;
		}
		return 0;
	}

	private double applyGuildsMultiplier(double amountToAdd, Player player){
		if (guildsMultiplier == 0)
			return 0;
		Guild playersGuild = guildsAPI.getGuild(player);
		if (playersGuild != null){
			int guildLevel = playersGuild.getTier().getLevel();
			return amountToAdd * guildsMultiplier * (guildLevel-1);
		}
		return 0;
	}
			
	// load multipliers
	
	private void loadLootingMultiplier(FileConfiguration config) {
		String strLootingMultiplier = config.getString("LootingMultiplier").replace("%", "");
		lootingMultiplier =  Double.parseDouble(strLootingMultiplier)/100;
		plugin.getMessageManager().logToConsole("&b[MoneyFromMobs] Successfully loaded Looting multiplier of " + strLootingMultiplier + "% per level of enchantment");
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
			plugin.getMessageManager().logToConsole("&b[MoneyFromMobs] Successfully loaded Permission multiplier of " + splitList[1] + " For Permission Group: " + permissionGroupName);
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
			plugin.getMessageManager().logToConsole("&b[MoneyFromMobs] Successfully loaded World multiplier of " + splitList[1] + " For World: " + worldName);
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
			plugin.getMessageManager().logToConsole("&b[MoneyFromMobs] Successfully loaded Player Death multiplier of " + splitList[1] + " For Permission Group: " + permissionGroupName);
		}
	}
	
	private void loadMythicMobsLevelsMultiplier(FileConfiguration config) {
		if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
			String strMythicMobsLevelsMultiplier = config.getString("MythicMobsLevelsMultiplier").replace("%", "");
			mythicMobsLevelsMultiplier = Double.parseDouble(strMythicMobsLevelsMultiplier)/100;

			plugin.getMessageManager().logToConsole("&b[MoneyFromMobs] Found MythicMobs and successfully loaded multiplier of " + strMythicMobsLevelsMultiplier + "% per level of mob");
		}	
	}
	
	private void loadLevelledMobsMultiplier(FileConfiguration config) {
		levelledMobsManager = new LevelledMobsManager();

		if (levelledMobsManager.hasLevelledMobsInstalled()) {
			String strLevelledMobsMultiplier = config.getString("LevelledMobsMultiplier").replace("%", "");
			levelledMobsMultiplier = Double.parseDouble(strLevelledMobsMultiplier)/100;
			plugin.getMessageManager().logToConsole("&b[MoneyFromMobs] Found LevelledMobs and successfully loaded multiplier of " + strLevelledMobsMultiplier + "% per level of mob");
		}
	}
	
	private void loadInfernalMobsMultiplier(FileConfiguration config) {
		if (Bukkit.getPluginManager().isPluginEnabled("InfernalMobs")) {
			infernalMobsAPI = (infernal_mobs) Bukkit.getPluginManager().getPlugin("InfernalMobs");
			String strInfernalMobsMultiplier = config.getString("InfernalMobsMultiplier").replace("%", "");
			infernalMobsMultiplier = Double.parseDouble(strInfernalMobsMultiplier)/100;
			plugin.getMessageManager().logToConsole("&b[MoneyFromMobs] Found Infernal Mobs and successfully loaded multiplier of " + strInfernalMobsMultiplier + "%");
		}
	}

	private void loadGuildsMultiplier(FileConfiguration config){
		if (Bukkit.getPluginManager().isPluginEnabled("Guilds")) {
			guildsAPI = Guilds.getApi();
			String strGuildsMultiplier = config.getString("GuildsMultiplier").replace("%", "");
			guildsMultiplier = Double.parseDouble(strGuildsMultiplier)/100;
			plugin.getMessageManager().logToConsole("&b[MoneyFromMobs] Found Guilds and successfully loaded multiplier of " + strGuildsMultiplier + "% per level of guild");
		}
	}

	public void setEventMultiplier(double eventMultiplier) {
		this.eventMultiplier = eventMultiplier;
	}
	
	public double getEventMultiplier() {
		return eventMultiplier;
	}

	public BukkitTask getCurrentMultiplierEvent(){
		return currentMultiplierEvent;
	}
	public void setCurrentMultiplierEvent(BukkitTask task, long eventDuration){
		currentMultiplierEvent = task;
		if (eventDuration != 0) {
			currentEventEndTime = System.currentTimeMillis() + (eventDuration * 1000);
		}
		else {
			currentEventEndTime = 0;
		}
	}

	public long getTimeLeftOfCurrentMultiplierEvent(){
		long timeLeft = (currentEventEndTime - System.currentTimeMillis()) / 1000;
		if (timeLeft < 0)
			timeLeft = 0L;

		return timeLeft;
	}

	public void reloadRepeatingMultiplierEvent(){
		FileConfiguration config = plugin.getMultipliersConfig().getConfig();
		int newDelay = config.getInt("RepeatingMultiplierEvent.RepeatDelay") * 60 * 20;

		if (newDelay != repeatingDelay || (plugin.getRepeatingMultiplierEvent() == null && config.getBoolean("RepeatingMultiplierEvent.Enabled")) ){
			if (plugin.getRepeatingMultiplierEvent() != null) {
				Bukkit.getScheduler().cancelTask(plugin.getRepeatingMultiplierEvent().getTaskId());
			}
			reloadRepeatingMultiplierEventValues(config);
			plugin.loadRepeatingMultiplierEvent();
		}
		else if (!config.getBoolean("RepeatingMultiplierEvent.Enabled")){
			if (plugin.getRepeatingMultiplierEvent() != null) {
				Bukkit.getScheduler().cancelTask(plugin.getRepeatingMultiplierEvent().getTaskId());
			}
			plugin.setRepeatingMultiplierEvent(null);
		}
	}

	public double getRepeatingMultiplier() {
		return repeatingMultiplier;
	}

	public int getRepeatingDuration() {
		return repeatingDuration;
	}

	public String getRepeatingStartMessage() {
		return repeatingStartMessage;
	}

	public String getRepeatingEndMessage() {
		return repeatingEndMessage;
	}

	public int getRepeatingDelay() {
		return repeatingDelay;
	}

	public int getRepeatingInitialDelay() {
		return repeatingInitialDelay;
	}
}
