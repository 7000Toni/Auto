import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class HorizontalScrollBar {
	private Chart chart;
	private int hsbHeight;
	private int hsbWidth;;
	
	private int position = 0;
	private boolean dragging = false;
	private int initPos = 0;
	
	public HorizontalScrollBar(Chart chart, int hsbHeight, int hsbWidth) {
		this.chart = chart;
		this.hsbHeight = hsbHeight;
		this.hsbWidth = hsbWidth;
		
		chart.getCanvas().setOnMousePressed(e -> {
			if (inScrollBar((int)e.getX(), (int)e.getY())) {					
				dragging = true;
				initPos = (int)e.getX();
				chart.drawChart();
			}
		});
		
		chart.getCanvas().setOnMouseReleased(e -> {
			dragging = false;
			initPos = 0;
			chart.drawChart();
		});
		
		chart.getCanvas().setOnMouseDragged(e -> {			
			if (dragging) {
				int posDiff = (int)e.getX() - initPos;
				if (position + posDiff > 1600 - hsbWidth) {
					position = 1600 - hsbWidth;
				} else if (position + posDiff < 0) {
					position = 0;
				} else {
					position += posDiff;
				}
				initPos = (int)e.getX();
				
				chart.drawChart();
			}
		});
	}
	
	private boolean inScrollBar(int x, int y) {
		int height = (int)chart.getCanvas().getHeight();
		if (y <= height && y >= height - hsbHeight) {
			if (x <= position + hsbWidth && x >= position) {
				return true;
			}
		}
		
		return false;
	}
	
	public int position() {
		return position;
	}
	
	public void drawHSB() {
		GraphicsContext gc = chart.getGraphicsContext();
		if (dragging) {
			gc.setFill(Color.GRAY);
		} else {
			gc.setFill(Color.DARKGRAY);
		}
		double x = position;
		double y = chart.getCanvas().getHeight() - hsbHeight;
		gc.fillRect(x, y, hsbWidth, hsbHeight);
		gc.setFill(Color.BLACK);
	}
}
