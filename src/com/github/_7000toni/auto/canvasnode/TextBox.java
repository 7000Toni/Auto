package com.github._7000toni.auto.canvasnode;

import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalScrollBar;
import com.github._7000toni.auto.chart.Chart;

import javafx.animation.AnimationTimer;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TextBox extends CanvasNode {
	protected Stage stage;
	protected String text;
	protected double textXOffset;
	protected double textYOffset;
	protected IVanGogh vg = null;
	protected InputType type;
	protected boolean dynamicSize;
	protected double mgn = 2;
	protected AnimationTimer cursorAnim;
	protected int cursorPos;
	protected boolean drawCursor = false;
	protected double roughCharWidth;
	
	public enum InputType {
		ANY(0),		
		INT(1),
		DOUBLE(2);
		
		public final int index;
		
		private InputType(int index) {
			this.index = index;
		}
	}
	
	public TextBox(Stage stage, GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset, InputType type, boolean dynamicSize) {
		constructorStuff(stage, gc, width, height, x, y, text, textXOffset, textYOffset, type, dynamicSize);
	}
	
	public TextBox(Stage stage, GraphicsContext gc, double width, double height, double x, double y, String text, InputType type, boolean dynamicSize) {
		constructorStuff(stage, gc, width, height, x, y, text, 0, 0, type, dynamicSize);
	}
	
	public TextBox(Stage stage, GraphicsContext gc, double width, double height, double x, double y, InputType type, boolean dynamicSize) {
		constructorStuff(stage, gc, width, height, x, y, null, 0, 0, type, dynamicSize);
	}
	
	private void constructorStuff(Stage stage, GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset, InputType type, boolean dynamicSize) {
		this.stage = stage;
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
		this.roughCharWidth = getTextWidth("5");
		setOnKeyTyped(e -> defaultOnKeyTyped(e));
		setOnKeyPressed(e -> defaultOnKeyPressed(e));
		setOnMousePressed(e -> defaultOnMousePressed(e));
		setOnMouseMoved(e -> defaultOnMouseMoved(e));
		setOnMouseExited(e -> defaultOnMouseExited(e));
	}
	
	public void calculateOffsets(Font font) {
		Text t = new Text(text);
		t.setFont(font);
		width = t.getLayoutBounds().getWidth() + mgn*2;
		width = width<20?20:width;
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
		if (!enabled) {
			gc.setStroke(Color.DIMGRAY);
			gc.setFill(Color.LIGHTGRAY);
		}
	}
	
	public void defaultDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height - mgn*2));
		calculateOffsets(gc.getFont());
		setColoursRect();
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
		setColoursText();
		gc.fillText(text, x + textXOffset, y + textYOffset, width - mgn*2);
		gc.setFont(oldFont);
	}
	
	public void alternateDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, height - mgn*2));
		calculateOffsets(gc.getFont());
		setColoursAlt();
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - mgn*2);
		gc.setFont(oldFont);
	}
	
	public void defaultDraw(Font font) {
		calculateOffsets(font);
		setColoursRect();
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
		setColoursText();
		gc.fillText(text, x + textXOffset, y + textYOffset, width - mgn*2);
	}
	
	public void alternateDraw(Font font) {
		calculateOffsets(font);
		setColoursAlt();
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - mgn*2);
	}
	
	public void drawCursor() {
		double midx = x + textXOffset;
		midx += getTextWidth(text.substring(0, cursorPos));	
		midx = (int)(midx)+0.5;
		gc.setStroke(Color.WHITE);
		gc.strokeLine(midx, y+mgn, midx, y+height-mgn);
	}
	
	@Override
	public void draw() {
		if (vg == null) {
			defaultDraw();
			if (drawCursor) {
				drawCursor();
			}
		} else {
			vg.draw(x, y, gc);
		}
	}
	
	public void defaultOnKeyPressed(KeyEvent e) {
		KeyCode keyCode = e.getCode();
		switch(keyCode) {
			case KeyCode.BACK_SPACE: 
				if (!text.isEmpty()) {	
					int l = text.length();
					String newText = text.substring(0, cursorPos-1<0?0:cursorPos-1);
					newText += text.substring(cursorPos, l);
					text = newText;
					cursorPos = cursorPos-1<0?0:cursorPos-1;
				}
				break;
			case KeyCode.LEFT:
				cursorPos = cursorPos-1<0?0:cursorPos-1;
				drawCursor = true;
				break;
			case KeyCode.RIGHT:
				int l = text.length();
				cursorPos = cursorPos+1>l?l:cursorPos+1;
				drawCursor = true;
				break;
			case KeyCode.HOME:
				cursorPos = 0;
				drawCursor = true;
				break;
			case KeyCode.END:
				cursorPos = text.length();
				drawCursor = true;
				break;
			default:
		}
	}
	
	public void defaultOnKeyTyped(KeyEvent e) {
		char c = e.getCharacter().charAt(0);
		if (type == InputType.ANY && c > 31 && c < 127|| (type == InputType.DOUBLE && (c == 46 && !text.contains(".") || c > 47 && c < 58)) || type == InputType.INT && c > 47 && c < 58) {
			int l = text.length();
			String newText = text.substring(0, cursorPos) + c;
			newText += text.substring(cursorPos, l);
			text = newText;
			cursorPos += 1;
		}
	}
	
	public void defaultOnMousePressed(MouseEvent e) {
		cursorPos = calculateCursorPos(e.getX());
	}
	
	public void defaultOnMouseMoved(MouseEvent e) {
		stage.getScene().setCursor(Cursor.TEXT);
	}
	
	public void defaultOnMouseExited(MouseEvent e) {
		stage.getScene().setCursor(Cursor.DEFAULT);
	}
	
	private int calculateCursorPos(double x) {
		double est = (x-this.x)/roughCharWidth;
		if ((est - (int)est) * 10 >= 5) {
			return (int)est + 1;
		} else {
			return (int)est;
		}
	}
	
	private double getTextWidth(String text) {
		Text t = new Text(text);
		t.setFont(Font.font(gc.getFont().getFamily(), FontWeight.EXTRA_BOLD, height - mgn*2));
		return t.getLayoutBounds().getWidth();
	}
	
	private void initCursorAnim() {
		cursorAnim = new AnimationTimer() {
			long lastDraw = -1;
			@Override
			public void handle(long now) {
				if (lastDraw == -1) {
					lastDraw = now;					
					return;
				}
				long diff = (now - lastDraw) / HorizontalScrollBar.NANO_TO_MILLI;
				if (diff >= 500) {
					drawCursor = !drawCursor;
					gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
					draw();
					lastDraw = now;
				}
			}
		};
		cursorAnim.start();
	}
	
	@Override
	public void setFocused(boolean focused) {		
		if (focused && !this.focused) {
			drawCursor = true;
			cursorPos = text.length();
			initCursorAnim();
		} else if (cursorAnim != null) {
			drawCursor = false;
			cursorAnim.stop();
		}
		this.focused = focused;
	}	
}
