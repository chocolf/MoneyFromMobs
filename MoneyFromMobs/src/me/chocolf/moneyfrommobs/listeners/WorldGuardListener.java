package me.chocolf.moneyfrommobs.listeners;

import me.chocolf.moneyfrommobs.integrations.WorldGuardFlags;
import me.chocolf.moneyfrommobs.managers.DropsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.api.events.AttemptToDropMoneyEvent;

public class WorldGuardListener implements Listener{

	MoneyFromMobs plugin;
	
	public WorldGuardListener(MoneyFromMobs plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onAttemptToDropMoney(AttemptToDropMoneyEvent e) {
		Entity entity = e.getEntity();
		Location loc = entity.getLocation();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
		DropsManager dropsManager = plugin.getDropsManager();

		// if drop-money flag is deny cancel the drop
		if (!set.testState(null, WorldGuardFlags.getDropMoneyFlag())){
			e.setCancelled(true);
		}

		// if player-drop-money flag is deny and entity is a player cancel the drop
		else if(!set.testState(null, WorldGuardFlags.getPlayerDropMoneyFlag()) && entity instanceof Player){
			e.setCancelled(true);
		}

		// if spawner-mob-drop-money flag is deny and entity was spawned from a spawner cancel the drop
		else if (!set.testState(null, WorldGuardFlags.getSpawnerMobDropMoneyFlag()) && dropsManager.getSpawnReason(entity) != null && dropsManager.getSpawnReason(entity).equals("SPAWNER")){
			e.setCancelled(true);
		}
	}
}
