package me.chocolf.moneyfrommobs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.chocolf.moneyfrommobs.armorstand.FloatingTextArmorStandV1_16_R3;
import me.chocolf.moneyfrommobs.events.GiveMoneyEvent;
import me.chocolf.moneyfrommobs.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.WorldServer;

public class MfmManager {
	
	private MoneyFromMobs plugin;	
	private ItemStack itemToDrop;
	private String itemName;
	private String message;
	private Particle particleEffect;
	private int numberOfParticles;
	private Sound sound;
	private double lootingMultiplier;
	private boolean permissionMultiplierEnabled;
	private HashSet<String> disabledWorlds = new HashSet<>();
	private boolean canDropIfNatural;
	private boolean canDropIfSpawner;
	private boolean canDropIfSpawnEgg;
	private boolean canDropIfSplitSlimes;
	
	
	public MfmManager(MoneyFromMobs plugin) {
		this.plugin = plugin;
		loadItem();
		loadMessage();
		loadParticlesAndSound();
		loadMultipliers();
		loadCanDropBooleans();
		loadDisabledWorlds();
	}
	
	public void loadCanDropBooleans() {
	    FileConfiguration config = this.plugin.getConfig();
	    this.canDropIfNatural = config.getBoolean("MoneyDropsFromNaturalMobs");
	    this.canDropIfSpawner = config.getBoolean("MoneyDropsFromSpawnerMobs");
	    this.canDropIfSpawnEgg = config.getBoolean("MoneyDropsFromSpawnEggMobs");
	    this.canDropIfSplitSlimes = config.getBoolean("MoneyDropsFromSplitSlimes");
	}
	  
	public void loadDisabledWorlds() {
		this.disabledWorlds.clear();
		@SuppressWarnings("unchecked")
		List<String> disabledWorldsInConfig = (List<String>) this.plugin.getConfig().getList("DisabledWorlds");
		for (String world : disabledWorldsInConfig)
		  this.disabledWorlds.add(world); 
	}
	
	public void loadItem() {
		// loads item to drop
		FileConfiguration config = plugin.getConfig();
		if ( config.getString("MoneyDropsOnGround.Item").contains("CustomHead:")) {
			setItemToDrop(getCustomHead(plugin.getConfig().getString("MoneyDropsOnGround.Item").replace("CustomHead:", "") ));
		}else {
			Material itemType = Material.valueOf(plugin.getConfig().getString("MoneyDropsOnGround.Item"));
			setItemToDrop(new ItemStack(itemType, 1));
		}
		
		ItemMeta meta = itemToDrop.getItemMeta();
		//sets custom model data
		if (!Bukkit.getVersion().contains("1.12") || !Bukkit.getVersion().contains("1.13")) {
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
		setItemName(Utils.applyColour(config.getString("MoneyDropsOnGround.ItemName")));
		
	}
	
	public void loadMessage() {
		FileConfiguration config = plugin.getConfig();
		setMessage(Utils.applyColour( config.getString("Message") ));
		
	}
	
	public void loadMultipliers() {
		FileConfiguration config = plugin.getConfig();
		setLootingMultiplier(config.getDouble("MoneyAddedPerLevel"));
		setPermissionMultiplierEnabled(config.getBoolean("PermissionMultipliersEnabled"));
	}
	
	public void loadParticlesAndSound() {
		FileConfiguration config = plugin.getConfig();
		if (config.getString("ParticleEffect").equalsIgnoreCase("NONE")) {
			setParticleEffect(null);
		}else {
			try {
				setParticleEffect( Particle.valueOf( config.getString("ParticleEffect").toUpperCase() ) );
				setNumberOfParticles(config.getInt("AmountOfParticles"));
			}
			catch(Exception e) {
				Bukkit.getLogger().warning("[MoneyFromMobs] Disabling particles on pickup. Make sure you have typed a valid Particle Effect in your config");
				setParticleEffect(null);
			}
		}
		
		if (config.getString("Sound").equalsIgnoreCase("NONE")) {
			setSound(null);
		}else {
			try {
				setSound( Sound.valueOf( config.getString("Sound").toUpperCase().replace(".", "_") ) );
			}
			catch (Exception e) {
				Bukkit.getLogger().warning("[MoneyFromMobs] Disabling sound on pick up. Make sure you have typed a valid Sound in your config");
				setSound(null);
			}
		}
		
		
	}

	private ItemStack getCustomHead(String value) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
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

	public void setItemToDrop(ItemStack itemToDrop) {
		this.itemToDrop = itemToDrop;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Particle getParticleEffect() {
		return particleEffect;
	}

	public void setParticleEffect(Particle particleEffect) {
		this.particleEffect = particleEffect;
	}
	
	public int getNumberOfParticles() {
		return numberOfParticles;
	}

	public void setNumberOfParticles(int numberOfParticles) {
		this.numberOfParticles = numberOfParticles;
	}

	public Sound getSound() {
		return sound;
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}

	public double getLootingMultiplier() {
		return lootingMultiplier;
	}

	public void setLootingMultiplier(double lootingMultiplier) {
		this.lootingMultiplier = lootingMultiplier;
	}

	public Set<String> getDisabledWorlds() {
		return disabledWorlds;
	}

	public boolean isPermissionMultiplierEnabled() {
		return permissionMultiplierEnabled;
	}

	public void setPermissionMultiplierEnabled(boolean permissionMultiplierEnabled) {
		this.permissionMultiplierEnabled = permissionMultiplierEnabled;
	}	
	
	// methods
	
	public boolean canDrop(String spawnReason) {
		switch (spawnReason) {
		case "NATURAL":
			return canDropIfNatural;
		case "SPAWNER":
			return canDropIfSpawner;
		case "SPAWNER_EGG":
			return canDropIfSpawnEgg;
		case "SLIME_SPLIT":
			return canDropIfSplitSlimes;
		default:
			return true;
		}
	}

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
			p.playSound(loc, sound, 1 , 1);
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
		
		sendMessage(strAmount,p);
		
	}
	private void sendMessage(String strAmount, Player p) {
		// send message
		if (this.getMessage().length() != 0) {
			String messageToSend = this.getMessage().replace("%amount%" ,strAmount);
			FileConfiguration config = plugin.getConfig();
			Location loc = p.getLocation();
			if ( p.hasMetadata("MfmMuteMessages")) {
				return;
			}
			if(config.getBoolean("ShowMessageInChat")) {
				p.sendMessage(messageToSend);
			}
			if (config.getBoolean("ShowMessageInActionBar")) {
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(messageToSend));
			}
			if(config.getBoolean("ShowMessageInTitle")) {
				p.sendTitle("", messageToSend, 1, 10, 1);
			}
			if(config.getBoolean("ShowMessageAsFloatingText")) {
				sendFloatingTextMessage(messageToSend, loc);
			}
		}
		
	}

