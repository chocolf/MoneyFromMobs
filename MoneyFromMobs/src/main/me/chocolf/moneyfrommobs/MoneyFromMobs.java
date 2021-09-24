package me.chocolf.moneyfrommobs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import me.chocolf.moneyfrommobs.command.ClearDropsCommand;
import me.chocolf.moneyfrommobs.command.DropMoneyCommand;
import me.chocolf.moneyfrommobs.command.DropMoneyTabCompleter;
import me.chocolf.moneyfrommobs.command.HelpCommand;
import me.chocolf.moneyfrommobs.command.MfmEventCommand;
import me.chocolf.moneyfrommobs.command.MfmEventTabCompleter;
import me.chocolf.moneyfrommobs.command.MuteMessagesCommand;
import me.chocolf.moneyfrommobs.command.ReloadCommand;
import me.chocolf.moneyfrommobs.integration.DropMoneyFlag;
import me.chocolf.moneyfrommobs.integration.MoneyFromMobsPlaceholderExpansion;
import me.chocolf.moneyfrommobs.integration.MythicMobsFileManager;
import me.chocolf.moneyfrommobs.integration.PlayerDropMoneyFlag;
import me.chocolf.moneyfrommobs.listener.DeathListeners;
import me.chocolf.moneyfrommobs.listener.MobSpawnListener;
import me.chocolf.moneyfrommobs.listener.OnJoinListener;
import me.chocolf.moneyfrommobs.listener.PaperListeners;
import me.chocolf.moneyfrommobs.listener.PickUpListeners;
import me.chocolf.moneyfrommobs.listener.PlaceholderAPIListener;
import me.chocolf.moneyfrommobs.listener.WorldGuardListener;
import me.chocolf.moneyfrommobs.manager.DropsManager;
import me.chocolf.moneyfrommobs.manager.MessageManager;
import me.chocolf.moneyfrommobs.manager.MultipliersFileManager;
import me.chocolf.moneyfrommobs.manager.MultipliersManager;
import me.chocolf.moneyfrommobs.manager.MobManager;
import me.chocolf.moneyfrommobs.manager.PickUpManager;
import me.chocolf.moneyfrommobs.runnable.NearEntitiesRunnable;
import me.chocolf.moneyfrommobs.util.ConfigUpdater;
import me.chocolf.moneyfrommobs.util.Metrics;
import me.chocolf.moneyfrommobs.util.UpdateChecker;
import me.chocolf.moneyfrommobs.util.VersionUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class MoneyFromMobs extends JavaPlugin{
	private Economy econ = null;
	private Permission permissions = null;
	private MythicMobsFileManager mmConfig;
	private MultipliersFileManager multipliersConfig;
	private PickUpManager pickUpManager;
	private MessageManager messageManager;
	private DropsManager dropsManager;
	private MobManager mobManager;
	private MultipliersManager multipliersManager;
	private BukkitTask inventoryIsFullRunnable;
	private PlaceholderAPIListener placeholderListener;
	private static MoneyFromMobs instance;
	
	@Override
	public void onEnable() {
		instance = this;
		
		// bstats
		new Metrics(this, 8361); // 8361 is this plugins id
		
		// vault econ and perms
		setupEconomy();
		setupPermissions();
		
		// listeners
		new PickUpListeners(this);
		new DeathListeners(this);
		new MobSpawnListener(this);
		new OnJoinListener(this);
		if (isUsingPaper()) new PaperListeners(this);
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && VersionUtils.getVersionNumber() > 15)
			new WorldGuardListener(this);
		
		// config stuff
		loadConfigs();
		
		// Commands
		new ClearDropsCommand(this);
		new DropMoneyCommand(this);
		this.getCommand("mfmdrop").setTabCompleter(new DropMoneyTabCompleter());
		new MfmEventCommand(this);
		this.getCommand("mfmevent").setTabCompleter(new MfmEventTabCompleter());
		new MuteMessagesCommand(this);
		new ReloadCommand(this);
		new HelpCommand(this);
		
		
		// Managers
		pickUpManager = new PickUpManager(this);
		messageManager = new MessageManager(this);
		dropsManager = new DropsManager(this);
		mobManager = new MobManager(this);
		multipliersManager = new MultipliersManager(this);
		
		// PlaceholderAPI integration
		if(Bukkit.getPluginManager().isPluginEnabled("PlaceHolderAPI")){
			new MoneyFromMobsPlaceholderExpansion(this).register();
			placeholderListener = new PlaceholderAPIListener(this);
		}
	
		// Bukkit runnable to allow players to pickup items when inventory is full
		loadInventoryIsFullRunnable();
		
		// Checks if user is using the latest version of the plugin on spigot
		try {
			if (UpdateChecker.checkForUpdate())
				getLogger().info("Update Available for MoneyFromMobs: https://www.spigotmc.org/resources/money-from-mobs-1-9-1-16-4.79137/");	
		}
		catch (Exception e) {
			getLogger().warning("Unable to retrieve latest update from SpigotMC.org");
		}
	}
	
	@Override
	public void onLoad() {
		// loads WorldGuard flag
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && VersionUtils.getVersionNumber() > 15) {
			DropMoneyFlag.registerFlag();
			PlayerDropMoneyFlag.registerFlag();
		}
	}
	
	@Override
	public void onDisable() {
		List<World> worldList = Bukkit.getServer().getWorlds();
		for (World world : worldList) {
			List<Entity> entList = world.getEntities();
			for (Entity entity : entList) {
				if ( entity instanceof ArmorStand ) {
					ArmorStand armorstand = (ArmorStand) entity;
					if (armorstand.hasMetadata("mfmas")) {
						armorstand.remove();
					}
				}
			}
		}
	}

	// sets up economy if server has Vault and an Economy plugin
	private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            return false;
        
    	RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    	if (rsp != null)
    		econ = rsp.getProvider();
        return econ != null;
    }
	
	// sets up permission hook
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permissions = rsp.getProvider();
        return permissions != null;
	}
	
	private void loadConfigs() {
		saveDefaultConfig();
		try {
			  ConfigUpdater.update(this, "config.yml", new File(getDataFolder(), "config.yml"), Arrays.asList());//The list is sections you want to ignore
		} 
		catch (IOException e) {
			  e.printStackTrace();
		}
		reloadConfig();
		
		// Makes MythicMobs and Multipliers Config file
		mmConfig = new MythicMobsFileManager(this);
		multipliersConfig = new MultipliersFileManager(this);
		
		try {
			  ConfigUpdater.update(this, "Multipliers.yml", new File(getDataFolder(), "Multipliers.yml"), Arrays.asList());//The list is sections you want to ignore
		} 
		catch (IOException e) {
			  e.printStackTrace();
		}
	}
	
	// loads runnable that allows players to pick up money when their inventory is full
	public void loadInventoryIsFullRunnable() {
		if (isUsingPaper()) return;
		
		if ( getConfig().getBoolean("PickupMoneyWhenInventoryIsFull.Enabled")) {
			int interval = this.getConfig().getInt("PickupMoneyWhenInventoryIsFull.Interval");
			inventoryIsFullRunnable = new NearEntitiesRunnable(this).runTaskTimer(this, interval, interval);
		}
	}
	
	// checks if server is running Paper 1.13+
	public boolean isUsingPaper() {
		String version = getServer().getVersion();
		return ( version.contains("Paper") || version.contains("Purpur") ) && !VersionUtils.getBukkitVersion().contains("1.12");
	}

	public MythicMobsFileManager getMMConfig() {
		return mmConfig;
	}
	public MultipliersFileManager getMultipliersConfig() {
		return multipliersConfig;
	}
	public Economy getEcon() {
		return econ;
	}
	public Permission getPerms() {
		return permissions;
	}
	public PickUpManager getPickUpManager() {
		return pickUpManager;
	}
	public MessageManager getMessageManager() {
		return messageManager;
	}
	public MultipliersManager getMultipliersManager() {
		return multipliersManager;
	}

	public DropsManager getDropsManager() {
		return dropsManager;
	}
	public MobManager getNumbersManager() {
		return mobManager;
	}
	public PlaceholderAPIListener getPlaceholdersListener() {
		return placeholderListener;
	}
	public BukkitTask getInventoryIsFullRunnable() {
		return inventoryIsFullRunnable;
	}
	public static MoneyFromMobs getInstance() {
		return instance;
	}
}
