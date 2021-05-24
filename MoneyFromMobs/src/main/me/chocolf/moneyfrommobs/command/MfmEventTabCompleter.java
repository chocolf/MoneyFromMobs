package me.chocolf.moneyfrommobs.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MfmEventTabCompleter implements TabCompleter{

	List<String> startOrStop = new ArrayList<>();
	List<String> percentageIncrease = new ArrayList<>();
	List<String> hours = new ArrayList<>();
	List<String> minutes = new ArrayList<>();
	List<String> seconds = new ArrayList<>();
	List<String> nothing = new ArrayList<>();
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		if ( startOrStop.isEmpty()) {
			startOrStop.add("Start");
			startOrStop.add("Stop");
		}
		
		if (percentageIncrease.isEmpty()) {
			percentageIncrease.add("<PercentageIncrease>");
		}
		
		if (hours.isEmpty()) {
			hours.add("<Hours>");
		}
		
		if (minutes.isEmpty()) {
			minutes.add("<Minutes>");
		}
		
		if (seconds.isEmpty()) {
			seconds.add("<Seconds>");
		}
				
		int argsLength = args.length;
		if (argsLength == 1)
			return startOrStop;
		if (args[0].equalsIgnoreCase("start")) {
			if (argsLength == 2) {
				return percentageIncrease;
			}
			
			if (argsLength == 3) {
				
				return hours;
			}
			
			if (argsLength == 4) {
				return minutes;
			}
			
			if (argsLength == 5) {
				return seconds;
			}
		}
		return nothing;
	}

	
	
}
