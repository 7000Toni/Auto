import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class HorizontalScrollBar {
	private static final long NANO_TO_MILLI = 1000000; 
	
	private final double HSB_MOVE_INDEX = 5;
	private final double HSB_FAST_MOVE_MULTIPLIER = 10;	
	
	private Chart chart;
	private int hsbHeight;
	private int hsbWidth;
	private double hsbMove;
	
	private double position = 0;	
	private boolean dragging = false;
	private boolean hovering = false;
	private boolean clickedInScrollBarArea = false;
	private double initPos = 0;
	
	public HorizontalScrollBar(Chart chart, int hsbHeight, int hsbWidth, int dataSize, int numDataPoints) {
		this.chart = chart;
		this.hsbHeight = hsbHeight;
		this.hsbWidth = hsbWidth;
		updateHSBMove(dataSize, numDataPoints);
		//this.hsbMove = (HSB_MOVE_INDEX * (chart.getWidth() - hsbWidth)) / (dataSize - numDataPoints - 1);
		if (hsbMove == 0.0) {			
			hsbMove = Double.MIN_VALUE;
		}
	}
	
	public void onMouseReleased() {
		dragging = false;
		clickedInScrollBarArea = false;
	}
	
	public void onMousePressed(MouseEvent e) {
		if (inScrollBar(e.getX(), e.getY())) {					
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
						if (initPos > position) {
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
					
					if (initPos >= position && initPos <= position + hsbWidth) {
						this.stop();
					}
					
					if (now - lastTick >= NANO_TO_MILLI*16) {						
						lastTick = now;		
						if (add) {
							setPosition(position + hsbWidth / 2);
							chart.drawChart();
						} else {
							setPosition(position - hsbWidth / 2);
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
		if (inScrollBar(e.getX(), e.getY())) {					
			hovering = true;
		} else {
			hovering = false;
		}
	}
	
	public void onMouseDragged(MouseEvent e) {
		if (dragging) {
			double posDiff = e.getX() - initPos;
			if (position + posDiff > chart.getWidth() - hsbWidth - Chart.PRICE_MARGIN) {
				position = chart.getWidth() - hsbWidth - Chart.PRICE_MARGIN;
			} else if (position + posDiff < 0) {
				position = 0;
			} else {
				position += posDiff;
			}
			initPos = (int)e.getX();
			chart.disableRoundUp();
		}
	}
	
	public void updateHSBMove(int dataSize, int numDataPoints) {	
		hsbMove = (HSB_MOVE_INDEX * (chart.getWidth() - hsbWidth)) / (dataSize - numDataPoints - 1);
		if (!chart.drawCandlesticks()) {
			hsbMove *= 10;
		}
	}
	
	public double hsbMove() {
		return hsbMove;
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getCode()) {
			case KeyCode.LEFT:				
				if (e.isControlDown()) {
					if (position >= hsbMove*HSB_FAST_MOVE_MULTIPLIER) {
						position -= hsbMove*HSB_FAST_MOVE_MULTIPLIER;
					} else {
						position = 0;
					}
				} else {
					if (position >= hsbMove) {
						position -= hsbMove;
					} else {
						position = 0;
					}
				}
				Chart.drawCharts();
				break;
			case KeyCode.RIGHT:				
				if (e.isControlDown()) {
					if (position <= chart.getWidth() - hsbWidth - Chart.PRICE_MARGIN - hsbMove*HSB_FAST_MOVE_MULTIPLIER) {
						position += hsbMove*HSB_FAST_MOVE_MULTIPLIER;
					} else {
						position = chart.getWidth() - hsbWidth - Chart.PRICE_MARGIN;
					}
				} else {
					if (position <= chart.getWidth() - hsbWidth - Chart.PRICE_MARGIN - hsbMove) {
						position += hsbMove;
					} else {
						position = chart.getWidth() - hsbWidth - Chart.PRICE_MARGIN;
					}
				}
				Chart.drawCharts();
				break;
			default:				
		}
	}
	
	private boolean inScrollBar(double x, double y) {
		double height = chart.getCanvas().getHeight();
		if (y <= height && y >= height - hsbHeight) {
			if (x <= position + hsbWidth && x >= position) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean inScrollBarArea(double x, double y) {		
		double height = chart.getCanvas().getHeight();
		if (y <= height && y >= height - hsbHeight) {
			if (x <= chart.getWidth() - Chart.PRICE_MARGIN && x >= 0) {				
				return true;
			}
		}
		
		return false;
	}
	
	public double position() {
		return position;
	}
	
	public void setPosition(double position) {
		if (position > chart.getWidth() - hsbWidth - Chart.PRICE_MARGIN) {
			this.position = chart.getWidth() - hsbWidth - Chart.PRICE_MARGIN;
		} else if (position < 0) {
			this.position = 0;
		} else {
			this.position = position;
		}
	}
	
	public void drawHSB() {
		GraphicsContext gc = chart.getGraphicsContext();
		if (hovering) {			
			if (dragging) {
				gc.setFill(Color.DIMGRAY);
			} else {
				gc.setFill(Color.GRAY);
			}
		} else {
			gc.setFill(Color.DARKGRAY);
		}
		double x = position;
		double y = chart.getCanvas().getHeight() - hsbHeight;
		gc.fillRect(x, y, hsbWidth, hsbHeight);
		gc.setFill(Color.BLACK);
	}
}
