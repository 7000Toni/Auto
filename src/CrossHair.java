import java.time.ZoneOffset;

import javafx.scene.paint.Color;

public class CrossHair {
	private static double x;
	private static double y;
	private static double price = 0;
	private static int dateIndex = 0;
	private static boolean isForCandle = false;
	
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
	
	private void setDateBarX(double xPos) {
		if (xPos < Chart.CHT_MARGIN + dateBarHalfWidth) {
			dateBarX = Chart.CHT_MARGIN;
		} else if (xPos > chart.chartWidth() + Chart.CHT_MARGIN - dateBarHalfWidth) {
			dateBarX = chart.chartWidth() + Chart.CHT_MARGIN - dateBarHalfWidth*2;
		} else {
			dateBarX = xPos - dateBarHalfWidth;
		}
	}
	
	private void drawOHLC(int index) {
		String ohlc = "O: " + chart.m1Candles().get(index).open();
		ohlc += "  H: " + chart.m1Candles().get(index).high();
		ohlc += "  L: " + chart.m1Candles().get(index).low();
		ohlc += "  C: " + chart.m1Candles().get(index).close();
		chart.graphicsContext().strokeText(ohlc, Chart.CHT_MARGIN * 3 + chart.fontSize() * chart.name().length(), Chart.CHT_MARGIN + chart.fontSize());
	}
	
	private void drawHorizontalLine(boolean focusedChart) {
		double yPos;
		if (focusedChart) {
			yPos = y;
		} else {
			yPos = ((chart.highest() + chart.dataMarginSize() - price) / (chart.range() + chart.dataMarginSize() * 2)) * chart.chartHeight() + Chart.CHT_MARGIN;
		}
		chart.graphicsContext().strokeLine(Chart.CHT_MARGIN, yPos, chart.width() - Chart.PRICE_MARGIN, yPos);
		drawPriceBox(yPos);
	}
	
	private void drawPriceBox(double yPos) {
		chart.graphicsContext().fillRect(chart.chartWidth() + Chart.CHT_MARGIN, yPos - chart.fontSize()/2, 100, chart.fontSize());
		chart.graphicsContext().setStroke(Color.WHITE);
		chart.graphicsContext().strokeText(((Double)(chart.roundToNearestTick(price))).toString(), chart.chartWidth() + Chart.CHT_MARGIN + Chart.PRICE_DASH_MARGIN, yPos + chart.fontSize()/3, Chart.PRICE_MARGIN - Chart.PRICE_DASH_SIZE - Chart.PRICE_DASH_MARGIN);
		if (!Chart.darkMode()) {
			chart.graphicsContext().setStroke(Color.BLACK);
		}
	}
	
	private void drawVerticalLine(double xPos, int index) {
		chart.graphicsContext().strokeLine(xPos, Chart.CHT_MARGIN, xPos, chart.height() - HorizontalScrollBar.HSB_HEIGHT - Chart.CHT_MARGIN);
		setDateBarX(xPos);
		drawDateBox(index);
	}
	
	private void drawDateBox(int index) {		
		chart.graphicsContext().fillRect(dateBarX, chart.chartHeight() - Chart.CHT_MARGIN - 1, dateBarHalfWidth*2, chart.fontSize());
		chart.graphicsContext().setStroke(Color.WHITE);
		if (chart.drawCandlesticks()) {
			chart.graphicsContext().strokeText(chart.m1Candles().get(index).dateTime().toString().replace('T', ' '), dateBarX + chart.fontSize() / 3, chart.chartHeight() + Chart.CHT_MARGIN - 1, dateBarHalfWidth*2);
		} else {
			chart.graphicsContext().strokeText(chart.tickData().get(index).dateTime().toString().replace('T', ' '), dateBarX + chart.fontSize() / 3, chart.chartHeight() + Chart.CHT_MARGIN - 1, dateBarHalfWidth*2);
		}
	}
	
	private void drawFocusedChartCrossHair() {		
		price = ((((chart.chartHeight() - (chart.chtDataMargin()*2)) - (y - Chart.CHT_MARGIN - chart.chtDataMargin())) / (double)(chart.chartHeight() - (chart.chtDataMargin()*2))) * chart.range()) + chart.lowest();		
		drawHorizontalLine(true);		
		dateIndex = chart.startIndex() + (int)(((x-Chart.CHT_MARGIN) / chart.chartWidth()) * (chart.endIndex()-chart.startIndex()));		
		if (dateIndex >= chart.endIndex()) {
			dateIndex--;
		}
		
		if (chart.drawCandlesticks()) {
			drawOHLC(dateIndex);
		}					
		drawVerticalLine(x, dateIndex);
	}	
	
