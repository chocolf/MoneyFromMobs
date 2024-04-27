package me.chocolf.moneyfrommobs.listener;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.util.VersionUtils;

public class MobSpawnListener implements Listener {
	
	private final MoneyFromMobs plugin;
	
	public MobSpawnListener(MoneyFromMobs plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}	
	
	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent e) {
		// saves spawn reason to mob to check later on death
		String spawnReason = e.getSpawnReason().name();
		LivingEntity entity = e.getEntity();
		if (VersionUtils.getVersionNumber() > 13) {
			PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
			NamespacedKey key = new NamespacedKey(plugin, "MfMSpawnReason");
			dataContainer.set(key, PersistentDataType.STRING, spawnReason);
		}
		else {
		    entity.setMetadata("MfMSpawnReason", new FixedMetadataValue(this.plugin, spawnReason));
		}
	}
}
