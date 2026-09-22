package com.github._7000toni.auto.canvasnode.scrollbar;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.IVanGogh;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class UniversalScrollBar extends CanvasNode {
	protected IScrollBarOwner sbo;
	
	public static final long NANO_TO_MILLI = 1000000; 
	
	protected boolean dragging = false;
	protected boolean hovering = false;
	protected boolean clickedInScrollBarArea = false;
	protected double initXPos = 0;
	protected double initYPos = 0;
	protected double maxXPos;
	protected double minXPos;
	protected double maxYPos;
	protected double minYPos;
	protected double sbWidth;
	protected double sbHeight;
	protected DoubleProperty xPercentage = new SimpleDoubleProperty(0);
	protected DoubleProperty yPercentage = new SimpleDoubleProperty(0);
	protected IVanGogh vg;
	
	public UniversalScrollBar(IScrollBarOwner sbo, double minXPos, double maxXPos, double minYPos, double maxYPos, double sbWidth, double sbHeight, double x, double y) {
		this.sbo = sbo;
		this.minXPos = minXPos;
		this.maxXPos = maxXPos;
		this.minYPos = minYPos;
		this.maxYPos = maxYPos;
		this.sbWidth = sbWidth;
		this.sbHeight = sbHeight;
		setXPosition(x, false);
		setYPosition(y, false);
		this.gc = sbo.graphicsContext();
		
		onMouseDragged = (e) -> {defaultOnMouseDragged(e);};
		onMouseExited = (e) -> {defaultOnMouseExited(e);};
		onMouseMoved = (e) -> {defaultOnMouseMoved(e);};
		onMousePressed = (e) -> {defaultOnMousePressed(e);};
		onMouseReleased = (e) -> {defaultOnMouseReleased(e);};
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
			initXPos = e.getX();
			initYPos = e.getY();
		} else if (inScrollBarArea(e.getX(), e.getY())) {
			clickedInScrollBarArea = true;
			setXPosition(e.getX(), false);
			setYPosition(e.getY(), false);
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
			double posXDiff = e.getX() - initXPos;
			double posYDiff = e.getY() - initYPos;
			if (x.get() + posXDiff > maxXPos - sbWidth) {
				x.set(maxXPos - sbWidth);
			} else if (x.get() + posXDiff < minXPos) {
				x.set(minXPos);
			} else {
				x.set(x.get() + posXDiff);
			}
			initXPos = (int)e.getX();
			if (y.get() + posYDiff > maxYPos - sbHeight) {
				y.set(maxYPos - sbHeight);
			} else if (y.get() + posYDiff < minYPos) {
				y.set(minYPos);
			} else {
				y.set(y.get() + posYDiff);
			}
			initYPos = (int)e.getY();
		}
	}
	
	public double maxXPos() {
		return this.maxXPos;
	}
	
	public double minXPos() {
		return this.minXPos;
	}
	
	public double maxYPos() {
		return this.maxYPos;
	}
	
	public double minYPos() {
		return this.minYPos;
	}
	
	public void setMaxXPos(double maxXPos) {
		this.maxXPos = maxXPos;
		setXPosition(x.get(), false);
	}
	
	public void setMinXPos(double minXPos) {
		this.minXPos = minXPos;
		setXPosition(x.get(), false);
	}
	
	public void setMaxYPos(double maxYPos) {
		this.maxYPos = maxYPos;
		setYPosition(y.get(), false);
	}
	
	public void setMinYPos(double minYPos) {
		this.minYPos = minYPos;
		setYPosition(y.get(), false);
	}
	
	public DoubleProperty xPercentage() {
		return xPercentage;
	}
	
	public DoubleProperty yPercentage() {
		return yPercentage;
	}
	
	protected void moveOwnerLeft(boolean fast) {}
	
	protected void moveOwnerRight(boolean fast) {}
	
	protected void moveOwnerUp(boolean fast) {}
	
	protected void moveOwnerDown(boolean fast) {}
	
	protected void reduceSBXPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveOwnerLeft(true);
		} else {
			moveOwnerLeft(false);
		}
	}
	
	protected void increaseSBXPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveOwnerRight(true);
		} else {
			moveOwnerRight(false);
		}
	}
	
	protected void reduceSBYPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveOwnerUp(true);
		} else {
			moveOwnerUp(false);
		}
	}
	
	protected void increaseSBYPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveOwnerDown(true);
		} else {
			moveOwnerDown(false);
		}
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getCode()) {
			case KeyCode.LEFT:				
				reduceSBXPos(e);
				sbo.draw();
				break;
			case KeyCode.RIGHT:				
				increaseSBXPos(e);
				sbo.draw();
				break;
			case KeyCode.UP:				
				reduceSBYPos(e);
				sbo.draw();
				break;
			case KeyCode.DOWN:				
				increaseSBYPos(e);
				sbo.draw();
				break;
			default:				
		}
	}
	
	protected boolean onScrollBar(double x, double y) {
		if (y <= this.y.get() + sbHeight && y >= this.y.get()) {
			if (x <= this.x.get() + sbWidth && x >= this.x.get()) {
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean inScrollBarArea(double x, double y) {	
		if (y <= maxYPos && y >= minYPos) {
			if (x <= maxXPos && x >= minXPos) {				
				return true;
			}
		}
		
		return false;
	}
	
	protected void checkXPos() {
		if (x.get() > maxXPos) {
			x.set(maxXPos - sbWidth);
		} else if (x.get() < minXPos) {
			x.set(minXPos - sbWidth);
		}
	}
	
	protected void checkYPos() {
		if (y.get() > maxYPos) {
			y.set(maxYPos - sbHeight);
		} else if (y.get() < minYPos) {
			y.set(minYPos - sbHeight);
		}
	}
	
	public void setXPosition(double pos, boolean increment) {
		if (Double.isNaN(pos)) {
			return;
		}
		if (increment) {
			if (pos + x.get() > maxXPos - sbWidth) {
				x.set(maxXPos - sbWidth);
			} else if (pos + x.get() < minXPos) {	
				x.set(minXPos);
			} else {
				x.set(x.get() + pos);
			}
		} else {
			if (pos > maxXPos - sbWidth) {
				x.set(maxXPos - sbWidth);
			} else if (pos < minXPos) {	
				x.set(minXPos);
			} else {
				x.set(pos);
			}
		}
		xPercentage.set((x.get() - minXPos) / (maxXPos - minXPos - sbWidth));
	}	
	
	public void setYPosition(double pos, boolean increment) {
		if (Double.isNaN(pos)) {
			return;
		}
		if (increment) {
			if (pos + y.get() > maxYPos - sbHeight) {
				y.set(maxYPos - sbHeight);
			} else if (pos + x.get() < minYPos) {	
				y.set(minYPos);
			} else {
				y.set(y.get() + pos);
			}
		} else {
			if (pos > maxYPos - sbHeight) {
				y.set(maxYPos - sbHeight);
			} else if (pos < minYPos) {	
				y.set(minYPos);
			} else {
				y.set(pos);
			}
		}
		yPercentage.set((y.get() - minYPos) / (maxYPos - minYPos - sbHeight));
	}	
	
	@Override
	public void setX(double x) {
		setXPosition(x, false);
	}
	
	@Override
	public void setY(double y) {
		setYPosition(y, false);
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
		gc.fillRect(x.get(), y.get(), sbWidth, sbHeight);
	}
	
	@Override
	public void draw() {		
		if (vg == null) {
			defaultDraw();
		} else {
			vg.draw(x.get(), y.get(), gc);
		}
	}	
	
	@Override
	public void onMouseReleased(MouseEvent e) {
		if (!onNode(e.getX(), e.getY())) {
			hovering = false;
		}
		if (onMouseReleased == null) {
			return;
		}
		onMouseReleased.handle(e);
	}

	@Override
	public boolean onNode(double x, double y) {
		return inScrollBarArea(x, y);
	}
}