	private void drawUnfocusedChartCrossHair() {
		if (price >= chart.lowest() - chart.dataMarginSize() && price <= chart.highest() + chart.dataMarginSize()) {					
			drawHorizontalLine(false);
		}
		if (!chart.drawCandlesticks()) {
			if (isForCandle) {
				long startEpochMin = (int)(chart.tickData().get(chart.startIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
				long endEpochMin = (int)(chart.tickData().get(chart.endIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
				long chdiEpochMin = (int)(chart.m1Candles().get(dateIndex).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
				if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
					int chdi = dateIndex;
					for (int i = chart.startIndex(); i < chart.endIndex(); i++) {
						long ldtEpochMin = (int)(chart.tickData().get(i).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
						
						if (ldtEpochMin == chdiEpochMin) {
							chdi = i;
							break;
						} else if (chdiEpochMin < ldtEpochMin) {
							break;
						}
						chdi = i;
					}
					int indexRange = chart.endIndex() - chart.startIndex();
					double percOfRange = (chdi - chart.startIndex()) / (double)indexRange;
					double xPos = chart.chartWidth() * percOfRange + Chart.CHT_MARGIN;				
					drawVerticalLine(xPos, chdi);
				}
			} else if (dateIndex >= chart.startIndex() && dateIndex <= chart.endIndex()) {
				int indexRange = chart.endIndex() - chart.startIndex();
				double percOfRange = (dateIndex - chart.startIndex()) / (double)indexRange;
				double xPos = chart.chartWidth() * percOfRange + Chart.CHT_MARGIN;			
				drawVerticalLine(xPos, dateIndex);
			}
		} else if (chart.drawCandlesticks()) {
			if (isForCandle) {
				if (dateIndex >= chart.startIndex() && dateIndex <= chart.endIndex()) {
					double xPos = (dateIndex - chart.startIndex()) * (chart.candlestickWidth() + chart.candlestickSpacing()) + chart.candlestickWidth() / 2 + Chart.CHT_MARGIN;				
					drawOHLC(dateIndex);					
					drawVerticalLine(xPos, dateIndex);
				}
			} else {
				long startEpochMin = (int)(chart.m1Candles().get(chart.startIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
				long endEpochMin;
				if (chart.endIndex() == chart.m1Candles().size()) {
					endEpochMin = (int)(chart.m1Candles().get(chart.endIndex()-1).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
				} else {
					endEpochMin = (int)(chart.m1Candles().get(chart.endIndex()).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
				}
				long chdiEpochMin = (int)(chart.tickData().get(dateIndex).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
				if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
					int dateIndex_Candle = 0;
					for (int i = chart.startIndex(); i < chart.endIndex(); i++) {
						long ldtEpochMin = (int)(chart.m1Candles().get(i).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
						if (ldtEpochMin == chdiEpochMin) {
							dateIndex_Candle = i;
							break;
						} else if (chdiEpochMin < ldtEpochMin) {
							break;
						}
						dateIndex_Candle = i;
					}
					int indexRange = chart.endIndex() - chart.startIndex();
					double percOfRange = (dateIndex_Candle - chart.startIndex()) / (double)indexRange;
					double stub = chart.chartWidth() - ((int)(chart.chartWidth() / (chart.candlestickWidth() + chart.candlestickSpacing())) * (chart.candlestickWidth() + chart.candlestickSpacing()));
					double xPos = (chart.chartWidth() - stub) * percOfRange + Chart.CHT_MARGIN + chart.candlestickWidth() / 2;		
					int convertedCHDI = (int)((xPos - Chart.CHT_MARGIN) / (chart.candlestickWidth() + chart.candlestickSpacing()));
					drawOHLC(convertedCHDI);					
					drawVerticalLine(xPos, dateIndex_Candle);				
				}
			}
		}
	}
	
	public void drawCrossHair() {					
		if (chart.focusedChart()) {		
			if (chart.onChart(x, y)) {
				drawFocusedChartCrossHair();
			}
		} else if (Chart.focusedOnChart()) {						
			drawUnfocusedChartCrossHair();
		}
		if (!Chart.darkMode()) {
			chart.graphicsContext().setStroke(Color.BLACK);
		}
	}
}
