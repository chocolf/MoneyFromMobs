package me.chocolf.moneyfrommobs.commands;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.api.events.DropMoneyEvent;
import me.chocolf.moneyfrommobs.managers.DropsManager;
import me.chocolf.moneyfrommobs.managers.MessageManager;
import me.chocolf.moneyfrommobs.managers.PickUpManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PlayerDropMoneyCommand implements CommandExecutor{

	private final MoneyFromMobs plugin;


	public PlayerDropMoneyCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("dropmoney").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PickUpManager pickUpManager = plugin.getPickUpManager();
		DropsManager dropsManager = plugin.getDropsManager();

		if (!(sender instanceof Player)) {
			sender.sendMessage(MessageManager.applyColour("&cYou must be a player to use this command."));
		}

		if(args.length > 0) {
			Player p = (Player) sender;
			double amount;
			try {
				amount = Double.parseDouble(args[0]);
				amount = Math.round(amount * 100.0) / 100.0; // changes it to 2 decimal places
			}
			catch (Exception e) {
				sender.sendMessage(MessageManager.applyColour("&cInvalid number."));
				return false;
			}

			if (plugin.getEcon().getBalance(p) < amount){
				sender.sendMessage(MessageManager.applyColour("&cYou do not have enough money to drop the amount specified."));
				return true;
			}

			int numberOfDrops = 1;
			ItemStack itemToDrop = pickUpManager.getItemToDrop();

			// sets location
			Vector playerDirection = p.getLocation().getDirection().multiply(3);
			Location location = p.getLocation().add(playerDirection);
			location.setY(location.getY()+1);

			// calls drop money event
			DropMoneyEvent dropMoneyEvent = new DropMoneyEvent(itemToDrop,amount, location, null, null, numberOfDrops);
			Bukkit.getPluginManager().callEvent(dropMoneyEvent);
			if (dropMoneyEvent.isCancelled())
				return true;
			itemToDrop = dropMoneyEvent.getItemToDrop();
			amount = dropMoneyEvent.getAmount();
			location = dropMoneyEvent.getLocation();
			numberOfDrops = dropMoneyEvent.getNumberOfDrops();
			
			dropsManager.dropItem(itemToDrop, amount*numberOfDrops, location, numberOfDrops, null, false);
			plugin.getEcon().withdrawPlayer(p, amount);
			return true;
		}
		return false;
	}
	
}
