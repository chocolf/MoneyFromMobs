package me.chocolf.moneyfrommobs.utils;

import org.bukkit.Bukkit;

import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class VersionUtils {

	private static final String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
	private static final int versionNumber = parseMinorVersion();

	private static int parseMinorVersion() {
		try {
			String raw = Bukkit.getServer().getBukkitVersion().split("\\.")[1];
			// Strip any non-digit characters (e.g. from "21-R0.1")
			String digits = raw.replaceAll("[^0-9]", "");
			return Integer.parseInt(digits);
		} catch (Exception e) {
			return 0;
		}
	}


	public static String getBukkitVersion() {
		return bukkitVersion;
	}
	
	public static int getVersionNumber() {
		return versionNumber;
	}

	public static String getPluginVersion() {
		return MoneyFromMobs.getInstance().getDescription().getVersion();
	}
	
}
