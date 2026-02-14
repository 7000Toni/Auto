import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public class ColourPicker implements CanvasNode, ScrollBarOwner {
	private double x;
	private double y;
	private double width;
	private double height;
	private GraphicsContext gc;
	private ColourPickerScrollBar hsb; 
	
	private EventHandler<? super MouseEvent> onMouseDragged;
	private EventHandler<? super MouseEvent> onMouseEntered;
	private EventHandler<? super MouseEvent> onMouseExited;
	private EventHandler<? super MouseEvent> onMousePressed;
	private EventHandler<? super MouseEvent> onMouseClicked;
	private EventHandler<? super MouseEvent> onMouseReleased;
	private EventHandler<? super MouseEvent> onMouseMoved;
	private EventHandler<? super ScrollEvent> onScroll;
	
	public ColourPicker(double x, double y, double width, double height, GraphicsContext gc) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		hsb = new ColourPickerScrollBar(this, 255*6, x - 2, x + 292, 15, 15, y + 150);
	}
	
	public ColourPickerScrollBar hsb() {
		return hsb;
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
		if (onMouseDragged == null) {
			return;
		}
		onMouseDragged.handle(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onMouseEntered == null) {
			return;
		}
		onMouseEntered.handle(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		if (onMouseExited == null) {
			return;
		}
		onMouseExited.handle(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		if (onMousePressed == null) {
			return;
		}
		onMousePressed.handle(e);
	}
	@Override
	public void onMouseClicked(MouseEvent e) {	
		if (onMouseClicked == null) {
			return;
		}
		onMouseClicked.handle(e);
	}
	
	@Override
	public void onMouseReleased(MouseEvent e) {	
		if (onMouseReleased == null) {
			return;
		}
		onMouseReleased.handle(e);		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		if (onMouseMoved == null) {
			return;
		}
		onMouseMoved.handle(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		if (onScroll == null) {
			return;
		}
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
	public void setOnMouseClicked(EventHandler<? super MouseEvent> e) {
		onMouseClicked = e;
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
		if (x >= this.x && x < this.x + width && y >= this.y && y < this.y + height) {
			return true;
		}
		return false;
	}

	@Override
	public void draw() {
		gc.clearRect(x, y, width, height);
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}	
		
		gc.strokeRect(x, y, width, 145);
		gc.strokeLine(x + 145, y, x + 145, y + 145);
		gc.strokeRect(x, y + 155, 290, 5);
		hsb.draw();
	}

	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}

	@Override
	public void setGraphicsContext(GraphicsContext gc) {
		this.gc = gc;
	}

	@Override
	public void setX(double x) {
		double hsbOffset = hsb.x() - this.x;
		this.x = x;		
		hsb.setMinPos(x - 2);
		hsb.setMaxPos(x + 292);
		hsb.setX(hsbOffset + x);
	}

	@Override
	public void setY(double y) {
		this.y = y;
		hsb.setY(y + 150);
	}

	@Override
	public double x() {
		return x;
	}

	@Override
	public double y() {
		return y;
	}
}
