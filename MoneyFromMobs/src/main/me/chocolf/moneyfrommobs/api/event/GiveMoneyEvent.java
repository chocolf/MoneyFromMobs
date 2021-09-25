package me.chocolf.moneyfrommobs.api.event;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GiveMoneyEvent extends Event implements Cancellable{
	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private final Player player;
	private Double amount;
	private Sound sound;
	private Particle particle;
	
	public GiveMoneyEvent(Player player, Double amount, Sound sound, Particle particle) {
		this.player = player;
		this.setAmount(amount);
		this.setSound(sound);
		this.setParticle(particle);
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

	public Player getPlayer() {
		return player;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Sound getSound() {
		return sound;
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}

	public Particle getParticle() {
		return particle;
	}

	public void setParticle(Particle particle) {
		this.particle = particle;
	}

}
