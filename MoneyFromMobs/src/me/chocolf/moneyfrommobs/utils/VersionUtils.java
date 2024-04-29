package me.chocolf.moneyfrommobs.utils;

import org.bukkit.Bukkit;

import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class VersionUtils {

	private static final String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
	private static final int versionNumber = Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("\\.")[1]);


	public static String getBukkitVersion() {
		return bukkitVersion;
	}
	
	public static int getVersionNumber() {
		return versionNumber;
	}

	public static double getPluginVersion() {
		return Double.parseDouble(MoneyFromMobs.getInstance().getDescription().getVersion());
	}
	
}
