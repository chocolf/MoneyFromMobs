package me.chocolf.moneyfrommobs.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {
	
	public static boolean checkForUpdate() {
		double currentVersion = VersionUtils.getPluginVersion();
		double latestVersion = Double.parseDouble(getLatestVersion());
		
		return currentVersion < latestVersion;
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
