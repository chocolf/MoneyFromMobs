package me.chocolf.moneyfrommobs.armorstand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.EntityArmorStand;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.MobEffect;
import net.minecraft.server.v1_16_R2.MobEffects;
import net.minecraft.server.v1_16_R2.WorldServer;

public class FloatingTextArmorStand_1_16_R2 extends EntityArmorStand{
	
	public FloatingTextArmorStand_1_16_R2(Location loc, String messageToSend) {
		super(EntityTypes.ARMOR_STAND, ((CraftWorld) loc.getWorld()).getHandle() );
		
		FloatingTextArmorStand_1_16_R2 armorstand = this;
		armorstand.addEffect(new MobEffect(MobEffects.LEVITATION, 100, 3, false, false));
		armorstand.setCustomNameVisible(true);
		armorstand.setInvisible(true);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
		armorstand.setCustomName(new ChatComponentText(messageToSend));
		armorstand.noclip = true;
		WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
		world.addEntity(armorstand);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(MoneyFromMobs.getInstance(), new Runnable() {
		    @Override
		    public void run() {
		    	armorstand.killEntity();
		    }
		}, 20L);
	}
}
