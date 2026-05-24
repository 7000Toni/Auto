package com.github._7000toni.auto.canvasnode.scrollbar;
import com.github._7000toni.auto.canvasnode.ColourCalculator;
import com.github._7000toni.auto.canvasnode.ColourPicker;

import javafx.animation.AnimationTimer;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ColourPickerScrollBar extends HorizontalScrollBar {
	
	public ColourPickerScrollBar(IScrollBarOwner sbo, double minPos, double maxPos, double sbWidth, double sbHeight, double y) {
		super(sbo, minPos, maxPos, sbWidth, sbHeight, y);
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
	
	private void drawHSBBar() {
		gc.strokeRect(minPos + 5, y + 5, maxPos - minPos - 10, 5);
		for (double i = minPos + 6; i < maxPos - 5; i++) {
			gc.setStroke(ColourCalculator.colour(i, minPos + 5, maxPos - 6));
			gc.strokeLine(i, y + 6, i, y + 9);
		}
	}
	
	@Override
	public void draw() {
		if (vg == null) {			
			defaultDraw();
		} else {
			drawHSBBar();
			vg.draw(x, y, gc);
		}
	}
	
	@Override
	public void defaultOnMousePressed(MouseEvent e) {
		if (onScrollBar(e.getX(), e.getY())) {					
			dragging = true;
			initPos = e.getX();
		} else if (inScrollBarArea(e.getX(), e.getY())) {
			clickedInScrollBarArea = true;
			initPos = e.getX();
			new AnimationTimer() {
				long lastTick = 0;
				boolean add;
				
				@Override
				public void handle(long now) {
					if (lastTick == 0) {
						if (initPos > x) {
							add = true;
						} else {
							add = false;
						}
						lastTick = now;
						return;
					}
					
					if (!clickedInScrollBarArea) {
						this.stop();
					}
					
					if (onScrollBar(initPos, y)) {
						this.stop();
					}
					
					if (now - lastTick >= NANO_TO_MILLI*16) {						
						lastTick = now;		
						if (add) {
							setPosition(sbWidth / 2, true);
							((ColourPicker)sbo).unintializeColours();
							((ColourPicker)sbo).chartMenu().chart().draw();
						} else {
							setPosition(-(sbWidth / 2), true);
							((ColourPicker)sbo).unintializeColours();
							((ColourPicker)sbo).chartMenu().chart().draw();
						}
					} 
				}
			}.start();
		}
	}
	
	@Override
	protected void moveOwnerLeft(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setPosition(-speed, true);
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
