package me.chocolf.moneyfrommobs.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.managers.MessageManager;
import me.chocolf.moneyfrommobs.utils.UpdateChecker;

import java.time.Duration;
import java.time.Instant;

public class OnJoinListener implements Listener{

	private final MoneyFromMobs plugin;
	private Instant lastCheckAt;
	
	public OnJoinListener(MoneyFromMobs plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (!e.getPlayer().isOp()) {
			return;
		}
		if (lastCheckAt != null && Duration.between(lastCheckAt, Instant.now()).toMinutes() < 30) {
			return;
		}
		lastCheckAt = Instant.now();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!UpdateChecker.checkForUpdate()) {
                return;
            }
			Player p = e.getPlayer();
            p.sendMessage("");
            p.sendMessage(MessageManager.applyColour("&aUpdate Available for &lMoneyFromMobs&a: "));
            p.sendMessage(MessageManager.applyColour("https://www.spigotmc.org/resources/money-from-mobs.79137/"));
            p.sendMessage("");
        });
	}
}
