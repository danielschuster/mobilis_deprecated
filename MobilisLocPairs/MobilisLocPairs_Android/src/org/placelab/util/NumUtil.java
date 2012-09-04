package org.placelab.util;

/**
 * 
 * Utilities for Numbers ;)
 */

import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

public class NumUtil {
	public static Random rand = new Random(17); // Call seedRand if you want a different seed
	
	public static void seedRand(long seed) {
		rand = new Random(seed);
	}

	public static String padDouble(double d, int digitsAfterDecimal, int len) {
		long mult=1;
		for (int i=0; i<digitsAfterDecimal; i++) {
			mult *= 10;
		}
		long l = (long)((d + (0.5/mult))*mult);
		double d2 = l/(double)mult;
		if (digitsAfterDecimal == 0) {
			return StringUtil.pad("" + ((int)d2),len);
		} else {
			return StringUtil.pad("" + d2,len);
		}
	}

	public static String doubleToString(double value, int precision) {
		NumberFormat formatter = NumberFormat.getInstance(Locale.US);
		formatter.setMaximumFractionDigits(precision);
		formatter.setMinimumFractionDigits(precision);
		formatter.setGroupingUsed(false);
		return formatter.format(value);
	}	
	
	public static double log10(double d) {
		return Math.log(d)/2.302585093;
	}
	
	/* pass in Vector of Doubles */
	public static double stDev(Vector listOfDoubles, double mean) {
		double tot=0.0;
		if (listOfDoubles.size() == 0) {
			return 0.0;
		}
		for (Enumeration it = listOfDoubles.elements(); it.hasMoreElements();) {
			Double d = (Double)it.nextElement();
			if (!Double.isNaN(d.doubleValue())) {
				tot += (mean-d.doubleValue())*(mean-d.doubleValue());
			}
		}
		tot /= listOfDoubles.size();
		return Math.sqrt(tot);
	}
	
}
