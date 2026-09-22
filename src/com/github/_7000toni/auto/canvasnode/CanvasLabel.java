package com.github._7000toni.auto.canvasnode;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CanvasLabel extends CanvasNode {
	protected String text;
	protected double textXOffset;
	protected double textYOffset;
	protected IVanGogh vg = null;
	
	public CanvasLabel(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset) {
		this.gc = gc;
		this.width.set(width);
		this.height.set(height);
		this.x.set(x);
		this.y.set(y);
		this.text = text;
		this.textXOffset = textXOffset;
		this.textYOffset = textYOffset;
	}
	
	public CanvasLabel(GraphicsContext gc, double width, double height, double x, double y, String text) {
		this.gc = gc;
		this.width.set(width);
		this.height.set(height);
		this.x.set(x);
		this.y.set(y);
		this.text = text;
	}
	
	public void calculateOffsets(Font font) {
		Text t = new Text(text);
		t.setFont(font);
		textXOffset = (width.get() - t.getLayoutBounds().getWidth()) / 2;
		textYOffset = font.getSize() + (height.get() - t.getLayoutBounds().getHeight()) / 2; 
		textXOffset = textXOffset<0?0:textXOffset;
		textYOffset = textYOffset<0?0:textYOffset;
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
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height.get() - 5));
		calculateOffsets(new Font(height.get() - 5));
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.fillText(text, x.get() + textXOffset, y.get() + textYOffset, width.get() - 5);
		gc.strokeLine(x.get(), y.get()+height.get()/2+0.5, x.get()+textXOffset-5, y.get()+height.get()/2+0.5);
		gc.strokeLine(x.get()+width.get()-textXOffset+5, y.get()+height.get()/2+0.5, x.get()+width.get(), y.get()+height.get()/2+0.5);
		gc.setFont(oldFont);
	}
	
	public void alternateDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height.get() - 5));
		calculateOffsets(new Font(height.get() - 5));
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.strokeText(text, x.get() + textXOffset, y.get() + textYOffset, width.get() - 5);
		gc.strokeLine(x.get(), y.get()+height.get()/2+0.5, x.get()+textXOffset-5, y.get()+height.get()/2+0.5);
		gc.strokeLine(x.get()+width.get()-textXOffset+5, y.get()+height.get()/2+0.5, x.get()+width.get(), y.get()+height.get()/2+0.5);
		gc.setFont(oldFont);
	}
	
	public void defaultDraw(Font font) {
		calculateOffsets(font);
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.fillText(text, x.get() + textXOffset, y.get() + textYOffset, width.get() - 5);
		gc.strokeLine(x.get(), y.get()+height.get()/2+0.5, x.get()+textXOffset-5, y.get()+height.get()/2+0.5);
		gc.strokeLine(x.get()+width.get()-textXOffset+5, y.get()+height.get()/2+0.5, x.get()+width.get(), y.get()+height.get()/2+0.5);
	}
	
	public void alternateDraw(Font font) {
		calculateOffsets(font);
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.strokeText(text, x.get() + textXOffset, y.get() + textYOffset, width.get() - 5);
		gc.strokeLine(x.get(), y.get()+height.get()/2+0.5, x.get()+textXOffset-5, y.get()+height.get()/2+0.5);
		gc.strokeLine(x.get()+width.get()-textXOffset+5, y.get()+height.get()/2+0.5, x.get()+width.get(), y.get()+height.get()/2+0.5);
	}
	
	public void simpleDefaultDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height.get() - 5));
		calculateOffsets(new Font(height.get() - 5));
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.fillText(text, x.get(), y.get() + textYOffset, width.get());
		gc.setFont(oldFont);
	}
	
	public void simpleAlternateDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height.get() - 5));
		calculateOffsets(new Font(height.get() - 5));
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.strokeText(text, x.get(), y.get() + textYOffset, width.get());
		gc.setFont(oldFont);
	}
	
	public void simpleDefaultDraw(Font font) {
		calculateOffsets(font);
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.fillText(text, x.get(), y.get() + textYOffset, width.get());
	}
	
	public void simpleAlternateDraw(Font font) {
		calculateOffsets(font);
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.strokeText(text, x.get(), y.get() + textYOffset, width.get());
	}
	
	public void draw() {
		if (vg == null) {
			defaultDraw();
		} else {
			vg.draw(x.get(), y.get(), gc);
		}
	}
}
