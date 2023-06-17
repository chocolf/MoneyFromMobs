package me.chocolf.moneyfrommobs.listener;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.PickUpManager;
import me.chocolf.moneyfrommobs.util.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class HopperListeners implements Listener {
    private final MoneyFromMobs plugin;

    public HopperListeners(MoneyFromMobs plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onHopperPickupItem(InventoryPickupItemEvent e) {
        Item item = e.getItem();
        ItemStack itemStack = item.getItemStack();
        PickUpManager pickUpManager = plugin.getPickUpManager();
        if (pickUpManager.isMoneyPickedUp(itemStack)){
            e.setCancelled(true);
            // checks if user is using 1.14+ and config option is set to PLACER
            if (VersionUtils.getVersionNumber() > 13 && pickUpManager.getWhoHopperGivesMoneyTo().equalsIgnoreCase("PLACER")){
                String uuid = null;

                // gets owner of hopper or hopper minecart
                if (e.getInventory().getHolder() instanceof Hopper) {
                    TileState state = (TileState) e.getInventory().getLocation().getBlock().getState();
                    NamespacedKey key = new NamespacedKey(plugin, "MfmHopperOwner");
                    uuid = state.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                }
                else if(e.getInventory().getHolder() instanceof HopperMinecart){
                    Entity hopperMinecart = (Entity) e.getInventory().getHolder();
                    NamespacedKey key = new NamespacedKey(plugin, "MfmHopperOwner");
                    uuid = hopperMinecart.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                }
                // gives money to hopper owner if they are online
                if (uuid != null && Bukkit.getPlayer(UUID.fromString(uuid)) != null){
                    List<String> itemLore = itemStack.getItemMeta().getLore();
                    Player p = Bukkit.getPlayer(UUID.fromString(uuid));
                    double amount = Double.parseDouble(itemLore.get(1));
                    pickUpManager.giveMoney(amount, p);
                    item.remove();
                }
                // gives money to hopper owner if they are offline and is allowed in config
                else if (pickUpManager.doesHopperGiveMoneyToOfflinePlayer() && uuid != null && Bukkit.getOfflinePlayer(UUID.fromString(uuid)) != null ){
                    List<String> itemLore = itemStack.getItemMeta().getLore();
                    OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    double amount = Double.parseDouble(itemLore.get(1));
                    plugin.getEcon().depositPlayer(p, amount);
                    item.remove();
                }
            }
            // if config option is set to KILLER
            else if (pickUpManager.getWhoHopperGivesMoneyTo().equalsIgnoreCase("KILLER")){
                List<String> itemLore = itemStack.getItemMeta().getLore();
                if (itemLore.size() > 2 && Bukkit.getPlayer(itemLore.get(2)) != null){
                    Player p = Bukkit.getPlayer(itemLore.get(2));
                    double amount = Double.parseDouble(itemLore.get(1));
                    pickUpManager.giveMoney(amount, p);
                    item.remove();
                }
            }
        }
    }

    @EventHandler
    private void onHopperPlaced(BlockPlaceEvent e){
        Block hopper = e.getBlock();
        if (hopper.getType() != Material.HOPPER)
            return;

        if (VersionUtils.getVersionNumber() > 13) {
            TileState state = (TileState) hopper.getState();
            PersistentDataContainer dataContainer = state.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "MfmHopperOwner");
            dataContainer.set(key, PersistentDataType.STRING, e.getPlayer().getUniqueId().toString());
            state.update();
        }
    }
}
