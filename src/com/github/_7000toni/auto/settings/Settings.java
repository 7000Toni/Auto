package com.github._7000toni.auto.settings;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.settings.sections.DarkModeColoursSection;
import com.github._7000toni.auto.settings.sections.DarkModeImageSettingsSection;
import com.github._7000toni.auto.settings.sections.GeneralSection;
import com.github._7000toni.auto.settings.sections.LightModeColoursSection;
import com.github._7000toni.auto.settings.sections.LightModeImageSettingsSection;
import com.github._7000toni.auto.settings.sections.MiscellaneousSettingsSection;

import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        
        return new File(settingsDir.getAbsoluteFile() + "/settings.ini");
	}
	
	private static void load() {
		try (FileInputStream fis = new FileInputStream(settings());
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			GeneralSection gs = new GeneralSection();
			LightModeColoursSection lmcs = new LightModeColoursSection();
			DarkModeColoursSection dmcs = new DarkModeColoursSection();
			LightModeImageSettingsSection lmiss = new LightModeImageSettingsSection();
			DarkModeImageSettingsSection dmiss = new DarkModeImageSettingsSection();
			MiscellaneousSettingsSection mss = new MiscellaneousSettingsSection();
			gs.setSettings(GeneralSection.loadSettings(GeneralSection.SECTION_NAME, gs.defaultSettings()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static void saveSettings() {
		File settings = settings();
		try (PrintWriter pw = new PrintWriter(settings)) {
			pw.println(version);
			pw.println(Chart.darkMode().get());
			pw.println(ColourSettings.string());
			pw.println(ImageSettings.string());
			pw.print(MiscellaneousSettings.string());
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDarkMode() {
		if (dontSave) {
			return;
		}
		try (PrintWriter pw = new PrintWriter(settings())) {
			pw.println(version);
			pw.println(Chart.darkMode().get());
			String subSettings = settings.substring(settings.indexOf('\n') + 1);
			pw.print(subSettings.substring(subSettings.indexOf('\n') + 1));
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
