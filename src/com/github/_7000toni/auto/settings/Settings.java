package com.github._7000toni.auto.settings;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.github._7000toni.auto.chart.Chart;

import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Settings {
	private static final String version = "2.0";
	private static String settings = null;	
	private static boolean dontSave = false;
	
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
        
        return new File(settingsDir.getAbsoluteFile() + "/settings");
	}
	
	private static void setSettingsString() {
		settings = version + "\n";
		settings += Chart.darkMode().get() + "\n";
		settings += ColourSettings.string() + "\n";
		settings += ImageSettings.string() + "\n";
		settings += MiscellaneousSettings.string() + "\n";
	}
	
	private static void load() {
		boolean darkMode;
		try (FileInputStream fis = new FileInputStream(settings());
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			if (!br.readLine().equals(Settings.version)) {
				setSettingsString();
				dontSave = true;
				return;
			}
			settings = version + "\n";
			darkMode = Boolean.parseBoolean(br.readLine());		
			settings += darkMode + "\n";
			for (int i = 0; i < ColourSettings.SIZE*2; i++) {
				String colour = br.readLine();
				settings += colour + "\n";
				if (colour == null || colour.isBlank()) {
					ColourSettings.setDefaultColours();
					saveSettings();
					break;
				} else {
					ColourSettings.colours().set(i, Color.web(colour));
				}
			}
			ArrayList<String> imgSettings = new ArrayList<String>();
			for (int i = 0; i < 2; i++) {
				String imgDir = br.readLine();
				settings += imgDir + "\n";
				imgSettings.add(imgDir);
				
				String brightnessString = br.readLine();
				settings += brightnessString + "\n";
				imgSettings.add(brightnessString);
				
				if (imgDir == null || brightnessString == null) {
					return;
				}
				String draw = br.readLine();
				settings += draw + "\n";
				imgSettings.add(draw);
				
				String stretch = br.readLine();
				settings += stretch + "\n";
				imgSettings.add(stretch);
			}			
			ImageSettings.setSettings(imgSettings.get(0), Double.parseDouble(imgSettings.get(1)), Boolean.parseBoolean(imgSettings.get(2)), Boolean.parseBoolean(imgSettings.get(3)),
									imgSettings.get(4), Double.parseDouble(imgSettings.get(5)), Boolean.parseBoolean(imgSettings.get(6)),Boolean.parseBoolean( imgSettings.get(7)));
			String initFileDir = br.readLine();
			settings += initFileDir + "\n";
			MiscellaneousSettings.setInitFileDir(initFileDir);
			String arcW = br.readLine();
			settings += arcW + "\n";
			MiscellaneousSettings.setArcW(Double.parseDouble(arcW));
			String arcH = br.readLine();
			settings += arcH + "\n";
			MiscellaneousSettings.setArcH(Double.parseDouble(arcH));
			String tbOffset = br.readLine();
			settings += tbOffset + "\n";
			MiscellaneousSettings.setTradeButtonOffset(Double.parseDouble(tbOffset));
			if (darkMode != Chart.darkMode().get()) {
				Chart.toggleDarkMode();
			}
		} catch (Exception e) {
			ColourSettings.setDefaultColours();
			ImageSettings.setDefaultSettings();
			MiscellaneousSettings.setDefaultSettings();
			saveSettings();
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
}
