package me.chocolf.moneyfrommobs.managers;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.utils.VersionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageManager {
	
	private final MoneyFromMobs plugin;
	private double floatingTextHeight;
	private boolean shouldSendChatMessage;
	private boolean shouldSendActionBarMessage;
	private boolean shouldSendFloatingTextMessage;
	private boolean sendEventMessageAsTitle;
	private boolean moveFloatingTextMessageUpwards;
	private double floatingTextDuration;
	private final HashMap<String, String> messagesMap = new HashMap<>();
	private static final Pattern hexColorPattern = Pattern.compile("#([A-Fa-f0-9]){6}");
	
	public MessageManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		loadMessage();
	}

	public void loadMessage() {
		FileConfiguration config = plugin.getConfig();
		
		floatingTextHeight = config.getDouble("ShowMessageAsFloatingText.Height")/4;
		
		shouldSendChatMessage = config.getBoolean("ShowMessageInChat.Enabled");
		shouldSendActionBarMessage = config.getBoolean("ShowMessageInActionBar.Enabled");
		shouldSendFloatingTextMessage = config.getBoolean("ShowMessageAsFloatingText.Enabled");
		sendEventMessageAsTitle = config.getBoolean("SendEventMessageAsTitle");
		moveFloatingTextMessageUpwards = config.getBoolean("ShowMessageAsFloatingText.Movement");
		floatingTextDuration = config.getDouble("ShowMessageAsFloatingText.Duration") * 20;
		
		messagesMap.clear();
		messagesMap.put("chatMessage", applyColour( config.getString("ShowMessageInChat.Message") ));
		messagesMap.put("actionBarMessage", applyColour( config.getString("ShowMessageInActionBar.Message") ));
		messagesMap.put("floatingTextMessage", applyColour( config.getString("ShowMessageAsFloatingText.Message") ));
		messagesMap.put("playerMessage", applyColour( config.getString("PLAYER.Message") ));
		
		messagesMap.put("muteToggleOnMessage", applyColour( config.getString("MuteToggleOnMessage") ));
		messagesMap.put("muteToggleOffMessage", applyColour( config.getString("MuteToggleOffMessage") ));
		
		messagesMap.put("clearMoneyDropsMessage", applyColour(config.getString("ClearMoneyDropsMessage") ));
		messagesMap.put("reloadMessage", applyColour(config.getString("ReloadMessage") ));
		
		messagesMap.put("maxDropsReachedMessage", applyColour(config.getString("MaxDropsReachedMessage") ));
		
		messagesMap.put("eventAlreadyRunningMessage", applyColour(config.getString("EventAlreadyRunningMessage") ));
		messagesMap.put("noEventRunningMessage", applyColour(config.getString("NoEventRunningMessage") ));
		messagesMap.put("eventStart", applyColour(config.getString("EventStart") ));
		messagesMap.put("eventFinish", applyColour(config.getString("EventFinish") ));
	}
	
	public void sendMessage(String strAmount, Player p) {
		if ( p.hasMetadata("MfmMuteMessages"))
			return;
		
		double balance = plugin.getEcon().getBalance(p);
		if (shouldSendChatMessage) {
			p.sendMessage(getMessage("chatMessage", balance, strAmount));
		}
		
		if (shouldSendActionBarMessage) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(getMessage("actionBarMessage", balance, strAmount)));
		}
		
		if (shouldSendFloatingTextMessage) {
			sendFloatingTextMessage(getMessage("floatingTextMessage", balance, strAmount), p.getLocation());
		}
	}
	
	public void sendPlayerMessage(double amount, Player p) {
		String strAmount = String.format("%.2f", amount);
		double balance = plugin.getEcon().getBalance(p);
		String messageToSend = getMessage("playerMessage", balance, strAmount);
		p.sendMessage(messageToSend);
	}

	private void sendFloatingTextMessage(String messageToSend, Location loc) {
		Vector directionVector = loc.getDirection();
		directionVector.setY(floatingTextHeight);
		loc.add(directionVector.multiply(4));

		ArmorStand armorstand = loc.getWorld().spawn(loc, ArmorStand.class, armorStand ->{
			armorStand.setVisible(false);
			armorStand.setMarker(true);
			armorStand.setGravity(false);
			armorStand.setCustomName(applyColour(messageToSend));
			armorStand.setCustomNameVisible(true);
			armorStand.setMetadata("mfmas", new FixedMetadataValue(this.plugin, "mfmas"));
		});

		if (moveFloatingTextMessageUpwards) {
			for (int i = 0; i < floatingTextDuration; i += 1) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> armorstand.teleport(armorstand.getLocation().add(0, 0.1,0)), i);
			}
		}

		Bukkit.getScheduler().runTaskLater(plugin, armorstand::remove, (long) floatingTextDuration);
	}
	
	
	public static String applyColour (String msg) {
		if ( VersionUtils.getVersionNumber() > 15) {
			Matcher match = hexColorPattern.matcher(msg);
			while (match.find()) {
				String color = msg.substring(match.start(), match.end());
				msg = msg.replace(color, ChatColor.of(color) + "");
				match = hexColorPattern.matcher(msg);
			}
		}
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public void logToConsole (String msg){
		plugin.getServer().getConsoleSender().sendMessage(MessageManager.applyColour(msg));
	}
	
	public String getMessage(String messageName) {
		return messagesMap.get(messageName);
	}
	
	private String getMessage(String messageName, double balance, String strAmount) {
		return messagesMap.get(messageName).replace("%amount%", strAmount).replace("%balance%", String.format("%.2f", balance)).replace("%balance_0dp%", String.format("%.0f", balance) );
	}

	public boolean shouldSendEventMessageAsTitle() {
		return sendEventMessageAsTitle;
	}
}
