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
		double newHSBPos = y.get() * (1 - speed);
		setPosition(newHSBPos, false);
	}
	
	@Override
	protected void moveOwnerDown(boolean fast) {
		double speed = 0.1;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos = y.get() * (1 + speed);
		setPosition(newHSBPos, false);
	}
	
	@Override
	public void setPosition(double pos, boolean increment) {
		if (Double.isNaN(pos)) {
			return;
		}
		if (increment) {
			if (pos + y.get() > maxPos - sbHeight) {
				y.set(maxPos - sbHeight);
			} else if (pos + y.get() < minPos) {	
				y.set(minPos);
			} else {
				y.set(y.get() + pos);
			}
		} else {
			if (pos > maxPos - sbHeight) {
				y.set(maxPos - sbHeight);
			} else if (pos < minPos) {	
				y.set(minPos);
			} else {
				y.set(pos);
			}
		}
		percentage.set((y.get() - minPos) / (maxPos - minPos - sbHeight));
		((Menu)sbo).adjustDatasetPositions();
	}	
}
