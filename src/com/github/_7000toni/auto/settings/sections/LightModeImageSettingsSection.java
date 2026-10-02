package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.settings.ImageSettings;

public class LightModeImageSettingsSection implements SettingsSection {
	public static final String SECTION_NAME = "LIGHT_MODE_IMAGE_SETTINGS";
	
	@Override
	public void setSettings(String section) {
		String imagePath = SettingsSectionHelper.setting(section, "IMAGE_PATH");
		String brightness = SettingsSectionHelper.setting(section, "BRIGHTNESS");
		String drawImage = SettingsSectionHelper.setting(section, "DRAW_IMAGE");
		String stretchImage = SettingsSectionHelper.setting(section, "STRETCH_IMAGE");
		ImageSettings.setLightModeSettings(imagePath, Double.parseDouble(brightness), Boolean.parseBoolean(drawImage), Boolean.parseBoolean(stretchImage));
	}
	
	@Override
	public String defaultSettings() {
		return ImageSettings.defaultSettings();
	}
	
	@Override
	public String currentSettings() {
		return ImageSettings.lightModeSettings();
	}
}
