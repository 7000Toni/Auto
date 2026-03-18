import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;

public class ImageSettings {
	private static Image image = null;
	private static String imgdir = null;
	private static double brightness = 0; 
	private static BooleanProperty draw = new SimpleBooleanProperty(false);
	private static BooleanProperty stretch = new SimpleBooleanProperty(false);
	private static final int size = 4; 
	
	private static void loadImage() {
		if (imgdir == null) {
			image = null;
			return;
		}
		File f = new File(imgdir);
		if (!f.exists()) {
			return;
		}
		try {
			image = new Image(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			image = null;
			setSettings(null, 0, false, false);
			Settings.saveSettings();
			e.printStackTrace();
		}
	}
	
	private static void loadImage(File file) {
		try {
			image = new Image(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			image = null;
			setSettings(null, 0, false, false);
			Settings.saveSettings();
			e.printStackTrace();
		}
	}
	
	public static void setSettings(String imgdir, double brightness, boolean draw, boolean stretch) {
		if (imgdir == null) {
			return;
		}
		ImageSettings.imgdir = imgdir;
		ImageSettings.brightness = brightness;
		ImageSettings.draw.set(draw);
		ImageSettings.stretch.set(stretch);		
		loadImage();
	}
	
	public static void setDefaultSettings() {
		ImageSettings.imgdir = null;
		ImageSettings.image = null;
		ImageSettings.brightness = 0;
		ImageSettings.stretch.set(false);
		ImageSettings.draw.set(false);
	}
	
	public static void clearImage() {
		ImageSettings.imgdir = null;
		ImageSettings.image = null;
		ImageSettings.draw.set(false);
	}
	
	public static void setImage(File f) {
		ImageSettings.imgdir = f.getAbsolutePath();
		loadImage(f);
	}
	
	public static void setImageDir(String imgdir) {
		ImageSettings.imgdir = imgdir;
		loadImage();
	}
	
	public static void setBrightness(double brightness) {
		ImageSettings.brightness = brightness;
	}
	
	public static void setDraw(boolean draw) {
		ImageSettings.draw.set(draw);
		if (image == null) {
			ImageSettings.draw.set(false);
		}
	}
	
	public static void setStretch(boolean stretch) {
		ImageSettings.stretch.set(stretch);
	}
	
	public static Image image() {
		return image;
	}
	
	public static String imageDir() {
		return imgdir;
	}
	
	public static double brightness() {
		return brightness;
	}
	
	public static BooleanProperty draw() {
		return draw;
	}
	
	public static BooleanProperty stretch() {
		return stretch;
	}
	
	public static int size() {
		return size;
	}
	
	public static String string() {
		String s = imgdir + '\n';
		s += brightness + "\n";
		s += ((Boolean) draw.get()).toString() + '\n';
		s += ((Boolean) stretch.get()).toString() + '\n';
		return s;
	}
}
