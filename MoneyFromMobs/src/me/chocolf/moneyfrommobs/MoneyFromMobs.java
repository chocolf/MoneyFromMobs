package me.chocolf.moneyfrommobs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import me.chocolf.moneyfrommobs.commands.ClearDropsCommand;
import me.chocolf.moneyfrommobs.commands.DropMoneyCommand;
import me.chocolf.moneyfrommobs.commands.DropMoneyTabCompleter;
import me.chocolf.moneyfrommobs.commands.MuteMessagesCommand;
import me.chocolf.moneyfrommobs.commands.ReloadCommand;
import me.chocolf.moneyfrommobs.integrations.DropMoneyFlag;
import me.chocolf.moneyfrommobs.integrations.MythicMobsFileManager;
import me.chocolf.moneyfrommobs.integrations.PlaceholderAPIIntegration;
import me.chocolf.moneyfrommobs.listeners.DeathListeners;
import me.chocolf.moneyfrommobs.listeners.PaperListeners;
import me.chocolf.moneyfrommobs.listeners.PickUpListeners;
import me.chocolf.moneyfrommobs.listeners.PlaceholderAPIListener;
import me.chocolf.moneyfrommobs.listeners.WorldGuardListener;
import me.chocolf.moneyfrommobs.runnables.NearEntitiesRunnable;
import me.chocolf.moneyfrommobs.utils.ConfigUpdater;
import me.chocolf.moneyfrommobs.utils.Metrics;
import me.chocolf.moneyfrommobs.utils.UpdateChecker;
import net.milkbowl.vault.economy.Economy;

public class MoneyFromMobs extends JavaPlugin{
	private Economy econ = null;
	private MythicMobsFileManager mmConfig;
	private MfmManager manager;
	private BukkitTask inventoryIsFullRunnable;
	private PlaceholderAPIListener placeholderListener;
	
	@Override
	public void onEnable() {
		// bstats
		new Metrics(this, 8361); // 8361 is this plugins id
		
		// listeners
		new PickUpListeners(this);
		new DeathListeners(this);
		if (checkIfPaper()) new PaperListeners(this);
		if(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) new WorldGuardListener(this);
		
		
		// Auto updates config
		saveDefaultConfig();
		try {
			  ConfigUpdater.update(this, "config.yml", new File(getDataFolder(), "config.yml"), Arrays.asList());//The list is sections you want to ignore
		} 
		catch (IOException e) {
			  e.printStackTrace();
		}
		reloadConfig();
		
		// Makes MythicMobs Config
		this.mmConfig = new MythicMobsFileManager(this);
		
		// Commands
		new ReloadCommand(this);
		new DropMoneyCommand(this);
		new ClearDropsCommand(this);
		new MuteMessagesCommand(this);
		this.getCommand("mfmdrop").setTabCompleter(new DropMoneyTabCompleter());
		
		// Manager
		manager = new MfmManager(this);
		
		// PlaceholderAPIIntegration integration
		if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPIIntegration") != null){
			new PlaceholderAPIIntegration(this).register();
			placeholderListener = new PlaceholderAPIListener(this);
		}
	
		// Bukkit runnable to allow players to pickup items when inventory is full
		loadInventoryIsFullRunnable();
		
		// Disables plugin if economy set up failed
		if (!setupEconomy()) {
			getLogger().severe("Disabled becuase you don't have Vault or an economy plugin installed");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		// Checks for updates to plugin
		try {
			UpdateChecker updateChecker = new UpdateChecker(this);
			if (updateChecker.checkForUpdate() && this.getConfig().getBoolean("UpdateNotification")) 
				Bukkit.getLogger().info("Update Available for MoneyFromMobs: https://www.spigotmc.org/resources/money-from-mobs-1-9-1-16-4.79137/");			
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[MoneyFromMobs] Unable to retrieve latest update from SpigotMC.org");
		}
	}
	
	// loads WorldGuard flag
	@Override
	public void onLoad() {
		if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null)
			new DropMoneyFlag();
	}

	// sets up economy if server has Vault and an Economy plugin
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	// loads runnable that allows players to pick up money when their inventory is full
	public void loadInventoryIsFullRunnable() {
		if (checkIfPaper()) return;
		
		if ( this.getConfig().getBoolean("PickupMoneyWhenInventoryIsFull.Enabled")) {
			int interval = this.getConfig().getInt("PickupMoneyWhenInventoryIsFull.Interval");
			inventoryIsFullRunnable = new NearEntitiesRunnable(this).runTaskTimer((Plugin)this, interval, interval);
		}
	}
	
	// checks if server is running Paper 1.13+
	public boolean checkIfPaper() {
		boolean isPaper;
		try {
        	isPaper = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;	
        }
        catch (Exception e) {
        	isPaper = false;
        }
		if (isPaper && Bukkit.getVersion().contains("1.12"))
			isPaper = false;
		
		return isPaper;
	}

	public MythicMobsFileManager getMMConfig() {
		return mmConfig;
	}
	public Economy getEcon() {
		return econ;
	}
	public MfmManager getManager() {
		return manager;
	}
	public PlaceholderAPIListener getPlaceholdersListener() {
		return placeholderListener;
	}
	public BukkitTask getInventoryIsFullRunnable() {
		return inventoryIsFullRunnable;
	}

}
