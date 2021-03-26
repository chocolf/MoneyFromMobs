package me.chocolf.moneyfrommobs.manager;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStand_1_12_R1;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStand_1_16_R2;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStand_1_16_R3;
import me.chocolf.moneyfrommobs.util.Utils;
import me.chocolf.moneyfrommobs.util.VersionUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageManager {
	
	private MoneyFromMobs plugin;
	private boolean sendChatMessage;
	private boolean sendActionBarMessage;
	private boolean sendFloatingTextMessage;
	private String playerMessage;
	private String chatMessage;
	private String actionBarMessage;
	private String floatingTextMessage;
	
	public MessageManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		loadMessage();
	}

	public void loadMessage() {
		FileConfiguration config = plugin.getConfig();
		
		sendChatMessage = config.getBoolean("ShowMessageInChat.Enabled");
		sendActionBarMessage = config.getBoolean("ShowMessageInActionBar.Enabled");
		sendFloatingTextMessage = config.getBoolean("ShowMessageAsFloatingText.Enabled");
		
		chatMessage = Utils.applyColour( config.getString("ShowMessageInChat.Message") );
		actionBarMessage = Utils.applyColour( config.getString("ShowMessageInActionBar.Message") );
		floatingTextMessage = Utils.applyColour( config.getString("ShowMessageAsFloatingText.Message") );
		playerMessage = Utils.applyColour( config.getString("PLAYER.Message") );
	}
	
	public void sendMessage(String strAmount, Player p) {
		String messageToSend;
		if ( p.hasMetadata("MfmMuteMessages")) 
			return;
		
		double balance = plugin.getEcon().getBalance(p);
		if (sendChatMessage) {
			messageToSend = chatMessage.replace("%amount%", strAmount).replace("%balance%", String.format("%.2f", balance) );
			p.sendMessage(messageToSend);
		}
		
		if (sendActionBarMessage) {
			messageToSend = actionBarMessage.replace("%amount%", strAmount).replace("%balance%", String.format("%.2f", balance) );
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(messageToSend));
		}
		
		if (sendFloatingTextMessage) {
			messageToSend = floatingTextMessage.replace("%amount%", strAmount).replace("%balance%", String.format("%.2f", balance) );
			sendFloatingTextMessage(messageToSend, p.getLocation());
		}
	}
	
	public void sendPlayerMessage(double amount, Player p) {
		String strAmount = String.format("%.2f", amount);
		String messageToSend = playerMessage.replace("%amount%", strAmount).replace("%balance%", String.format("%.2f", plugin.getEcon().getBalance(p)) );
		p.sendMessage(messageToSend);
	}

	private void sendFloatingTextMessage(String messageToSend, Location loc) {
		Vector directionVector = loc.getDirection();
		directionVector.setY(0.1);
		loc.add(directionVector.multiply(4));
		
		switch (VersionUtils.getNMSVersion()) {
		case "v1_16_R3":
			new FloatingTextArmorStand_1_16_R3(loc, messageToSend);
			break;
		case "v1_16_R2":
			new FloatingTextArmorStand_1_16_R2(loc, messageToSend);
			break;
		case "v1_12_R1":
			new FloatingTextArmorStand_1_12_R1(loc, messageToSend);
			break;
		default:
			plugin.getLogger().warning("Floating Text Messages are not compatible with your version. Versions Supported: 1.12.2 and 1.16.2-1.16.5. Please disable Floating Text Messages in your config to avoid this error message!");
		}
	}

}
