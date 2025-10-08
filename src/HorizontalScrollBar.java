import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public abstract class HorizontalScrollBar implements Drawable {
	protected ScrollBarOwner sbo;
	
	public static final long NANO_TO_MILLI = 1000000; 
	
	protected double xPos = 0;
	protected double yPos = 0;
	protected boolean dragging = false;
	protected boolean dragged = false;
	protected boolean hovering = false;
	protected boolean clickedInScrollBarArea = false;
	protected double initPos = 0;
	protected double maxPos;
	protected double minPos;
	protected double sbWidth;
	protected double sbHeight;
	
	public HorizontalScrollBar(ScrollBarOwner sbo, int dataSize, double minPos, double maxPos, double sbWidth, double sbHeight, double yPos) {
		this.sbo = sbo;
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.sbWidth = sbWidth;
		this.sbHeight = sbHeight;
		this.xPos = minPos;
		this.yPos = yPos;
	}
	
	public double sbWidth() {
		return this.sbWidth;
	}
	
	public double sbHeight() {
		return this.sbHeight;
	}
	
	public void onMouseReleased() {
		dragging = false;
		clickedInScrollBarArea = false;
	}
	
	public void onMousePressed(MouseEvent e) {
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
						if (initPos > xPos) {
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
					
					if (initPos >= xPos && initPos <= xPos + sbWidth) {
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
	
	public void onMouseExited() {
		if (!dragging) {
			hovering = false;
		}
	}
	
	public void onMouseMoved(MouseEvent e) {
		if (onScrollBar(e.getX(), e.getY())) {					
			hovering = true;
		} else {
			hovering = false;
		}
	}
	
	public void onMouseDragged(MouseEvent e) {
		if (dragging) {
			double posDiff = e.getX() - initPos;
			if (xPos + posDiff > maxPos - sbWidth) {
				xPos = maxPos - sbWidth;
			} else if (xPos + posDiff < minPos) {
				xPos = minPos;
			} else {
				xPos += posDiff;
			}
			initPos = (int)e.getX();
			dragged = true;
		}
	}
	
	public double maxPos() {
		return this.maxPos;
	}
	
	public double minPos() {
		return this.minPos;
	}
	
	public boolean dragged() {
		boolean ret = this.dragged;
		this.dragged = false;
		return ret;
	}
	
	public void setMaxPos(double maxPos) {
		this.maxPos = maxPos;
	}
	
	public void setMinPos(double minPos) {
		this.minPos = minPos;
	}
	
	public void setXPos(double xPos) {
		setPosition(xPos, false);
	}

	public void setYPos(double yPos) {
		this.yPos = yPos;
	}
	
	protected abstract void moveOwnerLeft(boolean fast);
	
	protected abstract void moveOwnerRight(boolean fast);
	
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
				sbo.draw();;
				break;
			case KeyCode.RIGHT:				
				increaseSBPos(e);
				sbo.draw();
				break;
			default:				
		}
	}
	
	protected boolean onScrollBar(double x, double y) {
		if (y <= yPos + sbHeight && y >= yPos) {
			if (x <= xPos + sbWidth && x >= xPos) {
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean inScrollBarArea(double x, double y) {	
		if (y <= yPos + sbHeight && y >= yPos) {
			if (x <= maxPos && x >= minPos) {				
				return true;
			}
		}
		
		return false;
	}
	
	public double xPos() {
		return this.xPos;
	}
	
	public double yPos() {
		return this.yPos;
	}
	
	public void setPosition(double pos, boolean increment) {
		if (Double.isNaN(pos) || dragging) {
			return;
		}
		if (increment) {
			if (pos + xPos > maxPos - sbWidth) {
				xPos = maxPos - sbWidth;
			} else if (pos + xPos < minPos) {	
				xPos = minPos;
			} else {
				xPos += pos;
			}
		} else {
			if (pos > maxPos - sbWidth) {
				xPos = maxPos - sbWidth;
			} else if (pos < minPos) {	
				xPos = minPos;
			} else {
				xPos = pos;
			}
		}
	}		
	
	public void draw() {		
		GraphicsContext gc = sbo.graphicsContext();
		if (hovering) {	
			gc.setFill(Color.GRAY);
		} else {
			gc.setFill(Color.DARKGRAY);
		}
		if (dragging) {
			gc.setFill(Color.DIMGRAY);
		} 
		gc.fillRect(xPos, yPos, sbWidth, sbHeight);
	}
}