	private void sendFloatingTextMessage(String messageToSend, Location loc) {
		loc.setY(loc.getY()-1);
		double rotation = loc.getYaw() - 180;
		if (rotation < 0) rotation += 360.0;
		
		if (0 <= rotation && rotation < 22.5) {
			// north
            loc.setZ(loc.getZ()-2);
        } else if (22.5 <= rotation && rotation < 67.5) {
        	// north east
        	loc.setZ(loc.getZ()-2.5);
        	loc.setX(loc.getX()+2.5);
        } else if (67.5 <= rotation && rotation < 112.5) {
            // east
        	loc.setX(loc.getX()+2.5);
        } else if (112.5 <= rotation && rotation < 157.5) {
            // south east
        	loc.setZ(loc.getZ()+2.5);
        	loc.setX(loc.getX()+2.5);
        } else if (157.5 <= rotation && rotation < 202.5) {
            // south
        	loc.setZ(loc.getZ()+2.5);
        } else if (202.5 <= rotation && rotation < 247.5) {
            // south west
        	loc.setZ(loc.getZ()+2.5);
        	loc.setX(loc.getX()-2.5);
        } else if (247.5 <= rotation && rotation < 292.5) {
            // west
        	loc.setX(loc.getX()-2.5);
        } else if (292.5 <= rotation && rotation < 337.5) {
            // north west
        	loc.setZ(loc.getZ()-2.5);
        	loc.setX(loc.getX()-2.5);
        } else if (337.5 <= rotation && rotation < 360.0) {
            // north
        	loc.setZ(loc.getZ()-2.5);
        }
		
		FloatingTextArmorStandV1_16_R3 armorstand = new FloatingTextArmorStandV1_16_R3(loc);
		armorstand.setCustomName(new ChatComponentText(messageToSend));
		WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
		world.addEntity(armorstand);
		
		new BukkitRunnable() {
		     @Override
		     public void run() {
		          armorstand.setHealth(0);
		     }
		}.runTaskTimer(plugin, 5, 5);
	}

	public void dropItem(ItemStack item, Double amount, Location location, int numberOfDrops) {
		if (amount == 0) return;
		amount = amount/numberOfDrops;
		for ( int i=0; i<numberOfDrops;i++ ) {
			ItemMeta meta = item.getItemMeta();
			List<String> lore = new ArrayList<>();
			lore.add(String.valueOf(Utils.intRandomNumber(1000000,9999999) + "mfm"));
						
			// adds lore so when picked up plugin knows how much money to give
			lore.add(String.valueOf(amount));
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			Item itemDropped = location.getWorld().dropItemNaturally(location, item );
			String strAmount = String.format("%.2f", amount);
			
			// removes decimal place
			if (plugin.getConfig().getBoolean("MoneyDropsOnGround.DisableDecimal") && strAmount.contains(".00"))
				strAmount = String.format("%.0f", amount);
			
			
			itemDropped.setCustomName(this.getItemName().replace("%amount%", strAmount));
			itemDropped.setCustomNameVisible(true);
		}
	}
	
	private static final Pattern pattern = Pattern.compile("([0-9]){6}mfm");
	
	public boolean checkIfMoney(ItemStack itemStack) {
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
