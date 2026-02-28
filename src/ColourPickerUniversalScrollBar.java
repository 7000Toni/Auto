import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ColourPickerUniversalScrollBar extends UniversalScrollBar {

	public ColourPickerUniversalScrollBar(ScrollBarOwner sbo, double minXPos, double maxXPos, double minYPos, double maxYPos, double sbWidth, double sbHeight, double x, double y) {
		super(sbo, minXPos, maxXPos, minYPos, maxYPos, sbWidth, sbHeight, x, y);
		setVanGogh((x2, y2, gc) -> {
			int r = (int)(143 * (x2 - this.minXPos)/(this.maxXPos - this.minXPos));
			int c = (int)(143 * (y2 - this.minYPos)/(this.maxYPos - this.minYPos));
			
			if (hovering) {	
				gc.setFill(Color.GRAY);
			} else {
				gc.setFill(Color.GRAY);
				gc.setFill(((ColourPicker)this.sbo).colours()[r][c]);
			}
			if (dragging) {
				gc.setFill(Color.GRAY);
				gc.setFill(((ColourPicker)this.sbo).colours()[r][c]);
			} 
			gc.setStroke(Color.WHITE);
			gc.fillOval(x2, y2, this.sbWidth, this.sbHeight);
			gc.strokeOval(x2, y2, this.sbWidth, this.sbHeight);
		});
	}
	
	@Override
	public void defaultOnMousePressed(MouseEvent e) {
		if (onScrollBar(e.getX(), e.getY())) {					
			dragging = true;
			initXPos = e.getX();
			initYPos = e.getY();
		} else if (inScrollBarArea(e.getX(), e.getY())) {
			clickedInScrollBarArea = true;
			setXPosition(e.getX() - 7.5, false);
			setYPosition(e.getY() - 7.5, false);
		}
	}

	@Override
	protected void moveOwnerLeft(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setXPosition(speed, true);
	}
	
	@Override
	protected void moveOwnerRight(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setXPosition(speed, true);
	}

	@Override
	protected void moveOwnerUp(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setYPosition(-speed, true);
	}

	@Override
	protected void moveOwnerDown(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setYPosition(speed, true);
	}

}
