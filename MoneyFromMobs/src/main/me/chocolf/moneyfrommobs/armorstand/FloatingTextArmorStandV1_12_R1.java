package me.chocolf.moneyfrommobs.armorstand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.MobEffect;
import net.minecraft.server.v1_12_R1.MobEffects;
import net.minecraft.server.v1_12_R1.WorldServer;

public class FloatingTextArmorStandV1_12_R1 extends EntityArmorStand{
	
	public FloatingTextArmorStandV1_12_R1(Location loc, String messageToSend) {
		super( ((CraftWorld) loc.getWorld()).getHandle() );
		
		FloatingTextArmorStandV1_12_R1 armorstand = this;
		armorstand.addEffect(new MobEffect(MobEffects.LEVITATION, 100, 3, false, false));
		armorstand.setCustomNameVisible(true);
		armorstand.setInvisible(true);
		armorstand.setCustomName(messageToSend);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
		WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
		world.addEntity(armorstand);
		new BukkitRunnable() {
		     @Override
		     public void run() {
		    	 armorstand.killEntity();
		     }
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("MoneyFromMobs"), 20, 20);
		
	}
}
