package com.github._7000toni.auto.chart;
import java.util.ArrayList;

import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.Dataset.Candlestick;
import com.github._7000toni.auto.dataset.Dataset.DataPair;
import com.github._7000toni.auto.dataset.timeframe.Timeframe;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;
import com.github._7000toni.auto.settings.MiscellaneousSettings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class CrossHair {
	public static final int DATE_BAR_MARGIN = 5;
	
	private static DoubleProperty x = new SimpleDoubleProperty();
	private static DoubleProperty y = new SimpleDoubleProperty();
	private static DoubleProperty price = new SimpleDoubleProperty(0);
	private static IntegerProperty dateIndex = new SimpleIntegerProperty(0);
	private static IntegerProperty tickIndex = new SimpleIntegerProperty(0);	
	private static BooleanProperty tickBased = new SimpleBooleanProperty(false);
	private static StringProperty name = new SimpleStringProperty();
	private static Timeframe tf;
	
	private ChartNode chart;
	private GraphicsContext gc;
	private double dateBarHalfWidth;
	private double dateBarX;
	private String ohlc;
	private IntegerProperty unfocusedDateIndex = new SimpleIntegerProperty(0);
	private Timeframe unfocusedTf;

	public CrossHair(ChartNode chart) {
		this.chart = chart;
		this.gc = chart.graphicsContext();
		this.dateBarHalfWidth = chart.fontSize() * 5;
	}

	public ReadOnlyIntegerProperty unfocusedDateIndex() {
		return IntegerProperty.readOnlyIntegerProperty(unfocusedDateIndex);
	}
	
	public static double price() {
		return CrossHair.price.get();
	}
	
	public static ReadOnlyIntegerProperty dateIndex() {
		return IntegerProperty.readOnlyIntegerProperty(dateIndex);
	}
	
	public static ReadOnlyBooleanProperty tickBased() {
		return BooleanProperty.readOnlyBooleanProperty(tickBased);
	}
	
	public static void setX(double x) {
		CrossHair.x.set(x);
	}
	
	public static void setY(double y) {
		CrossHair.y.set(y);
	}
	
	public static void setPrice(double price) {
		CrossHair.price.set(price);
	}
	
	public static void setDateIndex(int dateIndex) {
		CrossHair.dateIndex.set(dateIndex);
	}
	
	public static void setTickBased(boolean tickBased) {
		CrossHair.tickBased.set(tickBased);
	}
	
	public static void setName(String name) {
		CrossHair.name.set(name);
	}
	
	public String ohlc() {
		return ohlc;
	}
	
	public void resetOHLC() {
		ohlc = null;
	}
	
	@Override
	public String toString() {
		String ret = "name: " + name.get() + '\n'; 
		ret += "price: " + price.get() + '\n';
		ret += "tickBased: " + tickBased.get() + '\n';
		ret += "dateIndex: " + dateIndex.get() + '\n';
		ret += "x: " + x + '\n';
		ret += "y: " + y + '\n';
		return ret;
	}
	
	private String setDateBarX(double xPos, int index) {
		Text t = null;
		Timeframe tf = chart.timeframe();
		if (index == -1) {
			t = new Text("DONTBEDUMB");
		} else if (chart.drawCandlesticks().get() || !tf.base()) {
			t = new Text(tf.data().get(index).dateTime().toString().replace('T', ' '));
		} else {
			ArrayList<DataPair> data = chart.data().tickData();
			t = new Text(data.get(index).dateTime().toString().replace('T', ' '));
		}
		t.setFont(gc.getFont());
		dateBarHalfWidth = (t.getLayoutBounds().getWidth() / 2) + DATE_BAR_MARGIN;
		if (xPos < ChartNode.CHT_MARGIN + dateBarHalfWidth) {
			dateBarX = ChartNode.CHT_MARGIN;
		} else if (xPos > chart.width() + ChartNode.CHT_MARGIN - dateBarHalfWidth) {
			dateBarX = chart.width() + ChartNode.CHT_MARGIN - dateBarHalfWidth * 2;
		} else {
			dateBarX = xPos - dateBarHalfWidth;
		}
		return t.textProperty().get();
	}
	
	public void setOHLC(Dataset.Candlestick candle) {
		ohlc = "O: " + candle.open();
		ohlc += "  H: " + candle.high();
		ohlc += "  L: " + candle.low();
		ohlc += "  C: " + candle.close();
	}
	
	private void drawHorizontalLine(boolean focusedChart) {
		double yPos;
		if (focusedChart) {
			yPos = y.get();
			if (yPos <= ChartNode.CHT_MARGIN || yPos >= ChartNode.CHT_MARGIN + chart.height()) {
				chart.setFocusedChart(false);
				return;
			}
		} else {
			yPos = (int)(((chart.highest() + chart.dataMarginTickSize() - price.get()) / (chart.range() + chart.dataMarginTickSize() * 2)) * chart.height() + ChartNode.CHT_MARGIN);			
		}
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.strokeLine(ChartNode.CHT_MARGIN, yPos+0.5, ChartNode.CHT_MARGIN + chart.width(), yPos+0.5);
		drawPriceBox(yPos);
	}
	
	private void drawPriceBox(double yPos) {
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.fillRoundRect(chart.width() + ChartNode.CHT_MARGIN, yPos - chart.fontSize()/2, chart.chart().priceMargin().width(), chart.fontSize(), MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		if (Chart.darkMode().get()) {
			gc.setFill(Color.BLACK);
		} else {
			gc.setFill(Color.WHITE);
		}
		gc.fillText(((Double)(chart.roundToNearestTick(price.get()))).toString(), chart.width() + ChartNode.CHT_MARGIN + PriceMargin.EXTRA_SPACE/2, yPos + chart.fontSize()/3, chart.chart().priceMargin().width() - PriceMargin.EXTRA_SPACE);
	}
	
	private void drawVerticalLine(double xPos, int index) {
		xPos = (int)xPos;
		if (xPos < ChartNode.CHT_MARGIN || xPos > ChartNode.CHT_MARGIN + chart.width()) {
			chart.setFocusedChart(false);
			return;
		}
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.strokeLine(xPos+0.5, ChartNode.CHT_MARGIN, xPos+0.5, chart.height() + ChartNode.CHT_MARGIN - 0.5);		
		drawDateBox(index, setDateBarX(xPos, index));
	}
	
	private void drawDateBox(int index, String text) {	
		gc.setFill(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.fillRoundRect(dateBarX, chart.height() + ChartNode.CHT_MARGIN - chart.fontSize(), dateBarHalfWidth*2, chart.fontSize(), MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		if (Chart.darkMode().get()) {
			gc.setFill(Color.BLACK);
		} else {
			gc.setFill(Color.WHITE);
		}
		gc.fillText(text, dateBarX + DATE_BAR_MARGIN, chart.height() + ChartNode.CHT_MARGIN - 1, (dateBarHalfWidth + DATE_BAR_MARGIN) * 2);
	}
	
	private double getWidth() {
		double width = chart.width();
		if (chart.endMargin()) {
			if (chart.drawCandlesticks().get()) {
				width = (chart.candlestickWidth() + chart.candlestickSpacing()) * (chart.endIndex() - chart.startIndex()); 
			} else {
				width = chart.xDiff() * (chart.endIndex() - chart.startIndex()); 
			}
		}
		return width;
	}
	
	private void drawFocusedChartCrossHair() {			
		tf = chart.timeframe();
		drawHorizontalLine(true);
		boolean drawCandlesticks = chart.drawCandlesticks().get();	
		double width = getWidth();
		int var = !tf.base()&&!drawCandlesticks?-1:0;
		dateIndex.set(chart.startIndex() + (int)(((x.get() - ChartNode.CHT_MARGIN) / width) * (chart.endIndex() - chart.startIndex() + var)));
		if (dateIndex.get() >= chart.endIndex()) {
			if (chart.endMargin()) {
				dateIndex.set(-1);
			} else {				
				dateIndex.set(dateIndex.get() - 1);				
			}
		} 			
		if (dateIndex.get() != -1) {
			if (tf.base() && !drawCandlesticks) {
				tickIndex.set(dateIndex.get());
			} else {
				tickIndex.set(tf.data().get(dateIndex.get()).firstTickIndex());
			}
		} else {
			tickIndex.set(-1);
		}
		if (chart.drawCandlesticks().get() && dateIndex.get() != -1) {
			setOHLC(tf.data().get(dateIndex.get()));
		}					
		if (tf.base()) {
			if (drawCandlesticks) {
				tickBased.set(false);
			} else {
				tickBased.set(true);
			}
		} else {
			tickBased.set(tf.tickBased());
		}
		drawVerticalLine(x.get(), dateIndex.get());
	}
	
	private void drawTickToTickAndSameTFAndTimeCandleToTick() {
		if (dateIndex.get() == -1) {
			return;
		}
		boolean same = tf.equals(unfocusedTf);
		if (same && tf.base() && tickBased.get() == chart.drawCandlesticks().get()) {
			same = false;
		}
		int diff = same?dateIndex.get() - chart.startIndex():((int)(tickIndex.get()/(double)unfocusedTf.period()) - chart.startIndex());
		double xPos = ChartNode.CHT_MARGIN + diff * (chart.drawCandlesticks().get()?chart.candlestickWidth() + chart.candlestickSpacing():chart.xDiff());
		xPos += (chart.drawCandlesticks().get()?chart.candlestickWidth()/2:0);
		int index = same?dateIndex.get():chart.startIndex() + diff;
		drawVerticalLine(xPos, index);
	}
	
	private int indexSearch(ArrayList<Candlestick> data, int i1, int i2) {
		if (i1 > i2) {
			return -1;
		}
		int i = i1 + (i2 - i1) / 2;	
		int a = i+1;
		if (data.get(i).firstTickIndex() <= tickIndex.get() && (a == data.size() || data.get(a).firstTickIndex() > tickIndex.get())) {
			return i;
		} else if (data.get(i).firstTickIndex() < tickIndex.get()) {
			return indexSearch(data, i+1, i2);
		} else {
			return indexSearch(data, i1, i-1);
		}
	}
	
	private void drawTickToTimeCandleAndDiffTimeCToTimeC() {
		if (dateIndex.get() == -1) {
			return;
		}
		ArrayList<Candlestick> data = unfocusedTf.data();
		int startIndex = chart.startIndex();
		int endIndex = chart.endIndex();
		int firstIndex = data.get(startIndex).firstTickIndex();
		int lastIndex = endIndex==data.size()?chart.data().tickData().size()-1:data.get(endIndex - 1).firstTickIndex();
		if (!(tickIndex.get() > lastIndex || tickIndex.get() < firstIndex)) {
			int index = indexSearch(data, startIndex, endIndex - 1);			
			int diff = index - startIndex;
			double xPos = ChartNode.CHT_MARGIN + diff * (chart.drawCandlesticks().get()?chart.candlestickWidth() + chart.candlestickSpacing():chart.xDiff());
			xPos += (chart.drawCandlesticks().get()?chart.candlestickWidth()/2:0);
			setOHLC(unfocusedTf.data().get(index));
			unfocusedDateIndex.set(index);
			drawVerticalLine(xPos, index);
		}		
	}
	
	private void drawUnfocusedChartCrossHair() {
		if (price.get() >= chart.lowest() - chart.dataMarginTickSize() && price.get() <= chart.highest() + chart.dataMarginTickSize()) {					
			drawHorizontalLine(false);
		}
		unfocusedTf = chart.timeframe();
		boolean unfocusedTFTickBased = unfocusedTf.base()?(chart.drawCandlesticks().get()?false:true):unfocusedTf.tickBased(); 
		if (!(tickBased.get() && !unfocusedTFTickBased || !tickBased.get() && !unfocusedTFTickBased && !unfocusedTf.equals(tf))) {
			drawTickToTickAndSameTFAndTimeCandleToTick();
		} else {
			drawTickToTimeCandleAndDiffTimeCToTimeC();
		}
	}
	
	public void drawCrossHair() {
		if (chart.focusedChart().get()) {		
			drawFocusedChartCrossHair();
			if (chart.replayMode()) {
				chart.tradeButtons().limitOrder().setY(y.get() - chart.fontSize()/2); 
				chart.tradeButtons().stopOrder().setY(y.get() - chart.fontSize()/2);
			}
		} else if (chart.name().equals(name.get()) && ChartNode.onSomeChart(name.get())) {
			drawUnfocusedChartCrossHair();
		} 
	}
}