import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class HorizontalScrollBar {
	private final double HSB_MOVE_INDEX = 5;
	private final double HSB_FAST_MOVE_MULTIPLIER = 10;	
	
	private Chart chart;
	private int hsbHeight;
	private int hsbWidth;
	private double hsbMove;
	
	private double position = 0;	
	private boolean dragging = false;
	private boolean hovering = false;
	private int initPos = 0;
	
	public HorizontalScrollBar(Chart chart, int hsbHeight, int hsbWidth, int dataSize, int numDataPoints) {
		this.chart = chart;
		this.hsbHeight = hsbHeight;
		this.hsbWidth = hsbWidth;
		this.hsbMove = (HSB_MOVE_INDEX * (chart.getWidth() - hsbWidth))/(dataSize - numDataPoints - 1);
		if (hsbMove == 0.0) {
			hsbMove = Double.MIN_VALUE;
		}
		
		chart.getCanvas().setOnMouseExited(e -> {
			if (!dragging) {
				hovering = false;
			}
			chart.onMouseExited();
		});
		
		chart.getCanvas().setOnMouseEntered(e -> {
			chart.onMouseEntered();
		});
		
		chart.getCanvas().setOnMouseMoved(e -> {			
			if (inScrollBar(e.getX(), e.getY())) {					
				hovering = true;
			} else {
				hovering = false;
			}
			chart.onMouseMoved(e);
		});		
		
		chart.getCanvas().setOnMousePressed(e -> {
			if (inScrollBar(e.getX(), e.getY())) {					
				dragging = true;
				initPos = (int)e.getX();
			}
			chart.onMousePressed(e);
		});
		
		chart.getCanvas().setOnMouseReleased(e -> {
			if (dragging) {
				dragging = false;
				initPos = 0;
			}
			chart.onMouseReleased(e);
		});
		
		chart.getCanvas().setOnMouseDragged(e -> {			
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
			chart.onMouseDragged(e);
		});		
		
		chart.getCanvas().setOnScroll(e -> {
			chart.onScroll(e);
		});
	}
	
	public void updateHSBMove(int dataSize, int numDataPoints) {
		this.hsbMove = (HSB_MOVE_INDEX * (chart.getWidth() - hsbWidth))/(dataSize - numDataPoints - 1);
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
		int height = (int)chart.getCanvas().getHeight();
		if (y <= height && y >= height - hsbHeight) {
			if (x <= position + hsbWidth && x >= position) {
				return true;
			}
		}
		
		return false;
	}
	
	public double position() {
		return position;
	}
	
	public void setPosition(double position) {
		this.position = position;
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
