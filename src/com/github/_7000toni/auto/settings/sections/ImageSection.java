package com.github._7000toni.auto.settings.sections;

import java.util.ArrayList;

import com.github._7000toni.auto.settings.ImageSettings;

public class ImageSection {
	public static final String LM_SECTION_NAME = "[LIGHT_MODE_IMAGE_SETTINGS]";
	public static final String DM_SECTION_NAME = "[DARK_MODE_IMAGE_SETTINGS]";
	
	public static void setSettings(boolean darkMode, String section) {
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
			if (darkMode) {
				ImageSettings.setDarkModeSettings(imagePath, Double.parseDouble(brightness), Boolean.parseBoolean(drawImage), Boolean.parseBoolean(stretchImage));
			} else {
				ImageSettings.setLightModeSettings(imagePath, Double.parseDouble(brightness), Boolean.parseBoolean(drawImage), Boolean.parseBoolean(stretchImage));
			}
		} catch (NumberFormatException | NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public static String defaultSettings() {
		return ImageSettings.defaultSettings();
	}
	
	public static String currentSettings(boolean darkMode) {
		if (darkMode) {
			return ImageSettings.darkModeSettings();
		} else {
			return ImageSettings.lightModeSettings();
		}
	}
	
	public static ArrayList<String> settings() {
		ArrayList<String> settings = new ArrayList<String>();
		settings.add("IMAGE_PATH");	
		settings.add("BRIGHTNESS");
		settings.add("DRAW_IMAGE");
		settings.add("STRETCH_IMAGE");
		return settings;
	}
}
