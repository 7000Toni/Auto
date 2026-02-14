import javafx.scene.paint.Color;

public class ColourPickerScrollBar extends HorizontalScrollBar {
	
	public ColourPickerScrollBar(ScrollBarOwner sbo, int dataSize, double minPos, double maxPos, double sbWidth, double sbHeight, double y) {
		super(sbo, dataSize, minPos, maxPos, sbWidth, sbHeight, y);
		setVanGogh((x, y2, gc) -> {
			if (hovering) {	
				gc.setFill(Color.GRAY);
			} else {
				gc.setFill(sbColour());
			}
			if (dragging) {
				gc.setFill(sbColour());
			} 
			gc.fillOval(x, y2, sbWidth, sbHeight);
		});
	}

	public Color sbColour() {
		int r = 0;
		int g = 0;
		int b = 0;
		double perc = (x - minPos) / (maxPos - sbWidth - minPos);		
		if (perc > 2.0/3) {
			perc -= 2.0/3;
			if (perc > (1.0/3)/2) {
				perc -= (1.0/3)/2;
				r = 255;
				b = 255 - (int)(255*(perc/((1.0/3)/2)));
			} else {
				b = 255;
				r = (int)(255*(perc/((1.0/3)/2)));
			}			
		} else if (perc > 1.0/3) {
			perc -= 1.0/3;
			if (perc > (1.0/3)/2) {
				perc -= (1.0/3)/2;
				b = 255;
				g = 255 - (int)(255*(perc/((1.0/3)/2)));
			} else {
				g = 255;
				b = (int)(255*(perc/((1.0/3)/2)));
			}
		} else {
			if (perc > (1.0/3)/2) {
				perc -= (1.0/3)/2;
				g = 255;
				r = 255 - (int)(255*(perc/((1.0/3)/2)));
			} else {
				r = 255;
				g = (int)(255*(perc/((1.0/3)/2)));
			}
		}
		return Color.web("rgb(" + r + "," + g + "," +  b + ")");
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
}
