import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ColourPickerScrollBar extends HorizontalScrollBar {
	
	public ColourPickerScrollBar(ScrollBarOwner sbo, int dataSize, double minPos, double maxPos, double sbWidth, double sbHeight, double y) {
		super(sbo, dataSize, minPos, maxPos, sbWidth, sbHeight, y);
		setVanGogh((x, y2, gc) -> {
			if (hovering) {	
				gc.setFill(Color.GRAY);
			} else {
				gc.setFill(ColourCalculator.colour(x, this.minPos, this.maxPos - this.sbWidth));
			}
			if (dragging) {
				gc.setFill(ColourCalculator.colour(x, this.minPos, this.maxPos - this.sbWidth));
			} 
			gc.fillOval(x, y2, this.sbWidth, this.sbHeight);
		});
	}	
	
	@Override
	protected void moveOwnerLeft(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setPosition(speed, true);
	}
	
	@Override
	protected void moveOwnerRight(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setPosition(speed, true);
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
		((ColourPicker)sbo).unintializeColours();
		if (onMouseDragged == null) {
			return;
		}
		onMouseDragged.handle(e);
	}
}
