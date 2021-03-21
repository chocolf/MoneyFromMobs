package me.chocolf.moneyfrommobs.armorstand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.MobEffect;
import net.minecraft.server.v1_16_R3.MobEffects;
import net.minecraft.server.v1_16_R3.WorldServer;

public class FloatingTextArmorStandV1_16_R3 extends EntityArmorStand{
	
	public FloatingTextArmorStandV1_16_R3(Location loc, String messageToSend) {
		super(EntityTypes.ARMOR_STAND, ((CraftWorld) loc.getWorld()).getHandle() );
		
		FloatingTextArmorStandV1_16_R3 armorstand = this;
		armorstand.addEffect(new MobEffect(MobEffects.LEVITATION, 100, 3, false, false));
		armorstand.setCustomNameVisible(true);
		armorstand.setInvisible(true);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
		armorstand.setCustomName(new ChatComponentText(messageToSend));
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
