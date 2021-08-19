package me.chocolf.moneyfrommobs.integration;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class PlayerDropMoneyFlag {
	
	private static StateFlag playerDropMoney;

	public static void registerFlag() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    	Flag<?> existing = registry.get("player-drop-money");
        if (existing instanceof StateFlag) {
        	playerDropMoney = (StateFlag) existing;
        }
        else {
        	// create a flag with the name "player-drop-money", defaulting to true
	    	StateFlag flag = new StateFlag("player-drop-money", true);
	    	registry.register(flag);
	    	playerDropMoney = flag;	
	    }
	}

	public static StateFlag getPlayerDropMoneyFlag() {
		return playerDropMoney;
	}
}
