package me.chocolf.moneyfrommobs.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;



public class DropMoneyTabCompleter implements TabCompleter{
	
	List<String> amountToDrop = new ArrayList<>();
	List<String> numberOfDrops = new ArrayList<>();
	List<String> world = new ArrayList<>();
	List<String> posX = new ArrayList<>();
	List<String> posY = new ArrayList<>();
	List<String> posZ = new ArrayList<>();
	List<String> nothing = new ArrayList<>();
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if ( amountToDrop.isEmpty() ) {
			amountToDrop.add("<AmountToDrop>");
		}
		if ( numberOfDrops.isEmpty() ) {
			numberOfDrops.add("[NumberOfDrops]");
		}
		if ( world.isEmpty()){
			world.add("[World]");
		}
		if ( posX.isEmpty()){
			posX.add("[PosX]");
		}
		if ( posY.isEmpty()){
			posY.add("[PosY]");
		}
		if ( posZ.isEmpty()){
			posZ.add("[PosZ]");
		}
		
		if (args.length == 1) {
			return amountToDrop;
		}
		if (args.length == 2) {
			return numberOfDrops;
		}
		if (args.length == 3) {
			return world;
		}
		if (args.length == 4) {
			return posX;
		}
		if (args.length == 5) {
			return posY;
		}
		if (args.length == 6) {
			return posZ;
		}

		
		return nothing;
	}

}
