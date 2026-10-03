package com.github._7000toni.auto.settings;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github._7000toni.auto.settings.sections.DarkModeColoursSection;
import com.github._7000toni.auto.settings.sections.DarkModeImageSection;
import com.github._7000toni.auto.settings.sections.GeneralSection;
import com.github._7000toni.auto.settings.sections.LightModeColoursSection;
import com.github._7000toni.auto.settings.sections.LightModeImageSection;
import com.github._7000toni.auto.settings.sections.MiscellaneousSection;
import com.github._7000toni.auto.settings.sections.SettingsSectionHelper;
import com.github._7000toni.auto.settings.sections.UnknownSettingsSections;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Settings {
	public static final String version = "1.0";	
	private static String loadedVersion = null;
	
	private static GeneralSection gs = new GeneralSection();
	private static LightModeColoursSection lmcs = new LightModeColoursSection();
	private static DarkModeColoursSection dmcs = new DarkModeColoursSection();
	private static LightModeImageSection lmis = new LightModeImageSection();
	private static DarkModeImageSection dmis = new DarkModeImageSection();
	private static MiscellaneousSection ms = new MiscellaneousSection();
	
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
		gs.setSettings(SettingsSectionHelper.loadSettings(GeneralSection.SECTION_NAME, gs.defaultSettings()));
		lmcs.setSettings(SettingsSectionHelper.loadSettings(LightModeColoursSection.SECTION_NAME, lmcs.defaultSettings()));
		dmcs.setSettings(SettingsSectionHelper.loadSettings(DarkModeColoursSection.SECTION_NAME, dmcs.defaultSettings()));
		lmis.setSettings(SettingsSectionHelper.loadSettings(LightModeImageSection.SECTION_NAME, lmis.defaultSettings()));
		dmis.setSettings(SettingsSectionHelper.loadSettings(DarkModeImageSection.SECTION_NAME, dmis.defaultSettings()));
		ms.setSettings(SettingsSectionHelper.loadSettings(MiscellaneousSection.SECTION_NAME, ms.defaultSettings()));
	}	
	
	public static void saveSettings() {
		File settings = settings();
		String strUs = UnknownSettingsSections.unknownSections();
		try (PrintWriter pw = new PrintWriter(settings)) {
			pw.println(GeneralSection.SECTION_NAME + "\n" + gs.currentSettings());
			pw.println(LightModeColoursSection.SECTION_NAME + "\n" + lmcs.currentSettings());
			pw.println(DarkModeColoursSection.SECTION_NAME + "\n" + dmcs.currentSettings());
			pw.println(LightModeImageSection.SECTION_NAME + "\n" + lmis.currentSettings());
			pw.println(DarkModeImageSection.SECTION_NAME + "\n" + dmis.currentSettings());
			pw.println(MiscellaneousSection.SECTION_NAME + "\n" + ms.currentSettings());
			pw.print(strUs);
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDarkMode() {
		String strLmcs = LightModeColoursSection.SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(LightModeColoursSection.SECTION_NAME, lmcs.defaultSettings());
		String strDmcs = DarkModeColoursSection.SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(DarkModeColoursSection.SECTION_NAME, dmcs.defaultSettings());
		String strLmis = LightModeImageSection.SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(LightModeImageSection.SECTION_NAME, lmis.defaultSettings());
		String strDmis = DarkModeImageSection.SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(DarkModeImageSection.SECTION_NAME, dmis.defaultSettings());
		String strMs = MiscellaneousSection.SECTION_NAME + "\n" + SettingsSectionHelper.loadSettings(MiscellaneousSection.SECTION_NAME, ms.defaultSettings());
		String strUs = UnknownSettingsSections.unknownSections();
		try (PrintWriter pw = new PrintWriter(settings())) {
			pw.println(GeneralSection.SECTION_NAME + "\n" + gs.currentSettings());
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
