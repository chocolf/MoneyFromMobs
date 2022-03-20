package me.chocolf.moneyfrommobs.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class RandomNumberUtils {
	
	static Random r = new Random();
	
	public static int intRandomNumber(int min, int max) {
		// Min is included
		// Max is Excluded
		// TODO: check if can remove this
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
