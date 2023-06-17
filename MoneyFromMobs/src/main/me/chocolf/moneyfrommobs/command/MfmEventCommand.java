package me.chocolf.moneyfrommobs.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.MessageManager;
import me.chocolf.moneyfrommobs.manager.MultipliersManager;

import java.lang.reflect.Array;

public class MfmEventCommand implements CommandExecutor{
	
	private final MoneyFromMobs plugin;

	
	
	public MfmEventCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmevent").setExecutor(this);	
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		int numberOfArgs = args.length;
		MessageManager messageManager = plugin.getMessageManager();
		MultipliersManager multipliersManager = plugin.getMultipliersManager();
		BukkitTask task = multipliersManager.getCurrentMultiplierEvent();
		
		if (numberOfArgs > 0) {
			if (args[0].equalsIgnoreCase("stop")) {
				if (task != null) {
					multipliersManager.setEventMultiplier(0);
					String messageToSendOnEnd = messageManager.getMessage("eventFinish");
					Bukkit.getConsoleSender().sendMessage(messageToSendOnEnd);
					for (Player p : Bukkit.getServer().getOnlinePlayers()){
						if (!p.hasMetadata("MfmMuteMessages")){
							if (messageManager.shouldSendEventMessageAsTitle())
								p.sendTitle("",messageToSendOnEnd,1,50,1);
							else
								p.sendMessage(messageToSendOnEnd);
						}
					}
					Bukkit.getScheduler().cancelTask(task.getTaskId());
					multipliersManager.setCurrentMultiplierEvent(null);
				}
				else {
					sender.sendMessage(messageManager.getMessage("noEventRunningMessage") );
				}
				return true;
			}

			else if (args[0].equalsIgnoreCase("start") && numberOfArgs >= 3) {
				try {
					if (task != null) {
						sender.sendMessage(messageManager.getMessage("eventAlreadyRunningMessage"));
						return true;
					}
					
					multipliersManager.setEventMultiplier(Double.parseDouble(args[1].replace("%", ""))/100);
					String duration = args[2];
					int hours = 0;
					int minutes = 0;
					int seconds = 0;

					if (duration.contains("h")){
						String[] hoursArray = duration.split("h")[0].split("[a-zA-Z]");
						hours = Integer.parseInt(hoursArray[hoursArray.length - 1]);
					}
					if (duration.contains("m")){
						String[] minutesArray = duration.split("m")[0].split("[a-zA-Z]");
						minutes = Integer.parseInt(minutesArray[minutesArray.length - 1]);
					}
					if (duration.contains("s")){
						String[] secondsArray = duration.split("s")[0].split("[a-zA-Z]");
						seconds = Integer.parseInt(secondsArray[secondsArray.length - 1]);
					}
					int totalTime = hours*3600 + minutes*60 + seconds;

					if (totalTime == 0) return false;

					String messageToSend = messageManager.getMessage("eventStart")
							.replace("{multiplier}", args[1].replace("%", ""))
							.replace("{hours}", String.valueOf(hours))
							.replace("{minutes}", String.valueOf(minutes))
							.replace("{seconds}", String.valueOf(seconds));

					Bukkit.getConsoleSender().sendMessage(messageToSend);
					for (Player p : Bukkit.getServer().getOnlinePlayers()){
						if (!p.hasMetadata("MfmMuteMessages")){
							if (messageManager.shouldSendEventMessageAsTitle())
								p.sendTitle("",messageToSend,1,50,1);
							else
								p.sendMessage(messageToSend);
						}

					}

					BukkitTask currentTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
						multipliersManager.setEventMultiplier(0);
						multipliersManager.setCurrentMultiplierEvent(null);
						String messageToSendOnEnd = messageManager.getMessage("eventFinish");
						Bukkit.getConsoleSender().sendMessage(messageToSendOnEnd);
						for (Player p : Bukkit.getServer().getOnlinePlayers()){
							if (!p.hasMetadata("MfmMuteMessages")){
								if (messageManager.shouldSendEventMessageAsTitle())
									p.sendTitle("",messageToSendOnEnd,1,50,1);
								else
									p.sendMessage(messageToSendOnEnd);
							}
						}
					}, totalTime * 20L);
					multipliersManager.setCurrentMultiplierEvent(currentTask);
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


