package com.github._7000toni.auto.canvasnode;
import com.github._7000toni.auto.canvasnode.scrollbar.ColourPickerScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.ColourPickerUniversalScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenu;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ColourPicker extends CanvasNode implements IScrollBarOwner {
	private ColourPickerScrollBar hsb; 
	private ColourPickerUniversalScrollBar usb;
	private Color[][] colours;
	private boolean coloursInitialized;
	private ChartMenu chartMenu;
	private Color finalColour;
	
	public ColourPicker(double x, double y, double width, double height, GraphicsContext gc, ChartMenu chartMenu) {
		this.x.set(x);
		this.y.set(y);
		this.width.set(width);
		this.height.set(height);
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
		for (double i = x.get() + 146; i < x.get() + 289; i++) {
			double percx = (142 - (i - x.get() - 146)) / 142;			
			for (double j = y.get() + 1; j < y.get() + 144; j++) {
				double percy = (142 - (j - y.get() - 1)) / 142;
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
				colours[(int)(i - x.get() - 146)][(int)(j - y.get() - 1)] = Color.web("rgb(" + r2 + "," + g2 + "," +  b2 + ")");
				gc.getPixelWriter().setColor((int)i, (int)j, Color.web("rgb(" + r2 + "," + g2 + "," +  b2 + ")"));
			}
		}
		calculateFinalColour();
		coloursInitialized = true;
	}
	
	public void unintializeColours() {
		coloursInitialized = false;
	}
	
	private void fillColourSquare() {
		if (coloursInitialized) {
			for (double i = x.get() + 146; i < x.get() + 289; i++) {
				for (double j = y.get() + 1; j < y.get() + 144; j++) {			
					gc.getPixelWriter().setColor((int)i, (int)j, colours[(int)(i - x.get() - 146)][(int)(j - y.get() - 1)]);
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
	
	public void calculateFinalColour() {
		int r = (int)(142 * (usb.x() - usb.minXPos())/(usb.maxXPos() - usb.minXPos() - usb.sbWidth()));
		int c = (int)(142 * (usb.y() - usb.minYPos())/(usb.maxYPos() - usb.minYPos() - usb.sbHeight()));
		finalColour = colours[r][c];
	}
	
	public Color finalColour() {
		return finalColour;
	}
	
	public void setFinalColour(Color colour) {
		finalColour = colour;
	}
	
	@Override
	public void draw() {
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}			
			
		fillColourSquare();
		gc.setFill(finalColour);
		gc.fillRect(x.get() + 1, y.get() + 1, width.get()/2 - 2, width.get() / 2 - 2);
		if (finalColour.equals(ColourSettings.colour(ColourIndex.CHART_BACKGROUND))) {
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
				gc.strokeRect(x.get() + 1.5, y.get() + 1.5, width.get()/2 - 3, width.get() / 2 - 3);
			} else {
				gc.setStroke(Color.BLACK);
				gc.strokeRect(x.get() + 1.5, y.get() + 1.5, width.get()/2 - 3, width.get() / 2 - 3);
			}
		}
		hsb.draw();
		usb.draw();
	}

	@Override
	public void setX(double x) {
		double hsbOffset = hsb.x() - this.x.get();
		double usbXOffset = usb.x() - this.x.get();		
		this.x.set(x);		
		hsb.setMinPos(x - 5);
		hsb.setMaxPos(x + 295);
		hsb.setX(hsbOffset + x);
		usb.setMinXPos(x + width.get()/2 - 5);
		usb.setMaxXPos(x + 295);
		usb.setX(usbXOffset + x);
	}

	@Override
	public void setY(double y) {
		double usbYOffset = usb.y() - this.y.get();
		this.y.set(y);		
		hsb.setY(y + 150);
		usb.setMinYPos(y - 5);
		usb.setMaxYPos(y + 150);
		usb.setY(usbYOffset + y);
	}
}
