package me.chocolf.moneyfrommobs.integration;

import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

/**
 * This class will be registered through the register-method in the 
 * plugins onEnable-method.
 */
public class MoneyFromMobsPlaceholderExpansion extends PlaceholderExpansion {

    private final MoneyFromMobs plugin;

    public MoneyFromMobsPlaceholderExpansion(MoneyFromMobs plugin){
        this.plugin = plugin;
    }
    
    @Override
    public String onRequest(OfflinePlayer p, @NotNull String params){

        if(p == null) return "";

        switch (params.toLowerCase()) {
            // %moneyfrommobs_latest_picked_up%
            case "latest_picked_up" -> {
                Map<UUID, Double> latestPickedUpList = plugin.getPlaceholdersListener().getLatestPickedUp();
                if (latestPickedUpList.containsKey(p.getUniqueId()))
                    return String.format("%.2f", latestPickedUpList.get(p.getUniqueId()));
                else
                    return "0.00";
            }

            // %moneyfrommobs_current_event_multiplier%
            case "current_event_multiplier" -> {
                return plugin.getMultipliersManager().getEventMultiplier() * 100 + "%";
            }

            // %moneyfrommobs_current_event_multiplier%
            case "current_even_time_left_seconds" -> {
                return plugin.getMultipliersManager().getCurrentMultiplierEvent().toString();
            }
        }

//        // %moneyfrommobs_chat_message%
//        else if (identifier.equals("chat_message"))
//            return plugin.getMessageManager().getMessage("chatMessage");
//
//        // %moneyfrommobs_actionbar_message%
//        else if (identifier.equals("actionbar_message"))
//            return plugin.getMessageManager().getMessage("actionBarMessage");
//
//        // %moneyfrommobs_floatingtext_message%
//        else if (identifier.equals("floatingtext_message"))
//            return plugin.getMessageManager().getMessage("actionBarMessage");



//        // %someplugin_placeholder2%
//      if(identifier.equals("placeholder2")){
//            return plugin.getConfig().getString("placeholder2", "value doesn't exist");
//        }
 
        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%) 
        // was provided
        return null;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }


    @Override
    public @NotNull String getIdentifier(){
        return "moneyfrommobs";
    }


    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    
}