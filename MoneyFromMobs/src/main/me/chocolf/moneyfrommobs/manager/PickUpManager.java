package me.chocolf.moneyfrommobs.manager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.api.event.GiveMoneyEvent;
import me.chocolf.moneyfrommobs.util.VersionUtils;

public class PickUpManager {
	
	private static final Pattern pattern = Pattern.compile("([0-9]){6}mfm");
	private MoneyFromMobs plugin;
	private boolean onlyKillerPickUpMoney;
	private ItemStack itemToDrop;
	private String itemName;
	private Particle particleEffect;
	private int numberOfParticles;
	private Sound sound;
	
	public PickUpManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		init();
	}
	
	public void init() {
		FileConfiguration config = plugin.getConfig();
		loadItem(config);
		loadParticles(config);
		loadSound(config);
		onlyKillerPickUpMoney = config.getBoolean("OnlyKillerCanPickUpMoney");
	}
	
	private void loadItem(FileConfiguration config) {
		// loads item to drop
		try {
			if ( config.getString("MoneyDropsOnGround.Item").contains("CustomHead:")) 
				itemToDrop = (getCustomHead(plugin.getConfig().getString("MoneyDropsOnGround.Item").replace("CustomHead:", "") ));
			
			else itemToDrop = (new ItemStack(Material.valueOf(plugin.getConfig().getString("MoneyDropsOnGround.Item")), 1));
		}
		catch(Exception e) {
			plugin.getLogger().warning("Make sure you have entered a valid ItemType in your config. Setting ItemType to Emerald until fixed");
			itemToDrop = (new ItemStack(Material.EMERALD, 1));
		}

		ItemMeta meta = itemToDrop.getItemMeta();
		//sets custom model data
		if (VersionUtils.getVersionNumber() > 13) {
			meta.setCustomModelData(plugin.getConfig().getInt("MoneyDropsOnGround.CustomModelData"));
			itemToDrop.setItemMeta(meta);
		}
		// Makes item glow if it is enabled
		if (config.getBoolean("MoneyDropsOnGround.Enchanted")){
			itemToDrop.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			meta = itemToDrop.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			itemToDrop.setItemMeta(meta);
		}
		
		// loads Item Name
		itemName = MessageManager.applyColour( config.getString("MoneyDropsOnGround.ItemName") );
	}
	
	private void loadParticles(FileConfiguration config) {
		if (config.getString("ParticleEffect").equalsIgnoreCase("NONE")) {
			particleEffect = null;
		}
		else {
			try {
				particleEffect = Particle.valueOf( config.getString("ParticleEffect").toUpperCase() );
				numberOfParticles = config.getInt("AmountOfParticles");
			}
			catch(Exception e) {
				plugin.getLogger().warning("Disabling particles on pickup. Make sure you have entered a valid Particle Effect in your config");
				particleEffect = null;
			}
		}
	}
	
	private void loadSound(FileConfiguration config) {
		if (config.getString("Sound").equalsIgnoreCase("NONE")) {
			sound = null;
		}
		else {
			try {
				sound = Sound.valueOf( config.getString("Sound").toUpperCase().replace(".", "_") ) ;
			}
			catch (Exception e) {
				plugin.getLogger().warning("Disabling sound on pick up. Make sure you have entered a valid Sound in your config");
				sound = null;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private ItemStack getCustomHead(String value) {
		ItemStack head;
		if (VersionUtils.getVersionNumber() > 12) {
			head = new ItemStack(Material.PLAYER_HEAD, 1);
		}
		else {
			head = new ItemStack(Material.valueOf("SKULL_ITEM"),1,(short) 3);
		}
	    SkullMeta meta = (SkullMeta) head.getItemMeta();
	    GameProfile profile = new GameProfile(UUID.randomUUID(), "");
	    profile.getProperties().put("textures", new Property("textures", value));
	    Field profileField = null;
	    try {
	        profileField = meta.getClass().getDeclaredField("profile");
	        profileField.setAccessible(true);
	        profileField.set(meta, profile);
	    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
	        e.printStackTrace();
	    }
	    head.setItemMeta(meta);
	    return head;
	}

	public ItemStack getItemToDrop() {
		return itemToDrop;
	}

	public String getItemName() {
		return itemName;
	}

	public Particle getParticleEffect() {
		return particleEffect;
	}
	
	public int getNumberOfParticles() {
		return numberOfParticles;
	}

	public Sound getSound() {
		return sound;
	}

	public boolean shouldOnlyKillerPickUpMoney() {
		return onlyKillerPickUpMoney;
	}

	// methods
	public void giveMoney(Double amount,Player p) {
		// call pickup money event
		GiveMoneyEvent giveMoneyEvent = new GiveMoneyEvent(p, amount, sound, particleEffect);
		Bukkit.getPluginManager().callEvent(giveMoneyEvent);
		if (giveMoneyEvent.isCancelled()) return;
		amount = giveMoneyEvent.getAmount();
		sound = giveMoneyEvent.getSound();
		particleEffect = giveMoneyEvent.getParticle();
		
		if (amount == 0) return;
		
		Location loc = p.getLocation();
		
		// give money
		plugin.getEcon().depositPlayer(p,amount);
		
		// play sound
		if (sound != null) {
			p.playSound(loc, sound, 1, 1);
		}
		
		// spawn particle
		if (particleEffect != null) {
	    	loc.setY(loc.getY()+3);
	    	p.getWorld().spawnParticle(this.getParticleEffect(), loc, this.getNumberOfParticles());
	    }
		
		// convert amount to string ready to place it in message
		String strAmount = String.format("%.2f", amount);
		
		// take off decimal place if amount ends in .00
		if (plugin.getConfig().getBoolean("MoneyDropsOnGround.DisableDecimal") && strAmount.contains(".00"))
			strAmount = String.format("%.0f", amount);
		
		plugin.getMessageManager().sendMessage(strAmount,p);
		
	}
		
	public boolean isMoneyPickedUp(ItemStack itemStack) {
		// checks if item picked up is money
		if (itemStack == null) return false;
		if (!itemStack.hasItemMeta()) return false;
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (!itemMeta.hasLore()) return false;
		List<String> itemLore = itemMeta.getLore();
		Matcher matcher = pattern.matcher(itemLore.get(0));
		return matcher.find();
	}
	
	
}
