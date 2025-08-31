import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class Chart {
	//TODO consider a function to calculate the price given a y coordinate
	public final static double WIDTH_EXTRA = 16;
	public final static double HEIGHT_EXTRA = 39;
	
	public final static double MIN_WIDTH = 300; 
	public final static double MIN_HEIGHT = 300; 
	
	public final static int HSB_WIDTH = 100;
	public final static int HSB_HEIGHT = 10;
	public final static int CHT_MARGIN = 5;
	public final static int CHT_DATA_MARGIN = 20;
	public final static int PRICE_MARGIN = 100;
	
	public final static int PRICE_DASH_SPACING = 50;
	public final static int PRICE_DASH_SIZE = 5;
	public final static int PRICE_DASH_MARGIN = 5;
	
	//CrossHair
	private static double crossHairX = 0;
	private static double crossHairY = 0;
	private static double crossHairPrice = 0;
	private static int crossHairDateIndex = 0;
	private static boolean chdi_IsForCandle = false;
	
	private static ArrayList<Chart> charts = new ArrayList<Chart>();
	private static ArrayList<DataPair> data = new ArrayList<DataPair>();
	private static ArrayList<Candlestick> m1Candles = new ArrayList<Chart.Candlestick>();
	private static ArrayList<Double> lines = new ArrayList<Double>();
	private static int size = -1;
	private static int numDecimalPts;
	private static boolean init = false;
	private static boolean focusedOnChart = false;	
	private static double tickSize;
	
	private boolean focusedChart = false;
	private Canvas canvas;
	private double width;
	private double height;
	private double chartWidth;
	private double chartHeight;
	private GraphicsContext gc;
	private double range;	
	private double lowest;
	private double highest;
	private HorizontalScrollBar hsb;
	private int numDataPoints = 1495;//299	
	private int startIndex;
	private int endIndex;
	private double tickSizeOnChart;
	private double conversionVar;
	private double dataMarginSize;
	private double xDiff;
	private boolean newCHT_BTN_Hover = false;
	private boolean drawCandlesticksHover = false;
	private boolean drawCandlesticks = false;
	private double candlestickWidth;
	private double candlestickSpacing;
	private int numCandlesticks;
		
	private class DataPair {
		private double price;
		private LocalDateTime dateTime;		
		
		public DataPair(double price, LocalDateTime dateTime) {
			this.price = price;
			this.dateTime = dateTime;
		}
	}
	
	private class Candlestick {
		private double open;
		private double high;
		private double low;
		private double close;
		private LocalDateTime dateTime;
		
		public Candlestick(double open, double high, double low, double close, LocalDateTime dateTime) {
			this.open = open;
			this.high = high;
			this.low = low;
			this.close = close;
			this.dateTime = dateTime;
		}
		
		@Override
		public String toString() {
			String ret = "open:\t" + open;
			ret += "\nhigh:\t" + high;
			ret += "\nlow:\t" + low;
			ret += "\nclose:\t" + close;
			ret += "\nldt:\t" + dateTime;
			return ret;
		}
	}
	
	private class WidthListener implements ChangeListener<Number> {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {			
			double newHSBPos = (hsb.position() / (width - HSB_WIDTH - PRICE_MARGIN)) * (newValue.doubleValue() - WIDTH_EXTRA - HSB_WIDTH - PRICE_MARGIN);	
			width = newValue.intValue() - WIDTH_EXTRA;
			canvas.setWidth(width);
			chartWidth = width - PRICE_MARGIN - CHT_MARGIN;
			//TODO fix hard coding here and copy code to make it run on new charts
			candlestickWidth = chartWidth * 0.005;
			candlestickSpacing = candlestickWidth * 0.4;
			hsb.setPosition(newHSBPos);
			drawChart();
		}		
	}
	
	private class HeightListener implements ChangeListener<Number> {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			height = newValue.intValue() - HEIGHT_EXTRA;
			canvas.setHeight(height);
			chartHeight = height - HSB_HEIGHT - CHT_MARGIN*2; 
			drawChart();
		}		
	}
	
	public Chart(String filePath, int size, double width, double height, double tickSize, int numDecimalPts, Stage stage) throws Exception {
		if (size > 0 && !Chart.init) {
			Chart.size = size;
		}
		constructorStuff(filePath, width, height, tickSize, numDecimalPts, stage);
	}
	
	public Chart(String filePath, double width, double height, double tickSize, int numDecimalPts, Stage stage) throws Exception {
		constructorStuff(filePath, width, height, tickSize, numDecimalPts, stage);		
	}
	
	private void constructorStuff(String filePath, double width, double height, double tickSize, int numDecimalPts, Stage stage) throws Exception {
		if (numDecimalPts < 0) {
			throw new Exception("numDecimalPts cannot be less than 0");
		}			
		if (!Chart.init) {
			int val = (int)Math.pow(10, numDecimalPts);
			if (val % (tickSize * val) != 0) {
				throw new Exception(val + "(10^numDecimalPts" + ") is not divisible by " + tickSize * val + "(tickSize*" + val + ")");
			}
			Chart.tickSize = tickSize;
			Chart.numDecimalPts = numDecimalPts;
		}	
		this.width = width;
		this.height = height;
		stage.setMinWidth(MIN_WIDTH);
		stage.setMinHeight(MIN_HEIGHT);
		stage.heightProperty().addListener(new HeightListener());
		stage.widthProperty().addListener(new WidthListener());
		if (!Chart.init) {
			readData(filePath);
		} else {
			canvas = new Canvas(width, height);
			hsb = new HorizontalScrollBar(this, HSB_HEIGHT, HSB_WIDTH, data.size(), numDataPoints);
			gc = canvas.getGraphicsContext2D();	
			//TODO fix hard coding here and copy code to make it run on new charts
			candlestickWidth = chartWidth * 0.005;
			candlestickSpacing = candlestickWidth * 0.4;
			drawChart();
		}
		Chart.init = true;
	}
	
	private void readData(String filePath) {
		try (FileInputStream fis = new FileInputStream(filePath);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(" dd/MM/yyyy HH:mm:ss");
			String in;		
			String dateTime;
			String price;
			in = br.readLine();
			price = in.substring(0, in.indexOf(' '));
			dateTime = in.substring(in.indexOf(' '));
			LocalDateTime ldt = LocalDateTime.parse(dateTime, dtf);
			double val = Double.parseDouble(price);
			data.add(new DataPair(val, ldt));								
			int progress = 0;			
			
			double open = val;
			double high = val;
			double low = val;
			double close = val;
			LocalDateTime ldtPrev = ldt;
			double prevPrice = val;
			
			while (true) {
				progress++;
				in = br.readLine();
				if (size != -1) {
					int percent = (int)((double)progress/size*100);
					if (percent > 100) {
						System.out.println("99%");
					} else {
						System.out.println(percent + "%");
					}
				}
				if (in == null) {
					break;
				}
				if (progress == 100000) {
					break;
				}
				price = in.substring(0, in.indexOf(' '));
				dateTime = in.substring(in.indexOf(' '));
				ldt = LocalDateTime.parse(dateTime, dtf);
				val = Double.parseDouble(price);
				data.add(new DataPair(val, ldt));
				
				int diff = getDiffInMinutes(ldtPrev, ldt);
				if (ldtPrev.getMinute() == ldt.getMinute()) {
					if (val > high) {
						high = val;
					} else if (val < low) {
						low = val;
					}
				} else {
					close = prevPrice;					
					m1Candles.add(new Candlestick(open, high, low, close, ldt.minusSeconds(ldt.getSecond()).minusMinutes(diff)));
					open = val;
					high = val;
					low = val;
					close = val;
					ldtPrev = ldt;
				}
				prevPrice = val;
			}
			chartWidth = width - PRICE_MARGIN - CHT_MARGIN;
			chartHeight = height - HSB_HEIGHT - CHT_MARGIN*2;
			//TODO fix hard coding here and copy code to make it run on new charts
			candlestickWidth = chartWidth * 0.005;
			candlestickSpacing = candlestickWidth * 0.4;
			canvas = new Canvas(width, height);
			hsb = new HorizontalScrollBar(this, HSB_HEIGHT, HSB_WIDTH, data.size(), numDataPoints);
			gc = canvas.getGraphicsContext2D();
			charts.add(this);
			this.focusedChart = true;
			drawChart();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int getDiffInMinutes(LocalDateTime ldt1, LocalDateTime ldt2) {
		long ldt1EpochSec = ldt1.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
		long ldt2EpochSec = ldt2.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
		return (int)((ldt2EpochSec - ldt1EpochSec) / 60.0); 
	}
	
	public Canvas getCanvas() {
		return this.canvas;
	}
	
	public GraphicsContext getGraphicsContext() {
		return this.gc;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getHeight() {
		return height;
	}
	
	public HorizontalScrollBar getHSB() {
		return hsb;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public void drawCrosshair() {				
		double fontSize = gc.getFont().getSize();
		double dateBarHalfWidth = fontSize * 5;
		double dateBarX;
		if (focusedChart) {		
			if (onChart(crossHairX, crossHairY)) {
				gc.strokeLine(CHT_MARGIN, crossHairY, width - PRICE_MARGIN, crossHairY);
				gc.strokeLine(crossHairX, CHT_MARGIN, crossHairX, height - HSB_HEIGHT - CHT_MARGIN);
				
				crossHairPrice = ((((chartHeight - (CHT_DATA_MARGIN*2)) - (crossHairY - CHT_MARGIN - CHT_DATA_MARGIN))/ (double)(chartHeight - (CHT_DATA_MARGIN*2))) * range) + lowest;		
				
				gc.fillRect(chartWidth + CHT_MARGIN, crossHairY - fontSize/2, 100, fontSize);
				gc.setStroke(Color.WHITE);
				gc.strokeText(((Double)(roundToNearestTick(crossHairPrice))).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, crossHairY + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
				gc.setStroke(Color.BLACK);
				
				double stub;
				if (drawCandlesticks) {
					stub = chartWidth - ((int)(chartWidth / (candlestickWidth + candlestickSpacing)) * (candlestickWidth + candlestickSpacing));
				} else {
					stub = 0;
				}
				crossHairDateIndex = startIndex + (int)(((crossHairX-CHT_MARGIN)/(chartWidth - stub)) * (endIndex-startIndex));
				
				if (drawCandlesticks) {
					String ohlc = "O: " + m1Candles.get(crossHairDateIndex).open;
					ohlc += "  H: " + m1Candles.get(crossHairDateIndex).high;
					ohlc += "  L: " + m1Candles.get(crossHairDateIndex).low;
					ohlc += "  C: " + m1Candles.get(crossHairDateIndex).close;
					gc.strokeText(ohlc, CHT_MARGIN * 2, CHT_MARGIN + fontSize);
				}
				
				if (crossHairX < CHT_MARGIN + dateBarHalfWidth) {
					dateBarX = CHT_MARGIN;
				} else if (crossHairX > chartWidth + CHT_MARGIN - dateBarHalfWidth) {
					dateBarX = chartWidth + CHT_MARGIN - dateBarHalfWidth*2;
				} else {
					dateBarX = crossHairX - dateBarHalfWidth;
				}
				
				gc.fillRect(dateBarX, chartHeight - CHT_MARGIN - 1, dateBarHalfWidth*2, fontSize);
				gc.setStroke(Color.WHITE);
				if (drawCandlesticks) {
					gc.strokeText(m1Candles.get(crossHairDateIndex).dateTime.toString().replace('T', ' '), dateBarX + fontSize / 3, chartHeight + CHT_MARGIN - 1, dateBarHalfWidth*2);
				} else {
					gc.strokeText(data.get(crossHairDateIndex).dateTime.toString().replace('T', ' '), dateBarX + fontSize / 3, chartHeight + CHT_MARGIN - 1, dateBarHalfWidth*2);
				}
				gc.setStroke(Color.BLACK);
			}
		} else if (focusedOnChart) {						
			if (crossHairPrice >= lowest - dataMarginSize && crossHairPrice <= highest + dataMarginSize) {					
				double yPos = ((highest + dataMarginSize - crossHairPrice) / (range + dataMarginSize * 2)) * chartHeight + CHT_MARGIN;
				gc.strokeLine(CHT_MARGIN, yPos, width - PRICE_MARGIN, yPos);
				gc.fillRect(chartWidth + CHT_MARGIN, yPos - fontSize/2, 100, fontSize);
				gc.setStroke(Color.WHITE);
				gc.strokeText(((Double)(roundToNearestTick(crossHairPrice))).toString().replace('T', ' '), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, yPos + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
				gc.setStroke(Color.BLACK);
			}
			if (crossHairDateIndex >= startIndex && crossHairDateIndex <= endIndex && !drawCandlesticks) {
				if (chdi_IsForCandle) {
					long startEpochMin = (int)(data.get(startIndex).dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
					long endEpochMin = (int)(data.get(endIndex).dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
					long chdiEpochMin = (int)(m1Candles.get(crossHairDateIndex).dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
					if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
						for (int i = startIndex; i < endIndex; i++) {
							long ldtEpochMin = (int)(data.get(i).dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
							if (ldtEpochMin == chdiEpochMin) {
								crossHairDateIndex = i;
								break;
							} else if (chdiEpochMin < ldtEpochMin) {
								break;
							}
							crossHairDateIndex = i;
						}
						int indexRange = endIndex - startIndex;
						double percOfRange = (crossHairDateIndex - startIndex) / (double)indexRange;
						double xPos = chartWidth * percOfRange + CHT_MARGIN;
						gc.strokeLine(xPos, CHT_MARGIN, xPos, height - HSB_HEIGHT - CHT_MARGIN);
						
						if (xPos < CHT_MARGIN + dateBarHalfWidth) {
							dateBarX = CHT_MARGIN;
						} else if (xPos > chartWidth + CHT_MARGIN - dateBarHalfWidth) {
							dateBarX = chartWidth + CHT_MARGIN - dateBarHalfWidth*2;
						} else {
							dateBarX = xPos - dateBarHalfWidth;
						}
						
						gc.fillRect(dateBarX, chartHeight - CHT_MARGIN - 1, dateBarHalfWidth*2, fontSize);
						gc.setStroke(Color.WHITE);
						gc.strokeText(data.get(crossHairDateIndex).dateTime.toString().replace('T', ' '), dateBarX + fontSize / 3, chartHeight + CHT_MARGIN - 1, dateBarHalfWidth*2);
						gc.setStroke(Color.BLACK);
					}
				} else {
					int indexRange = endIndex - startIndex;
					double percOfRange = (crossHairDateIndex - startIndex) / (double)indexRange;
					double xPos = chartWidth * percOfRange + CHT_MARGIN;
					gc.strokeLine(xPos, CHT_MARGIN, xPos, height - HSB_HEIGHT - CHT_MARGIN);
					
					if (xPos < CHT_MARGIN + dateBarHalfWidth) {
						dateBarX = CHT_MARGIN;
					} else if (xPos > chartWidth + CHT_MARGIN - dateBarHalfWidth) {
						dateBarX = chartWidth + CHT_MARGIN - dateBarHalfWidth*2;
					} else {
						dateBarX = xPos - dateBarHalfWidth;
					}
					
					gc.fillRect(dateBarX, chartHeight - CHT_MARGIN - 1, dateBarHalfWidth*2, fontSize);
					gc.setStroke(Color.WHITE);
					gc.strokeText(data.get(crossHairDateIndex).dateTime.toString().replace('T', ' '), dateBarX + fontSize / 3, chartHeight + CHT_MARGIN - 1, dateBarHalfWidth*2);
					gc.setStroke(Color.BLACK);
				}
			} else if (drawCandlesticks) {
				if (chdi_IsForCandle) {
					if (crossHairDateIndex >= startIndex && crossHairDateIndex <= endIndex) {
						double xPos = (crossHairDateIndex - startIndex) * (candlestickWidth + candlestickSpacing) + (candlestickWidth + candlestickSpacing) / 2 + CHT_MARGIN;
						gc.strokeLine(xPos, CHT_MARGIN, xPos, height - HSB_HEIGHT - CHT_MARGIN);
						
						if (xPos < CHT_MARGIN + dateBarHalfWidth) {
							dateBarX = CHT_MARGIN;
						} else if (xPos > chartWidth + CHT_MARGIN - dateBarHalfWidth) {
							dateBarX = chartWidth + CHT_MARGIN - dateBarHalfWidth*2;
						} else {
							dateBarX = xPos - dateBarHalfWidth;
						}
						
						gc.fillRect(dateBarX, chartHeight - CHT_MARGIN - 1, dateBarHalfWidth*2, fontSize);
						gc.setStroke(Color.WHITE);
						gc.strokeText(m1Candles.get(crossHairDateIndex).dateTime.toString().replace('T', ' '), dateBarX + fontSize / 3, chartHeight + CHT_MARGIN - 1, dateBarHalfWidth*2);
						gc.setStroke(Color.BLACK);
					}
				} else {
					long startEpochMin = (int)(m1Candles.get(startIndex).dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
					long endEpochMin = (int)(m1Candles.get(endIndex).dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
					long chdiEpochMin = (int)(data.get(crossHairDateIndex).dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
					if (chdiEpochMin >= startEpochMin && chdiEpochMin <= endEpochMin) {
						int crossHairDateIndex_Candle = 0;
						for (int i = startIndex; i < endIndex; i++) {
							long ldtEpochMin = (int)(m1Candles.get(i).dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() / 60.0);
							if (ldtEpochMin == chdiEpochMin) {
								crossHairDateIndex_Candle = i;
								break;
							} else if (chdiEpochMin < ldtEpochMin) {
								break;
							}
							crossHairDateIndex_Candle = i;
						}
						int indexRange = endIndex - startIndex;
						double percOfRange = (crossHairDateIndex_Candle - startIndex) / (double)indexRange;
						double stub = chartWidth - ((int)(chartWidth / (candlestickWidth + candlestickSpacing)) * (candlestickWidth + candlestickSpacing));
						double xPos = (chartWidth - stub) * percOfRange + CHT_MARGIN + candlestickWidth / 2;
						gc.strokeLine(xPos, CHT_MARGIN, xPos, height - HSB_HEIGHT - CHT_MARGIN);
						
						if (xPos < CHT_MARGIN + dateBarHalfWidth) {
							dateBarX = CHT_MARGIN;
						} else if (xPos > chartWidth + CHT_MARGIN - dateBarHalfWidth) {
							dateBarX = chartWidth + CHT_MARGIN - dateBarHalfWidth*2;
						} else {
							dateBarX = xPos - dateBarHalfWidth;
						}
						
						gc.fillRect(dateBarX, chartHeight - CHT_MARGIN - 1, dateBarHalfWidth*2, fontSize);
						gc.setStroke(Color.WHITE);
						gc.strokeText(m1Candles.get(crossHairDateIndex_Candle).dateTime.toString().replace('T', ' '), dateBarX + fontSize / 3, chartHeight + CHT_MARGIN - 1, dateBarHalfWidth*2);
						gc.setStroke(Color.BLACK);
					}
				}
			}
		}
	}
	
	public void onMouseExited() {
		focusedChart = false;
		newCHT_BTN_Hover = false;
		drawCandlesticksHover = false;
		for (Chart c : charts) {
			c.drawChart();
		}
	}
	
	public void onMouseEntered() {
		focusedChart = true;
		chdi_IsForCandle = drawCandlesticks;
		for (Chart c : charts) {
			c.drawChart();
		}
	}
	
	public void onMouseMoved(MouseEvent e) {
		crossHairX = e.getX();
		crossHairY = e.getY();
		if (e.getX() >= CHT_MARGIN + chartWidth && e.getX() <= CHT_MARGIN + chartWidth + PRICE_MARGIN / 2 - 1 && e.getY() >= CHT_MARGIN + chartHeight) {
			newCHT_BTN_Hover = true;
		} else {
			newCHT_BTN_Hover = false;
		}
		if (e.getX() >= CHT_MARGIN + chartWidth  + PRICE_MARGIN / 2 + 1 && e.getY() >= CHT_MARGIN + chartHeight) {
			drawCandlesticksHover = true;
		} else {
			drawCandlesticksHover = false;
		}
		for (Chart c : charts) {
			c.drawChart();
		}
	}		
	
	public void onMousePressed(MouseEvent e) {			
		if (e.isPrimaryButtonDown()) {
			if (onChart(e.getX(), e.getY())) {
				lines.add(crossHairPrice);
			}
		} else if (e.getX() >= CHT_MARGIN + chartWidth && e.getX() <= CHT_MARGIN + chartWidth + PRICE_MARGIN / 2 - 1 && e.getY() >= CHT_MARGIN + chartHeight) {
			Stage s = new Stage();
			ChartPane c = new ChartPane(s, width, height);
			charts.add(c.getChart());
			Scene scene = new Scene(c);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().getHSB().keyPressed(ev));
			s.setScene(scene);
			s.setOnCloseRequest(ev -> {
				charts.remove(c.getChart());
			});
			s.show();
		} else if (e.getX() >= CHT_MARGIN + chartWidth  + PRICE_MARGIN / 2 + 1 && e.getY() >= CHT_MARGIN + chartHeight) {
			if (drawCandlesticks) {
				drawCandlesticks = false;
				if (focusedChart) {
					chdi_IsForCandle = false;
				}
			} else {
				drawCandlesticks = true;
				if (focusedChart) {
					chdi_IsForCandle = true;
				}
			}
		} else {
			if (!lines.isEmpty()) {
				lines.removeLast();
			}
		}
		for (Chart c : charts) {
			c.drawChart();
		}
	}
	
	public void onMouseReleased(MouseEvent e) {	
		for (Chart c : charts) {
			c.drawChart();
		}
	}
	
	public void onMouseDragged(MouseEvent e) {	
		onMouseMoved(e);
	}
	
	public void onScroll(ScrollEvent e) {			
		if (e.getDeltaY() > 0) {
			setNumDataPoints(numDataPoints - 100);
		} else {
			setNumDataPoints(numDataPoints + 100);
		}
		double xDiff = chartWidth / (double)numDataPoints;
		if (xDiff * (data.size() - 1) < chartWidth) {
			setNumDataPoints(data.size() - 1);
		} else if (numDataPoints < 100) {
			setNumDataPoints(100);
		}
		double newHSBPOS = (width - HSB_WIDTH - PRICE_MARGIN) * ((double)startIndex /(data.size() - numDataPoints - 1));
		if (newHSBPOS > width - HSB_WIDTH - PRICE_MARGIN) {
			newHSBPOS = width - HSB_WIDTH - PRICE_MARGIN;
		}
		hsb.setPosition(newHSBPOS);
		for (Chart c : charts) {
			c.drawChart();
		}
	}		
	
	public boolean onChart(double x, double y) {
		if (y <= height - HSB_HEIGHT - CHT_MARGIN*2 && y >= CHT_MARGIN) {
			if (x <= width - PRICE_MARGIN - CHT_MARGIN && x >= CHT_MARGIN) {
				if (focusedChart) {
					focusedOnChart = true;
				}
				return true;
			}
		}
		if (focusedChart) {
			focusedOnChart = false;
		}
		return false;
	}
	
	
	
	private void calculateRange(int beginIndex, int endIndex) {
		//TODO Re-factor/Optimise
		if (drawCandlesticks) {
			lowest = m1Candles.get(beginIndex).low;
			highest = m1Candles.get(beginIndex).high;				
			
			for (int i = beginIndex; i < endIndex; i++) {			
				double low = m1Candles.get(i).low;
				double high = m1Candles.get(i).high;
				if (high > highest) {
					highest = high;
				} else if (low < lowest) {
					lowest = low;
				}
			}
			
			range = highest - lowest;
		} else {
			lowest = data.get(beginIndex).price;
			highest = data.get(beginIndex).price;				
			
			for (int i = beginIndex; i < endIndex; i++) {			
				double val = data.get(i).price;
				if (val > highest) {
					highest = val;
				} else if (val < lowest) {
					lowest = val;
				}
			}
			
			range = highest - lowest;
		}
	}
	
	private double roundDownToTick(double price) {
		int intPart = (int)price;
		price = price - intPart;
		int pow = (int)Math.pow(10, numDecimalPts);
		price *= pow;
		int numTicks = (int)(1 / tickSize);
		int intTick = (int)(pow * tickSize);
		int testTick = (int)(pow - intTick);	
		for (int i = 0; i < numTicks; i++) {
			if (price >= testTick) {
				price = testTick;
				break;
			}
			testTick -= intTick;
		}
		return intPart + price / pow;
	}	
	
	private double roundToNearestTick(double price) {
		int intPart = (int)price;
		price = price - intPart;
		int pow = (int)Math.pow(10, numDecimalPts);
		price *= pow;
		int intTick = (int)(pow * tickSize);		
		int quotient = (int)(price / intTick);
		double remainder = price % intTick;
		if (remainder > intTick / 2) {
			return intPart + (intTick * (quotient + 1)) / (double)pow; 
		}
		return intPart + (intTick * quotient) / (double)pow;
	}
	
	private double roundUpToTick(double price) {
		int intPart = (int)price;
		price = price - intPart;
		int pow = (int)Math.pow(10, numDecimalPts);
		price *= pow;
		int numTicks = (int)(1 / tickSize);
		int intTick = (int)(pow * tickSize);
		int testTick = 0;		
		for (int i = 0; i < numTicks + 1; i++) {
			if (price <= testTick) {
				price = testTick;
				break;
			}
			testTick += intTick;
		}
		return intPart + price / pow;
	}	
	
	private void drawLines() {
		//TODO check for inaccuracy		
		double trueLowest = roundUpToTick(lowest - dataMarginSize);
		double trueHighest = roundDownToTick(highest + dataMarginSize);
		for (Double d : lines) {
			if (d >= trueLowest && d <= trueHighest) {
				double fontSize = gc.getFont().getSize();
				double trueRange = trueHighest - trueLowest;
				double y = chartHeight + CHT_MARGIN - (((d - trueLowest) / trueRange) * chartHeight);
				gc.strokeLine(CHT_MARGIN, y, chartWidth + CHT_MARGIN, y);
				
				gc.setFill(Color.GRAY);
				gc.fillRect(chartWidth + CHT_MARGIN, y - fontSize/2, 100, fontSize);
				gc.setStroke(Color.WHITE);
				gc.strokeText(((Double)(roundUpToTick(d))).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, y + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
				gc.setFill(Color.BLACK);
				gc.setStroke(Color.BLACK);
			}
		}
	}
	
	public void drawCandleStick(Candlestick candle, double xPos, double yPos) {
		//TODO Re-factor/Optimise
		if (candle.open < candle.close) {
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.close - candle.open) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high - candle.close) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.close - candle.open) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.close - candle.low) / conversionVar);
			gc.setFill(Color.GREEN);
			gc.fillRect(xPos + 1, yPos + 1, candlestickWidth - 2, (candle.close - candle.open) / conversionVar - 2);
			gc.setFill(Color.BLACK);
		} else {
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.open - candle.close) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high - candle.open) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.open - candle.close) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.open - candle.low) / conversionVar);
			gc.setFill(Color.RED);
			gc.fillRect(xPos + 1, yPos + 1, candlestickWidth - 2, (candle.open - candle.close) / conversionVar - 2);
			gc.setFill(Color.BLACK);
		}
	}
	
	private void lineChartProc() {		
		xDiff = chartWidth / (double)numDataPoints;
		startIndex = (int)((hsb.position() / (width - HSB_WIDTH - PRICE_MARGIN)) * (data.size() - numDataPoints - 1));
		endIndex = startIndex + numDataPoints;
		gc.clearRect(0, 0, width, height);
		gc.strokeRect(CHT_MARGIN, CHT_MARGIN, chartWidth, chartHeight);
		
		gc.strokeRect(CHT_MARGIN + chartWidth, CHT_MARGIN + chartHeight, PRICE_MARGIN, HSB_HEIGHT + CHT_MARGIN);
		gc.strokeLine(CHT_MARGIN + chartWidth + PRICE_MARGIN / 2, CHT_MARGIN + chartHeight, CHT_MARGIN + chartWidth + PRICE_MARGIN / 2, CHT_MARGIN * 2 + chartHeight + HSB_HEIGHT);
		if (newCHT_BTN_Hover) {
			gc.fillRect(CHT_MARGIN + chartWidth, CHT_MARGIN + chartHeight, PRICE_MARGIN / 2, HSB_HEIGHT + CHT_MARGIN);
		}
		if (drawCandlesticksHover) {
			gc.fillRect(CHT_MARGIN + chartWidth + PRICE_MARGIN / 2, CHT_MARGIN + chartHeight, PRICE_MARGIN / 2, HSB_HEIGHT + CHT_MARGIN);
		}
		
		hsb.drawHSB();
		calculateRange(startIndex, endIndex + 1);		
		tickSizeOnChart = (chartHeight - CHT_DATA_MARGIN * 2) / (range / tickSize);
		dataMarginSize = (CHT_DATA_MARGIN / tickSizeOnChart) * tickSize;
		double conversionVar = tickSize / tickSizeOnChart;		
		double startY = chartHeight - CHT_DATA_MARGIN + CHT_MARGIN - (((data.get(startIndex).price - lowest) / range) * (chartHeight - CHT_DATA_MARGIN * 2));		
		double prevY = startY - ((data.get(startIndex + 1).price - data.get(startIndex).price) / conversionVar);
		gc.strokeLine(CHT_MARGIN, startY, xDiff + CHT_MARGIN, prevY);		
		for (int i = 1; i < numDataPoints; i++) {
			gc.strokeLine((i * xDiff)+CHT_MARGIN, prevY, ((i + 1) * xDiff)+CHT_MARGIN, prevY - ((data.get(startIndex + i + 1).price - data.get(startIndex + i).price) / conversionVar));
			prevY = prevY - ((data.get(startIndex + i + 1).price - data.get(startIndex + i).price) / conversionVar);
		}	
		double spacing = tickSizeOnChart * (int)(PRICE_DASH_SPACING / tickSizeOnChart);
		if (spacing == 0) {
			spacing = tickSizeOnChart;
		}
		double index = chartHeight - CHT_DATA_MARGIN + CHT_MARGIN;
		double priceDashPos = chartWidth + CHT_MARGIN;
		double pricePos = priceDashPos + PRICE_DASH_SIZE + PRICE_DASH_MARGIN;
		int pricePosYMargin = (int)(gc.getFont().getSize()/3);
		double diff = (spacing / tickSizeOnChart) * tickSize;
		int i = 0;
		while (index > CHT_MARGIN + gc.getFont().getSize()/3) {
			gc.strokeLine(priceDashPos, index, priceDashPos + PRICE_DASH_SIZE, index);
			gc.strokeText(((Double)(lowest + (diff * i))).toString(), pricePos, index + pricePosYMargin, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
			index -= spacing;
			i++;
		}
		if (!lines.isEmpty()) {
			drawLines();
		}
		drawCrosshair();
	}
	
	private void candlestickChartProc() {		
		numCandlesticks = (int)(chartWidth / (candlestickWidth + candlestickSpacing));
		startIndex = (int)((hsb.position() / (width - HSB_WIDTH - PRICE_MARGIN)) * (m1Candles.size() - numCandlesticks - 1));
		endIndex = startIndex + numCandlesticks;
		gc.clearRect(0, 0, width, height);
		gc.strokeRect(CHT_MARGIN, CHT_MARGIN, chartWidth, chartHeight);
		
		gc.strokeRect(CHT_MARGIN + chartWidth, CHT_MARGIN + chartHeight, PRICE_MARGIN, HSB_HEIGHT + CHT_MARGIN);
		gc.strokeLine(CHT_MARGIN + chartWidth + PRICE_MARGIN / 2, CHT_MARGIN + chartHeight, CHT_MARGIN + chartWidth + PRICE_MARGIN / 2, CHT_MARGIN * 2 + chartHeight + HSB_HEIGHT);
		if (newCHT_BTN_Hover) {
			gc.fillRect(CHT_MARGIN + chartWidth, CHT_MARGIN + chartHeight, PRICE_MARGIN / 2, HSB_HEIGHT + CHT_MARGIN);
		}
		if (drawCandlesticksHover) {
			gc.fillRect(CHT_MARGIN + chartWidth + PRICE_MARGIN / 2, CHT_MARGIN + chartHeight, PRICE_MARGIN / 2, HSB_HEIGHT + CHT_MARGIN);
		}
		
		hsb.drawHSB();
		calculateRange(startIndex, endIndex + 1);
		tickSizeOnChart = (chartHeight - CHT_DATA_MARGIN * 2) / (range / tickSize);
		dataMarginSize = (CHT_DATA_MARGIN / tickSizeOnChart) * tickSize;
		conversionVar = tickSize / tickSizeOnChart;	
		for (int i = 0; i < numCandlesticks; i++) {
			Candlestick c = m1Candles.get(startIndex + i); 
			double xPos = CHT_MARGIN + (candlestickWidth + candlestickSpacing) * i;
			double yPos;
			if (c.open < c.close) {
				yPos = ((highest - c.close) / range) * (chartHeight - CHT_DATA_MARGIN * 2) + CHT_DATA_MARGIN + CHT_MARGIN;
			} else {
				yPos = ((highest - c.open) / range) * (chartHeight - CHT_DATA_MARGIN * 2) + CHT_DATA_MARGIN + CHT_MARGIN;
			}
			drawCandleStick(c, xPos, yPos);
		}	
		double spacing = tickSizeOnChart * (int)(PRICE_DASH_SPACING / tickSizeOnChart);
		if (spacing == 0) {
			spacing = tickSizeOnChart;
		}
		double index = chartHeight - CHT_DATA_MARGIN + CHT_MARGIN;
		double priceDashPos = chartWidth + CHT_MARGIN;
		double pricePos = priceDashPos + PRICE_DASH_SIZE + PRICE_DASH_MARGIN;
		int pricePosYMargin = (int)(gc.getFont().getSize()/3);
		double diff = (spacing / tickSizeOnChart) * tickSize;
		int i = 0;
		while (index > CHT_MARGIN + gc.getFont().getSize()/3) {
			gc.strokeLine(priceDashPos, index, priceDashPos + PRICE_DASH_SIZE, index);
			gc.strokeText(((Double)(lowest + (diff * i))).toString(), pricePos, index + pricePosYMargin, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
			index -= spacing;
			i++;
		}
		if (!lines.isEmpty()) {
			drawLines();
		}
		drawCrosshair();
	}
	
	public void drawChart() {		
		if (drawCandlesticks) {
			candlestickChartProc();
		} else {
			lineChartProc();
		}
	}
	
	public void setNumDataPoints(int numDataPoints) {
		this.numDataPoints = numDataPoints;
		hsb.updateHSBMove(data.size(), numDataPoints);
	}	
	
	public static void drawCharts() {
		for (Chart c : charts) {
			c.drawChart();
		}
	}
}
