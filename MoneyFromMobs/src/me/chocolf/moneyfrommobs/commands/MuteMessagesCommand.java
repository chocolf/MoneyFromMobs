package me.chocolf.moneyfrommobs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.utils.Utils;

public class MuteMessagesCommand implements CommandExecutor{
	
	private MoneyFromMobs plugin;
	
	
	public MuteMessagesCommand(MoneyFromMobs plugin) {
		this.plugin = plugin;
		plugin.getCommand("mfmmute").setExecutor(this);		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("mfmmute")) {
			if (sender.hasPermission("MoneyFromMobs.mute")) {
				Player p = (Player) sender;
				if (p.hasMetadata("MfmMuteMessages")) {
					p.removeMetadata("MfmMuteMessages", plugin);
					p.sendMessage(Utils.applyColour(plugin.getConfig().getString("MuteToggleOffMessage")));
				}else {
					p.setMetadata("MfmMuteMessages", new FixedMetadataValue(plugin, 0));
					p.sendMessage(Utils.applyColour(plugin.getConfig().getString("MuteToggleOnMessage")));
				}
			}else {
				sender.sendMessage(Utils.applyColour("&cYou don't have permission to use this command!"));
				return true;
			}
		}
		return true;
	}
	
}
