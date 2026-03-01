import java.nio.file.Path;
import java.nio.file.Paths;

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
        	return;
        }
        
        load(settings);
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
	
	private static void load(File settings) {
		 try (FileInputStream fis = new FileInputStream(settings);
				 BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
			settings.createNewFile();
			for (int i = 0; i < ColourSettings.colours().size(); i++) {
				String colour = br.readLine();
				if (colour == null) {
					saveSettings();
					break;
				}
				ColourSettings.colours().set(i, Color.web(colour));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public static void saveSettings() {
		File settings = settings();
		try (PrintWriter pw = new PrintWriter(settings)) {
			settings.createNewFile();
			pw.print(ColourSettings.string());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
