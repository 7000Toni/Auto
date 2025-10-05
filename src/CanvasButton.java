import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CanvasButton {
	protected GraphicsContext gc;
	protected double width;
	protected double height;
	protected double x;
	protected double y;
	protected String text;
	protected double textXOffset;
	protected double textYOffset;
	protected boolean hover = false;
	protected boolean pressed = false;
	protected ButtonVanGogh bvg;
	
	public CanvasButton(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset, ButtonVanGogh bvg) {
		this.gc = gc;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.text = text;
		this.textXOffset = textXOffset;
		this.textYOffset = textYOffset;
		this.bvg = bvg;
	}
	
	public boolean hover() {
		return hover;
	}
	
	public boolean pressed() {
		return pressed;
	}
	
	public void setHover(boolean hover) {
		if (hover) {
			pressed = false;
		}
		this.hover = hover;
	}
	
	public void setPressed(boolean clicked) {
		if (clicked) {
			hover = false;
		}
		this.pressed = clicked;
	}
	
	public double x() {
		return this.x;
	}
	
	public double y() {
		return this.y;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setVanGogh(ButtonVanGogh bvg) {
		this.bvg = bvg;
	}
	
	public void defaultDrawButton() {
		gc.setStroke(Color.BLACK);
		gc.setFill(Color.BLACK);
		if (hover) {
			gc.setStroke(Color.GRAY);
			gc.setFill(Color.GRAY);
		}
		if (pressed) {
			gc.setStroke(Color.DIMGRAY);
			gc.setFill(Color.DIMGRAY);
		}
		gc.strokeRect(x, y, width, height);
		gc.fillText(text, x + textXOffset, y + textYOffset);
	}
	
	public void drawButton() {
		if (bvg == null) {
			defaultDrawButton();
		} else {
			bvg.drawButton(x, y, gc);
		}
	}
	
	public boolean onButton(double x, double y) {
		if (x > this.x + width || x < this.x) {
			return false;
		}
		if (y > this.y + height || y < this.y) {
			return false;
		}
		return true;
	}
}
