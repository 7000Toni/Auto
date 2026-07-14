package com.github._7000toni.auto.canvasnode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.chart.Chart;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TextBox extends CanvasNode {
	protected String text;
	protected double textXOffset;
	protected double textYOffset;
	protected IVanGogh vg = null;
	protected InputType type;
	protected boolean dynamicSize;
	
	public enum InputType {
		ANY(0),		
		INT(1),
		DOUBLE(2);
		
		public final int index;
		
		private InputType(int index) {
			this.index = index;
		}
	}
	
	public TextBox(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset, InputType type, boolean dynamicSize) {
		constructorStuff(gc, width, height, x, y, text, textXOffset, textYOffset, type, dynamicSize);
	}
	
	public TextBox(GraphicsContext gc, double width, double height, double x, double y, String text, InputType type, boolean dynamicSize) {
		constructorStuff(gc, width, height, x, y, text, 0, 0, type, dynamicSize);
	}
	
	public TextBox(GraphicsContext gc, double width, double height, double x, double y, InputType type, boolean dynamicSize) {
		constructorStuff(gc, width, height, x, y, null, 0, 0, type, dynamicSize);
	}
	
	private void constructorStuff(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset, InputType type, boolean dynamicSize) {
		this.gc = gc;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		if (text != null) {
			this.text = text;
		} else {
			this.text = "";
		}
		this.textXOffset = textXOffset;
		this.textYOffset = textYOffset;
		this.type = type;
		this.dynamicSize = dynamicSize;
		setOnKeyPressed(e -> defaultOnKeyPressed(e));
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
	
	public void setColoursRect() {
		if (Chart.darkMode().get()) {
			gc.setFill(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
		}		
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
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.BLACK);
		}		
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
	
	public void defaultDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height - 5));
		calculateOffsets(gc.getFont());
		setColoursRect();
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
		setColoursText();
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.setFont(oldFont);
	}
	
	public void alternateDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height - 5));
		calculateOffsets(gc.getFont());
		setColoursAlt();
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - 5);
		gc.setFont(oldFont);
	}
	
	public void defaultDraw(Font font) {
		calculateOffsets(font);
		setColoursRect();
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
		setColoursText();
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 5);
	}
	
	public void alternateDraw(Font font) {
		calculateOffsets(font);
		setColoursAlt();
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
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
	
	public void defaultOnKeyPressed(KeyEvent e) {
		keyCode = e.getCode();
		if (keyCode.isLetterKey() || keyCode.isDigitKey() || keyCode.isKeypadKey() || keyCode.isWhitespaceKey()) {
			text += keyCode.getChar();
			System.out.println(text);
		}
	}
}
