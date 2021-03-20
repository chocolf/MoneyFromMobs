package me.chocolf.moneyfrommobs.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStandV1_12_R1;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStandV1_16_R2;
import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStandV1_16_R3;
import me.chocolf.moneyfrommobs.util.Utils;
import me.chocolf.moneyfrommobs.util.VersionUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageManager {
	
	private MoneyFromMobs plugin;
	private boolean sendChatMessage;
	private boolean sendActionBarMessage;
	private boolean sendFloatingTextMessage;
	private String message;
	
	public MessageManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		loadMessage();
	}

	public void loadMessage() {
		FileConfiguration config = plugin.getConfig();
		sendChatMessage = config.getBoolean("ShowMessageInChat");
		sendActionBarMessage = config.getBoolean("ShowMessageInActionBar");
		sendFloatingTextMessage = config.getBoolean("ShowMessageAsFloatingText");
		message = Utils.applyColour( config.getString("Message") );
	}
	
	public void sendMessage(String strAmount, Player p) {
		String messageToSend = message.replace("%amount%", strAmount);
		
		if ( p.hasMetadata("MfmMuteMessages")) {
			return;
		}
		
		if (sendChatMessage) {
			p.sendMessage(messageToSend);
		}
		if (sendActionBarMessage) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(messageToSend));
		}
		if (sendFloatingTextMessage) {
			sendFloatingTextMessage(messageToSend, p.getLocation());
		}
		
	}

	private void sendFloatingTextMessage(String messageToSend, Location loc) {
		double rotation = (loc.getYaw() + 180) % 360.0F;
		if (rotation < 0.0D) rotation += 360.0D;
		
		if ((0D <= rotation && rotation < 22.5D) || (337.5D <= rotation && rotation < 360.0D)) {
			// north
            loc.setZ(loc.getZ()-2.5);
        } else if (22.5D <= rotation && rotation < 67.5D) {
        	// north east
        	loc.setZ(loc.getZ()-2.5);
        	loc.setX(loc.getX()+2.5);
        } else if (67.5D <= rotation && rotation < 112.5D) {
            // east
        	loc.setX(loc.getX()+2.5);
        } else if (112.5D <= rotation && rotation < 157.5D) {
            // south east
        	loc.setZ(loc.getZ()+2.5);
        	loc.setX(loc.getX()+2.5);
        } else if (157.5D <= rotation && rotation < 202.5D) {
            // south
        	loc.setZ(loc.getZ()+2.5);
        } else if (202.5D <= rotation && rotation < 247.5D) {
            // south west
        	loc.setZ(loc.getZ()+2.5);
        	loc.setX(loc.getX()-2.5);
        } else if (247.5D <= rotation && rotation < 292.5D) {
            // west
        	loc.setX(loc.getX()-2.5);
        } else if (292.5D <= rotation && rotation < 337.5D) {
            // north west
        	loc.setZ(loc.getZ()-2.5);
        	loc.setX(loc.getX()-2.5);
        }
		
		switch (VersionUtils.getNMSVersion()) {
		case "v1_16_R3":
			new FloatingTextArmorStandV1_16_R3(loc, messageToSend);
			break;
		case "v1_16_R2":
			FloatingTextArmorStandV1_16_R2 armorstandV1_16_R2 = new FloatingTextArmorStandV1_16_R2(loc, messageToSend);
			new BukkitRunnable() {
			     @Override
			     public void run() {
			          armorstandV1_16_R2.killEntity();
			     }
			}.runTaskTimer(plugin, 20, 20);
			break;
		case "v1_12_R1":
			FloatingTextArmorStandV1_12_R1 armorstandV1_12_R1 = new FloatingTextArmorStandV1_12_R1(loc, messageToSend);
			new BukkitRunnable() {
			     @Override
			     public void run() {
			    	 armorstandV1_12_R1.killEntity();
			     }
			}.runTaskTimer(plugin, 20, 20);
			break;
		default:
			Bukkit.getLogger().warning("[MoneyFromMobs] Floating Text Messages are not compatible with your version. Versions Supported: 1.12.2 and 1.16.2-1.16.5. Please disable this in your config to avoid this error message!");
		}
	}

}
