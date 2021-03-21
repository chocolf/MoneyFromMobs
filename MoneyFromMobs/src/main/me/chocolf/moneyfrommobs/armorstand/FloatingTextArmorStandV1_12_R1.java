package me.chocolf.moneyfrommobs.armorstand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

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
		Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("MoneyFromMobs"), new Runnable() {
		    @Override
		    public void run() {
		    	armorstand.killEntity();
		    }
		}, 20L);
	}
}
