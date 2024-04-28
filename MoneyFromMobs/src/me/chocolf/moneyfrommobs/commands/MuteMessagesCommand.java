package me.chocolf.moneyfrommobs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.managers.MessageManager;

public class MuteMessagesCommand implements CommandExecutor{
	
	private final MoneyFromMobs plugin;
	
	
	public MuteMessagesCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmmute").setExecutor(this);		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		MessageManager messageManager = plugin.getMessageManager();
		Player p = (Player) sender;
		if (p.hasMetadata("MfmMuteMessages")) {
			p.removeMetadata("MfmMuteMessages", plugin);
			p.sendMessage(messageManager.getMessage("muteToggleOffMessage"));
		}else {
			p.setMetadata("MfmMuteMessages", new FixedMetadataValue(plugin, 0));
			p.sendMessage(messageManager.getMessage("muteToggleOnMessage"));
		}
		return true;
	}
	
}
