package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.settings.ImageSettings;

public class DarkModeImageSettingsSection extends SettingsSection {
	public static final String SECTION_NAME = "DARK_MODE_IMAGE_SETTINGS";
	
	@Override
	public void setSettings(String section) {
		String imagePath = setting(section, "IMAGE_PATH");
		String brightness = setting(section, "BRIGHTNESS");
		String drawImage = setting(section, "DRAW_IMAGE");
		String stretchImage = setting(section, "STRETCH_IMAGE");
		ImageSettings.setDarkModeSettings(imagePath, Double.parseDouble(brightness), Boolean.parseBoolean(drawImage), Boolean.parseBoolean(stretchImage));
	}
	
	@Override
	public String defaultSettings() {
		return ImageSettings.defaultSettings();
	}
	
	@Override
	public String currentSettings() {
		return ImageSettings.darkModeSettings();
	}
}
