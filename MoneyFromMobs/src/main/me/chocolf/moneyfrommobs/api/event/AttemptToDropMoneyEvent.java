package me.chocolf.moneyfrommobs.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AttemptToDropMoneyEvent extends Event implements Cancellable{
	
	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private double dropChance;
	private final Entity entity;
	private final Player killer;
	
	public AttemptToDropMoneyEvent(double dropChance, Entity entity, Player killer) {
		this.setDropChance(dropChance);
		this.entity = entity;
		this.killer = killer;
	}

	@Override
    public HandlerList getHandlers() {
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

	public double getDropChance() {
		return dropChance;
	}

	public void setDropChance(double dropChance) {
		this.dropChance = dropChance;
	}

	public Entity getEntity() {
		return entity;
	}

	public Player getKiller() {
		return killer;
	}


}
