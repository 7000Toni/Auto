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
        File lsettings = settings(true);
        File dsettings = settings(false);
        if (!lsettings.exists()) {
        	saveSettings(true);
        }
        if (!dsettings.exists()) {
        	saveSettings(false);
        }
        
        load();
	}

	public static File settings(boolean light) {
		String userHome = System.getProperty("user.home");
        Path docs = Paths.get(userHome, "Documents/Auto");
        File settingsDir = docs.toFile();
        
        if (!settingsDir.exists()) {
        	settingsDir.mkdir();
        }
        
        String settings;
        if (light) {
        	settings = "lsettings";
        } else {
        	settings = "dsettings";
        }
        return new File(settingsDir.getAbsoluteFile() + "/" + settings);
	}
	
	private static void load() {
		boolean darkMode;
		 try (FileInputStream fis = new FileInputStream(settings(true));
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				 FileInputStream fis2 = new FileInputStream(settings(false));
					BufferedReader br2 = new BufferedReader(new InputStreamReader(fis2))) {
			darkMode = Boolean.parseBoolean(br.readLine());
			if (darkMode != Chart.darkMode().get()) {
				Chart.toggleDarkMode();
			}
			boolean lcorrupt = false;
			boolean dcorrupt = false;
			for (int i = 0; i < ColourSettings.lightColours().size(); i++) {
				if (!lcorrupt) {
					String lcolour = br.readLine();
					if (lcolour == null || lcolour.isBlank()) {
						ColourSettings.setDefaultLightColours();
						saveSettings(true);
						lcorrupt = true;
					} else {
						ColourSettings.lightColours().set(i, Color.web(lcolour));
					}
				}
				if (!dcorrupt) {
					String dcolour = br2.readLine();
					if (dcolour == null || dcolour.isBlank()) {
						ColourSettings.setDefaultDarkColours();
						saveSettings(false);
						dcorrupt = true;
					} else {
						ColourSettings.darkColours().set(i, Color.web(dcolour));
					}
				}
				if (lcorrupt && dcorrupt) {
					break;
				}
			}
		} catch (IOException e) {
			ColourSettings.setDefaultColours();
			saveSettings();
			e.printStackTrace();
		}
	}	
	
	public static ArrayList<Color> loadColours(boolean light) {
		File settings = settings(light);
		ArrayList<Color> colours = new ArrayList<Color>();
		 try (FileInputStream fis = new FileInputStream(settings);
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
			for (int i = 0; i < ColourSettings.lightColours().size(); i++) {
				if (light && i == 0) {
					br.readLine();
				}
				String colour = br.readLine();
				if (colour == null || colour.isBlank()) {
					if (light) {
						ColourSettings.setDefaultLightColours();
					} else {
						ColourSettings.setDefaultDarkColours();
					}
					saveSettings(light);
					if (light) {
						return ColourSettings.defaultLightColours();
					} else {
						return ColourSettings.defaultDarkColours();
					}
				}
				colours.add(Color.web(colour));
			}
		} catch (IOException e) {
			ColourSettings.setDefaultColours();
			saveSettings();
			e.printStackTrace();
		}
		return colours;
	}
	
	public static void saveSettings() {
		File lsettings = settings(true);
		File dsettings = settings(false);
		try (PrintWriter pw = new PrintWriter(lsettings);
				PrintWriter pw2 = new PrintWriter(dsettings)) {
			pw.println(Chart.darkMode().get());
			pw.print(ColourSettings.lightString());
			
			pw2.print(ColourSettings.darkString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveSettings(boolean light) {
		File settings = settings(light);
		try (PrintWriter pw = new PrintWriter(settings)) {
			if (light) {
				pw.println(Chart.darkMode().get());
				pw.print(ColourSettings.lightString());
			} else {
				pw.print(ColourSettings.darkString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDarkMode() {
		File settings = settings(true);
		ArrayList<Color> colours = loadColours(true);
		String s = "";
		for (Color c : colours) {
			s += c.toString() + '\n';
		}
		try (PrintWriter pw = new PrintWriter(settings)) {
			pw.println(Chart.darkMode().get());
			pw.print(s);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
