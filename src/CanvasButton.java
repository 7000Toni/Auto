import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public class CanvasButton implements CanvasNode {
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
	protected ButtonVanGogh bvg = null;
	protected boolean enabled;
	
	private EventHandler<? super MouseEvent> onMouseDragged;
	private EventHandler<? super MouseEvent> onMouseEntered;
	private EventHandler<? super MouseEvent> onMouseExited;
	private EventHandler<? super MouseEvent> onMousePressed;
	private EventHandler<? super MouseEvent> onMouseReleased;
	private EventHandler<? super MouseEvent> onMouseMoved;
	private EventHandler<? super ScrollEvent> onScroll;
	
	public CanvasButton(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset) {
		this.gc = gc;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.text = text;
		this.textXOffset = textXOffset;
		this.textYOffset = textYOffset;
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
		if (!enabled) {
			this.hover = false;
		}
	}
	
	public void setPressed(boolean pressed) {		
		this.pressed = pressed;
		if (!enabled) {
			this.pressed = false;
		}
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

	@Override
	public void onMouseDragged(MouseEvent e) {
		onMouseDragged.handle(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		onMouseEntered.handle(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		onMouseExited.handle(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		onMousePressed.handle(e);
	}

	@Override
	public void onMouseReleased(MouseEvent e) {
		onMouseReleased.handle(e);
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		onMouseMoved.handle(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		onScroll.handle(e);
	}

	@Override
	public void setOnMouseDragged(EventHandler<? super MouseEvent> e) {
		onMouseDragged = e;
	}

	@Override
	public void setOnMouseEntered(EventHandler<? super MouseEvent> e) {
		onMouseEntered = e;
	}

	@Override
	public void setOnMouseExited(EventHandler<? super MouseEvent> e) {
		onMouseExited = e;
	}

	@Override
	public void setOnMousePressed(EventHandler<? super MouseEvent> e) {
		onMousePressed = e;
	}

	@Override
	public void setOnMouseReleased(EventHandler<? super MouseEvent> e) {
		onMouseReleased = e;
	}

	@Override
	public void setOnMouseMoved(EventHandler<? super MouseEvent> e) {
		onMouseMoved = e;
	}

	@Override
	public void setOnScroll(EventHandler<? super ScrollEvent> e) {
		onScroll = e;
	}

	@Override
	public boolean onNode(double x, double y) {
		return onButton(x, y);
	}
}
