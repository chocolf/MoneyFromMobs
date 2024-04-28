package me.chocolf.moneyfrommobs.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class MultipliersFileManager {
	private final MoneyFromMobs plugin;
	private FileConfiguration multipliersConfig = null;
	private File configFile = null;
	
	public MultipliersFileManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		this.saveDefaultConfig();
	}
	
	public void reloadConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "Multipliers.yml");
		
		this.multipliersConfig = YamlConfiguration.loadConfiguration(this.configFile);
		
		InputStream defaultStream = this.plugin.getResource("Multipliers.yml");
		if ( defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			this.multipliersConfig.setDefaults(defaultConfig);
		}
	}
	
	public FileConfiguration getConfig() {
		if ( this.multipliersConfig==null)
			reloadConfig();
		return this.multipliersConfig;
	}
	
	public void saveConfig() {
		if (this.multipliersConfig==null || this.configFile == null)
			return;
		
		try {
			this.getConfig().save(this.configFile);
		} catch (IOException e) {
			Bukkit.getLogger().warning("[MoneyFromMobs] Could not save Multipliers.yml");
		}
	}
	
	public void saveDefaultConfig() {
		if (this.configFile==null)
			this.configFile = new File(this.plugin.getDataFolder(), "Multipliers.yml");
		
		if (!this.configFile.exists()) {
			this.plugin.saveResource("Multipliers.yml", false);
		}
	}
}
