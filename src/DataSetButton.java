import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DataSetButton extends CanvasButton {
	private CanvasButton close;
	
	public DataSetButton(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset) {
		super(gc, width, height, x, y, text, textXOffset, textYOffset);
		close = new CanvasButton(gc, 38, 38, x + width - 5 - 38, y + 5, "x", 5, 5);
	}
	
	@Override
	public void drawButton() {
		gc.setStroke(Color.BLACK);
		if (hover) {
			gc.setStroke(Color.GRAY);
		}
		if (clicked) {
			gc.setStroke(Color.DIMGRAY);
		}
		gc.strokeRect(x, y, width, height);
		gc.strokeText(text, x + textXOffset, y + textYOffset);
		close.drawButton();
	}
	
	@Override
	public void setX(double x) {
		double diff = this.x - x;
		this.x = x;
		close.setX(close.x - diff);
	}
	
	@Override
	public void setY(double y) {
		double diff = this.y - y;
		this.y = y;
		close.setY(close.y - diff);
	}
	
	public CanvasButton closeButton() {
		return this.close;
	}
}
