package me.chocolf.moneyfrommobs.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;



public class DropMoneyTabCompleter implements TabCompleter{
	
	List<String> arguments1 = new ArrayList<>();
	List<String> arguments2 = new ArrayList<>();
	List<String> nothing = new ArrayList<>();
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if ( arguments1.isEmpty() ) {
			arguments1.add("<AmountToDrop>");
		}
		if ( arguments2.isEmpty() ) {
			arguments2.add("<NumberOfDrops>");
		}
		
		if (args.length == 1) {
			return arguments1;
		}
		if (args.length == 2) {
			return arguments2;
		}
		
		return nothing;
	}

}
