package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.settings.Settings;

public class GeneralSection implements SettingsSection {	
	public static final String SECTION_NAME = "[GENERAL]";
	
	@Override
	public void setSettings(String section) {
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
	
	@Override
	public String defaultSettings() {
		String s = "VERSION=" + Settings.version + "\n";
		s += "DARK_MODE=false\n";
		return s;
	}
	
	@Override
	public String currentSettings() {
		String s = "VERSION=" + Settings.loadedVersion() + "\n";
		s += "DARK_MODE=" + Chart.darkMode().get() + "\n";
		return s;
	}
}
