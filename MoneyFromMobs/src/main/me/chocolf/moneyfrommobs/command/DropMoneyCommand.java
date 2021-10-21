package me.chocolf.moneyfrommobs.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.api.event.DropMoneyEvent;
import me.chocolf.moneyfrommobs.manager.DropsManager;
import me.chocolf.moneyfrommobs.manager.MessageManager;
import me.chocolf.moneyfrommobs.manager.PickUpManager;

public class DropMoneyCommand implements CommandExecutor{
	
	private final MoneyFromMobs plugin;
	
	
	public DropMoneyCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmdrop").setExecutor(this);	
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PickUpManager pickUpManager = plugin.getPickUpManager();
		DropsManager dropsManager = plugin.getDropsManager();
		
		if(args.length > 0) {
			double amount = Double.parseDouble(args[0]);
			int numberOfDrops = 1;
			ItemStack itemToDrop = pickUpManager.getItemToDrop();

			if (args.length >= 2) {
				try {
					numberOfDrops = Integer.parseInt(args[1]);
				}
				catch (Exception e) {
					return false;
				}
				if (numberOfDrops > 100) {
					sender.sendMessage(MessageManager.applyColour("&9Number of drops can not be above 100."));
					numberOfDrops = 100;
				}
			}
			// sets location if they have included it in the command
			Location location;

			if (args.length == 3 && args[2].equalsIgnoreCase("here")){
				location = ((Entity) sender).getLocation();
			}
			else if (args.length >= 6){
				location = new Location(Bukkit.getWorld(args[2]), Double.valueOf(args[3]), Double.valueOf(args[4]), Double.valueOf(args[5]));
			}
			else if (sender instanceof Player && args.length < 3){
				Player p = (Player) sender;
				location = p.getTargetBlock(null, 100).getLocation();
				location.setY(location.getY()+1);
			}
			else{
				return false;
			}

			// calls drop money event
			DropMoneyEvent dropMoneyEvent = new DropMoneyEvent(itemToDrop,amount, location, null, null, numberOfDrops);
			Bukkit.getPluginManager().callEvent(dropMoneyEvent);
			if (dropMoneyEvent.isCancelled()) return true;
			itemToDrop = dropMoneyEvent.getItemToDrop();
			amount = dropMoneyEvent.getAmount();
			location = dropMoneyEvent.getLocation();
			numberOfDrops = dropMoneyEvent.getNumberOfDrops();
			
			dropsManager.dropItem(itemToDrop, amount*numberOfDrops, location, numberOfDrops, null);
			return true;
		}
		return false;
	}
	
}
