package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.settings.ImageSettings;

public class DarkModeImageSection implements SettingsSection {
	public static final String SECTION_NAME = "[DARK_MODE_IMAGE_SETTINGS]";
	
	@Override
	public void setSettings(String section) {
		if (section == null) {
			return;
		}
		
		String imagePath = SettingsSectionHelper.setting(section, "IMAGE_PATH");
		imagePath = SettingsSectionHelper.settingValue(imagePath);
		String brightness = SettingsSectionHelper.setting(section, "BRIGHTNESS");
		brightness = SettingsSectionHelper.settingValue(brightness);
		String drawImage = SettingsSectionHelper.setting(section, "DRAW_IMAGE");
		drawImage = SettingsSectionHelper.settingValue(drawImage);
		String stretchImage = SettingsSectionHelper.setting(section, "STRETCH_IMAGE");
		stretchImage = SettingsSectionHelper.settingValue(stretchImage);
		try {
			ImageSettings.setDarkModeSettings(imagePath, Double.parseDouble(brightness), Boolean.parseBoolean(drawImage), Boolean.parseBoolean(stretchImage));
		} catch (NumberFormatException | NullPointerException e) {
			e.printStackTrace();
		}
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
