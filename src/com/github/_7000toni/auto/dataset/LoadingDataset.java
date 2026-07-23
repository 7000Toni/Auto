package com.github._7000toni.auto.dataset;
import java.io.File;

import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalScrollBar;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.dataset.reader.ITickDataFileReader;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.settings.ColourSettings;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoadingDataset {
	private String signature;
	private double y;
	private IntegerProperty progress = new SimpleIntegerProperty(0);
	private IntegerProperty addIndex = new SimpleIntegerProperty(0);
	private boolean validFullSignature;
	private String name;
	private static double opacity = 1;
	private static boolean add = false;
	private static AnimationTimer anim = null;
	private static int loadingSets = 0;
	
	public LoadingDataset(double y, int addIndex, String signature) {
		this.y = y;
		this.addIndex.set(addIndex);
		this.signature = signature;
		validFullSignature = Signature.validFull(signature);	
		if (validFullSignature) {
			name = signature.substring(signature.indexOf(' ') + 1);
			name = name.substring(0, name.indexOf(' ') + 1);
		} else {
			name = signature.substring(0, signature.indexOf(' '));
		}			
	}
	
	public Dataset load(File file, ITickDataFileReader reader) {		
		Dataset data = new Dataset(file, reader, progress);
		if (data.failed()) {
			return null;
		} else {
			return data;
		}
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setAddIndex(int i) {
		addIndex.set(i);
	}
	
	public ReadOnlyIntegerProperty addIndex() {
		return IntegerProperty.readOnlyIntegerProperty(addIndex);
	}
	
	public double y() {
		return y;
	}
	
	public String signature() {
		return signature;
	}
	
	public ReadOnlyIntegerProperty progress() {
		return ReadOnlyIntegerProperty.readOnlyIntegerProperty(progress);
	}
	
	public boolean validFullSignature() {
		return validFullSignature;
	}
	
	public String name() {
		return name;
	}
	
	public void draw() {
		initAnm();
		GraphicsContext gc = Menu.menu().graphicsContext();
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2).deriveColor(0, 1, 1, opacity));
		gc.fillRoundRect(120, y, 510, 48, CanvasButton.ARC_W, CanvasButton.ARC_H);
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, 20));
		gc.setFill(Chart.darkMode().get()?Color.rgb(0, 0, 0, opacity):Color.rgb(255, 255, 255, opacity));
		gc.fillText("LOADING: " + name + " " + progress().get(), 123, y + 45, 500);
		gc.setFont(oldFont);
	}
	
	private static void drawMenu() {
		Menu m = Menu.menu();
		if (m != null) {				
			m.draw();
		}
	}
	
	private static void initAnm() {
		if (anim != null) {
			return;
		}
		anim = new AnimationTimer() {			
			long lastDraw = -1;		
			@Override
			public void handle(long now) {
				if (lastDraw == -1) {
					lastDraw = now;	
					return;
				}
				if (loadingSets < 1) {
					stop();
					anim = null;
				}
				long diff = (now - lastDraw) / HorizontalScrollBar.NANO_TO_MILLI;
				if (diff >= 20) {
					opacity += 0.02*(add?1:-1);
					if (opacity > 1) {
						opacity = 1;
						add = false;
					} else if (opacity < 0) {
						opacity = 0;
						add = true;
					}					
					drawMenu();
					lastDraw = now;
				}
			}
		};
		anim.start();
	}
	
	public static int loadingSets() {
		return loadingSets;
	}
	
	public static void setLoadingSets(int loadingSets) {
		LoadingDataset.loadingSets = loadingSets<0?0:loadingSets;
	}
	
	public static void incrementLoadingSets() {
		loadingSets++;
	}
	
	public static void decrementLoadingSets() {
		loadingSets = loadingSets-1<0?0:loadingSets-1;
	}
}
