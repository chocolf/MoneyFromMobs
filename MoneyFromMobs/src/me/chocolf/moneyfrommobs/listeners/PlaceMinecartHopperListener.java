package me.chocolf.moneyfrommobs.listeners;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlaceMinecartHopperListener implements Listener {
    private final MoneyFromMobs plugin;

    public PlaceMinecartHopperListener(MoneyFromMobs plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    private void onHopperMinecartPlaced(EntityPlaceEvent e){
        Entity hopperMinecart = e.getEntity();

        if (hopperMinecart instanceof HopperMinecart){
            PersistentDataContainer dataContainer = hopperMinecart.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "MfmHopperOwner");
            dataContainer.set(key, PersistentDataType.STRING, e.getPlayer().getUniqueId().toString());
        }
    }
}
