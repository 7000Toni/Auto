package com.github._7000toni.auto.chart;
import java.time.ZoneOffset;
import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.Dataset.DataPair;
import com.github._7000toni.auto.dataset.timeframe.Timeframe;

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
	private double dateBarHalfWidth;
	private double dateBarX;
	private String ohlc;
	private IntegerProperty unfocusedDateIndex = new SimpleIntegerProperty(0);
	private Timeframe unfocusedTf;

	public CrossHair(ChartNode chart) {
		this.chart = chart;
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
			if (chart.focusedChart().get() || tickBased.get()) {
				t = new Text(data.get(index).dateTime().toString().replace('T', ' '));
			} else {
				t = new Text(data.get(index).dateTime().minusNanos(data.get(index).dateTime().getNano()).minusSeconds(data.get(index).dateTime().getSecond()).toString().replace('T', ' '));
			}
		}
		t.setFont(chart.graphicsContext().getFont());
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
			yPos = ((chart.highest() + chart.dataMarginTickSize() - price.get()) / (chart.range() + chart.dataMarginTickSize() * 2)) * chart.height() + ChartNode.CHT_MARGIN;			
		}
		if (Chart.darkMode().get()) {
			chart.graphicsContext().setStroke(Color.WHITE);
		} else {
			chart.graphicsContext().setStroke(Color.BLACK);
		}
		chart.graphicsContext().strokeLine(ChartNode.CHT_MARGIN, yPos+0.5, ChartNode.CHT_MARGIN + chart.width(), yPos+0.5);
		drawPriceBox(yPos);
	}
	
	private void drawPriceBox(double yPos) {
		if (Chart.darkMode().get()) {
			chart.graphicsContext().setFill(Color.WHITE);
		} else {
			chart.graphicsContext().setFill(Color.BLACK);
		}
		chart.graphicsContext().fillRoundRect(chart.width() + ChartNode.CHT_MARGIN, yPos - chart.fontSize()/2, chart.chart().priceMargin().width(), chart.fontSize(), CanvasButton.ARC_W, CanvasButton.ARC_H);
		if (Chart.darkMode().get()) {
			chart.graphicsContext().setFill(Color.BLACK);
		} else {
			chart.graphicsContext().setFill(Color.WHITE);
		}
		chart.graphicsContext().fillText(((Double)(chart.roundToNearestTick(price.get()))).toString(), chart.width() + ChartNode.CHT_MARGIN + PriceMargin.EXTRA_SPACE/2, yPos + chart.fontSize()/3, chart.chart().priceMargin().width() - PriceMargin.EXTRA_SPACE);
	}
	
	private void drawVerticalLine(double xPos, int index) {
		if (xPos < ChartNode.CHT_MARGIN || xPos > ChartNode.CHT_MARGIN + chart.width()) {
			chart.setFocusedChart(false);
			return;
		}
		if (Chart.darkMode().get()) {
			chart.graphicsContext().setStroke(Color.WHITE);
		} else {
			chart.graphicsContext().setStroke(Color.BLACK);
		}
		chart.graphicsContext().strokeLine(xPos+0.5, ChartNode.CHT_MARGIN, xPos+0.5, chart.height() + ChartNode.CHT_MARGIN - 0.5);		
		drawDateBox(index, setDateBarX(xPos, index));
	}
	
	private void drawDateBox(int index, String text) {	
		if (Chart.darkMode().get()) {
			chart.graphicsContext().setFill(Color.WHITE);
		} else {
			chart.graphicsContext().setFill(Color.BLACK);
		}
		chart.graphicsContext().fillRoundRect(dateBarX, chart.height() + ChartNode.CHT_MARGIN - chart.fontSize(), dateBarHalfWidth*2, chart.fontSize(), CanvasButton.ARC_W, CanvasButton.ARC_H);
		if (Chart.darkMode().get()) {
			chart.graphicsContext().setFill(Color.BLACK);
		} else {
			chart.graphicsContext().setFill(Color.WHITE);
		}
		chart.graphicsContext().fillText(text, dateBarX + DATE_BAR_MARGIN, chart.height() + ChartNode.CHT_MARGIN - 1, (dateBarHalfWidth + DATE_BAR_MARGIN) * 2);
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
			if (tf.base()) {
				if (drawCandlesticks) {
					tickIndex.set(tf.data().get(dateIndex().get()).firstTickIndex());
				} else {
					tickIndex.set(dateIndex.get());
				}
			} else {
				tickIndex.set(tf.data().get(dateIndex.get()).firstTickIndex());
			}
		} else {
			tickIndex.set(-1);
		}
		if (chart.drawCandlesticks().get() && dateIndex.get() != -1) {
			setOHLC(chart.timeframe().data().get(dateIndex.get()));
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
	
	private void drawUnfocusedTickToTick() {
		if (dateIndex.get() == -1) {
			return;
		}
		int indexRange = chart.endIndex() - chart.startIndex();
		double percOfRange = (dateIndex.get() - chart.startIndex()) / (double)indexRange;
		double width = getWidth();
		double xPos = width * percOfRange + ChartNode.CHT_MARGIN;			
		drawVerticalLine(xPos, dateIndex.get());
	}
	
	private void drawUnfocusedCandleToTick() {
		if (dateIndex.get() == -1) {
			return;
		}
		long startEpochMin = (int)(chart.data().tickData().get(chart.startIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long endEpochMin = (int)(chart.data().tickData().get(chart.endIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long chdiEpochMin = (int)(chart.data().m1Candles().get(dateIndex.get()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
			int chdi = chart.data().m1Candles().get(dateIndex.get()).firstTickIndex();
			double xPos = (chdi - chart.startIndex()) * chart.xDiff() + ChartNode.CHT_MARGIN;
			drawVerticalLine(xPos, chdi);
		}
	}

	private void drawUnfocusedCandleToCandle() {
		if (dateIndex.get() == -1) {
			return;
		}
		double xPos = (dateIndex.get() - chart.startIndex()) * (chart.candlestickWidth() + chart.candlestickSpacing()) + chart.candlestickWidth() / 2 + ChartNode.CHT_MARGIN;				
		setOHLC(chart.data().m1Candles().get(dateIndex.get()));					
		drawVerticalLine(xPos, dateIndex.get());
	}

	private void drawUnfocusedTickToCandle() {
		if (dateIndex.get() == -1) {
			return;
		}
		long startEpochMin = (int)(chart.data().m1Candles().get(chart.startIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long endEpochMin;
		if (chart.endIndex() == chart.data().m1Candles().size()) {
			endEpochMin = (int)(chart.data().m1Candles().get(chart.endIndex()-1).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		} else {
			endEpochMin = (int)(chart.data().m1Candles().get(chart.endIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		}
		long chdiEpochMin = (int)(chart.data().tickData().get(dateIndex.get()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
			int chdi = chart.data().tickData().get(dateIndex.get()).candleIndex();
			int indexRange = chart.endIndex() - chart.startIndex();
			double percOfRange = (chdi - chart.startIndex()) / (double)indexRange;
			double width = getWidth();
			double xPos = width * percOfRange + ChartNode.CHT_MARGIN + chart.candlestickWidth() / 2;
			if (chart.data().tickData().get(dateIndex.get()).candleIndex() == chart.data().m1CandlesDataSize(chart.replayMode()).get() - 1) {
				setOHLC(chart.lastCandlestick());
			} else {
				setOHLC(chart.data().m1Candles().get(chart.data().tickData().get(dateIndex.get()).candleIndex()));
			}
			drawVerticalLine(xPos, chdi);				
		}
	}
	
	private void drawTickBasedAndSameTF() {
		if (dateIndex.get() == -1) {
			return;
		}
		boolean same = tf.equals(unfocusedTf);
		int diff = same?((int)(tickIndex.get()/(double)unfocusedTf.period()) - chart.startIndex()):dateIndex.get() - chart.startIndex();
		double xPos = ChartNode.CHT_MARGIN + diff * (chart.drawCandlesticks().get()?chart.candlestickWidth() + chart.candlestickSpacing():chart.xDiff());
		xPos += (chart.drawCandlesticks().get()?chart.candlestickWidth()/2:0);		
		int index = same?chart.startIndex() + diff:dateIndex.get();
		drawVerticalLine(xPos, index);
	}
	
	private void drawBaseToBase() {
		if (!chart.drawCandlesticks().get()) {
			if (!tickBased.get()) {
				drawUnfocusedCandleToTick();
			} else if (dateIndex.get() >= chart.startIndex() && dateIndex.get() <= chart.endIndex()) {
				drawUnfocusedTickToTick();
			}
		} else {
			if (!tickBased.get()) {
				if (dateIndex.get() >= chart.startIndex() && dateIndex.get() <= chart.endIndex()) {
					drawUnfocusedCandleToCandle();
				}
			} else {
				drawUnfocusedTickToCandle();
			}
		}
	}
		
	private void drawUnfocusedChartCrossHair() {
		if (price.get() >= chart.lowest() - chart.dataMarginTickSize() && price.get() <= chart.highest() + chart.dataMarginTickSize()) {					
			drawHorizontalLine(false);
		}
		unfocusedTf = chart.timeframe();
		if (unfocusedTf.base() && tf.base()) {
			drawBaseToBase();
		} else if (unfocusedTf.equals(tf) || tickBased.get() && (chart.timeframe().tickBased() && !unfocusedTf.base() || unfocusedTf.base() && !chart.drawCandlesticks().get())) {
			drawTickBasedAndSameTF();
		} else {
			
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