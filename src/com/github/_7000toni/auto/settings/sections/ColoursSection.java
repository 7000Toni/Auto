package com.github._7000toni.auto.settings.sections;

import java.util.ArrayList;

import com.github._7000toni.auto.settings.ColourSettings;

import javafx.scene.paint.Color;

public class ColoursSection {
	public static final String LM_SECTION_NAME = "[LIGHT_MODE_COLOURS]";
	public static final String DM_SECTION_NAME = "[DARK_MODE_COLOURS]";
	
	public static void setSettings(boolean darkMode, String section) {
		if (section == null) {
			return;
		}
		int offset = darkMode?ColourSettings.SIZE:0;
		for (int i = offset; i < ColourSettings.SIZE + offset; i++) {
			ColourSettings.ColourIndex ci = ColourSettings.ColourIndex.values()[i - offset];
			String setting = SettingsSectionHelper.setting(section, ci.name());
			if (setting == null || setting.equals("")) {
				continue;
			}
			try {
				ColourSettings.colours().set(ci.index + offset, Color.web(SettingsSectionHelper.settingValue(setting)));
			} catch (IllegalArgumentException | NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String defaultSettings(boolean darkMode) {
		if (darkMode) {
			return ColourSettings.defaultDarkModeString();
		} else {
			return ColourSettings.defaultLightModeString();
		}
	}
	
	public static String currentSettings(boolean darkMode) {
		if (darkMode) {
			return ColourSettings.darkModeString();
		} else {
			return ColourSettings.lightModeString();
		}
	}
	
	public static ArrayList<String> settings() {
		ArrayList<String> settings = new ArrayList<String>();
		for (ColourSettings.ColourIndex c : ColourSettings.ColourIndex.values()) {
			settings.add(c.name());			
		}
		return settings;
	}
}
