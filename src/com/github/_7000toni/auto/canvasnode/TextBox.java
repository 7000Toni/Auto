package com.github._7000toni.auto.canvasnode;

import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalScrollBar;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;

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
	private Stage stage;
	private String text;
	private double textXOffset;
	private double textYOffset;
	private IVanGogh vg = null;
	private InputType type;
	private boolean dynamicSize;
	private double mgn = 2;
	private AnimationTimer cursorAnim;
	private int cursorPos;
	private boolean drawCursor = false;
	private double roughCharWidth;
	private boolean border;
	private boolean fillBorder;
	private Font font = null;
	
	public enum InputType {
		ANY(0),		
		INT(1),
		ABS_INT(2),
		DOUBLE(3),
		ABS_DOUBLE(4);
		
		public final int index;
		
		private InputType(int index) {
			this.index = index;
		}
	}
	
	public TextBox(Stage stage, GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset, InputType type, boolean dynamicSize, boolean border, boolean fillBorder) {
		constructorStuff(stage, gc, width, height, x, y, text, textXOffset, textYOffset, type, dynamicSize, border, fillBorder);
	}
	
	public TextBox(Stage stage, GraphicsContext gc, double width, double height, double x, double y, String text, InputType type, boolean dynamicSize, boolean border, boolean fillBorder) {
		constructorStuff(stage, gc, width, height, x, y, text, 0, 0, type, dynamicSize, border, fillBorder);
	}
	
	public TextBox(Stage stage, GraphicsContext gc, double width, double height, double x, double y, InputType type, boolean dynamicSize, boolean border, boolean fillBorder) {
		constructorStuff(stage, gc, width, height, x, y, null, 0, 0, type, dynamicSize, border, fillBorder);
	}
	
	private void constructorStuff(Stage stage, GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset, InputType type, boolean dynamicSize, boolean border, boolean fillBorder) {
		this.stage = stage;
		this.gc = gc;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		if (text != null && ((type == InputType.DOUBLE || type == InputType.ABS_DOUBLE) && validateDouble(text)) ||
				((type == InputType.INT || type == InputType.ABS_INT) && validateInt(text)) || type == InputType.ANY) {
			this.text = text;
		} else {
			this.text = "";
		}
		this.textXOffset = textXOffset;
		this.textYOffset = textYOffset;
		this.type = type;
		this.dynamicSize = dynamicSize;
		this.border = border;
		this.fillBorder = border?fillBorder:false;
		this.roughCharWidth = getTextWidth("5");		
	}
		
	private boolean validateInt(String text) {
		if (text.contains("-") && type == InputType.ABS_INT) {
			return false;
		}
		try {			
			Integer.parseInt(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean validateDouble(String text) {	
		if (text.contains("-") && type == InputType.ABS_DOUBLE) {
			return false;
		}
		try {
			Double.parseDouble(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void setFont(Font font) {
		this.font = font;
		this.roughCharWidth = getTextWidth("5");
	}
	
	public void calculateOffsets(Font font) {
		Text t = new Text(text);
		t.setFont(font);
		if (dynamicSize) {
			width = t.getLayoutBounds().getWidth() + mgn*2;
		}
		width = width<20?20:width;
		textXOffset = (width - t.getLayoutBounds().getWidth()) / 2;
		textXOffset = textXOffset<mgn?mgn:textXOffset;
		textYOffset = font.getSize() + (height - t.getLayoutBounds().getHeight()) / 2; 		
	}
	
	public double textXOffset() {
		return textXOffset;
	}
	
	public double textYOffset() {
		return textYOffset;
	}
	
	public void setText(String text) {
		switch (type) {
			case ANY:
				this.text = text;
				break;
			case DOUBLE:
			case ABS_DOUBLE:
				if (validateDouble(text)) {
					this.text = text;
				}
				break;
			case INT:
			case ABS_INT:
				if (validateInt(text)) {
					this.text = text;
				}
				break;
			default:
		}
		setCursorPos(this.text.length());
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
		if (!fillBorder) {
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}
		}
		if (!border) {
			gc.setFill(ColourSettings.colour(ColourIndex.CHART_BACKGROUND));			
		} else if (Chart.darkMode().get()) {
			gc.setFill(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
		}
		if (!enabled) {
			gc.setFill(Color.LIGHTGRAY);
		}
	}
	
	public void setColoursText() {
		boolean dm = Chart.darkMode().get(); 
		if (!dm && !border || !dm && !fillBorder || dm && border && fillBorder) {
			gc.setFill(Color.BLACK);
		} else {
			gc.setFill(Color.WHITE);
		}
		if (!enabled) {
			gc.setFill(Color.DIMGRAY);
		}
	}
	
	public void setColoursAlt() {
		if (!border) {
			gc.setFill(ColourSettings.colour(ColourIndex.CHART_BACKGROUND));
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}
		} else if (Chart.darkMode().get()) {
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
	
	private void drawBorder() {
		if (border && !fillBorder) {
			gc.setFill(ColourSettings.colour(ColourIndex.CHART_BACKGROUND));
		}
		gc.fillRoundRect(x, y, width, height, CanvasButton.ARC_W, CanvasButton.ARC_H);
		if (border && !fillBorder) {
			gc.strokeRoundRect(x+0.5, y+0.5, width-1, height-1, CanvasButton.ARC_W, CanvasButton.ARC_H);
		}
	}
	
	public void defaultDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.MEDIUM, height - mgn*2));
		calculateOffsets(gc.getFont());
		setColoursRect();
		drawBorder();
		setColoursText();
		gc.fillText(text, x + textXOffset, y + textYOffset, width - mgn*2);
		gc.setFont(oldFont);
		if (drawCursor) {
			drawCursor();
		}
	}
	
	public void alternateDraw() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.MEDIUM, height - mgn*2));
		calculateOffsets(gc.getFont());
		setColoursAlt();
		drawBorder();
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - mgn*2);
		gc.setFont(oldFont);
		if (drawCursor) {
			drawCursor();
		}
	}
	
	public void defaultDraw(Font font) {
		Font oldFont = gc.getFont(); 
		gc.setFont(font);
		calculateOffsets(font);
		setColoursRect();
		drawBorder();
		setColoursText();		
		gc.fillText(text, x + textXOffset, y + textYOffset, width - mgn*2);
		if (drawCursor) {
			drawCursor();
		}
		gc.setFont(oldFont);
	}
	
	public void alternateDraw(Font font) {
		Font oldFont = gc.getFont(); 
		gc.setFont(font);
		calculateOffsets(font);
		setColoursAlt();
		drawBorder();
		gc.strokeText(text, x + textXOffset, y + textYOffset, width - mgn*2);
		if (drawCursor) {
			drawCursor();
		}
		gc.setFont(oldFont);
	}
	
	public void drawCursor() {
		double midx = x + textXOffset;
		midx += getTextWidth(text.substring(0, cursorPos));	
		midx = (int)(midx>x+width?x+width-3:midx)+0.5;
		boolean dm = Chart.darkMode().get(); 
		if (!dm && !border || !dm && !fillBorder || dm && border && fillBorder) {
			gc.setStroke(Color.BLACK);
		} else {
			gc.setStroke(Color.WHITE);
		}
		gc.strokeLine(midx, y+mgn*2, midx, y+height-mgn*2);
	}
	
	public void setCursorPos(int cursorPos) {
		int l = text.length();
		cursorPos = cursorPos<0?0:cursorPos;
		cursorPos = cursorPos>l?l:cursorPos;
		this.cursorPos = cursorPos;
	}
	
	public double margin() {
		return mgn;
	}
	
	@Override
	public void draw() {
		if (vg == null) {
			if (font == null) {
				defaultDraw();
			} else {
				defaultDraw(font);
			}
		} else {
			vg.draw(x, y, gc);
		}
	}
	
	private void checkZeroes() {
		if (type != InputType.ANY && text.length() > 1 && Double.parseDouble(text) == 0) {
			text = "0";
			setCursorPos(cursorPos);
		}
	}
	
	private void defaultOnKeyPressed(KeyEvent e) {
		KeyCode keyCode = e.getCode();
		int l;
		switch(keyCode) {
			case KeyCode.BACK_SPACE: 
				if (!text.isEmpty()) {	
					l = text.length();
					String newText = text.substring(0, cursorPos-1<0?0:cursorPos-1);
					newText += text.substring(cursorPos, l);
					text = newText;
					cursorPos = cursorPos-1<0?0:cursorPos-1;
					checkZeroes();
				}
				break;
			case KeyCode.LEFT:
				cursorPos = cursorPos-1<0?0:cursorPos-1;
				drawCursor = true;
				break;
			case KeyCode.RIGHT:
				l = text.length();
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
			case KeyCode.DELETE:
				if (!text.isEmpty()) {	
					l = text.length();
					String newText = text.substring(0, cursorPos);
					newText += text.substring(cursorPos+1>l?cursorPos:cursorPos+1, l);
					text = newText;
					checkZeroes();
				}
				break;
			default:
		}
	}
	
	private void defaultOnKeyTyped(KeyEvent e) {
		char c = e.getCharacter().charAt(0);
		if ((type == InputType.ANY && c > 31 && c < 127 || ((type == InputType.DOUBLE || type == InputType.ABS_DOUBLE) && c == 46 && !text.contains(".") 
				|| c > 47 && c < 58) || (type == InputType.INT || type == InputType.ABS_INT) && c > 47 && c < 58 || 
				(type == InputType.DOUBLE || type == InputType.INT) && c == 45 && text.length() == 0) && 
				!(cursorPos == 0 && c == 48 && text.length() > 0)) {
			if (type != InputType.ANY && text.equals("0")) {
				text = "";
				setCursorPos(cursorPos);
			}
			int l = text.length();
			String newText = text.substring(0, cursorPos) + c;
			newText += text.substring(cursorPos, l);
			text = newText;
			setCursorPos(cursorPos + 1);
		}
	}
	
	private void defaultOnMousePressed(MouseEvent e) {
		setCursorPos(calculateCursorPos(e.getX()));
	}
	
	private void defaultOnMouseMoved(MouseEvent e) {
		stage.getScene().setCursor(Cursor.TEXT);
	}
	
	private void defaultOnMouseExited(MouseEvent e) {
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
		if (font == null) {
			t.setFont(Font.font(gc.getFont().getFamily(), FontWeight.MEDIUM, height - mgn*2));
		} else {
			t.setFont(font);
		}
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
					draw();
					lastDraw = now;
				}
			}
		};
		cursorAnim.start();
	}	

	@Override
	public void onMouseMoved(MouseEvent e) {
		NodeChecks.mouseNodeHoverCheck(this, e.getX(), e.getY());
		defaultOnMouseMoved(e);
		if (onMouseMoved == null || !enabled) {
			return;
		}		
		onMouseMoved.handle(e);
	}
	
	@Override
	public void onMouseExited(MouseEvent e) {
		setPressed(false);
		setHover(false);
		defaultOnMouseExited(e);
		if (onMouseExited == null || !enabled) {
			return;
		}		
		onMouseExited.handle(e);
	}
	
	@Override
	public void onMousePressed(MouseEvent e) {
		setPressed(true);
		defaultOnMousePressed(e);
		if (onMousePressed == null || !enabled) {
			return;
		}		
		onMousePressed.handle(e);
	}
	
	@Override
	public void onKeyPressed(KeyEvent e) {
		defaultOnKeyPressed(e);
		if (onKeyPressed == null || !enabled) {
			return;
		}		
		onKeyPressed.handle(e);
	}

	@Override
	public void onKeyTyped(KeyEvent e) {
		defaultOnKeyTyped(e);
		if (onKeyTyped == null || !enabled) {
			return;
		}		
		onKeyTyped.handle(e);
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
