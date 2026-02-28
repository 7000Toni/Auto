import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CanvasButton extends CanvasLabel implements CanvasNode {
	protected boolean hover = false;
	protected boolean pressed = false;
	protected boolean on = false;
	protected boolean enabled;
	
	public CanvasButton(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset) {
		super(gc, width, height, x, y, text, textXOffset, textYOffset);
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
	
	public void draw() {
		if (vg == null) {
			defaultDraw();
		} else {
			vg.draw(x, y, gc);
		}
	}

	@Override
	public void onMouseDragged(MouseEvent e) {
		if (onMouseDragged == null || !enabled) {
			return;
		}
		onMouseDragged.handle(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onMouseEntered == null || !enabled) {
			return;
		}
		onMouseEntered.handle(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		setPressed(false);
		setHover(false);
		if (onMouseExited == null || !enabled) {
			return;
		}
		onMouseExited.handle(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		setPressed(true);
		if (onMousePressed == null || !enabled) {
			return;
		}
		onMousePressed.handle(e);
	}
	@Override
	public void onMouseClicked(MouseEvent e) {	
		if (onMouseClicked == null || !enabled) {
			return;
		}
		onMouseClicked.handle(e);
	}
	
	@Override
	public void onMouseReleased(MouseEvent e) {	
		setPressed(false);	
		if (onMouseReleased == null || !enabled) {
			return;
		}
		onMouseReleased.handle(e);		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		ButtonChecks.mouseButtonHoverCheck(this, e.getX(), e.getY());
		if (onMouseMoved == null || !enabled) {
			return;
		}
		onMouseMoved.handle(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		if (onScroll == null || !enabled) {
			return;
		}
		onScroll.handle(e);
	}
}
