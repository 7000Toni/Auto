package com.github._7000toni.auto.canvasnode;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ColourPicker extends CanvasNode implements IScrollBarOwner {
	private ColourPickerScrollBar hsb; 
	private ColourPickerUniversalScrollBar usb;
	private Color[][] colours;
	private boolean coloursInitialized;
	private ChartMenu chartMenu;
	
	public ColourPicker(double x, double y, double width, double height, GraphicsContext gc, ChartMenu chartMenu) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chartMenu = chartMenu;
		hsb = new ColourPickerScrollBar(this, x - 5, x + 295, 15, 15, y + 150);
		usb = new ColourPickerUniversalScrollBar(this, x + width/2 - 5, x + 295, y - 5, y + 150, 15, 15, x + 295, y - 5);
		colours = new Color[143][143];
		initializeColours();
	}
	
	private void initializeColours() {
		Color c = ColourCalculator.colour(hsb.x(), hsb.minPos(), hsb.maxPos() - hsb.sbWidth());
		int r = (int)(c.getRed() * 255);
		int g = (int)(c.getGreen() * 255);
		int b = (int)(c.getBlue() * 255);
		
		double hsbPerc = (hsb.x() - hsb.minPos()) / (hsb.maxPos() - hsb.sbWidth() - hsb.minPos());
		for (double i = x + 146; i < x + 289; i++) {
			double percx = (142 - (i - x - 146)) / 142;			
			for (double j = y + 1; j < y + 144; j++) {
				double percy = (142 - (j - y - 1)) / 142;
				int r2;
				int g2;
				int b2;
				if (hsbPerc > 5.0/6 || hsbPerc < 1.0/6) {
					r2 = (int) (percy * r);
					g2 = (int) (((percx * (255 - g)) + g) * percy);
					b2 = (int) (((percx * (255 - b)) + b) * percy);
				} else if (hsbPerc > 1.0/6 && hsbPerc < 1.0/2) {
					r2 = (int) (((percx * (255 - r)) + r) * percy);
					g2 = (int) (percy * g);
					b2 = (int) (((percx * (255 - b)) + b) * percy);
				} else {
					r2 = (int) (((percx * (255 - r)) + r) * percy);
					g2 = (int) (((percx * (255 - g)) + g) * percy);
					b2 = (int) (percy * b);
				}				
				colours[(int)(i - x - 146)][(int)(j - y - 1)] = Color.web("rgb(" + r2 + "," + g2 + "," +  b2 + ")");
				gc.getPixelWriter().setColor((int)i, (int)j, Color.web("rgb(" + r2 + "," + g2 + "," +  b2 + ")"));
			}
		}
		coloursInitialized = true;
	}
	
	public void unintializeColours() {
		coloursInitialized = false;
	}
	
	private void fillBrightnessSquare() {
		if (coloursInitialized) {
			for (double i = x + 146; i < x + 289; i++) {
				for (double j = y + 1; j < y + 144; j++) {			
					gc.getPixelWriter().setColor((int)i, (int)j, colours[(int)(i - x - 146)][(int)(j - y - 1)]);
				}
			}
		} else {
			initializeColours();
		}
	}
	
	public Color[][] colours() {
		return colours;
	}
	
	public ColourPickerScrollBar hsb() {
		return hsb;
	}
	
	public ColourPickerUniversalScrollBar usb() {
		return usb;
	}		
	
	public ChartMenu chartMenu() {
		return chartMenu;
	}
	
	public Color finalColour() {
		int r = (int)(142 * (usb.x() - usb.minXPos())/(usb.maxXPos() - usb.minXPos() - usb.sbWidth()));
		int c = (int)(142 * (usb.y() - usb.minYPos())/(usb.maxYPos() - usb.minYPos() - usb.sbHeight()));
		return colours[r][c];
	}
	
	@Override
	public void draw() {
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}			
		
		gc.strokeRect(x, y, width, 145);
		gc.strokeLine(x + 145, y, x + 145, y + 145);		
		fillBrightnessSquare();
		gc.setFill(finalColour());
		gc.fillRect(x + 1, y + 1, width/2 - 2, width / 2 - 2);
		hsb.draw();
		usb.draw();
	}

	@Override
	public void setX(double x) {
		double hsbOffset = hsb.x() - this.x;
		double usbXOffset = usb.x() - this.x;		
		this.x = x;		
		hsb.setMinPos(x - 5);
		hsb.setMaxPos(x + 295);
		hsb.setX(hsbOffset + x);
		usb.setMinXPos(x + width/2 - 5);
		usb.setMaxXPos(x + 295);
		usb.setX(usbXOffset + x);
	}

	@Override
	public void setY(double y) {
		double usbYOffset = usb.y() - this.y;
		this.y = y;		
		hsb.setY(y + 150);
		usb.setMinYPos(y - 5);
		usb.setMaxYPos(y + 150);
		usb.setY(usbYOffset + y);
	}
}
