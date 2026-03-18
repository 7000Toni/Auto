import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Settings {
	
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
	
	private static void load() {
		boolean darkMode;
		 try (FileInputStream fis = new FileInputStream(settings());
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			darkMode = Boolean.parseBoolean(br.readLine());			
			for (int i = 0; i < ColourSettings.size()*2; i++) {
				String colour = br.readLine();
				if (colour == null || colour.isBlank()) {
					ColourSettings.setDefaultColours();
					saveSettings();
					break;
				} else {
					ColourSettings.colours().set(i, Color.web(colour));
				}
			}
			String imgdir = br.readLine();
			String brightnessString = br.readLine();
			if (imgdir == null || brightnessString == null) {
				return;
			}
			double brightness = Double.parseDouble(brightnessString);
			boolean draw = Boolean.parseBoolean(br.readLine());
			boolean stretch = Boolean.parseBoolean(br.readLine());
			ImageSettings.setSettings(imgdir, brightness, draw, stretch);
			if (darkMode != Chart.darkMode().get()) {
				Chart.toggleDarkMode();
			}
		} catch (IOException e) {
			ColourSettings.setDefaultColours();
			ImageSettings.setDefaultSettings();
			saveSettings();
			e.printStackTrace();
		}
	}	
	
	public static ArrayList<Color> loadColours() {
		File settings = settings();
		ArrayList<Color> colours = new ArrayList<Color>();
		try (FileInputStream fis = new FileInputStream(settings);
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
			br.readLine();
			for (int i = 0; i < ColourSettings.size()*2; i++) {
				String colour = br.readLine();
				if (colour == null || colour.isBlank()) {
					ColourSettings.setDefaultColours();
					saveSettings();
					return ColourSettings.defaultColours();
				}
				colours.add(Color.web(colour));
			}
		} catch (IOException e) {
			ColourSettings.setDefaultColours();
			ImageSettings.setDefaultSettings();
			saveSettings();
			e.printStackTrace();
		}
		return colours;
	}
	
	public static void saveSettings() {
		File settings = settings();
		try (PrintWriter pw = new PrintWriter(settings)) {
			pw.println(Chart.darkMode().get());
			pw.print(ColourSettings.string());
			pw.print(ImageSettings.string());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDarkMode() {
		File settings = settings();
		ArrayList<Color> colours = loadColours();
		String s = "";
		for (Color c : colours) {
			s += c.toString() + '\n';
		}
		try (PrintWriter pw = new PrintWriter(settings)) {
			pw.println(Chart.darkMode().get());
			pw.print(s);	
			pw.print(ImageSettings.string());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
