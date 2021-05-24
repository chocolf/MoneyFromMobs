package me.chocolf.moneyfrommobs.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.MessageManager;
import me.chocolf.moneyfrommobs.manager.NumbersManager;

public class MfmEventCommand implements CommandExecutor{
	
	private MoneyFromMobs plugin;
	
	BukkitTask task;
	
	
	public MfmEventCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmevent").setExecutor(this);	
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		int numberOfArgs = args.length;
		MessageManager messageManager = plugin.getMessageManager();
		NumbersManager numbersManager = plugin.getNumbersManager();
		
		if (numberOfArgs > 0) {
			if (args[0].equalsIgnoreCase("stop")) {
				if (task != null) {
					numbersManager.setEventMultiplier(0);
					Bukkit.broadcastMessage(messageManager.getMessage("eventFinish"));
					Bukkit.getScheduler().cancelTask(task.getTaskId());
					task = null;
					return true;
				}
				else {
					sender.sendMessage(messageManager.getMessage("noEventRunningMessage") );
					return true;
				}
			}

			else if (args[0].equalsIgnoreCase("start") && numberOfArgs >= 5) {
				try {
					if (task != null) {
						sender.sendMessage(messageManager.getMessage("eventAlreadyRunningMessage"));
						return true;
					}
					
					numbersManager.setEventMultiplier(Double.parseDouble(args[1].replace("%", ""))/100);
					
					int hours = Integer.parseInt(args[2]);
					int minutes = Integer.parseInt(args[3]);
					int seconds = Integer.parseInt(args[4]);
					int totalTime = hours*3600 + minutes*60 + seconds;
					
					Bukkit.broadcastMessage(messageManager.getMessage("eventStart")
							.replace("{multiplier}", args[1].replace("%", ""))
							.replace("{hours}", args[2])
							.replace("{minutes}", args[3])
							.replace("{seconds}", args[4]));
					
					task = Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					    @Override
					    public void run() {
					    	numbersManager.setEventMultiplier(0);
					    	task = null;
					    	Bukkit.broadcastMessage(messageManager.getMessage("eventFinish"));
					    }
					}, totalTime * 20L);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
			else
				return false;
			
		}
		return false;
		
	}

}


