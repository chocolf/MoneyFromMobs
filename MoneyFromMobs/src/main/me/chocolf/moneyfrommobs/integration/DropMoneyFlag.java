package me.chocolf.moneyfrommobs.integration;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class DropMoneyFlag {
	
	private static StateFlag dropMoney;

	public static void registerFlag() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    	Flag<?> existing = registry.get("drop-money");
        if (existing instanceof StateFlag) {
        	dropMoney = (StateFlag) existing;
        }
        else {
        	// create a flag with the name "MoneyDrops", defaulting to true
	    	StateFlag flag = new StateFlag("drop-money", true);
	    	registry.register(flag);
	    	dropMoney = flag;	
	    }
	}

	public static StateFlag getDropMoneyFlag() {
		return dropMoney;
	}
	    

}
