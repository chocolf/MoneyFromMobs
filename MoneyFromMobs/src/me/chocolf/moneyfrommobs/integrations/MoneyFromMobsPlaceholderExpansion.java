package me.chocolf.moneyfrommobs.integrations;

import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

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

            // %moneyfrommobs_current_event_time_left_seconds%
            case "current_event_time_left_seconds" -> {
                var timeLeft = plugin.getMultipliersManager().getTimeLeftOfCurrentMultiplierEvent();

                if (timeLeft != 0)
                    return String.valueOf(timeLeft);
                else
                    return "0";
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
    public @NotNull String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }


    @Override
    public @NotNull String getIdentifier(){
        return "moneyfrommobs";
    }


    @Override
    public @NotNull String getVersion(){
        return plugin.getDescription().getVersion();
    }

    
}