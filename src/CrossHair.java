import java.time.ZoneOffset;

import javafx.scene.paint.Color;

public class CrossHair {
	private static double x;
	private static double y;
	private static double price = 0;
	private static int dateIndex = 0;
	private static boolean isForCandle = false;
	private static String name;
	
	private Chart chart;
	private double dateBarHalfWidth;
	private double dateBarX;

	public CrossHair(Chart chart) {
		this.chart = chart;
		this.dateBarHalfWidth = chart.fontSize() * 5;
	}
		
	public static double price() {
		return CrossHair.price;
	}
	
	public static int dateIndex() {
		return CrossHair.dateIndex;
	}
	
	public static void setX(double x) {
		CrossHair.x = x;
	}
	
	public static void setY(double y) {
		CrossHair.y = y;
	}
	
	public static void setPrice(double price) {
		CrossHair.price = price;
	}
	
	public static void setDateIndex(int dateIndex) {
		CrossHair.dateIndex = dateIndex;
	}
	
	public static void setIsForCandle(boolean isForCandle) {
		CrossHair.isForCandle = isForCandle;
	}
	
	public static void setName(String name) {
		CrossHair.name = name;
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
	
	private void drawOHLC(int index) {
		if (dateIndex == -1) {
			return;
		}
		String ohlc = "O: " + chart.m1Candles().get(index).open();
		ohlc += "  H: " + chart.m1Candles().get(index).high();
		ohlc += "  L: " + chart.m1Candles().get(index).low();
		ohlc += "  C: " + chart.m1Candles().get(index).close();
		if (Chart.darkMode()) {
			chart.graphicsContext().setStroke(Color.WHITE);
		} else {
			chart.graphicsContext().setStroke(Color.BLACK);
		}
		chart.graphicsContext().strokeText(ohlc, Chart.CHT_MARGIN + Chart.INFO_MARGIN + chart.fontSize() * (chart.name().length() + 2) * 0.9, Chart.CHT_MARGIN + chart.fontSize());
	}
	
	private void drawHorizontalLine(boolean focusedChart) {
		double yPos;
		if (focusedChart) {
			yPos = y;
		} else {
			yPos = ((chart.highest() + chart.dataMarginTickSize() - price) / (chart.range() + chart.dataMarginTickSize() * 2)) * chart.chartHeight() + Chart.CHT_MARGIN;
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
		chart.graphicsContext().fillRect(chart.chartWidth() + Chart.CHT_MARGIN, yPos - chart.fontSize()/2, 100, chart.fontSize());
		chart.graphicsContext().strokeText(((Double)(chart.roundToNearestTick(price))).toString(), chart.chartWidth() + Chart.CHT_MARGIN + Chart.PRICE_DASH_MARGIN, yPos + chart.fontSize()/3, Chart.PRICE_MARGIN - Chart.PRICE_DASH_SIZE - Chart.PRICE_DASH_MARGIN);
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
		price = ((((chart.chartHeight() - (chart.chtDataMargin()*2)) - (y - Chart.CHT_MARGIN - chart.chtDataMargin())) / (double)(chart.chartHeight() - (chart.chtDataMargin()*2))) * chart.range()) + chart.lowest();
		drawHorizontalLine(true);
		double width = getWidth();
		dateIndex = chart.startIndex() + (int)(((x - Chart.CHT_MARGIN) / width) * (chart.endIndex() - chart.startIndex()));
		if (dateIndex >= chart.endIndex()) {
			if (chart.endMargin()) {
				dateIndex = -1;
			} else {				
				dateIndex--;				
			}
		} 
		
		if (chart.drawCandlesticks()) {
			drawOHLC(dateIndex);
		}					
		drawVerticalLine(x, dateIndex);
	}	
	
	private void drawUnfocusedTickToTick() {
		if (dateIndex == -1) {
			return;
		}
		int indexRange = chart.endIndex() - chart.startIndex();
		double percOfRange = (dateIndex - chart.startIndex()) / (double)indexRange;
		double width = getWidth();
		double xPos = width * percOfRange + Chart.CHT_MARGIN;			
		drawVerticalLine(xPos, dateIndex);
	}
	
	private void drawUnfocusedTickToCandle() {
		if (dateIndex == -1) {
			return;
		}
		long startEpochMin = (int)(chart.tickData().get(chart.startIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long endEpochMin = (int)(chart.tickData().get(chart.endIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long chdiEpochMin = (int)(chart.m1Candles().get(dateIndex).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
			int chdi = chart.m1Candles().get(dateIndex).firstTickIndex();
			double xPos = (chdi - chart.startIndex()) * chart.xDiff() + Chart.CHT_MARGIN;
			drawVerticalLine(xPos, chdi);
		}
	}

	private void drawUnfocusedCandleToCandle() {
		if (dateIndex == -1) {
			return;
		}
		double xPos = (dateIndex - chart.startIndex()) * (chart.candlestickWidth() + chart.candlestickSpacing()) + chart.candlestickWidth() / 2 + Chart.CHT_MARGIN;				
		drawOHLC(dateIndex);					
		drawVerticalLine(xPos, dateIndex);
	}

	private void drawUnfocusedCandleToTick() {
		if (dateIndex == -1) {
			return;
		}
		long startEpochMin = (int)(chart.m1Candles().get(chart.startIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		long endEpochMin;
		if (chart.endIndex() == chart.m1Candles().size()) {
			endEpochMin = (int)(chart.m1Candles().get(chart.endIndex()-1).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		} else {
			endEpochMin = (int)(chart.m1Candles().get(chart.endIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		}
		long chdiEpochMin = (int)(chart.tickData().get(dateIndex).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
		if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
			int chdi = chart.tickData().get(dateIndex).candleIndex();
			int indexRange = chart.endIndex() - chart.startIndex();
			double percOfRange = (chdi - chart.startIndex()) / (double)indexRange;
			double width = getWidth();
			double xPos = width * percOfRange + Chart.CHT_MARGIN + chart.candlestickWidth() / 2;		
			int convertedCHDI = (int)((xPos - Chart.CHT_MARGIN) / (chart.candlestickWidth() + chart.candlestickSpacing()));
			drawOHLC(convertedCHDI);					
			drawVerticalLine(xPos, chdi);				
		}
	}
	
	private void drawUnfocusedChartCrossHair() {
		if (price >= chart.lowest() - chart.dataMarginTickSize() && price <= chart.highest() + chart.dataMarginTickSize()) {					
			drawHorizontalLine(false);
		}
		if (!chart.drawCandlesticks()) {
			if (isForCandle) {
				drawUnfocusedTickToCandle();
			} else if (dateIndex >= chart.startIndex() && dateIndex <= chart.endIndex()) {
				drawUnfocusedTickToTick();
			}
		} else if (chart.drawCandlesticks()) {
			if (isForCandle) {
				if (dateIndex >= chart.startIndex() && dateIndex <= chart.endIndex()) {
					drawUnfocusedCandleToCandle();
				}
			} else {
				drawUnfocusedCandleToTick();
			}
		}
	}
	
	public void drawCrossHair() {					
		if (chart.focusedChart()) {		
			if (chart.onChart(x, y)) {
				drawFocusedChartCrossHair();
			}
		} else if (Chart.focusedOnChart() && chart.name().equals(name)) {						
			drawUnfocusedChartCrossHair();
		}
	}
}
