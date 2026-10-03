package com.github._7000toni.auto.settings.sections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.github._7000toni.auto.settings.Settings;

public class UnknownSettingsSections {
	private static ArrayList<String> sections = new ArrayList<String>();
	private static boolean init = false;
	
	private static void init() {
		sections.add(ColoursSection.LM_SECTION_NAME);
		sections.add(ColoursSection.DM_SECTION_NAME);
		sections.add(GeneralSection.SECTION_NAME);
		sections.add(ImageSection.LM_SECTION_NAME);
		sections.add(ImageSection.DM_SECTION_NAME);
		sections.add(MiscellaneousSection.SECTION_NAME);
		init = true;
	}
	
	public static String unknownSections() {
		if (!init) {
			init();
		}
		File settings = Settings.settings();
		
		try (FileInputStream fis = new FileInputStream(settings);
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			String s = "";
			String in;
			while ((in = SettingsSectionHelper.nextSection(br)) != null && in.contains("[") && in.contains("]")) {
				if (sections.contains(in)) {
					continue;
				}
				s += in + "\n" + SettingsSectionHelper.loadSection(br, true);
				while (true) {
					int i = s.lastIndexOf("[");
					int j = s.lastIndexOf("]");
					if (i != -1 && j != -1 && i < j) {
						String nextSection = s.substring(i, j + 1);						
						if (nextSection.equals(in)) {
							break;
						}
						if (!sections.contains(nextSection)) {
							s += nextSection + "\n" + SettingsSectionHelper.loadSection(br, true);							
						} else {
							break;
						}
					}
					break;
				}
			}
			return s.equals("")?s:"\n"+s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static String readLine(String in) {
		int i = in.indexOf("\n");
		if (i != -1) {
			return in.substring(0, i);
		}
		return in;
	}
	
	private static String discardLine(String in) {
		int i = in.indexOf("\n");
		if (i != -1 && i != in.length() - 1) {
			return in.substring(i + 1);
		}
		return null;
	}
	
	private static String settingName(String in) {
		int i = in.indexOf("=");
		if (i != -1) {
			return in.substring(0, i);
		}
		return null;
	}
	
	public static String unknownSettings(String section, ArrayList<String> settings) {
		if (section == null || settings == null) {
			return null;
		}
		String out = "";
		do {
			String in = readLine(section);
			String settingName = settingName(in);
			if (settingName != null && !settings.contains(settingName)) {
				out += in + "\n";
			}
		} while ((section = discardLine(section)) != null);
		return out;
	}
}
