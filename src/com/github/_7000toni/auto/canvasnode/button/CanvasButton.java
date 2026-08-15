package com.github._7000toni.auto.canvasnode.button;
import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.MiscellaneousSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
	
	public void setColoursRect() {
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));	
		if (hover) {
			gc.setFill(Color.GRAY);
		}
		if (pressed) {
			gc.setFill(Color.DIMGRAY);
		}
		if (!enabled) {
			gc.setFill(Color.LIGHTGRAY);
		}
	}
	
	public void setColoursText() {
		if (Chart.darkMode().get()) {
			gc.setFill(Color.BLACK);
		} else {
			gc.setFill(Color.WHITE);
		}		
		if (hover) {
			gc.setFill(Color.WHITE);
		}
		if (pressed) {
			gc.setFill(Color.BLACK);
		}
		if (!enabled) {
			gc.setFill(Color.DIMGRAY);
		}
	}
	
	public void setColoursAlt() {
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		if (hover) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.GRAY);
		}
		if (pressed) {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.DIMGRAY);
		}
		if (!enabled) {
			gc.setStroke(Color.DIMGRAY);
			gc.setFill(Color.LIGHTGRAY);
		}
	}
	
	@Override
	public void defaultDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height - 5));
		calculateOffsets(gc.getFont());
		setColoursRect();
		gc.fillRoundRect(x, y, width, height, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		setColoursText();
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.setFont(oldFont);
	}
	
	@Override
	public void alternateDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height - 5));
		calculateOffsets(gc.getFont());
		setColoursAlt();
		gc.fillRoundRect(x, y, width, height, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.setFont(oldFont);
	}
	
	@Override
	public void defaultDraw(Font font) {
		calculateOffsets(font);
		setColoursRect();
		gc.fillRoundRect(x, y, width, height, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		setColoursText();
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 5);
	}
	
	@Override
	public void alternateDraw(Font font) {
		calculateOffsets(font);
		setColoursAlt();
		gc.fillRoundRect(x, y, width, height, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
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
