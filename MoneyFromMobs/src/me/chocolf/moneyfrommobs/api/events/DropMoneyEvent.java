package me.chocolf.moneyfrommobs.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DropMoneyEvent extends Event implements Cancellable{
	
	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private ItemStack itemToDrop;
	private Double amount;
	private Location location;
	private final Player killer;
	private final Entity entity;
	private int numberOfDrops;
	
	public DropMoneyEvent(ItemStack itemToDrop, Double amount, Location location, Player killer, Entity entity, int numberOfDrops){
		this.killer = killer;
		this.entity = entity;
		this.setItemToDrop(itemToDrop);
		this.setAmount(amount);
		this.setLocation(location);
		this.setNumberOfDrops(numberOfDrops);
	}
	
	@Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
	
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
		
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Player getKiller() {
		return killer;
	}


	public Entity getEntity() {
		return entity;
	}

	public int getNumberOfDrops() {
		return numberOfDrops;
	}

	public void setNumberOfDrops(int numberOfDrops) {
		this.numberOfDrops = numberOfDrops;
	}

	public ItemStack getItemToDrop() {
		return itemToDrop;
	}

	public void setItemToDrop(ItemStack itemToDrop) {
		this.itemToDrop = itemToDrop;
	}

}
