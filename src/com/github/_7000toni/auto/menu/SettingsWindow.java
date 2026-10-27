package com.github._7000toni.auto.menu;

import com.github._7000toni.auto.settings.ColourSettings;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SettingsWindow {
	private static double[] xP = new double[4];
	private static double[] yP = new double[4];
	
	public static void drawIcon(GraphicsContext gc, double centerX, double centerY, double radius) {
		double[] xP = new double[5];
		double[] yP = new double[5];
		
		double off = -Math.PI/10;
		for (int i = 0; i < 5; i++) {
			xP[i] = centerX + radius*Math.cos(off+(i*2*Math.PI)/5);
			yP[i] = centerY + radius*Math.sin(off+(i*2*Math.PI)/5);
		}
		
		gc.setFill(Color.GRAY);
		gc.fillOval(centerX - radius, centerY - radius, radius*2, radius*2);
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MENU_BACKGROUND));
		gc.fillOval(centerX - radius/2, centerY - radius/2, radius, radius);
				
		double rotation = 2*Math.PI/5;
		for (int i = 0; i < 5; i++) {			
			drawTooth(gc, rotation, 70, 40, xP[4], yP[4]+10, centerX, centerY);
			rotation += 2*Math.PI/5;
		}
	}
	
	private static void drawTooth(GraphicsContext gc, double rotation, double width, double height, double topToothX, double topToothY, double centerX, double centerY) {
		initTopTooth(width, height, topToothX, topToothY);
		double[] x = new double[4];
		double[] y = new double[4];
		for (int i = 0; i < 4; i++) {
			x[i] = centerX + (xP[i] - centerX)*Math.cos(rotation) - (yP[i] - centerY)*Math.sin(rotation);
			y[i] = centerY + (xP[i] - centerX)*Math.sin(rotation) + (yP[i] - centerY)*Math.cos(rotation);
		}
		gc.setFill(Color.GRAY);
		gc.fillPolygon(x, y, 4);
	}
	
	private static void initTopTooth(double width, double height, double topToothX, double topToothY) {
		double topWidth = width * 3/4;
		xP[0] = topToothX - width/2;
		xP[1] = xP[0] + (width - topWidth) / 2;
		xP[2] = xP[1] + topWidth;
		xP[3] = xP[0] + width;
		
		yP[0] = topToothY;
		yP[1] = topToothY - height;
		yP[2] = topToothY - height;
		yP[3] = topToothY;
	}
 }
