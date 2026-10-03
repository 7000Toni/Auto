package com.github._7000toni.auto.settings.sections;

import java.util.ArrayList;

import com.github._7000toni.auto.settings.MiscellaneousSettings;

public class MiscellaneousSection {
	public static final String SECTION_NAME = "[MISCELLANEOUS_SETTINGS]";
	
	public static void setSettings(String section) {
		if (section == null) {
			return;
		}
		
		String dataPath = SettingsSectionHelper.setting(section, "DATA_PATH");
		if (dataPath != null) {
			MiscellaneousSettings.setDataPath(SettingsSectionHelper.settingValue(dataPath));
		}
		String arcW = SettingsSectionHelper.setting(section, "ARC_WIDTH");
		try {
			if (arcW != null) {
				MiscellaneousSettings.setArcW(Double.parseDouble(SettingsSectionHelper.settingValue(arcW)));
			}
			String arcH = SettingsSectionHelper.setting(section, "ARC_HEIGHT");
			if (arcH != null) {
				MiscellaneousSettings.setArcH(Double.parseDouble(SettingsSectionHelper.settingValue(arcH)));
			}
			String tradeButOff = SettingsSectionHelper.setting(section, "TRADE_BUTTON_OFFSET");
			if (tradeButOff != null) {
				MiscellaneousSettings.setTradeButtonOffset(Double.parseDouble(SettingsSectionHelper.settingValue(tradeButOff)));
			}
		} catch (NumberFormatException | NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public static String defaultSettings() {
		return MiscellaneousSettings.defaultSettings();
	}
	
	public static String currentSettings() {
		return MiscellaneousSettings.string();
	}
	
	public static ArrayList<String> settings() {
		ArrayList<String> settings = new ArrayList<String>();
		settings.add("DATA_PATH");	
		settings.add("ARC_WIDTH");
		settings.add("ARC_HEIGHT");
		settings.add("TRADE_BUTTON_OFFSET");
		return settings;
	}
}
