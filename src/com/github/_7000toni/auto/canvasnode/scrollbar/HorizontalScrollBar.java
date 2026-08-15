package com.github._7000toni.auto.canvasnode.scrollbar;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.settings.MiscellaneousSettings;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class HorizontalScrollBar extends CanvasNode {
	protected IScrollBarOwner sbo;
	
	public static final long NANO_TO_MILLI = 1000000; 
	
	protected boolean dragging = false;
	protected boolean hovering = false;
	protected boolean clickedInScrollBarArea = false;
	protected double initPos = 0;
	protected double maxPos;
	protected double minPos;
	protected double sbWidth;
	protected double sbHeight;
	protected DoubleProperty percentage = new SimpleDoubleProperty(0);
	protected IVanGogh vg;
	
	public HorizontalScrollBar(IScrollBarOwner sbo, double minPos, double maxPos, double sbWidth, double sbHeight, double y) {
		this.sbo = sbo;
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.sbWidth = sbWidth;
		this.sbHeight = sbHeight;
		this.x = minPos;
		this.y = y;
		this.gc = sbo.graphicsContext();
		
		onMouseDragged = e -> {defaultOnMouseDragged(e);};
		onMouseExited = e -> {defaultOnMouseExited(e);};
		onMouseMoved = e -> {defaultOnMouseMoved(e);};
		onMousePressed = e -> {defaultOnMousePressed(e);};
		onMouseReleased = e -> {defaultOnMouseReleased(e);};
		onKeyPressed = e -> {keyPressed(e);};
	}
	
	public double sbWidth() {
		return this.sbWidth;
	}
	
	public double sbHeight() {
		return this.sbHeight;
	}
	
	public void setVanGogh(IVanGogh vg) {
		this.vg = vg;
	}
	
	public void defaultOnMouseReleased(MouseEvent e) {
		dragging = false;
		clickedInScrollBarArea = false;
	}
	
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
							sbo.draw();
						} else {
							setPosition(-(sbWidth / 2), true);
							sbo.draw();
						}
					} 
				}
			}.start();
		}
	}
	
	public void defaultOnMouseExited(MouseEvent e) {
		if (!dragging) {
			hovering = false;
		}
	}
	
	public void defaultOnMouseMoved(MouseEvent e) {
		if (onScrollBar(e.getX(), e.getY())) {					
			hovering = true;
		} else {
			hovering = false;
		}
	}
	
	public void defaultOnMouseDragged(MouseEvent e) {
		if (dragging) {
			double posDiff = e.getX() - initPos;
			setPosition(x + posDiff, false);
			initPos = (int)e.getX();
		}
	}
	
	public double maxPos() {
		return this.maxPos;
	}
	
	public double minPos() {
		return this.minPos;
	}
	
	public void setMaxPos(double maxPos) {
		this.maxPos = maxPos;
		setPosition(x, false);
	}
	
	public void setMinPos(double minPos) {
		this.minPos = minPos;
		setPosition(x, false);
	}
	
	public DoubleProperty percentage() {
		return percentage;
	}
	
	protected void moveOwnerLeft(boolean fast) {}
	
	protected void moveOwnerRight(boolean fast) {}
	
	protected void reduceSBPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveOwnerLeft(true);
		} else {
			moveOwnerLeft(false);
		}
	}
	
	protected void increaseSBPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveOwnerRight(true);
		} else {
			moveOwnerRight(false);
		}
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getCode()) {
			case KeyCode.LEFT:				
				reduceSBPos(e);
				sbo.draw();
				break;
			case KeyCode.RIGHT:				
				increaseSBPos(e);
				sbo.draw();
				break;
			default:				
		}
	}
	
	protected boolean onScrollBar(double x, double y) {
		if (y <= this.y + sbHeight && y >= this.y) {
			if (x <= this.x + sbWidth && x >= this.x) {
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean inScrollBarArea(double x, double y) {	
		if (y <= this.y + sbHeight && y >= this.y) {
			if (x <= maxPos && x >= minPos) {				
				return true;
			}
		}
		
		return false;
	}
	
	protected void checkXPos() {
		if (x > maxPos) {
			x = maxPos - sbWidth;
		} else if (x < minPos) {
			x = minPos - sbWidth;
		}
	}
	
	public void setPosition(double pos, boolean increment) {
		if (Double.isNaN(pos)) {
			return;
		}
		if (increment) {
			if (pos + x > maxPos - sbWidth) {
				x = maxPos - sbWidth;
			} else if (pos + x < minPos) {	
				x = minPos;
			} else {
				x += pos;
			}
		} else {
			if (pos > maxPos - sbWidth) {
				x = maxPos - sbWidth;
			} else if (pos < minPos) {	
				x = minPos;
			} else {
				x = pos;
			}
		}
		percentage.set((x - minPos) / (maxPos - minPos - sbWidth));
	}		
	
	@Override
	public void setX(double x) {
		setPosition(x, false);
	}	
	
	public void defaultDraw() {
		if (hovering) {	
			gc.setFill(Color.GRAY);
		} else {
			gc.setFill(Color.DARKGRAY);
		}
		if (dragging) {
			gc.setFill(Color.DIMGRAY);
		} 
		gc.fillRoundRect(x, y, sbWidth, sbHeight, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
	}
	
	@Override
	public void draw() {		
		if (vg == null) {
			defaultDraw();
		} else {
			vg.draw(x, y, gc);
		}
	}	
	
	@Override
	public void onMouseReleased(MouseEvent e) {
		if (!onNode(e.getX(), e.getY())) {
			hovering = false;
		}
		if (onMouseReleased == null || !enabled) {
			return;
		}
		onMouseReleased.handle(e);
	}

	@Override
	public boolean onNode(double x, double y) {
		return inScrollBarArea(x, y);
	}
}
