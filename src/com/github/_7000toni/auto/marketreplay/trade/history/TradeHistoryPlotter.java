package com.github._7000toni.auto.marketreplay.trade.history;
import java.util.ArrayList;

import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.timeframe.Timeframe;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TradeHistoryPlotter {
	private ChartNode chart;
	
	public TradeHistoryPlotter(ChartNode chartNode) {
		this.chart = chartNode;
	}
	
	public void plotHistory(ArrayList<? extends ITradeHistory> history) {
		GraphicsContext gc = chart.graphicsContext();
		Timeframe tf = chart.timeframe();
		ArrayList<Dataset.DataPair> data = chart.data().tickData();
		for (ITradeHistory h : history) {			
			if (inRange(h)) {
				int enI;
				int exI;
				double x1;
				double x2;
				double y1;
				double y2;
				if (tf.base() && !chart.drawCandlesticks().get()) {
					x1 = ChartNode.CHT_MARGIN + (h.entryIndex() - chart.startIndex()) * chart.xDiff();
					x2 = ChartNode.CHT_MARGIN + (h.exitIndex() - chart.startIndex()) * chart.xDiff();
				} else {
					enI = tf.getIndexContaining(h.entryIndex());
					exI = tf.getIndexContaining(h.exitIndex());	
					double width = chart.candlestickWidth() + chart.candlestickSpacing();
					double offset = chart.candlestickWidth() / 2;
					x1 = ChartNode.CHT_MARGIN + (enI - chart.startIndex()) * width + offset;
					x2 = ChartNode.CHT_MARGIN + (exI - chart.startIndex()) * width + offset;
				}
				y1 = chart.priceToYCoord(data.get(h.entryIndex()).price());
				y2 = chart.priceToYCoord(data.get(h.exitIndex()).price());
				
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
						dy = -(y1 - ChartNode.CHT_MARGIN);
					} else {
						dy = chart.height() + ChartNode.CHT_MARGIN - y1;						
					}
					dx = -(chart.width() + ChartNode.CHT_MARGIN - x1);					
					calcx = -dy / gradient;
					calcy = -gradient * dx;
					
					tempx = x1 + calcx;
					if (gradient > 0) {
						tempxy = ChartNode.CHT_MARGIN;
					} else {
						tempxy = ChartNode.CHT_MARGIN + chart.height();
					}
					grad1 = (-tempxy+y1)/(tempx-x1);
					diff1 = gradient - grad1;
					
					tempy = y1 - calcy;
					tempyx = ChartNode.CHT_MARGIN + chart.width();
					grad2 = (-tempy+y1)/(tempyx-x1);
					diff2 = gradient - grad2;
					if ((Math.abs(diff1) > Math.abs(diff2) && coordsInChart(x1, y1, tempyx, tempy)) || !coordsInChart(x1, y1, tempx, tempxy)) {
						y2 = tempy;
						x2 = tempyx;
					} else {
						y2 = tempxy;
						x2 = tempx;
					}					
				} else if (onlyCloseInRange(h)) {
					if (gradient < 0) {
						dy = -(y2 - ChartNode.CHT_MARGIN);
					} else {
						dy = chart.height() + ChartNode.CHT_MARGIN - y2;						
					}
					dx = -(x2 - ChartNode.CHT_MARGIN);					
					calcx = -dy / gradient;
					calcy = -gradient * dx;
					
					tempx = x2 + calcx;
					if (gradient > 0) {
						tempxy = ChartNode.CHT_MARGIN + chart.height();
					} else {
						tempxy = ChartNode.CHT_MARGIN;
					}
					grad1 = (-y2+tempxy)/(x2-tempx);
					diff1 = gradient - grad1;
					
					tempy = y2 + calcy;
					tempyx = ChartNode.CHT_MARGIN;
					grad2 = (-y2+tempy)/(x2-tempyx);
					diff2 = gradient - grad2;
					if ((Math.abs(diff1) > Math.abs(diff2) && coordsInChart(tempyx, tempy, x2, y2)) || !coordsInChart(tempx, tempxy, x2, y2)) {
						y1 = tempy;
						x1 = tempyx;
					} else {
						y1 = tempxy;
						x1 = tempx;
					}					
				}
				
				
				
				if (h.buy()) {
					gc.setStroke(Color.BLUE);
				} else {
					gc.setStroke(Color.RED);
				}
				gc.strokeLine(x1, y1, x2, y2);
			}
		}
	}
	
	private boolean coordsInChart(double x1, double y1, double x2, double y2) {
		if (x1 >= ChartNode.CHT_MARGIN && x1 <= ChartNode.CHT_MARGIN + chart.width() &&
				y1 >= ChartNode.CHT_MARGIN && y1 <= ChartNode.CHT_MARGIN + chart.height() &&
				x2 >= ChartNode.CHT_MARGIN && x2 <= ChartNode.CHT_MARGIN + chart.width() &&
				y2 >= ChartNode.CHT_MARGIN && y2 <= ChartNode.CHT_MARGIN + chart.height()) {
			return true;
		}
		return false;
	}
	
	private int getTickDataStartIndex() {
		Timeframe tf = chart.timeframe();
		int si;
		if (tf.base() && !chart.drawCandlesticks().get()) {
			si = chart.startIndex();
		} else {
			ArrayList<Dataset.Candlestick> data = tf.data();
			si = data.get(chart.startIndex()).firstTickIndex();		
		}
		return si;
	}
	
	private int getTickDataEndIndex() {
		Timeframe tf = chart.timeframe();
		int ei;
		if (tf.base() && !chart.drawCandlesticks().get()) {
			ei = chart.endIndex();
		} else {
			ArrayList<Dataset.Candlestick> data = tf.data();		
			if (chart.endIndex() + 1 > data.size()) {
				ei = chart.data().tickData().size() - 1;
			} else {
				ei = data.get(chart.endIndex() + 1).firstTickIndex() - 1;
			}
		}
		return ei;
	}
	
	private boolean inRange(ITradeHistory h) {
		int si = getTickDataStartIndex();
		int ei = getTickDataEndIndex();
		if ((h.entryIndex() >= si && h.entryIndex() < ei + 1 || h.exitIndex() >= si && h.exitIndex() < ei + 1) && h.entryIndex() != -1 && h.exitIndex() != -1) {
			return true;
		}
		return false;
	}
	
	private boolean onlyCloseInRange(ITradeHistory h) {
		int si = getTickDataStartIndex();
		int ei = getTickDataEndIndex();
		if ((!(h.entryIndex() >= si && h.entryIndex() < ei + 1) && h.exitIndex() >= si && h.exitIndex() < ei + 1) && h.entryIndex() != -1 && h.exitIndex() != -1) {
			return true;
		}
		return false;
	}
	
	private boolean onlyOpenInRange(ITradeHistory h) {
		int si = getTickDataStartIndex();
		int ei = getTickDataEndIndex();
		if ((h.entryIndex() >= si && h.entryIndex() < ei + 1 && !(h.exitIndex() >= si && h.exitIndex() < ei + 1)) && h.entryIndex() != -1 && h.exitIndex() != -1) {
			return true;
		}
		return false;
	}
}
