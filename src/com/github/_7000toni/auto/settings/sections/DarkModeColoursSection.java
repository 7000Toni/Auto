package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.settings.ColourSettings;

import javafx.scene.paint.Color;

public class DarkModeColoursSection implements SettingsSection {
	public static final String SECTION_NAME = "[DARK_MODE_COLOURS]";
	
	@Override
	public void setSettings(String section) {
		if (section == null) {
			return;
		}
		
		for (int i = ColourSettings.SIZE; i < ColourSettings.SIZE*2; i++) {
			ColourSettings.ColourIndex ci = ColourSettings.ColourIndex.values()[i - ColourSettings.SIZE];
			String setting = SettingsSectionHelper.setting(section, ci.name());
			if (setting == null || setting.equals("")) {
				continue;
			}
			try {
				ColourSettings.colours().set(ci.index + ColourSettings.SIZE, Color.web(SettingsSectionHelper.settingValue(setting)));
			} catch (IllegalArgumentException | NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String defaultSettings() {
		return ColourSettings.defaultDarkModeString();
	}
	
	@Override
	public String currentSettings() {
		return ColourSettings.darkModeString();
	}
}
