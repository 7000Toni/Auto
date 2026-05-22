package com.github._7000toni.auto.dataset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatumChecker {
	public static void check(Pattern pattern, String datum) throws Exception {
		Matcher m = pattern.matcher(datum);
		if (!m.matches()) {
			throw new Exception("Invalid datum/file: " + datum);
		}
	}
}
