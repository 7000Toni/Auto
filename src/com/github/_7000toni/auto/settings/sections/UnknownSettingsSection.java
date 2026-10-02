package com.github._7000toni.auto.settings.sections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.github._7000toni.auto.settings.Settings;

public class UnknownSettingsSection {
	private static ArrayList<String> sections = new ArrayList<String>();
	private static boolean init = false;
	
	private static void init() {
		sections.add(DarkModeColoursSection.SECTION_NAME);
		sections.add(LightModeColoursSection.SECTION_NAME);
		sections.add(GeneralSection.SECTION_NAME);
		sections.add(LightModeImageSettingsSection.SECTION_NAME);
		sections.add(DarkModeImageSettingsSection.SECTION_NAME);
		sections.add(MiscellaneousSettingsSection.SECTION_NAME);
		init = true;
	}
	
	public static String unknownSection() {
		if (!init) {
			init();
		}
		File settings = Settings.settings();
		
		try (FileInputStream fis = new FileInputStream(settings);
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			String s = "";
			String in;
			String lastSection;
			while ((in = SettingsSectionHelper.nextSection(br)) != null) {
				if (sections.contains(lastSection = in.substring(1, in.length() - 2))) {
					continue;
				}
				s += in + SettingsSectionHelper.loadSection(br, true);
				boolean chain = true;				
				while (chain) {
					int i = s.lastIndexOf("[");
					int j = s.lastIndexOf("]");
					if (i != -1 && j != -1 && i < j) {
						String nextSection = s.substring(i + 1, j);
						if (nextSection.equals(lastSection)) {							
							break;
						}
						if (!sections.contains(nextSection)) {
							s += SettingsSectionHelper.loadSection(br, true);
						} else {
							chain = false;
						}
					}
				}
			}
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
