package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.settings.ColourSettings;

import javafx.scene.paint.Color;

public class LightModeColoursSection implements SettingsSection {
	public static final String SECTION_NAME = "LIGHT_MODE_COLOURS";
	
	@Override
	public void setSettings(String section) {
		for (int i = 0; i < ColourSettings.SIZE; i++) {
			ColourSettings.ColourIndex ci = ColourSettings.ColourIndex.values()[i];
			String setting = SettingsSectionHelper.setting(section, ci.name());
			ColourSettings.setColour(ci, Color.web(SettingsSectionHelper.settingValue(setting)));
		}
	}
	
	@Override
	public String defaultSettings() {
		return ColourSettings.defaultLightModeString();
	}
	
	@Override
	public String currentSettings() {
		return ColourSettings.lightModeString();
	}
}
