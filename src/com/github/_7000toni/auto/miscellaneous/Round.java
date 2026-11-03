package com.github._7000toni.auto.miscellaneous;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Round {
	public static double round(double val, int decimalPlaces) {
		if (Double.isNaN(val)) {
			return val;
		}
		return BigDecimal.valueOf(val).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
	}
}
