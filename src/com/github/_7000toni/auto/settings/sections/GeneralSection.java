package com.github._7000toni.auto.settings.sections;

import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.settings.Settings;

public class GeneralSection extends SettingsSection {	
	public static final String SECTION_NAME = "GENERAL";
	
	@Override
	public void setSettings(String section) {
		String version = setting(section, "VERSION");
		Settings.setLoadedVersion(settingValue(version));
		String darkMode = setting(section, "DARK_MODE");
		boolean dm = Boolean.parseBoolean(settingValue(darkMode));
		if (dm != Chart.darkMode().get()) {
			Chart.toggleDarkMode();
		}
	}
	
	@Override
	public String defaultSettings() {
		String s = "VERSION=" + Settings.version + "\n";
		s += "DARK_MODE=false\n\n";
		return s;
	}
	
	@Override
	public String currentSettings() {
		String s = "VERSION=" + Settings.loadedVersion() + "\n";
		s += "DARK_MODE=" + Chart.darkMode().get() + "\n\n";
		return s;
	}
}
