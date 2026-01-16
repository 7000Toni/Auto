import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public abstract class HorizontalScrollBar implements CanvasNode {
	protected ScrollBarOwner sbo;
	
	public static final long NANO_TO_MILLI = 1000000; 
	
	protected double x = 0;
	protected double y = 0;
	protected boolean dragging = false;
	protected boolean hovering = false;
	protected boolean clickedInScrollBarArea = false;
	protected double initPos = 0;
	protected double maxPos;
	protected double minPos;
	protected double sbWidth;
	protected double sbHeight;
	protected GraphicsContext gc;
	
	private EventHandler<? super MouseEvent> onMouseDragged;
	private EventHandler<? super MouseEvent> onMouseEntered;
	private EventHandler<? super MouseEvent> onMouseExited;
	private EventHandler<? super MouseEvent> onMousePressed;
	private EventHandler<? super MouseEvent> onMouseReleased;
	private EventHandler<? super MouseEvent> onMouseMoved;
	private EventHandler<? super ScrollEvent> onScroll;
	
	public HorizontalScrollBar(ScrollBarOwner sbo, int dataSize, double minPos, double maxPos, double sbWidth, double sbHeight, double y) {
		this.sbo = sbo;
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.sbWidth = sbWidth;
		this.sbHeight = sbHeight;
		this.x = minPos;
		this.y = y;
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
					
					if (initPos >= x && initPos <= x + sbWidth) {
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
			if (x + posDiff > maxPos - sbWidth) {
				x = maxPos - sbWidth;
			} else if (x + posDiff < minPos) {
				x = minPos;
			} else {
				x += posDiff;
			}
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
	}
	
	public void setMinPos(double minPos) {
		this.minPos = minPos;
	}
	
	@Override
	public void setX(double x) {
		setPosition(x, false);
	}

	@Override
	public void setY(double y) {
		this.y = y;
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
	
	@Override
	public GraphicsContext graphicsContext() {
		return this.gc;
	}
	
	@Override
	public void setGraphicsContext(GraphicsContext gc) {
		this.gc = gc;
	}
	
	@Override
	public double x() {
		return this.x;
	}
	
	@Override
	public double y() {
		return this.y;
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
	}		
	
	@Override
	public void draw() {		
		if (hovering) {	
			gc.setFill(Color.GRAY);
		} else {
			gc.setFill(Color.DARKGRAY);
		}
		if (dragging) {
			gc.setFill(Color.DIMGRAY);
		} 
		gc.fillRect(x, y, sbWidth, sbHeight);
	}	

	@Override
	public void onMouseDragged(MouseEvent e) {
		if (onMouseDragged == null) {
			return;
		}
		onMouseDragged.handle(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onMouseEntered == null) {
			return;
		}
		onMouseEntered.handle(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		if (onMouseExited == null) {
			return;
		}
		onMouseExited.handle(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		if (onMousePressed == null) {
			return;
		}
		onMousePressed.handle(e);
	}

	@Override
	public void onMouseReleased(MouseEvent e) {
		if (onMouseReleased == null) {
			return;
		}
		onMouseReleased.handle(e);
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		if (onMouseMoved == null) {
			return;
		}
		onMouseMoved.handle(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		if (onScroll == null) {
			return;
		}
		onScroll.handle(e);
	}

	@Override
	public void setOnMouseDragged(EventHandler<? super MouseEvent> e) {
		onMouseDragged = e;
	}

	@Override
	public void setOnMouseEntered(EventHandler<? super MouseEvent> e) {
		onMouseEntered = e;
	}

	@Override
	public void setOnMouseExited(EventHandler<? super MouseEvent> e) {
		onMouseExited = e;
	}

	@Override
	public void setOnMousePressed(EventHandler<? super MouseEvent> e) {
		onMousePressed = e;
	}

	@Override
	public void setOnMouseReleased(EventHandler<? super MouseEvent> e) {
		onMouseReleased = e;
	}

	@Override
	public void setOnMouseMoved(EventHandler<? super MouseEvent> e) {
		onMouseMoved = e;
	}

	@Override
	public void setOnScroll(EventHandler<? super ScrollEvent> e) {
		onScroll = e;
	}

	@Override
	public boolean onNode(double x, double y) {
		return inScrollBarArea(x, y);
	}
}
