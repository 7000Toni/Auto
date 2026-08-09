package com.github._7000toni.auto.canvasnode.scrollbar;
import com.github._7000toni.auto.menu.Menu;

public class VerticalMenuScrollBar extends VerticalScrollBar {
	
	public VerticalMenuScrollBar(Menu menu, double minPos, double maxPos, double sbWidth, double sbHeight, double xPos) {
		super(menu, minPos, maxPos, sbWidth, sbHeight, xPos);
	}
	
	@Override
	protected void moveOwnerUp(boolean fast) {
		double speed = 0.1;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos = y * (1 - speed);
		setPosition(newHSBPos, false);
	}
	
	@Override
	protected void moveOwnerDown(boolean fast) {
		double speed = 0.1;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos = y * (1 + speed);
		setPosition(newHSBPos, false);
	}
	
	@Override
	public void setPosition(double pos, boolean increment) {
		if (Double.isNaN(pos)) {
			return;
		}
		if (increment) {
			if (pos + y > maxPos - sbHeight) {
				y = maxPos - sbHeight;
			} else if (pos + y < minPos) {	
				y = minPos;
			} else {
				y += pos;
			}
		} else {
			if (pos > maxPos - sbHeight) {
				y = maxPos - sbHeight;
			} else if (pos < minPos) {	
				y = minPos;
			} else {
				y = pos;
			}
		}
		((Menu)sbo).adjustDatasetPositions();
	}	
}
