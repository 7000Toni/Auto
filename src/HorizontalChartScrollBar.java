import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class HorizontalChartScrollBar extends HorizontalScrollBar {
	
	public HorizontalChartScrollBar(Chart chart, int dataSize, double minPos, double maxPos, double sbWidth, double sbHeight, double yPos) {
		super(chart, dataSize, minPos, maxPos, sbWidth, sbHeight, yPos);
	}
	
	@Override
	public void onMousePressed(MouseEvent e) {
		if (((Chart) sbo).replayMode()) {
			if (onScrollBar(e.getX(), e.getY())) {
				dragging = true;
				initPos = e.getX();
			}
		} else if (onScrollBar(e.getX(), e.getY())) {					
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
							((Chart) sbo).setKeepStartIndex(false);
							sbo.draw();
						} else {
							setPosition(-(sbWidth / 2), true);
							((Chart) sbo).setKeepStartIndex(false);
							sbo.draw();
						}
					} 
				}
			}.start();
		}
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
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
			((Chart) sbo).setKeepStartIndex(false);
			dragged = true;
		}
	}
	
	@Override
	protected void moveOwnerLeft(boolean fast) {
		int speed = 10;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos;
		int startIndex = ((Chart) sbo).startIndex();
		if (((Chart) sbo).drawCandlesticks()) {
			startIndex -= Chart.CNDL_INDX_MOVE_COEF * speed;	
			newHSBPos = (((Chart) sbo).width() - sbWidth - Chart.PRICE_MARGIN) * ((double)startIndex /(((Chart) sbo).data().m1CandlesDataSize(((Chart) sbo).replayMode()) - ((Chart) sbo).numCandlesticks() * Chart.END_MARGIN_COEF));
			((Chart) sbo).setKeepStartIndex(false);
		} else {
			startIndex -= Chart.TICK_INDX_MOVE_COEF * speed;	
			newHSBPos = (((Chart) sbo).width() - sbWidth - Chart.PRICE_MARGIN) * ((double)startIndex /(((Chart) sbo).data().tickDataSize(((Chart) sbo).replayMode()) - ((Chart) sbo).numDataPoints() * Chart.END_MARGIN_COEF));
			((Chart) sbo).setKeepStartIndex(false);
		}			
		((Chart) sbo).setKeepStartIndex(false);
		setPosition(newHSBPos, false);
	}
	
	@Override
	protected void moveOwnerRight(boolean fast) {
		int speed = 10;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos;
		int startIndex = ((Chart) sbo).startIndex();
		if (((Chart) sbo).drawCandlesticks()) {
			startIndex += Chart.CNDL_INDX_MOVE_COEF * speed;
			newHSBPos = (((Chart) sbo).width() - sbWidth - Chart.PRICE_MARGIN) * ((double)startIndex /(((Chart) sbo).data().m1CandlesDataSize(((Chart) sbo).replayMode()) - ((Chart) sbo).numCandlesticks() * Chart.END_MARGIN_COEF));
			((Chart) sbo).setKeepStartIndex(false);
		} else {
			startIndex += Chart.TICK_INDX_MOVE_COEF * speed;	
			newHSBPos = (((Chart) sbo).width() - sbWidth - Chart.PRICE_MARGIN) * ((double)startIndex /(((Chart) sbo).data().tickDataSize(((Chart) sbo).replayMode()) - ((Chart) sbo).numDataPoints() * Chart.END_MARGIN_COEF));
			((Chart) sbo).setKeepStartIndex(false);
		}			
		((Chart) sbo).setKeepStartIndex(false);
		setPosition(newHSBPos, false);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getCode()) {
			case KeyCode.LEFT:				
				reduceSBPos(e);
				Chart.drawCharts(((Chart) sbo).name());
				break;
			case KeyCode.RIGHT:				
				increaseSBPos(e);
				Chart.drawCharts(((Chart) sbo).name());
				break;
			default:				
		}
	}
}
