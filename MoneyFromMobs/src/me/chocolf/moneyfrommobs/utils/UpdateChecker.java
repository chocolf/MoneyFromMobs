package me.chocolf.moneyfrommobs.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class UpdateChecker implements Listener {
	private MoneyFromMobs plugin;

	public UpdateChecker(MoneyFromMobs plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
	}
	
	
	public boolean checkForUpdate() {
		double currentVersion = Double.valueOf(plugin.getDescription().getVersion());
		double latestVersion = Double.valueOf(getLatestVersion());
		
		if (currentVersion >= latestVersion) {
			return false;
		}
		else {
			return true;
		}
		
	}

	private String getLatestVersion() {
		try {
	        URLConnection urlConnection = new URL("https://api.spigotmc.org/legacy/update.php?resource=79137").openConnection();
	        return new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
	    } catch (Exception exception) {
	        return null;
	    }
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (!p.isOp()) return;
		if (!plugin.getConfig().getBoolean("UpdateNotification")) return;
		
		if (checkForUpdate()) {
			p.sendMessage(Utils.applyColour("&bUpdate Available for MoneyFromMobs:"));
			p.sendMessage(Utils.applyColour("&bhttps://www.spigotmc.org/resources/money-from-mobs-1-9-1-16-4.79137/"));
		}
	}
}
