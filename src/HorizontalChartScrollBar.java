import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class HorizontalChartScrollBar {
	public static final long NANO_TO_MILLI = 1000000; 
	
	private Chart chart;
	
	private double xPos = 0;
	private double yPos = 0;
	private boolean dragging = false;
	private boolean hovering = false;
	private boolean clickedInScrollBarArea = false;
	private double initPos = 0;
	private double maxPos;
	private double minPos;
	private double sbWidth;
	private double sbHeight;
	//private boolean vertical;	
	
	public HorizontalChartScrollBar(Chart chart, int dataSize, int numDataPoints, double minPos, double maxPos, double sbWidth, double sbHeight, double pos) {
		this.chart = chart;
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.sbWidth = sbWidth;
		this.sbHeight = sbHeight;
		xPos = minPos;
		yPos = pos;
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
							chart.drawChart();
						} else {
							setPosition(-(sbWidth / 2), true);
							chart.drawChart();
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
			chart.disableRoundUp();
		}
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
	
	private void moveChartLeft(boolean fast) {
		int speed = 10;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos;
		int startIndex = chart.startIndex();
		if (chart.drawCandlesticks()) {
			startIndex -= Chart.CNDL_INDX_MOVE_COEF * speed;	
			newHSBPos = (chart.width() - sbWidth - Chart.PRICE_MARGIN) * ((double)startIndex /(chart.data().m1CandlesDataSize(chart.replayMode()) - chart.numCandlesticks() * Chart.END_MARGIN_COEF));
		} else {
			startIndex -= Chart.TICK_INDX_MOVE_COEF * speed;	
			newHSBPos = (chart.width() - sbWidth - Chart.PRICE_MARGIN) * ((double)startIndex /(chart.data().tickDataSize(chart.replayMode()) - chart.numDataPoints() * Chart.END_MARGIN_COEF));
		}			
		chart.setStartIndex(startIndex);
		setPosition(newHSBPos, false);
	}
	
	private void moveChartRight(boolean fast) {
		int speed = 10;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos;
		int startIndex = chart.startIndex();
		if (chart.drawCandlesticks()) {
			startIndex += Chart.CNDL_INDX_MOVE_COEF * speed;
			newHSBPos = (chart.width() - sbWidth - Chart.PRICE_MARGIN) * ((double)startIndex /(chart.data().m1CandlesDataSize(chart.replayMode()) - chart.numCandlesticks() * Chart.END_MARGIN_COEF));
		} else {
			startIndex += Chart.TICK_INDX_MOVE_COEF * speed;	
			newHSBPos = (chart.width() - sbWidth - Chart.PRICE_MARGIN) * ((double)startIndex /(chart.data().tickDataSize(chart.replayMode()) - chart.numDataPoints() * Chart.END_MARGIN_COEF));
		}			
		chart.setStartIndex(startIndex);
		setPosition(newHSBPos, false);
	}
	
	private void reduceSBPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveChartLeft(true);
		} else {
			moveChartLeft(false);
		}
	}
	
	private void increaseSBPos(KeyEvent e) {
		if (e.isControlDown()) {
			moveChartRight(true);
		} else {
			moveChartRight(false);
		}
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getCode()) {
			case KeyCode.LEFT:				
				reduceSBPos(e);
				Chart.drawCharts(chart.name());
				break;
			case KeyCode.RIGHT:				
				increaseSBPos(e);
				Chart.drawCharts(chart.name());
				break;
			default:				
		}
	}
	
	private boolean onScrollBar(double x, double y) {
		if (y <= yPos + sbHeight && y >= yPos) {
			if (x <= xPos + sbWidth && x >= xPos) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean inScrollBarArea(double x, double y) {	
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
		if (Double.isNaN(pos)) {
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
	
	public void drawHSB() {		
		GraphicsContext gc = chart.graphicsContext();
		if (hovering) {			
			if (dragging) {
				gc.setFill(Color.DIMGRAY);
			} else {
				gc.setFill(Color.GRAY);
			}
		} else {
			gc.setFill(Color.DARKGRAY);
		}
		gc.fillRect(xPos, yPos, sbWidth, sbHeight);
	}
}
