package me.chocolf.moneyfrommobs.integration;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class WorldGuardFlags {

    private static StateFlag dropMoney;
    private static StateFlag playerDropMoney;
    private static StateFlag spawnerMobDropMoney;

    public static void registerFlags() {
        registerDropMoneyFlag();
        registerPlayerDropMoneyFlag();
        registerSpawnerMobDropMoneyFlag();
    }

    private static void registerDropMoneyFlag(){
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        Flag<?> existing = registry.get("drop-money");
        if (existing instanceof StateFlag) {
            dropMoney = (StateFlag) existing;
        }
        else {
            // create a flag with the name "drop-money", defaulting to true
            StateFlag flag = new StateFlag("drop-money", true);
            registry.register(flag);
            dropMoney = flag;
        }
    }

    private static void registerPlayerDropMoneyFlag(){
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

    private static void registerSpawnerMobDropMoneyFlag(){
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        Flag<?> existing = registry.get("spawner-mob-drop-money");
        if (existing instanceof StateFlag) {
            spawnerMobDropMoney = (StateFlag) existing;
        }
        else {
            // create a flag with the name "player-drop-money", defaulting to true
            StateFlag flag = new StateFlag("spawner-mob-drop-money", true);
            registry.register(flag);
            spawnerMobDropMoney = flag;
        }
    }

    public static StateFlag getDropMoneyFlag() {
        return dropMoney;
    }
    public static StateFlag getPlayerDropMoneyFlag() {
        return playerDropMoney;
    }
    public static StateFlag getSpawnerMobDropMoneyFlag() {
        return spawnerMobDropMoney;
    }
}
