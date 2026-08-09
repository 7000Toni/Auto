package com.github._7000toni.auto.canvasnode.scrollbar;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public abstract class VerticalScrollBar extends CanvasNode {
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
	protected IVanGogh vg;
	
	public VerticalScrollBar(IScrollBarOwner sbo, double minPos, double maxPos, double sbWidth, double sbHeight, double x) {
		this.sbo = sbo;
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.sbWidth = sbWidth;
		this.sbHeight = sbHeight;
		this.x = x;
		this.y = minPos;
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
			initPos = e.getY();
		} else if (inScrollBarArea(e.getX(), e.getY())) {
			clickedInScrollBarArea = true;
			initPos = e.getY();
			new AnimationTimer() {
				long lastTick = 0;
				boolean add;
				
				@Override
				public void handle(long now) {
					if (lastTick == 0) {
						if (initPos > y) {
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
					
					if (onScrollBar(x, initPos)) {
						this.stop();
					}
					
					if (now - lastTick >= NANO_TO_MILLI*16) {						
						lastTick = now;		
						if (add) {
							setPosition(sbHeight / 2, true);
							sbo.draw();
						} else {
							setPosition(-(sbHeight / 2), true);
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
			double posDiff = e.getY() - initPos;
			setPosition(y + posDiff, false);
			initPos = (int)e.getY();
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
		setPosition(y, false);
	}
	
	public void setMinPos(double minPos) {
		this.minPos = minPos;
		setPosition(y, false);
	}
	
	protected abstract void moveOwnerUp(boolean fast);
	
	protected abstract void moveOwnerDown(boolean fast);
	
	protected void reduceSBPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveOwnerUp(true);
		} else {
			moveOwnerUp(false);
		}
	}
	
	protected void increaseSBPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveOwnerDown(true);
		} else {
			moveOwnerDown(false);
		}
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getCode()) {
			case KeyCode.UP:				
				reduceSBPos(e);
				sbo.draw();
				break;
			case KeyCode.DOWN:				
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
		if (y <= maxPos && y >= minPos) {
			if (x <= this.x + sbWidth && x >= this.x) {	
				return true;
			}
		}
		return false;
	}
	
	protected void checkYPos() {
		if (y > maxPos) {
			y = maxPos - sbHeight;
		} else if (y < minPos) {
			y = minPos - sbHeight;
		}
	}
	
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
	}		
	
	@Override
	public void setY(double y) {
		setPosition(y, false);
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
		gc.fillRoundRect(x, y, sbWidth, sbHeight, CanvasButton.ARC_W, CanvasButton.ARC_H);
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
