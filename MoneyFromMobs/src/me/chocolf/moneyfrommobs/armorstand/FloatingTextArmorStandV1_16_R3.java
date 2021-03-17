package me.chocolf.moneyfrommobs.armorstand;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.MobEffect;
import net.minecraft.server.v1_16_R3.MobEffects;

public class FloatingTextArmorStandV1_16_R3 extends EntityArmorStand{
	
	public FloatingTextArmorStandV1_16_R3(Location loc) {
		super(EntityTypes.ARMOR_STAND, ((CraftWorld) loc.getWorld()).getHandle() );
		
		this.addEffect(new MobEffect(MobEffects.LEVITATION, 100, 3, false, false));
		this.setCustomNameVisible(true);
		this.setInvisible(true);
		this.setPosition(loc.getX(), loc.getY(), loc.getZ());
		
	}
}
