package com.github._7000toni.auto.settings;

public class MiscellaneousSettings {
	private static String dataPath = "./";
	private static double arcW = 8;
	private static double arcH = 8;
	private static double tbOffset = 0.5;
	
	public static String dataPath() {
		return dataPath;
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
	
	public static void setDataPath(String dataPath) {
		MiscellaneousSettings.dataPath = dataPath;
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
		String s = "DATA_PATH=" + dataPath + "\n";
		s += "ARC_WIDTH=" + ((Double)arcW).toString() + "\n";
		s += "ARC_HEIGHT=" + ((Double)arcH).toString() + "\n";
		s += "TRADE_BUTTON_OFFSET=" + ((Double)tbOffset).toString() + "\n\n";
		return s;
	}
	
	public static void setDefaultSettings() {
		dataPath = "./";
		arcW = 8;
		arcH = 8;
		tbOffset = 0.5;
	}
	
	public static String defaultSettings() {
		String s = "DATA_PATH=./\n";
		s += "ARC_WIDTH=8\n";
		s += "ARC_HEIGHT=8\n";
		s += "TRADE_BUTTON_OFFSET=0.5\n\n";
		return s;
	}
}
