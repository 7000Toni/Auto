package com.github._7000toni.auto.canvasnode;
import com.github._7000toni.auto.chart.Chart;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CanvasLabel extends CanvasNode {
	protected String text;
	protected double textXOffset;
	protected double textYOffset;
	protected IVanGogh vg = null;
	
	public CanvasLabel(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset) {
		this.gc = gc;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.text = text;
		this.textXOffset = textXOffset;
		this.textYOffset = textYOffset;
	}
	
	public CanvasLabel(GraphicsContext gc, double width, double height, double x, double y, String text) {
		this.gc = gc;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.text = text;
	}
	
	public void calculateOffsets(Font font) {
		Text t = new Text(text);
		t.setFont(font);
		textXOffset = (width - t.getLayoutBounds().getWidth()) / 2;
		textYOffset = font.getSize() + (height - t.getLayoutBounds().getHeight()) / 2; 
	}
	
	public double textXOffset() {
		return textXOffset;
	}
	
	public double textYOffset() {
		return textYOffset;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setTextXOffset(double textXOffset) {
		this.textXOffset = textXOffset;
	}
	
	public void setTextYOffset(double textYOffset) {
		this.textYOffset = textYOffset;
	}
	
	public void setVanGogh(IVanGogh vg) {
		this.vg = vg;
	}
	
	public String text() {
		return this.text;
	}
	
	public void defaultDraw() {
		double oldFontSize = gc.getFont().getSize();
		gc.setFont(new Font(height - 5));
		calculateOffsets(new Font(height - 5));
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}	
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.strokeLine(x, y+height/2, x+textXOffset-5, y+height/2);
		gc.strokeLine(x+width-textXOffset+5, y+height/2, x+width, y+height/2);
		gc.setFont(new Font(oldFontSize));
	}
	
	public void alternateDraw() {
		double oldFontSize = gc.getFont().getSize();
		gc.setFont(new Font(height - 5));
		calculateOffsets(new Font(height - 5));
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}	
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.strokeLine(x, y+height/2, x+textXOffset-5, y+height/2);
		gc.strokeLine(x+width-textXOffset+5, y+height/2, x+width, y+height/2);
		gc.setFont(new Font(oldFontSize));
	}
	
	public void defaultDraw(Font font) {
		calculateOffsets(font);
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}	
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.strokeLine(x, y+height/2, x+textXOffset-5, y+height/2);
		gc.strokeLine(x+width-textXOffset+5, y+height/2, x+width, y+height/2);
	}
	
	public void alternateDraw(Font font) {
		calculateOffsets(font);
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}	
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.strokeLine(x, y+height/2, x+textXOffset-5, y+height/2);
		gc.strokeLine(x+width-textXOffset+5, y+height/2, x+width, y+height/2);
	}
	
	public void draw() {
		if (vg == null) {
			defaultDraw();
		} else {
			vg.draw(x, y, gc);
		}
	}
}
