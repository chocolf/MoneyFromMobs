package me.chocolf.moneyfrommobs.armorstand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import net.minecraft.server.v1_13_R2.ChatComponentText;
import net.minecraft.server.v1_13_R2.EntityArmorStand;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_13_R2.WorldServer;

public class FloatingTextArmorStand_1_13_R2 extends EntityArmorStand{
	
	public FloatingTextArmorStand_1_13_R2(Location loc, String messageToSend, Player p) {
		super( ((CraftWorld) loc.getWorld()).getHandle() );
		
		FloatingTextArmorStand_1_13_R2 armorstand = this;
		armorstand.setMarker(true);
		armorstand.setNoGravity(true);
		armorstand.setCustomNameVisible(true);
		armorstand.setInvisible(true);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
		armorstand.setCustomName(new ChatComponentText(messageToSend));
		armorstand.noclip = true;
		WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
		world.addEntity(armorstand);
		
		for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
			if (onlinePlayer != p) {
				PacketPlayOutEntityDestroy  packet = new PacketPlayOutEntityDestroy(armorstand.getId());
				((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packet);
			}
		}
		
		MoneyFromMobs plugin = MoneyFromMobs.getInstance();
		
		for (int i = 0; i<20; i+=1) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			    @Override
			    public void run() {
		    		loc.add(0,0.1,0);
			    	armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
			    }
			}, i);
		}
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
		    @Override
		    public void run() {
		    	armorstand.killEntity();
		    }
		}, 20L);
	}
}
