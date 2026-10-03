package com.github._7000toni.auto.settings;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github._7000toni.auto.settings.sections.ColoursSection;
import com.github._7000toni.auto.settings.sections.GeneralSection;
import com.github._7000toni.auto.settings.sections.ImageSection;
import com.github._7000toni.auto.settings.sections.MiscellaneousSection;
import com.github._7000toni.auto.settings.sections.SettingsSectionHelper;
import com.github._7000toni.auto.settings.sections.UnknownSettingsSections;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Settings {
	public static final String version = "1.0";	
	private static String loadedVersion = null;
	
	public static void loadSettings() {        
        File settings = settings();
        if (!settings.exists()) {
        	saveSettings();
        }
        
        load();
	}

	public static File settings() {
		String userHome = System.getProperty("user.home");
        Path docs = Paths.get(userHome, "Documents/Auto");
        File settingsDir = docs.toFile();
        
        if (!settingsDir.exists()) {
        	settingsDir.mkdir();
        }
        
        File settings = new File(settingsDir.getAbsoluteFile() + "/settings.ini");
        if (!settings.exists()) {
        	try {
				settings.createNewFile();
				loadedVersion = version;
				saveSettings();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return settings;
	}
	
	private static void load() {		
		GeneralSection.setSettings(SettingsSectionHelper.loadSettings(GeneralSection.SECTION_NAME, GeneralSection.defaultSettings()));
		ColoursSection.setSettings(false, SettingsSectionHelper.loadSettings(ColoursSection.LM_SECTION_NAME, ColoursSection.defaultSettings(false)));
		ColoursSection.setSettings(true, SettingsSectionHelper.loadSettings(ColoursSection.DM_SECTION_NAME, ColoursSection.defaultSettings(true)));
		ImageSection.setSettings(false, SettingsSectionHelper.loadSettings(ImageSection.LM_SECTION_NAME, ImageSection.defaultSettings()));
		ImageSection.setSettings(true, SettingsSectionHelper.loadSettings(ImageSection.DM_SECTION_NAME, ImageSection.defaultSettings()));
		MiscellaneousSection.setSettings(SettingsSectionHelper.loadSettings(MiscellaneousSection.SECTION_NAME, MiscellaneousSection.defaultSettings()));
	}	
	
	public static void saveSettings() {
		File settings = settings();
		String strUgs = UnknownSettingsSections.unknownSettings(SettingsSectionHelper.loadSettings(GeneralSection.SECTION_NAME, GeneralSection.defaultSettings()), GeneralSection.settings());
		String strUlmcs = UnknownSettingsSections.unknownSettings(SettingsSectionHelper.loadSettings(ColoursSection.LM_SECTION_NAME, ColoursSection.defaultSettings(false)), ColoursSection.settings());
		String strUdmcs = UnknownSettingsSections.unknownSettings(SettingsSectionHelper.loadSettings(ColoursSection.DM_SECTION_NAME, ColoursSection.defaultSettings(true)), ColoursSection.settings());
		String strUlmis = UnknownSettingsSections.unknownSettings(SettingsSectionHelper.loadSettings(ImageSection.LM_SECTION_NAME, ImageSection.defaultSettings()), ImageSection.settings());
		String strUdmis = UnknownSettingsSections.unknownSettings(SettingsSectionHelper.loadSettings(ImageSection.DM_SECTION_NAME, ImageSection.defaultSettings()), ImageSection.settings());
		String strUms = UnknownSettingsSections.unknownSettings(SettingsSectionHelper.loadSettings(MiscellaneousSection.SECTION_NAME, MiscellaneousSection.defaultSettings()), MiscellaneousSection.settings());
		String strUs = UnknownSettingsSections.unknownSections();
		try (PrintWriter pw = new PrintWriter(settings)) {
			pw.println(GeneralSection.SECTION_NAME + "\n" + GeneralSection.currentSettings() + strUgs);
			pw.println(ColoursSection.LM_SECTION_NAME + "\n" + ColoursSection.currentSettings(false) + strUlmcs);
			pw.println(ColoursSection.DM_SECTION_NAME + "\n" + ColoursSection.currentSettings(true) + strUdmcs);
			pw.println(ImageSection.LM_SECTION_NAME + "\n" + ImageSection.currentSettings(false) + strUlmis);
			pw.println(ImageSection.DM_SECTION_NAME + "\n" + ImageSection.currentSettings(true) + strUdmis);
			pw.print(MiscellaneousSection.SECTION_NAME + "\n" + MiscellaneousSection.currentSettings() + strUms);
			pw.print(strUs);
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDarkMode() {
		String strLmcs = ColoursSection.LM_SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(ColoursSection.LM_SECTION_NAME, ColoursSection.defaultSettings(false));
		String strDmcs = ColoursSection.DM_SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(ColoursSection.DM_SECTION_NAME, ColoursSection.defaultSettings(true));
		String strLmis = ImageSection.LM_SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(ImageSection.LM_SECTION_NAME, ImageSection.defaultSettings());
		String strDmis = ImageSection.DM_SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(ImageSection.DM_SECTION_NAME, ImageSection.defaultSettings());
		String strMs = MiscellaneousSection.SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(MiscellaneousSection.SECTION_NAME, MiscellaneousSection.defaultSettings());
		String strUs = UnknownSettingsSections.unknownSections();
		try (PrintWriter pw = new PrintWriter(settings())) {
			pw.println(GeneralSection.SECTION_NAME + "\n" + GeneralSection.currentSettings());
			pw.print(strLmcs);
			pw.print(strDmcs);
			pw.print(strLmis);
			pw.print(strDmis);
			pw.print(strMs);
			pw.print(strUs);
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setLoadedVersion(String version) {
		loadedVersion = version;
	}
	
	public static String loadedVersion() {
		return loadedVersion;
	}
}
