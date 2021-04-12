package me.chocolf.moneyfrommobs.armorstand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.EntityArmorStand;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.WorldServer;

public class FloatingTextArmorStand_1_16_R2 extends EntityArmorStand{
	
	public FloatingTextArmorStand_1_16_R2(Location loc, String messageToSend) {
		super(EntityTypes.ARMOR_STAND, ((CraftWorld) loc.getWorld()).getHandle() );
		
		FloatingTextArmorStand_1_16_R2 armorstand = this;
		armorstand.setMarker(true);
		armorstand.setNoGravity(true);
		armorstand.setCustomNameVisible(true);
		armorstand.setInvisible(true);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
		armorstand.setCustomName(new ChatComponentText(messageToSend));
		armorstand.noclip = true;
		WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
		world.addEntity(armorstand);
		
		MoneyFromMobs plugin = MoneyFromMobs.getInstance();
		
		for (int i = 0; i<=20; i+=1) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
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
