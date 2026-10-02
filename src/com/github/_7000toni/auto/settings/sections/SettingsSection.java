package com.github._7000toni.auto.settings.sections;

public interface SettingsSection {
	public void setSettings(String section);	
	public String defaultSettings();
	public String currentSettings();
}
