package me.chocolf.moneyfrommobs.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import me.chocolf.moneyfrommobs.MoneyFromMobs;

public class UpdateChecker {
	
	public static boolean checkForUpdate() {
		MoneyFromMobs plugin = MoneyFromMobs.getInstance();
		if (!plugin.getConfig().getBoolean("UpdateNotification"))
			return false;
		
		String currentVersion = VersionUtils.getPluginVersion();
		String latestVersion = getLatestVersion();
		if (latestVersion == null) return false;
		
		return compareVersions(currentVersion, latestVersion) < 0;
	}

	private static int compareVersions(String v1, String v2) {
		String[] parts1 = v1.split("\\.");
		String[] parts2 = v2.split("\\.");
		int length = Math.max(parts1.length, parts2.length);
		for (int i = 0; i < length; i++) {
			int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
			int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
			if (p1 != p2) return Integer.compare(p1, p2);
		}
		return 0;
	}

	private static String getLatestVersion() {
		try {
	        URLConnection urlConnection = new URL("https://api.spigotmc.org/legacy/update.php?resource=79137").openConnection();
	        return new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
	    } 
		catch (Exception ignored) {
	        return null;
	    }
	}
	
	
}
