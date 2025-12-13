import java.time.ZoneOffset;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class CrossHair {
	private static DoubleProperty x = new SimpleDoubleProperty();
	private static DoubleProperty y = new SimpleDoubleProperty();
	private static DoubleProperty price = new SimpleDoubleProperty(0);
	private static IntegerProperty dateIndex = new SimpleIntegerProperty(0);
	private static BooleanProperty isForCandle = new SimpleBooleanProperty(false);
	private static StringProperty name = new SimpleStringProperty();
	
	private Chart chart;
	private double dateBarHalfWidth;
	private double dateBarX;
	private String ohlc;

	public CrossHair(Chart chart) {
		this.chart = chart;
		this.dateBarHalfWidth = chart.fontSize() * 5;
	}
		
	public static double price() {
		return CrossHair.price.get();
	}
	
	public static ReadOnlyIntegerProperty dateIndex() {
		return IntegerProperty.readOnlyIntegerProperty(dateIndex);
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
	
	public static void setIsForCandle(boolean isForCandle) {
		CrossHair.isForCandle.set(isForCandle);
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
		String ret = "name: " + name + '\n'; 
		ret += "price: " + price + '\n';
		ret += "isForCandle: " + isForCandle + '\n';
		ret += "dateIndex: " + dateIndex + '\n';
		ret += "x: " + x + '\n';
		ret += "y: " + y + '\n';
		return ret;
	}
	
	private void setDateBarX(double xPos) {
		if (xPos < Chart.CHT_MARGIN + dateBarHalfWidth) {
			dateBarX = Chart.CHT_MARGIN;
		} else if (xPos > chart.chartWidth() + Chart.CHT_MARGIN - dateBarHalfWidth) {
			dateBarX = chart.chartWidth() + Chart.CHT_MARGIN - dateBarHalfWidth * 2;
		} else {
			dateBarX = xPos - dateBarHalfWidth;
		}
	}
	
	private void setOHLC(int index) {
		if (dateIndex.get() == -1) {
			return;
		}
		ohlc = "O: " + chart.m1Candles().get(index).open();
		ohlc += "  H: " + chart.m1Candles().get(index).high();
		ohlc += "  L: " + chart.m1Candles().get(index).low();
		ohlc += "  C: " + chart.m1Candles().get(index).close();
	}
	
	private void drawHorizontalLine(boolean focusedChart) {
		double yPos;
		if (focusedChart) {
			yPos = y.get();
		} else {
			yPos = ((chart.highest() + chart.dataMarginTickSize() - price.get()) / (chart.range() + chart.dataMarginTickSize() * 2)) * chart.chartHeight() + Chart.CHT_MARGIN;
		}
		if (Chart.darkMode()) {
			chart.graphicsContext().setStroke(Color.WHITE);
		} else {
			chart.graphicsContext().setStroke(Color.BLACK);
		}
		chart.graphicsContext().strokeLine(Chart.CHT_MARGIN, yPos, chart.width() - Chart.PRICE_MARGIN, yPos);
		drawPriceBox(yPos);
	}
	
	private void drawPriceBox(double yPos) {
		if (Chart.darkMode()) {
			chart.graphicsContext().setStroke(Color.BLACK);
			chart.graphicsContext().setFill(Color.WHITE);
		} else {
			chart.graphicsContext().setStroke(Color.WHITE);
			chart.graphicsContext().setFill(Color.BLACK);
		}
		chart.graphicsContext().fillRect(chart.chartWidth() + Chart.CHT_MARGIN, yPos - chart.fontSize()/2, Chart.PRICE_MARGIN, chart.fontSize());
		chart.graphicsContext().strokeText(((Double)(chart.roundToNearestTick(price.get()))).toString(), chart.chartWidth() + Chart.CHT_MARGIN + Chart.PRICE_DASH_MARGIN, yPos + chart.fontSize()/3, Chart.PRICE_MARGIN - Chart.PRICE_DASH_SIZE - Chart.PRICE_DASH_MARGIN);
	}
	
	private void drawVerticalLine(double xPos, int index) {
		if (Chart.darkMode()) {
			chart.graphicsContext().setStroke(Color.WHITE);
		} else {
			chart.graphicsContext().setStroke(Color.BLACK);
		}
		chart.graphicsContext().strokeLine(xPos, Chart.CHT_MARGIN, xPos, chart.height() - chart.hsb().sbHeight() - Chart.CHT_MARGIN);
		setDateBarX(xPos);
		drawDateBox(index);
	}
	
	private void drawDateBox(int index) {	
		if (Chart.darkMode()) {
			chart.graphicsContext().setStroke(Color.BLACK);
			chart.graphicsContext().setFill(Color.WHITE);
		} else {
			chart.graphicsContext().setStroke(Color.WHITE);
			chart.graphicsContext().setFill(Color.BLACK);
		}
		chart.graphicsContext().fillRect(dateBarX, chart.chartHeight() + Chart.CHT_MARGIN - chart.fontSize(), dateBarHalfWidth*2, chart.fontSize());
		if (index != -1) {
			if (chart.drawCandlesticks()) {
				chart.graphicsContext().strokeText(chart.m1Candles().get(index).dateTime().toString().replace('T', ' '), dateBarX + chart.fontSize() / 3, chart.chartHeight() + Chart.CHT_MARGIN - 1, dateBarHalfWidth*2);
			} else {
				chart.graphicsContext().strokeText(chart.tickData().get(index).dateTime().minusNanos(chart.tickData().get(index).dateTime().getNano()).toString().replace('T', ' '), dateBarX + chart.fontSize() / 3, chart.chartHeight() + Chart.CHT_MARGIN - 1, dateBarHalfWidth*2);
			}
		} else {
			chart.graphicsContext().strokeText("DONTBEDUMB", dateBarX + chart.fontSize() / 3, chart.chartHeight() + Chart.CHT_MARGIN - 1, dateBarHalfWidth*2);
		}
	}
	
	private double getWidth() {
		double width = chart.chartWidth();
		if (chart.endMargin()) {
			if (chart.drawCandlesticks()) {
				width = (chart.candlestickWidth() + chart.candlestickSpacing()) * (chart.endIndex() - chart.startIndex()); 
			} else {
				width = chart.xDiff() * (chart.endIndex() - chart.startIndex()); 
			}
		}
		return width;
	}
	
	private void drawFocusedChartCrossHair() {				
		price.set(chart.yCoordToPrice(y.get()));
		drawHorizontalLine(true);
		double width = getWidth();
		dateIndex.set(chart.startIndex() + (int)(((x.get() - Chart.CHT_MARGIN) / width) * (chart.endIndex() - chart.startIndex())));
		if (dateIndex.get() >= chart.endIndex()) {
			if (chart.endMargin()) {
				dateIndex.set(-1);
			} else {				
				dateIndex.set(dateIndex.get() - 1);				
			}
		} 
		
		if (chart.drawCandlesticks()) {
			setOHLC(dateIndex.get());
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
		double xPos = width * percOfRange + Chart.CHT_MARGIN;			
		drawVerticalLine(xPos, dateIndex.get());
	}
	
	private void drawUnfocusedCandleToTick() {
		if (dateIndex.get() == -1) {
			return;
		}
		long startEpochMin = (int)(chart.tickData().get(chart.startIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long endEpochMin = (int)(chart.tickData().get(chart.endIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long chdiEpochMin = (int)(chart.m1Candles().get(dateIndex.get()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
			int chdi = chart.m1Candles().get(dateIndex.get()).firstTickIndex();
			double xPos = (chdi - chart.startIndex()) * chart.xDiff() + Chart.CHT_MARGIN;
			drawVerticalLine(xPos, chdi);
		}
	}

	private void drawUnfocusedCandleToCandle() {
		if (dateIndex.get() == -1) {
			return;
		}
		double xPos = (dateIndex.get() - chart.startIndex()) * (chart.candlestickWidth() + chart.candlestickSpacing()) + chart.candlestickWidth() / 2 + Chart.CHT_MARGIN;				
		setOHLC(dateIndex.get());					
		drawVerticalLine(xPos, dateIndex.get());
	}

	private void drawUnfocusedTickToCandle() {
		if (dateIndex.get() == -1) {
			return;
		}
		long startEpochMin = (int)(chart.m1Candles().get(chart.startIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long endEpochMin;
		if (chart.endIndex() == chart.m1Candles().size()) {
			endEpochMin = (int)(chart.m1Candles().get(chart.endIndex()-1).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		} else {
			endEpochMin = (int)(chart.m1Candles().get(chart.endIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		}
		long chdiEpochMin = (int)(chart.tickData().get(dateIndex.get()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
			int chdi = chart.tickData().get(dateIndex.get()).candleIndex();
			int indexRange = chart.endIndex() - chart.startIndex();
			double percOfRange = (chdi - chart.startIndex()) / (double)indexRange;
			double width = getWidth();
			double xPos = width * percOfRange + Chart.CHT_MARGIN + chart.candlestickWidth() / 2;	
			if (isForCandle.get()) {
				setOHLC(dateIndex.get());
			} else {
				setOHLC(chart.tickData().get(dateIndex.get()).candleIndex());
			}
			drawVerticalLine(xPos, chdi);				
		}
	}
	
	private void drawUnfocusedChartCrossHair() {
		if (price.get() >= chart.lowest() - chart.dataMarginTickSize() && price.get() <= chart.highest() + chart.dataMarginTickSize()) {					
			drawHorizontalLine(false);
		}
		if (!chart.drawCandlesticks()) {
			if (isForCandle.get()) {
				drawUnfocusedCandleToTick();
			} else if (dateIndex.get() >= chart.startIndex() && dateIndex.get() <= chart.endIndex()) {
				drawUnfocusedTickToTick();
			}
		} else {
			if (isForCandle.get()) {
				if (dateIndex.get() >= chart.startIndex() && dateIndex.get() <= chart.endIndex()) {
					drawUnfocusedCandleToCandle();
				}
			} else {
				drawUnfocusedTickToCandle();
			}
		}
	}
	
	public void drawCrossHair() {						
		if (chart.focusedChart().get()) {		
			if (chart.onChart(x.get(), y.get(), true)) {
				drawFocusedChartCrossHair();
			}
		} else if (Chart.focusedOnChart().get() && chart.name().equals(name.get())) {
			drawUnfocusedChartCrossHair();
		}
	}
}
