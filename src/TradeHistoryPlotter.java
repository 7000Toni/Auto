import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TradeHistoryPlotter {
	private Chart chart;
	
	public TradeHistoryPlotter(Chart chart) {
		this.chart = chart;
	}
	
	public void plotHistory(ArrayList<TradeHistoryLoader.ApproxTradeHistory> history) {
		if (chart.drawCandlesticks().get()) {
			return;
		}
		GraphicsContext gc = chart.graphicsContext();
		ArrayList<DataSet.DataPair> data = chart.data().tickData();
		for (TradeHistoryLoader.ApproxTradeHistory h : history) {
			if (inRange(h)) {
				double x1 = Chart.CHT_MARGIN + (h.openIndex - chart.startIndex()) * chart.xDiff();
				double x2 = Chart.CHT_MARGIN + (h.closeIndex - chart.startIndex()) * chart.xDiff();
				double y1 = chart.priceToYCoord(data.get(h.openIndex).price());
				double y2 = chart.priceToYCoord(data.get(h.closeIndex).price());
				
				double gradient = (-y2+y1)/(x2-x1);				
				double dy = 0;
				double dx = 0;
				double calcx;
				double calcy;
				double tempx;
				double tempxy;
				double grad1;
				double diff1;
				double tempy;
				double tempyx;
				double grad2;
				double diff2;
				if (onlyOpenInRange(h)) {
					if (gradient > 0) {
						dy = -(y1 - Chart.CHT_MARGIN);
					} else {
						dy = chart.chartHeight() + Chart.CHT_MARGIN - y1;						
					}
					dx = -(chart.chartWidth() + Chart.CHT_MARGIN - x1);					
					calcx = -dy / gradient;
					calcy = -gradient * dx;
					
					tempx = x1 + calcx;
					if (gradient > 0) {
						tempxy = Chart.CHT_MARGIN;
					} else {
						tempxy = Chart.CHT_MARGIN + chart.chartHeight();
					}
					grad1 = (-tempxy+y1)/(tempx-x1);
					diff1 = gradient - grad1;
					
					tempy = y1 - calcy;
					tempyx = Chart.CHT_MARGIN + chart.chartWidth();
					grad2 = (-tempy+y1)/(tempyx-x1);
					diff2 = gradient - grad2;
					//System.out.println(dy + " " + y1 + " " + dx + " " + x1 + " " + calcx + " " + calcy + " " + gradient);
					//System.out.println(gradient + " " + grad1 + " " + grad2 + " " + diff1 + " " + diff2 + "\n");
					if ((Math.abs(diff1) > Math.abs(diff2) && coordsInChart(x1, y1, tempyx, tempy)) || !coordsInChart(x1, y1, tempx, tempxy)) {
						y2 = tempy;
						x2 = tempyx;
					} else {
						y2 = tempxy;
						x2 = tempx;
					}					
				} else if (onlyCloseInRange(h)) {
					if (gradient < 0) {
						dy = -(y2 - Chart.CHT_MARGIN);
					} else {
						dy = chart.chartHeight() + Chart.CHT_MARGIN - y2;						
					}
					dx = -(x2 - Chart.CHT_MARGIN);					
					calcx = -dy / gradient;
					calcy = -gradient * dx;
					
					tempx = x2 + calcx;
					if (gradient > 0) {
						tempxy = Chart.CHT_MARGIN + chart.chartHeight();
					} else {
						tempxy = Chart.CHT_MARGIN;
					}
					grad1 = (-y2+tempxy)/(x2-tempx);
					diff1 = gradient - grad1;
					
					tempy = y2 + calcy;
					tempyx = Chart.CHT_MARGIN;
					grad2 = (-y2+tempy)/(x2-tempyx);
					diff2 = gradient - grad2;
					//System.out.println(dy + " " + y2 + " " + dx + " " + x2 + " " + calcx + " " + calcy + " " + gradient);
					//System.out.println(gradient + " " + grad1 + " " + grad2 + " " + diff1 + " " + diff2 + "\n");
					if ((Math.abs(diff1) > Math.abs(diff2) && coordsInChart(tempyx, tempy, x2, y2)) || !coordsInChart(tempx, tempxy, x2, y2)) {
						y1 = tempy;
						x1 = tempyx;
					} else {
						y1 = tempxy;
						x1 = tempx;
					}					
				}
				
				
				
				if (h.buy) {
					gc.setStroke(Color.BLUE);
				} else {
					gc.setStroke(Color.RED);
				}
				gc.strokeLine(x1, y1, x2, y2);
			}
		}
	}
	
	private boolean coordsInChart(double x1, double y1, double x2, double y2) {
		if (x1 >= Chart.CHT_MARGIN && x1 <= Chart.CHT_MARGIN + chart.chartWidth() &&
				y1 >= Chart.CHT_MARGIN && y1 <= Chart.CHT_MARGIN + chart.chartHeight() &&
				x2 >= Chart.CHT_MARGIN && x2 <= Chart.CHT_MARGIN + chart.chartWidth() &&
				y2 >= Chart.CHT_MARGIN && y2 <= Chart.CHT_MARGIN + chart.chartHeight()) {
			return true;
		}
		return false;
	}
	
	private boolean inRange(TradeHistoryLoader.ApproxTradeHistory h) {
		if (h.openIndex >= chart.startIndex() && h.openIndex < chart.endIndex() + 1 || h.closeIndex >= chart.startIndex() && h.closeIndex < chart.endIndex() + 1) {
			return true;
		}
		return false;
	}
	
	private boolean onlyCloseInRange(TradeHistoryLoader.ApproxTradeHistory h) {
		if (!(h.openIndex >= chart.startIndex() && h.openIndex < chart.endIndex() + 1) && h.closeIndex >= chart.startIndex() && h.closeIndex < chart.endIndex() + 1) {
			return true;
		}
		return false;
	}
	
	private boolean onlyOpenInRange(TradeHistoryLoader.ApproxTradeHistory h) {
		if (h.openIndex >= chart.startIndex() && h.openIndex < chart.endIndex() + 1 && !(h.closeIndex >= chart.startIndex() && h.closeIndex < chart.endIndex() + 1)) {
			return true;
		}
		return false;
	}
}
