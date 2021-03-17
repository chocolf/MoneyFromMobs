package me.chocolf.moneyfrommobs.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MfmManager;
import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.event.DropMoneyEvent;
import me.chocolf.moneyfrommobs.util.Utils;

public class DropMoneyCommand implements CommandExecutor{
	
	private MoneyFromMobs plugin;
	
	
	public DropMoneyCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmdrop").setExecutor(this);	
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("mfmdrop")) {
			if (sender.hasPermission("MoneyFromMobs.drop")) {
				MfmManager manager = plugin.getManager();
				if(args.length > 0) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(Utils.applyColour("&cThis command must be ran by a player."));
						return true;
					}
					Player p = (Player) sender;
					
					Location location = p.getTargetBlock(null, 100).getLocation();
					location.setY(location.getY()+1);
					double amount = Double.parseDouble(args[0]);
					int numberOfDrops = 1;
					ItemStack itemToDrop = manager.getItemToDrop();
					if (args.length >= 2) {
						try {
							Integer.parseInt(args[1]);
						}
						catch (Exception e) {
							return false;
						}
						numberOfDrops = Integer.valueOf(args[1]);
						if (numberOfDrops > 100) {
							sender.sendMessage(Utils.applyColour("&9Number of drops can not be above 100."));
							numberOfDrops = 100;
						}
					}
					DropMoneyEvent dropMoneyEvent = new DropMoneyEvent(itemToDrop,amount, location, p, null, numberOfDrops);
					Bukkit.getPluginManager().callEvent(dropMoneyEvent);
					if (dropMoneyEvent.isCancelled()) return true;
					itemToDrop = dropMoneyEvent.getItemToDrop();
					amount = dropMoneyEvent.getAmount();
					location = dropMoneyEvent.getLocation();
					numberOfDrops = dropMoneyEvent.getNumberOfDrops();
					
					manager.dropItem(itemToDrop, amount*numberOfDrops, location, numberOfDrops);
					return true;
				}
				
				
			}else {
				sender.sendMessage(Utils.applyColour("&cYou don't have permission to use this command!"));
				return true;
			}
		}
		return false;
	}
	
}
