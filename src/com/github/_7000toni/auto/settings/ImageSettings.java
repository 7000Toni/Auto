package com.github._7000toni.auto.settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.github._7000toni.auto.chart.Chart;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;

public class ImageSettings {
	private static Image dmImage = null;
	private static String dmImgDir = null;
	private static double dmBrightness = 0;
	private static BooleanProperty dmDraw = new SimpleBooleanProperty(false);
	private static BooleanProperty dmStretch = new SimpleBooleanProperty(false);
	private static Image lmImage = null;
	private static String lmImgDir = null;
	private static double lmBrightness = 0;
	private static BooleanProperty lmDraw = new SimpleBooleanProperty(false);
	private static BooleanProperty lmStretch = new SimpleBooleanProperty(false);
	private static final int size = 4; 
	
	private static void loadImage() {
		File f;
		if (dmImgDir == null) {
			dmImage = null;
			dmDraw.set(false);
		} else {
			f = new File(dmImgDir);
			if (f.exists()) {
				try {
					dmImage = new Image(new FileInputStream(f));
				} catch (FileNotFoundException e) {
					dmImage = null;
					setSettings(null, 0, false, false, null, 0, false, false);
					Settings.saveSettings();
					e.printStackTrace();
				}
			} else {
				dmDraw.set(false);
			}
		}
		
		if (lmImgDir == null) {
			lmImage = null;
			lmDraw.set(false);
		} else {
			f = new File(lmImgDir);
			if (f.exists()) {
				try {
					lmImage = new Image(new FileInputStream(f));
				} catch (FileNotFoundException e) {
					lmImage = null;
					setSettings(null, 0, false, false, null, 0, false, false);
					Settings.saveSettings();
					e.printStackTrace();
				}
			} else {
				lmDraw.set(false);
			}
		}
	}
	
	private static void loadImage(File file) {
		try {
			if (Chart.darkMode().get()) {
				dmImage = new Image(new FileInputStream(file));
			} else {
				lmImage = new Image(new FileInputStream(file));
			}
		} catch (FileNotFoundException e) {
			if (Chart.darkMode().get()) {
				dmImage = null;
			} else {
				lmImage = null;
			}
			setSettings(null, 0, false, false, null, 0, false, false);
			Settings.saveSettings();
			e.printStackTrace();
		}
	}
	
	public static void setSettings(String dmImgDir, double dmBrightness, boolean dmDraw, boolean dmStretch, String lmImgDir, double lmBrightness, boolean lmDraw, boolean lmStretch) {
		if (dmImgDir == null && lmImgDir == null) {
			return;
		}
		ImageSettings.dmImgDir = dmImgDir;
		ImageSettings.dmBrightness = dmBrightness;
		ImageSettings.dmDraw.set(dmDraw);
		ImageSettings.dmStretch.set(dmStretch);	
		
		ImageSettings.lmImgDir = lmImgDir;
		ImageSettings.lmBrightness = lmBrightness;
		ImageSettings.lmDraw.set(lmDraw);
		ImageSettings.lmStretch.set(lmStretch);	
		loadImage();
	}
	
	public static void setDefaultSettings() {
		setSettings(null, 0, false, false, null, 0, false, false);
	}
	
	public static void clearImage() {
		if (Chart.darkMode().get()) {
			ImageSettings.dmImage = null;
			ImageSettings.dmImgDir = null;
			ImageSettings.dmDraw.set(false);
		} else {
			ImageSettings.lmImage = null;
			ImageSettings.lmImgDir = null;
			ImageSettings.lmDraw.set(false);
		}		
	}
	
	public static void setImage(File f) {
		if (Chart.darkMode().get()) {
			ImageSettings.dmImgDir = f.getAbsolutePath();
		} else {
			ImageSettings.lmImgDir = f.getAbsolutePath();
		}
		loadImage(f);
	}
	
	public static void setImageDir(String imgDir) {
		if (Chart.darkMode().get()) {
			ImageSettings.dmImgDir = imgDir;
		} else {
			ImageSettings.lmImgDir = imgDir;
		}		
		loadImage(new File(imgDir));
	}
	
	public static void setBrightness(double brightness) {
		if (Chart.darkMode().get()) {
			ImageSettings.dmBrightness = brightness;
		} else {
			ImageSettings.lmBrightness = brightness;
		}
	}
	
	public static void setDraw(boolean draw) {
		if (Chart.darkMode().get()) {
			ImageSettings.dmDraw.set(draw);
			if (dmImage == null) {
				ImageSettings.dmDraw.set(false);
			}
		} else {
			ImageSettings.lmDraw.set(draw);
			if (lmImage == null) {
				ImageSettings.lmDraw.set(false);
			}
		}		
	}
	
	public static void setStretch(boolean stretch) {
		if (Chart.darkMode().get()) {
			ImageSettings.dmStretch.set(stretch);
		} else {
			ImageSettings.lmStretch.set(stretch);
		}
	}
	
	public static Image image() {
		if (Chart.darkMode().get()) {
			return ImageSettings.dmImage;
		} else {
			return ImageSettings.lmImage;
		}
	}
	
	public static String imageDir() {
		if (Chart.darkMode().get()) {
			return dmImgDir;
		} else {
			return lmImgDir;
		}
	}
	
	public static double brightness() {
		if (Chart.darkMode().get()) {
			return dmBrightness;
		} else {
			return lmBrightness;
		}
	}
	
	public static BooleanProperty draw() {
		if (Chart.darkMode().get()) {
			return dmDraw;
		} else {
			return lmDraw;
		}
	}
	
	public static BooleanProperty stretch() {
		if (Chart.darkMode().get()) {
			return dmStretch;
		} else {
			return lmStretch;
		}
	}
	
	public static int size() {
		return size;
	}
	
	public static String string() {
		String s = dmImgDir + "\n";
		s += dmBrightness + "\n";
		s += ((Boolean) dmDraw.get()).toString() + "\n";
		s += ((Boolean) dmStretch.get()).toString() + "\n";
		s += lmImgDir + "\n";		
		s += lmBrightness + "\n";		
		s += ((Boolean) lmDraw.get()).toString() + "\n";		
		s += ((Boolean) lmStretch.get()).toString();
		return s;
	}
}
