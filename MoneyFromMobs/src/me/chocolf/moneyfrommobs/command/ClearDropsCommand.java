package me.chocolf.moneyfrommobs.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class ClearDropsCommand implements CommandExecutor{
	
	private final MoneyFromMobs plugin;
	
	public ClearDropsCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmclear").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		int count = 0;
		List<World> worldList = Bukkit.getServer().getWorlds();
		for (World world : worldList) {
			List<Entity> entList = world.getEntities();
			for (Entity entity : entList) {
				if ( entity instanceof Item ) {
					ItemStack item = ((Item) entity).getItemStack();
					if (plugin.getPickUpManager().isMoneyPickedUp(item)) {
						entity.remove();
						count++;
					}
				}
			}
		}
		if (sender instanceof Player) {
			Player p = (Player) sender;
			List<Entity> entList = p.getNearbyEntities(0.5, 0.5, 0.5);
			for (Entity entity : entList) {
				if (entity instanceof ArmorStand) {
					ArmorStand armorStand = (ArmorStand) entity;
					if (armorStand.isInvisible() && armorStand.isMarker() && !armorStand.hasGravity() && armorStand.isCustomNameVisible())
						armorStand.remove();
				}
			}
		}
		
		sender.sendMessage(plugin.getMessageManager().getMessage("clearMoneyDropsMessage").replace("%amount%", String.valueOf(count)));
		return true;
	}

}
