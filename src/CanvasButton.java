import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CanvasButton implements Drawable {
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
	protected boolean enabled;
	
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
		this.enabled = true;
	}
	
	public boolean hover() {
		return hover;
	}
	
	public boolean pressed() {
		return pressed;
	}
	
	public void setHover(boolean hover) {
		this.hover = hover;
	}
	
	public void setPressed(boolean clicked) {
		this.pressed = clicked;
	}
	
	@Override
	public GraphicsContext graphicsContext() {
		return this.gc;
	}
	
	@Override
	public void setGraphicsContext(GraphicsContext gc) {
		this.gc = gc;
	}
	
	@Override
	public double x() {
		return this.x;
	}
	
	@Override
	public double y() {
		return this.y;
	}
	
	@Override
	public void setX(double x) {
		this.x = x;
	}
	
	@Override
	public void setY(double y) {
		this.y = y;
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
	
	public void setVanGogh(ButtonVanGogh bvg) {
		this.bvg = bvg;
	}
	
	public void disable() {
		this.enabled = false;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public boolean enabled() {
		return this.enabled;
	}
	
	public String text() {
		return this.text;
	}
	
	public void defaultDrawButton() {
		if (Chart.darkMode()) {
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
		gc.fillText(text, x + textXOffset, y + textYOffset);
	}
	
	public void draw() {
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
