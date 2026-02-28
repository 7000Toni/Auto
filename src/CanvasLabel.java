import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CanvasLabel implements CanvasNode {
	protected GraphicsContext gc;
	protected double width;
	protected double height;
	protected double x;
	protected double y;
	protected String text;
	protected double textXOffset;
	protected double textYOffset;
	protected VanGogh vg = null;
	
	protected EventHandler<? super MouseEvent> onMouseDragged;
	protected EventHandler<? super MouseEvent> onMouseEntered;
	protected EventHandler<? super MouseEvent> onMouseExited;
	protected EventHandler<? super MouseEvent> onMousePressed;
	protected EventHandler<? super MouseEvent> onMouseClicked;
	protected EventHandler<? super MouseEvent> onMouseReleased;
	protected EventHandler<? super MouseEvent> onMouseMoved;
	protected EventHandler<? super ScrollEvent> onScroll;
	
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
	
	public double width() {
		return width;
	}
	
	public double textXOffset() {
		return textXOffset;
	}
	
	public double textYOffset() {
		return textYOffset;
	}
	
	public double height() {
		return height;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}
	
	public void setHeight(double height) {
		this.height = height;
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
	
	public void setVanGogh(VanGogh vg) {
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
		if (x > this.x + width || x < this.x) {
			return false;
		}
		if (y > this.y + height || y < this.y) {
			return false;
		}
		return true;
	}
}
