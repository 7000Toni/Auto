package com.github._7000toni.auto.settings.sections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.github._7000toni.auto.settings.Settings;

public abstract class SettingsSection {
	
	public static String loadSettings(String SectionName, String defaultSettings) {
		File settings = Settings.settings();
		
		try (FileInputStream fis = new FileInputStream(settings);
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			String s;
			while (!(s = nextSection(br)).equals(SectionName)) {
				if (s.equals(null)) {
					return defaultSettings;
				}
			}
			return loadSection(br);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static String nextSection(BufferedReader br) {
		String in = null;
		try {			
			in = br.readLine();
			while (in != null) {
				if (in.startsWith("[") && in.endsWith("]")) {
					return in;
				}
				in = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	}
	
	private static String loadSection(BufferedReader br) {
		String sec = "";
		try {		
			String in = br.readLine();			
			while (in != null && !(in.startsWith("[") && in.endsWith("]"))) {		
				sec += in;
				in = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sec;
	}
	
	protected static String setting(String section, String name) {
		return section.substring(section.indexOf(name + "="), section.indexOf("\n"));
	}
	
	protected static String settingValue(String setting) {
		return setting.substring(setting.indexOf("=") + 1);
	}
	
	public abstract void setSettings(String section);	
	public abstract String defaultSettings();
	public abstract String currentSettings();
}
