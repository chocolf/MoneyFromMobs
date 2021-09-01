package me.chocolf.moneyfrommobs.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.MessageManager;

public class HelpCommand implements CommandExecutor{
	
	private MoneyFromMobs plugin;
	
	public HelpCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmhelp").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		sender.sendMessage(MessageManager.applyColour(""));
		sender.sendMessage(MessageManager.applyColour("&a----------Money&fFrom&aMobs Help----------"));
		sender.sendMessage(MessageManager.applyColour(""));
		sender.sendMessage(MessageManager.applyColour("&a/MfmReload - &fReloads MoneyFromMobs"));
		sender.sendMessage(MessageManager.applyColour(""));
		sender.sendMessage(MessageManager.applyColour("&a/MfmClear - &fClears all money from the ground"));
		sender.sendMessage(MessageManager.applyColour(""));
		sender.sendMessage(MessageManager.applyColour("&a/MfmMute - &fMute incoming messages when picking up money for the player who ran this command"));
		sender.sendMessage(MessageManager.applyColour(""));
		sender.sendMessage(MessageManager.applyColour("&a/MfmDrop <Amount> [NumberOfDrops] - &fDrops money on player's cursor"));
		sender.sendMessage(MessageManager.applyColour(""));
		sender.sendMessage(MessageManager.applyColour("&a/MfmEvent <Start/Stop> [PercentageIncrease] [Hours] [Minutes] [Seconds] - &fCreate global multipliers for a limited time"));
		sender.sendMessage(MessageManager.applyColour(""));
		sender.sendMessage(String.format(MessageManager.applyColour("&aCurrent Version: &f%s"),plugin.getDescription().getVersion() ));
		
		return true;
	}
}
