import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DataSetButton extends CanvasButton {
	private CanvasButton close;
	private ButtonVanGogh drawCross;
	
	public DataSetButton(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset, ButtonVanGogh bvg) {
		super(gc, width, height, x, y, text, textXOffset, textYOffset, bvg);	/*	
		double x2 = x + width - 42;
		double y2 = y + 6;*/
		drawCross = (x2, y2, gc2) -> {
			gc2.setFill(Color.BLACK);
			gc2.setStroke(Color.BLACK);
			if (close.hover) {
				gc2.setFill(Color.GRAY);
				gc2.setStroke(Color.GRAY);
			}
			if (close.pressed) {
				gc2.setFill(Color.RED);
				gc2.setStroke(Color.RED);
			}			
			gc2.strokeRect(x2, y2, 42, 42);			
			double valx = x2 + 3 + 12;
			double val2x = x2 + 3 + 24;
			double val3x = x2 + 3 + 36;
			double val4x = x2 + 3 + 18;
			double valy = y2 + 3 + 9;
			double val2y = y2 + 3 + 27;
			double val3y = y2 + 3 + 36;
			double val4y = y2 + 3 + 18;
			double[] x3 = {x2 + 3, valx, val4x, val2x, val3x, val2x, val3x, val2x, val4x, valx, x2 + 3, valx, x2 + 3};
			double[] y3 = {y2 + 3, y2 + 3, valy, y2 + 3, y2 + 3, val4y, val3y, val3y, val2y, val3y, val3y, val4y, y2 + 3};
			gc2.fillPolygon(x3, y3, 13);
		};
		close = new CanvasButton(gc, 42, 42, x + width - 3 - 42, y + 3, "", 3, 3, drawCross);		
	}
	
	@Override
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
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 48);
		close.drawButton();
	}
	
	@Override
	public void drawButton() {
		if (bvg == null) {
			defaultDrawButton();
		} else {
			bvg.drawButton(x, y, gc);
		}
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
