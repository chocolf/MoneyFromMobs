package me.chocolf.moneyfrommobs.util;

import org.bukkit.Bukkit;

public class VersionUtils {
	
	private static String v = Bukkit.getServer().getClass().getPackage().getName();
	private static String nmsVersion =  v.substring(v.lastIndexOf('.') + 1);
	private static String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
	private static int versionNumber = Integer.parseInt(nmsVersion.split("_")[1]);
	
	public static String getNMSVersion() {
		return nmsVersion;
	}

	public static String getBukkitVersion() {
		return bukkitVersion;
	}
	
	public static int getVersionNumber() {
		return versionNumber;
	}
	
}
