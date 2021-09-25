package me.chocolf.moneyfrommobs.integration;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

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
    public String onPlaceholderRequest(Player p, String identifier){

        if(p == null) return "";
        
        // %moneyfrommobs_latest_picked_up%
        if(identifier.equals("latest_picked_up")){
        	Map<UUID, Double> latestPickedUpList = plugin.getPlaceholdersListener().getLatestPickedUp();
        	if (latestPickedUpList.containsKey(p.getUniqueId())) 
        		return String.format("%.2f",latestPickedUpList.get(p.getUniqueId()));
        	else 
        		return "0.00";
        }
        
        // %moneyfrommobs_current_event_multiplier%
        else if (identifier.equals("current_event_multiplier")) {
        	return plugin.getMultipliersManager().getEventMultiplier()*100+"%";
        }
        

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
    public String getIdentifier(){
        return "moneyfrommobs";
    }


    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    
}