package me.chocolf.moneyfrommobs.integration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class MythicMobsFileManager {
	private MoneyFromMobs plugin;
	private FileConfiguration mythicMobsConfig = null;
	private File configFile = null;
	
	public MythicMobsFileManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		this.saveDefaultConfig();
	}
	
	public void reloadConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "MfmMythicMobs.yml");
		
		this.mythicMobsConfig = YamlConfiguration.loadConfiguration(this.configFile);
		
		InputStream defaultStream = this.plugin.getResource("MfmMythicMobs.yml");
		if ( defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			this.mythicMobsConfig.setDefaults(defaultConfig);
		}
	}
	
	public FileConfiguration getConfig() {
		if ( this.mythicMobsConfig==null)
			reloadConfig();
		return this.mythicMobsConfig;
	}
	
	public void saveConfig() {
		if (this.mythicMobsConfig==null || this.configFile == null)
			return;
		
		try {
			this.getConfig().save(this.configFile);
		} catch (IOException e) {
			Bukkit.getLogger().warning("[MoneyFromMobs] Could not save MfmMythicMobs config file");
		}
		
		
	}
	
	public void saveDefaultConfig() {
		if (this.configFile==null)
			this.configFile = new File(this.plugin.getDataFolder(), "MfmMythicMobs.yml");
		
		if (!this.configFile.exists()) {
			this.plugin.saveResource("MfmMythicMobs.yml", false);
		}
	}
	
}
