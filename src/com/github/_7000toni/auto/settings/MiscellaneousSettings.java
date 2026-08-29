package com.github._7000toni.auto.settings;

public class MiscellaneousSettings {
	private static String initFileDir = "./";
	private static double arcW = 8;
	private static double arcH = 8;
	private static double tbOffset = 0.5;
	
	public static String initFileDir() {
		return initFileDir;
	}
	
	public static double arcW() {
		return arcW;
	}
	
	public static double arcH() {
		return arcH;
	}
	
	public static double tradeButtonOffset() {
		return tbOffset;
	}
	
	public static void setInitFileDir(String initFileDir) {
		MiscellaneousSettings.initFileDir = initFileDir;
	}
	
	public static void setArcW(double arcW) {
		MiscellaneousSettings.arcW = arcW;
	}
	
	public static void setArcH(double arcH) {
		MiscellaneousSettings.arcH = arcH;
	}
	
	public static void setTradeButtonOffset(double tbOffset) {
		MiscellaneousSettings.tbOffset = tbOffset;
	}
	
	public static String string() {
		String s = "";
		s += initFileDir + "\n";
		s += ((Double)arcW).toString() + "\n";
		s += ((Double)arcH).toString() + "\n";
		s += ((Double)tbOffset).toString();
		return s;
	}
	
	public static void setDefaultSettings() {
		initFileDir = "./";
		arcW = 8;
		arcH = 8;
		tbOffset = 0.5;
	}
}
