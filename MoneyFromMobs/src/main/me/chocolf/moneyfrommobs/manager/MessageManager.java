package me.chocolf.moneyfrommobs.manager;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStand_1_12_R1;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStand_1_16_R2;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStand_1_16_R3;
import me.chocolf.moneyfrommobs.util.VersionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageManager {
	
	private MoneyFromMobs plugin;
	private boolean shouldSendChatMessage;
	private boolean shouldSendActionBarMessage;
	private boolean shouldSendFloatingTextMessage;
	private HashMap<String, String> messagesMap = new HashMap<>();
	private static final Pattern pattern = Pattern.compile("#([A-Fa-f0-9]){6}");
	
	public MessageManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		loadMessage();
	}

	public void loadMessage() {
		FileConfiguration config = plugin.getConfig();
		
		shouldSendChatMessage = config.getBoolean("ShowMessageInChat.Enabled");
		shouldSendActionBarMessage = config.getBoolean("ShowMessageInActionBar.Enabled");
		shouldSendFloatingTextMessage = config.getBoolean("ShowMessageAsFloatingText.Enabled");
		
		messagesMap.clear();
		messagesMap.put("chatMessage", applyColour( config.getString("ShowMessageInChat.Message") ));
		messagesMap.put("actionBarMessage", applyColour( config.getString("ShowMessageInActionBar.Message") ));
		messagesMap.put("floatingTextMessage", applyColour( config.getString("ShowMessageAsFloatingText.Message") ));
		messagesMap.put("playerMessage", applyColour( config.getString("PLAYER.Message") ));
		
		messagesMap.put("muteToggleOnMessage", applyColour( config.getString("MuteToggleOnMessage") ));
		messagesMap.put("muteToggleOffMessage", applyColour( config.getString("MuteToggleOffMessage") ));
	}
	
	public void sendMessage(String strAmount, Player p) {
		String messageToSend;
		if ( p.hasMetadata("MfmMuteMessages")) 
			return;
		
		double balance = plugin.getEcon().getBalance(p);
		if (shouldSendChatMessage) {
			messageToSend = getMessage("chatMessage", balance, strAmount);
			p.sendMessage(messageToSend);
		}
		
		if (shouldSendActionBarMessage) {
			messageToSend = getMessage("actionBarMessage", balance, strAmount);
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(messageToSend));
		}
		
		if (shouldSendFloatingTextMessage) {
			messageToSend = getMessage("floatingTextMessage", balance, strAmount);
			sendFloatingTextMessage(messageToSend, p.getLocation(), p);
		}
	}
	
	public void sendPlayerMessage(double amount, Player p) {
		String strAmount = String.format("%.2f", amount);
		double balance = plugin.getEcon().getBalance(p);
		String messageToSend = getMessage("playerMessage", balance, strAmount);
		p.sendMessage(messageToSend);
	}

	private void sendFloatingTextMessage(String messageToSend, Location loc, Player p) {
		Vector directionVector = loc.getDirection();
		directionVector.setY(0.6);
		loc.add(directionVector.multiply(4));
		
		switch (VersionUtils.getNMSVersion()) {
		case "v1_16_R3":
			new FloatingTextArmorStand_1_16_R3(loc, messageToSend, p);
			break;
		case "v1_16_R2":
			new FloatingTextArmorStand_1_16_R2(loc, messageToSend, p);
			break;
		case "v1_12_R1":
			new FloatingTextArmorStand_1_12_R1(loc, messageToSend, p);
			break;
		default:
			plugin.getLogger().warning("Floating Text Messages are not compatible with your version. Versions Supported: 1.12.2 and 1.16.2-1.16.5. Please disable Floating Text Messages in your config to avoid this error message!");
		}
	}
	
	
	public static String applyColour (String msg) {
		if ( VersionUtils.getVersionNumber() > 15) {
			Matcher match = pattern.matcher(msg);
			while (match.find()) {
				String color = msg.substring(match.start(), match.end());
				msg = msg.replace(color, ChatColor.of(color) + "");
				match = pattern.matcher(msg);
			}
		}
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public String getMessage(String messageName) {
		return messagesMap.get(messageName);
	}
	
	private String getMessage(String messageName, double balance, String strAmount) {
		return messagesMap.get(messageName).replace("%amount%", strAmount).replace("%balance%", String.format("%.2f", balance) );
	}

}
