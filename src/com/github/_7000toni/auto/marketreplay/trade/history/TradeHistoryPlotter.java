package com.github._7000toni.auto.marketreplay.trade.history;
import java.util.ArrayList;

import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.timeframe.Timeframe;
import com.github._7000toni.auto.marketreplay.trade.Trade;
import com.github._7000toni.auto.miscellaneous.Round;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TradeHistoryPlotter {	
	private ChartNode chart;
	private static BooleanProperty detailed = new SimpleBooleanProperty(false);
	private boolean noSpace = false;
	
	public TradeHistoryPlotter(ChartNode chartNode) {
		this.chart = chartNode;
	}
	
	public void plotHistory(ArrayList<? extends ITradeHistory> history) {
		ITradeHistory lastTrade = null;
		for (ITradeHistory h : history) {			
			if (inRange(h)) {
				drawLine(h, lastTrade);
				lastTrade = h;
			}
		}
	}
	
	public static ReadOnlyBooleanProperty detailed() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(detailed);
	}
	
	public static void toggleDetailed() {
		detailed.set(!detailed.get());
	}
	
	private void drawLine(ITradeHistory h, ITradeHistory lastTrade) {
		GraphicsContext gc = chart.graphicsContext();
		Timeframe tf = chart.timeframe();
		ArrayList<Dataset.DataPair> data = chart.data().tickData();
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
			gc.setStroke(ColourSettings.colour(ColourIndex.BUY_HISTORY));
		} else {
			gc.setStroke(ColourSettings.colour(ColourIndex.SELL_HISTORY));
		}
		gc.strokeLine(x1, y1, x2, y2);
		
		boolean draw = false;
		if (lastTrade != null && lastTrade.entryIndex() == h.entryIndex()) {
			if ((chart.data().tickData().get(h.entryIndex()).price() <= chart.data().tickData().get(h.exitIndex()).price())
					!= (chart.data().tickData().get(lastTrade.entryIndex()).price() <= chart.data().tickData().get(lastTrade.exitIndex()).price()) && !noSpace) {
				noSpace = true;
				draw = true;
			}
		} else {
			draw = true;
			noSpace = false;
		}
		if (detailed.get() && draw) {
			if (h.sl() != Trade.NO_VAL) {
				drawSLTP(x1, y1, chart.priceToYCoord(h.sl()), gc);
			}
			if (h.tp() != Trade.NO_VAL) {
				drawSLTP(x1, y1, chart.priceToYCoord(h.tp()), gc);
			}
			drawTradeInfo(h, gc, x1);
		}
	}
	
	private void drawTradeInfo(ITradeHistory h, GraphicsContext gc, double x) {
		double fs = 15;
		double y;
		double entryPrice = chart.data().tickData().get(h.entryIndex()).price();
		double exitPrice = chart.data().tickData().get(h.exitIndex()).price();
		if (h.tp() != Trade.NO_VAL) {
			y = chart.priceToYCoord(h.tp()) + (h.buy()?fs:-fs*2);
		} else if (h.sl() != Trade.NO_VAL) {
			y = chart.priceToYCoord(h.sl()) + (h.buy()?-fs*2:fs);
		} else {
			y = chart.priceToYCoord(entryPrice) + (entryPrice>exitPrice?-fs*2:fs);
		}
		x += 2;		
		double profit = Round.round((exitPrice - entryPrice) * h.volume(), 2) * (h.buy()?1:-1);
		double risk = Round.round(Math.abs((entryPrice - h.sl()) * h.volume()), 2);
		double rr = Round.round(profit / risk, 2);
		if (h.buy()) {
			gc.setFill(ColourSettings.colour(ColourIndex.BUY_HISTORY));
		} else {
			gc.setFill(ColourSettings.colour(ColourIndex.SELL_HISTORY));
		}
		String info = (h.sl()==Trade.NO_VAL?"REWARD/RISK: N/A":("REWARD/RISK: " + ((Double)rr).toString())) + "\n";
		info += (h.sl()==Trade.NO_VAL?"RISK: N/A":("RISK: " + ((Double)risk).toString())) + "\n";
		info += (h.volume()==Trade.NO_VAL?"PROFIT: N/A":"PROFIT: " + ((Double)profit).toString());		
		Font oldFont = gc.getFont();			
		gc.setFont(Font.font("TW Cen MT Condensed", FontWeight.NORMAL, fs));					
		if (hasRoom(gc.getFont(), info, x, y)) {
			gc.fillText(info, x, y);
		}
		gc.setFont(oldFont);		
	}
	
	private boolean hasRoom(Font f, String info, double x, double y) {
		Text t = new Text(info);
		t.setFont(f);
		double width = t.getLayoutBounds().getWidth();
		double height = f.getSize() * 2 + 3;
		return x + width < ChartNode.CHT_MARGIN + chart.width() && y + height < ChartNode.CHT_MARGIN + chart.height();
	}
	
	private void drawSLTP(double x1, double y1, double sltpY, GraphicsContext gc) {
		if (sltpY < ChartNode.CHT_MARGIN + chart.height() && sltpY > ChartNode.CHT_MARGIN) {
			drawDashedLine((int)x1 + 0.5, y1, (int)x1 + 0.5, sltpY, gc);
		} else {
			if (sltpY < ChartNode.CHT_MARGIN) {
				drawDashedLine((int)x1 + 0.5, y1, (int)x1 + 0.5, ChartNode.CHT_MARGIN, gc);
			} else {
				drawDashedLine((int)x1 + 0.5, y1, (int)x1 + 0.5, ChartNode.CHT_MARGIN + chart.height(), gc);
			}
		}
	}
	
	private void drawDashedLine(double x1, double y1, double x2, double y2, GraphicsContext gc) {
		gc.setLineDashes(4, 4);
		gc.strokeLine(x1, y1, x2, y2);
		gc.setLineDashes(null);
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
