package me.chocolf.moneyfrommobs.managers;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class LevelledMobsManager {
    private final Boolean levelledMobsIsInstalled;
    private NamespacedKey key;

    public LevelledMobsManager(){
        Plugin levelledMobsPlugin = Bukkit.getPluginManager().getPlugin("LevelledMobs");
        levelledMobsIsInstalled = levelledMobsPlugin != null && levelledMobsPlugin.isEnabled();

        if (levelledMobsIsInstalled){
            key = new NamespacedKey(levelledMobsPlugin, "level");
        }
    }

    public boolean hasLevelledMobsInstalled(){
        return levelledMobsIsInstalled != null && levelledMobsIsInstalled;
    }

    public int getLevelledMobsMobLevel(Entity entity){
        if (!hasLevelledMobsInstalled()) return 0;

        Integer mobLevel = entity.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return Objects.requireNonNullElse(mobLevel, 0);
    }
}
