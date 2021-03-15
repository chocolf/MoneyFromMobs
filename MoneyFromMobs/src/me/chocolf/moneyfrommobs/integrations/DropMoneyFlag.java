package me.chocolf.moneyfrommobs.integrations;

import org.bukkit.Bukkit;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class DropMoneyFlag {
	
	public static StateFlag MONEY_DROPS;
	
	public DropMoneyFlag() {
		try {
			registerFlag();
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[MoneyFromMobs] Unable to load custom world guard flag. Make sure you have the latest version of WorldGuard and WorldEdit installed.");
		}
		
	}

	private void registerFlag() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
	    try {
	    	Flag<?> existing = registry.get("drop-money");
	        if (existing instanceof StateFlag) {
	        	MONEY_DROPS = (StateFlag) existing;
	        }
	        else {
	        	// create a flag with the name "MoneyDrops", defaulting to true
		    	StateFlag flag = new StateFlag("drop-money", true);
		    	registry.register(flag);
		    	MONEY_DROPS = flag;	
		    }
	    }catch (FlagConflictException e) {
	        Flag<?> existing = registry.get("drop-money");
	        if (existing instanceof StateFlag) {
	        	MONEY_DROPS = (StateFlag) existing;
	        }else {
		    	
		    }
		}
		
	}
	    

}
