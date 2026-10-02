package com.github._7000toni.auto.settings.sections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.github._7000toni.auto.settings.Settings;

public class SettingsSectionHelper {
	
	public static String loadSettings(String SectionName, String defaultSettings) {
		File settings = Settings.settings();
		
		try (FileInputStream fis = new FileInputStream(settings);
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			String s;
			while (!(s = nextSection(br)).equals("[" + SectionName + "]")) {
				if (s.equals(null)) {
					return defaultSettings;
				}
			}
			return loadSection(br, false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String nextSection(BufferedReader br) {
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
	
	public static String loadSection(BufferedReader br, boolean nextSection) {
		String sec = "";
		String in = "";
		try {		
			in = br.readLine();			
			while (in != null && !(in.startsWith("[") && in.endsWith("]"))) {		
				sec += in;
				in = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sec + (nextSection?in:"");
	}
	
	public static String setting(String section, String name) {
		int i = section.indexOf(name + "=");
		if (i == -1) {
			return null;
		}
		section = section.substring(i);
		int j = section.indexOf("\n");
		if (j == -1) {
			return section;
		}
		
		return section.substring(0, j);
	}
	
	public static String settingValue(String setting) {
		int i = setting.indexOf("=");
		if (i == -1) {
			return null;
		}
		
		return setting.substring(i + 1);
	}	
}
