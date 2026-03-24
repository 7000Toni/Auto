import javafx.animation.AnimationTimer;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class BrightnessScrollBar extends HorizontalScrollBar {
	
	public BrightnessScrollBar(IScrollBarOwner sbo, double minPos, double maxPos, double sbWidth, double sbHeight, double y) {
		super(sbo, minPos, maxPos, sbWidth, sbHeight, y);
		setVanGogh((x, y2, gc) -> {
			if (hovering) {	
				gc.setFill(Color.GRAY);
			} else {
				gc.setFill(ColourCalculator.grayScale(x, this.minPos, this.maxPos - this.sbWidth));
			}
			if (dragging) {
				gc.setFill(ColourCalculator.grayScale(x, this.minPos, this.maxPos - this.sbWidth));
			} 
			gc.fillOval(x, y2, this.sbWidth, this.sbHeight);
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}
			gc.strokeOval(x, y2, this.sbWidth, this.sbHeight);
		});
	}		
	
	private void drawHSBBar() {
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}		
		gc.strokeRect(minPos + 5, y + 5, maxPos - minPos - 10, 5);
		for (double i = minPos + 6; i < maxPos - 5; i++) {
			gc.setStroke(ColourCalculator.grayScale(i, minPos + 5, maxPos - 6));
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
							double b = ((x - minPos) / (maxPos - minPos - 10)) * 2 - 1;
							ImageSettings.setBrightness(b);
							Chart.drawCharts(null);
							MarketReplayPane.drawReplayPanes();
						} else {
							setPosition(-(sbWidth / 2), true);
							double b = ((x - minPos) / (maxPos - minPos - 10)) * 2 - 1;
							ImageSettings.setBrightness(b);
							Chart.drawCharts(null);
							MarketReplayPane.drawReplayPanes();
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
		double b = ((x - minPos) / (maxPos - minPos - 10)) * 2 - 1;
		ImageSettings.setBrightness(b);
		Chart.drawCharts(null);
		MarketReplayPane.drawReplayPanes();
		if (onMouseDragged == null) {
			return;
		}
		onMouseDragged.handle(e);
	}
}
