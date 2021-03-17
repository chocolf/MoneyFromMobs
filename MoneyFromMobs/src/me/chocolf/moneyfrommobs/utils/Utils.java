package me.chocolf.moneyfrommobs.utils;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class Utils {
	
	private static final Pattern pattern = Pattern.compile("#([A-Fa-f0-9]){6}");
	static Random r = new Random();
	
	public static String applyColour (String msg) {
		if ( Bukkit.getVersion().contains("1.16")) {
			Matcher match = pattern.matcher(msg);
			while (match.find()) {
				String color = msg.substring(match.start(), match.end());
				msg = msg.replace(color, ChatColor.of(color) + "");
				match = pattern.matcher(msg);
			}
		}
		return ChatColor.translateAlternateColorCodes('&', msg);
		
	}
	
	public static int intRandomNumber(int min, int max) {
		// Min is included
		// Max is Excluded
		r = new Random();
		return r.nextInt(max-min) + min;
	}
	
	public static double doubleRandomNumber(Double min, Double max) {
		// min and max are included
		
		return min + (max - min) *r.nextDouble();
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

	
}
