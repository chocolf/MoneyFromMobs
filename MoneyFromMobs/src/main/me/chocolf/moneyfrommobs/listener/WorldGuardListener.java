package me.chocolf.moneyfrommobs.listener;

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
import me.chocolf.moneyfrommobs.api.event.AttemptToDropMoneyEvent;
import me.chocolf.moneyfrommobs.integration.DropMoneyFlag;
import me.chocolf.moneyfrommobs.integration.PlayerDropMoneyFlag;

public class WorldGuardListener implements Listener{
	
	
	public WorldGuardListener(MoneyFromMobs plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onAttemptToDropMoney(AttemptToDropMoneyEvent e) {
		// if drop-money flag is deny cancel the drop
		Entity entity = e.getEntity();
		Location loc = entity.getLocation();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
		if (!set.testState(null, DropMoneyFlag.getDropMoneyFlag()))
			e.setCancelled(true);
		if (!set.testState(null, PlayerDropMoneyFlag.getPlayerDropMoneyFlag()) && entity instanceof Player)
			e.setCancelled(true);
	}

}
