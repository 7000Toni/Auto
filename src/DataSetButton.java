import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class DataSetButton extends CanvasButton {
	private CanvasButton close;
	private CanvasButton mr;
	private int dataSetIndex;
	
	public DataSetButton(GraphicsContext gc, double width, double height, double x, double y, String text, double textXOffset, double textYOffset) {
		super(gc, width, height, x, y, text, textXOffset, textYOffset);
		ButtonVanGogh drawCross = (x2, y2, gc2) -> {
			if (Chart.darkMode()) {
				gc2.setStroke(Color.WHITE);
				gc2.setFill(Color.WHITE);
			} else {
				gc2.setStroke(Color.BLACK);
				gc2.setFill(Color.BLACK);
			}	
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
		ButtonVanGogh mrvg = (x2, y2, gc2) -> {
			if (Chart.darkMode()) {
				gc2.setStroke(Color.WHITE);
				gc2.setFill(Color.WHITE);
			} else {
				gc2.setStroke(Color.BLACK);
				gc2.setFill(Color.BLACK);
			}	
			if (mr.hover) {
				gc2.setFill(Color.GRAY);
				gc2.setStroke(Color.GRAY);
			}
			if (mr.pressed) {
				gc2.setFill(Color.DIMGRAY);
				gc2.setStroke(Color.DIMGRAY);
			}			
			gc2.strokeRect(x2, y2, 42, 42);
			
			double[] xa = {x2 + 7, x2 + 21, x2 + 21, x2 + 35, x2 + 35, x2 + 7};
			double[] ya = {y2 + 19, y2 + 9, y2 + 14, y2 + 14, y2 + 19, y2 + 19};
			gc2.fillPolygon(xa, ya, 6);
			
			double[] xa2 = {x2 + 7, x2 + 35, x2 + 21, x2 + 21, x2 + 7, x2 + 7};
			double[] ya2 = {y2 + 23, y2 + 23, y2 + 33, y2 + 28, y2 + 28, y2 + 23};
			gc2.fillPolygon(xa2, ya2, 6);
		};
		close = new CanvasButton(gc, 42, 42, x + width - 3 - 42, y + 3, null, 3, 3);	
		close.setVanGogh(drawCross);
		mr = new CanvasButton(gc, 42, 42, x + width - 3 - 42 - 3 - 42, y + 3, null, 3, 3);
		mr.setVanGogh(mrvg);
	}
	
	public void setDataSetIndex(int index) {
		dataSetIndex = index;
	}
	
	public int dataSetIndex() {
		return dataSetIndex;
	}
	
	@Override
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
		gc.strokeRect(x, y, width, height);
		gc.fillText(text, x + textXOffset, y + textYOffset, width - 93);
		close.draw();
		mr.draw();
	}
	
	@Override
	public void draw() {
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
		mr.setX(mr.x - diff);
	}
	
	@Override
	public void setY(double y) {
		double diff = this.y - y;
		this.y = y;
		close.setY(close.y - diff);
		mr.setY(mr.y - diff);
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
		if (close.onButton(e.getX(), e.getY())) {
			close.setPressed(true);
			if (onMousePressed == null) {
				return;
			}
			close.onMousePressed.handle(e);
		} else if (mr.onButton(e.getX(), e.getY())) {
			mr.setPressed(true);
			if (onMousePressed == null) {
				return;
			}
			mr.onMousePressed.handle(e);
		} else {
			setPressed(true);
			if (onMousePressed == null) {
				return;
			}
			onMousePressed.handle(e);
		}
	}

	@Override
	public void onMouseReleased(MouseEvent e) {		
		if (close.onButton(e.getX(), e.getY())) {
			close.setPressed(false);
			if (onMouseReleased == null) {
				return;
			}
			close.onMouseReleased.handle(e);			
		} else if (mr.onButton(e.getX(), e.getY())) {
			mr.setPressed(false);
			if (onMouseReleased == null) {
				return;
			}
			mr.onMouseReleased.handle(e);			
		} else {
			setPressed(false);
			if (onMouseReleased == null) {
				return;
			}
			onMouseReleased.handle(e);			
		}		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		if (ButtonChecks.mouseButtonHoverCheck(close, e.getX(), e.getY()) || ButtonChecks.mouseButtonHoverCheck(mr, e.getX(), e.getY())) {
			pressed = false;
			hover = false;
		} else {
			ButtonChecks.mouseButtonHoverCheck(this, e.getX(), e.getY());
		}
		if (onMouseMoved == null) {
			return;
		}
		onMouseMoved.handle(e);
	}
	
	public CanvasButton closeButton() {
		return this.close;
	}
	
	public CanvasButton mrButton() {
		return this.mr;
	}
}
