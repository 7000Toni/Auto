package com.github._7000toni.auto.canvasnode.button;
import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.chart.Chart;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CanvasButton extends CanvasLabel {
	protected boolean on = false;
	
	public CanvasButton(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset) {
		super(gc, width, height, x, y, text, textXOffset, textYOffset);
	}
	
	public CanvasButton(GraphicsContext gc, double width, double height, double x, double y, String text) {
		super(gc, width, height, x, y, text);
		this.enabled = true;
	}	
	
	public boolean on() {
		return on;
	}
	
	public void toggleOn() {
		on = !on;
	}
	
	public void setOn(boolean on) {
		this.on = on;
	}
	
	public void disable() {
		enabled = false;
		pressed = false;
		hover = false;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public boolean enabled() {
		return this.enabled;
	}
	
	@Override
	public void defaultDraw() {
		double oldFontSize = gc.getFont().getSize();
		gc.setFont(new Font(height - 5));
		calculateOffsets(gc.getFont());
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}		
		if (hover) {
			gc.setStroke(Color.GRAY);
			gc.setFill(Color.GRAY);
		}
		if (pressed) {
			gc.setStroke(Color.DIMGRAY);
			gc.setFill(Color.DIMGRAY);
		}
		if (!enabled) {
			gc.setStroke(Color.LIGHTGRAY);
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.strokeRect(x, y, width, height);
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.setFont(new Font(oldFontSize));
	}
	
	@Override
	public void alternateDraw() {
		double oldFontSize = gc.getFont().getSize();
		gc.setFont(new Font(height - 5));
		calculateOffsets(gc.getFont());
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}		
		if (hover) {
			gc.setStroke(Color.GRAY);
			gc.setFill(Color.GRAY);
		}
		if (pressed) {
			gc.setStroke(Color.DIMGRAY);
			gc.setFill(Color.DIMGRAY);
		}
		if (!enabled) {
			gc.setStroke(Color.LIGHTGRAY);
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.strokeRect(x, y, width, height);
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.setFont(new Font(oldFontSize));
	}
	
	@Override
	public void defaultDraw(Font font) {
		calculateOffsets(font);
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}		
		if (hover) {
			gc.setStroke(Color.GRAY);
			gc.setFill(Color.GRAY);
		}
		if (pressed) {
			gc.setStroke(Color.DIMGRAY);
			gc.setFill(Color.DIMGRAY);
		}
		if (!enabled) {
			gc.setStroke(Color.LIGHTGRAY);
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.strokeRect(x, y, width, height);
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 5);
	}
	
	@Override
	public void alternateDraw(Font font) {
		calculateOffsets(font);
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}		
		if (hover) {
			gc.setStroke(Color.GRAY);
			gc.setFill(Color.GRAY);
		}
		if (pressed) {
			gc.setStroke(Color.DIMGRAY);
			gc.setFill(Color.DIMGRAY);
		}
		if (!enabled) {
			gc.setStroke(Color.LIGHTGRAY);
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.strokeRect(x, y, width, height);
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - 5);
	}
	
	@Override
	public void draw() {
		if (vg == null) {
			defaultDraw();
		} else {
			vg.draw(x, y, gc);
		}
	}
}
