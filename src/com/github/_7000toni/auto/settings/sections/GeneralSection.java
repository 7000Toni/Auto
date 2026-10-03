package com.github._7000toni.auto.settings.sections;

import java.util.ArrayList;

import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.settings.Settings;

public class GeneralSection {	
	public static final String SECTION_NAME = "[GENERAL]";
	
	public static void setSettings(String section) {
		if (section == null) {
			return;
		}
		
		String version = SettingsSectionHelper.setting(section, "VERSION");
		version = SettingsSectionHelper.settingValue(version);
		if (version != null && !version.equals("")) {
			Settings.setLoadedVersion(version);
		}
		String darkMode = SettingsSectionHelper.setting(section, "DARK_MODE");
		darkMode = SettingsSectionHelper.settingValue(darkMode);
		if (darkMode != null) {
			boolean dm = Boolean.parseBoolean(darkMode);
			if (dm != Chart.darkMode().get()) {
				Chart.toggleDarkMode();
			}
		}
	}
	
	public static String defaultSettings() {
		String s = "VERSION=" + Settings.version + "\n";
		s += "DARK_MODE=false\n";
		return s;
	}
	
	public static String currentSettings() {
		String s = "VERSION=" + Settings.loadedVersion() + "\n";
		s += "DARK_MODE=" + Chart.darkMode().get() + "\n";
		return s;
	}
	
	public static ArrayList<String> settings() {
		ArrayList<String> settings = new ArrayList<String>();
		settings.add("VERSION");	
		settings.add("DARK_MODE");
		return settings;
	}
}
