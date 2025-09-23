import java.util.ArrayList;
import java.util.Random;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class Chart {
	public final static double HSB_WIDTH = 100;
	public final static double HSB_HEIGHT = 10;
	
	//TODO consider a function to calculate the price given a y coordinate
	public final static double WIDTH_EXTRA = 16;
	public final static double HEIGHT_EXTRA = 39;
	
	public final static double MIN_WIDTH = 500; 
	public final static double MIN_HEIGHT = 300; 
	
	public final static double CHT_MARGIN = 5;
	public final static double INFO_MARGIN = 5;
	public final static double CHT_DATA_MARGIN_COEF = 0.45;	
	public final static double PRICE_MARGIN = 100;
	public final static double END_MARGIN_COEF = 1/1.5;
	
	public final static double PRICE_DASH_SPACING = 50;
	public final static double PRICE_DASH_SIZE = 5;
	public final static double PRICE_DASH_MARGIN = 5;
	
	public final static double CNDL_WDTH_COEF = 0.005;
	public final static double CNDL_SPAC_COEF = 0.4;
	
	//ChartType
	private static ArrayList<Chart> charts = new ArrayList<Chart>();	
	private static boolean focusedOnChart = false;	
	private static boolean darkMode = false;
	
	private DataSet data;
	private CrossHair crossHair;
	
	private int numDecimalPts;
	private double tickSize;
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
	private ScrollBar hsb;
	private int numDataPoints = 1495;//299	
	private int startIndex;
	private int endIndex;
	private double tickSizeOnChart;
	private double conversionVar;
	private double dataMarginSize;
	private double xDiff;
	private double fontSize;
	private double chtDataMargin;
	private boolean priceDragging = false;
	private double priceInitPos;
	private boolean chartDragging = false;
	private double chartInitPos;
	private boolean endMargin = false;
	private Stage stage;
	private boolean replayMode = false;
	
	//ChartButton
	private boolean newCHT_BTN_Hover = false;
	private boolean drawCandlesticksHover = false;
	private boolean darkModeHover = false;
	private boolean newCHT_BTN_Clicked = false;
	private boolean drawCandlesticksClicked = false;
	private boolean darkModeClicked = false;
	
	//Candlestick static variables
	private boolean drawCandlesticks = false;
	private double candlestickWidth;
	private double candlestickSpacing;
	private int numCandlesticks;
	private boolean roundUp = false;
	
	private class WidthListener implements ChangeListener<Number> {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {			
			double newHSBPos = (hsb.xPos() / (width - hsb.sbWidth() - PRICE_MARGIN)) * (newValue.doubleValue() - WIDTH_EXTRA - hsb.sbWidth() - PRICE_MARGIN);	
			width = newValue.doubleValue() - WIDTH_EXTRA;
			canvas.setWidth(width);
			chartWidth = width - PRICE_MARGIN - CHT_MARGIN;
			if (!drawCandlesticks) {
				hsb.setSBMove(data.tickDataSize(replayMode), numDataPoints);				
			}			
			setCandleStickVars(numCandlesticks);
			hsb.setMaxPos(width - PRICE_MARGIN);
			hsb.setPosition(newHSBPos, false);
			drawChart();
		}		
	}
	
	private class HeightListener implements ChangeListener<Number> {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			height = newValue.doubleValue() - HEIGHT_EXTRA;
			canvas.setHeight(height);
			chartHeight = height - hsb.sbHeight() - CHT_MARGIN * 2; 
			if (chtDataMargin > chartHeight * CHT_DATA_MARGIN_COEF) {
				chtDataMargin = chartHeight * CHT_DATA_MARGIN_COEF;
			} else {
				if (!((Double)oldValue.doubleValue()).isNaN()) {
					double ratio = chtDataMargin / (oldValue.doubleValue() - HEIGHT_EXTRA);
					chtDataMargin = (newValue.doubleValue() - HEIGHT_EXTRA) * ratio;
				}
			}
			hsb.setYPos(height - HSB_HEIGHT);
			drawChart();
		}		
	}
	
	public Chart(double width, double height, Stage stage, DataSet data) throws Exception {
		constructorStuff(width, height, stage, data);
	}
	
	private void constructorStuff(double width, double height, Stage stage, DataSet data) throws Exception {		
		if (numDecimalPts < 0) {
			throw new Exception("numDecimalPts cannot be less than 0");
		}
		this.numDecimalPts = data.numDecimalPts();
		this.tickSize = data.tickSize();
		this.width = width;
		this.height = height;
		this.data = data;
		stage.setMinWidth(MIN_WIDTH);
		stage.setMinHeight(MIN_HEIGHT);
		stage.heightProperty().addListener(new HeightListener());
		stage.widthProperty().addListener(new WidthListener());	
		stage.setOnCloseRequest(ev -> {
			charts.remove(this);
		});
		this.stage = stage;
		canvas = new Canvas(width, height);
		hsb = new ScrollBar(this, data.tickDataSize(this.replayMode), numDataPoints, 0, width - PRICE_MARGIN, HSB_WIDTH, HSB_HEIGHT, false, height - HSB_HEIGHT);		
		gc = canvas.getGraphicsContext2D();	
		fontSize = gc.getFont().getSize();
		crossHair = new CrossHair(this);		
		chartWidth = width - PRICE_MARGIN - CHT_MARGIN;
		chartHeight = height - hsb.sbHeight() - CHT_MARGIN*2;
		candlestickWidth = chartWidth * CNDL_WDTH_COEF;
		candlestickSpacing = candlestickWidth * CNDL_SPAC_COEF;
		numCandlesticks = (int)(chartWidth / (candlestickWidth + candlestickSpacing));
		chtDataMargin = CHT_MARGIN + fontSize;		
		Chart.charts.add(this);
		setEventHandlers();		
		drawChart();
	}
	
	private void setEventHandlers() {
		canvas.setOnMouseDragged(e -> onMouseDragged(e));
		canvas.setOnMouseEntered(e -> onMouseEntered());
		canvas.setOnMouseExited(e -> onMouseExited());
		canvas.setOnMousePressed(e -> onMousePressed(e));
		canvas.setOnMouseReleased(e -> onMouseReleased(e));
		canvas.setOnMouseMoved(e -> onMouseMoved(e));
		canvas.setOnScroll(e -> onScroll(e));
	}
	
	public Canvas canvas() {
		return this.canvas;
	}
	
	public GraphicsContext graphicsContext() {
		return this.gc;
	}
	
	public DataSet data() {
		return this.data;
	}
	
	public double width() {
		return width;
	}
	
	public double height() {
		return height;
	}
	
	public double chartWidth() {
		return chartWidth;
	}
	
	public double chartHeight() {
		return chartHeight;
	}
	
	public double range() {
		return this.range;
	}
	
	public double lowest() {
		return this.lowest;
	}
	
	public double highest() {
		return this.highest;
	}
	
	public int startIndex() {
		return this.startIndex;
	}
	
	public int endIndex() {
		return this.endIndex;
	}
	
	public double chtDataMargin() {
		return this.chtDataMargin;
	}

	public double dataMarginSize() {
		return this.dataMarginSize;
	}
	
	public double candlestickWidth() {
		return this.candlestickWidth;
	}
	
	public double candlestickSpacing() {
		return this.candlestickSpacing;
	}
	
	public String name() {
		return this.data.name();
	}
	
	public static boolean focusedOnChart() {
		return Chart.focusedOnChart;
	}
	
	public ArrayList<DataSet.Candlestick> m1Candles() {
		return this.data.m1Candles();
	}
	
	public ArrayList<DataSet.DataPair> tickData() {
		return this.data.tickData();
	}
	
	public void close() {
		stage.close();
	}
	
	public static boolean darkMode() {
		return Chart.darkMode;
	}
	
	public void setReplayMode(boolean replayMode) {
		this.replayMode = replayMode;
	}
	
	public boolean replayMode() {
		return this.replayMode;
	}
	
	public static void closeAll(String name) {
		Object[] chts = charts.toArray();
		int i = 0;
		for (Object c : chts) {
			Chart cht = (Chart)c;
			if (cht.name().equals(name)) {
				Chart ch = charts.get(i);
				ch.close();
				charts.remove(i);
				i--;
			}
			i++;
		}
	}
	
	public ScrollBar hsb() {
		return this.hsb;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public boolean endMargin() {
		return this.endMargin;
	}
	
	public double xDiff() {
		return this.xDiff;
	}
	
	public void onMouseExited() {
		hsb.onMouseExited();
		focusedChart = false;
		newCHT_BTN_Hover = false;
		drawCandlesticksHover = false;
		darkModeHover = false;
		for (Chart c : charts) {
			c.drawChart();
		}
	}
	
	public void onMouseEntered() {
		focusedChart = true;
		CrossHair.setIsForCandle(drawCandlesticks);
		CrossHair.setDateIndex(0);
		CrossHair.setName(data.name());
		for (Chart c : charts) {
			c.drawChart();
		}
	}
	
	public void onMouseMoved(MouseEvent e) {
		hsb.onMouseMoved(e);
		CrossHair.setX(e.getX());
		CrossHair.setY(e.getY());
		if (checkNewChtBtn(e.getX(), e.getY())) {
			if (!newCHT_BTN_Clicked) {
				newCHT_BTN_Hover = true;
			}
		} else {
			newCHT_BTN_Hover = false;
			newCHT_BTN_Clicked = false;
		}
		if (checkChartTypeBtn(e.getX(), e.getY())) {
			if (!drawCandlesticksClicked) {
				drawCandlesticksHover = true;
			}
		} else {
			drawCandlesticksHover = false;
			drawCandlesticksClicked = false;	
		}
		if (checkDarkModeBtn(e.getX(), e.getY())) {
			if (!darkModeClicked) {
				darkModeHover = true;
			}
		} else {
			darkModeHover = false;
			darkModeClicked = false;
		}
		for (Chart c : charts) {
			c.drawChart();
		}
	}				
	
	private boolean checkNewChtBtn(double x, double y) {
		if (x >= CHT_MARGIN + chartWidth && x <= CHT_MARGIN + chartWidth + PRICE_MARGIN / 3 - 1 && y >= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}
	
	private boolean checkChartTypeBtn(double x, double y) {
		if (x >= CHT_MARGIN + chartWidth  + PRICE_MARGIN / 3 + 1 && x <= CHT_MARGIN + chartWidth + PRICE_MARGIN * 2 / 3 - 1 && y >= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}

	private boolean checkDarkModeBtn(double x, double y) {
		if (x >= CHT_MARGIN + chartWidth  + PRICE_MARGIN * 2 / 3 + 1 && y >= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}
	
	public void onMousePressed(MouseEvent e) {		
		hsb.onMousePressed(e);
		if (e.getButton() == MouseButton.MIDDLE) {
			if (onChart(e.getX(), e.getY())) {
				data.lines().add(roundToNearestTick(CrossHair.price()));
			}
		} else if (e.getButton() == MouseButton.SECONDARY) {
			if (!data.lines().isEmpty()) {
				data.lines().removeLast();
			}
		}
		if (e.isPrimaryButtonDown()) {			
			if (e.getX() >= width - PRICE_MARGIN && e.getY() <= chartHeight + CHT_MARGIN) {
				priceDragging = true;
				priceInitPos = e.getY();
			}
			if (onChart(e.getX(), e.getY())) {
				chartDragging = true;
				chartInitPos = e.getX();
			}
			if (checkNewChtBtn(e.getX(), e.getY())) {
				newCHT_BTN_Clicked = true;
				newCHT_BTN_Hover = false;
			} else if (checkChartTypeBtn(e.getX(), e.getY())) {
				drawCandlesticksClicked = true;	
				drawCandlesticksHover = false;
			} else if (checkDarkModeBtn(e.getX(), e.getY())) {
				darkModeClicked = true;
				darkModeHover = false;
			}
		} 				
		for (Chart c : charts) {
			c.drawChart();
		}
	}
	
	public void onMouseReleased(MouseEvent e) {	
		hsb.onMouseReleased();
		if (newCHT_BTN_Clicked && checkNewChtBtn(e.getX(), e.getY())) {
			newCHT_BTN_Clicked = false;
			Stage s = new Stage();
			ChartPane c = new ChartPane(s, width, height, data);
			Scene scene = new Scene(c);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		} else if (drawCandlesticksClicked && checkChartTypeBtn(e.getX(), e.getY())) {
			drawCandlesticksClicked = false;
			if (drawCandlesticks) {
				drawCandlesticks = false;
				CrossHair.setIsForCandle(false);
				int convertedIndex = (int)(((double)startIndex / data.m1CandlesDataSize(this.replayMode)) * data.tickDataSize(this.replayMode));
				double newHSBPos = ((double)convertedIndex / (data.tickDataSize(this.replayMode) - (numDataPoints - 1)  * END_MARGIN_COEF)) * (width - HSB_WIDTH - PRICE_MARGIN);
				hsb.setPosition(newHSBPos, false);
				hsb.setSBMove(data.tickDataSize(this.replayMode), numDataPoints);
			} else {
				if (m1Candles().isEmpty()) {
					return;
				}
				drawCandlesticks = true;	
				CrossHair.setIsForCandle(true);
				int convertedIndex = (int)(((double)startIndex / data.tickDataSize(this.replayMode)) * data.m1CandlesDataSize(this.replayMode));
				double newHSBPos = ((double)convertedIndex / (data.m1CandlesDataSize(this.replayMode) - numCandlesticks * END_MARGIN_COEF)) * (width - HSB_WIDTH - PRICE_MARGIN);
				int si = (int)((newHSBPos / (width - hsb.sbWidth() - PRICE_MARGIN)) * (data.m1CandlesDataSize(this.replayMode) - numCandlesticks * END_MARGIN_COEF));
				if (si < startIndex) {
					roundUp = true;
				} else {
					roundUp = false;
				}
				hsb.setPosition(newHSBPos, false);
				hsb.setSBMove(data.m1CandlesDataSize(this.replayMode), numCandlesticks);
			}
		} else if (darkModeClicked && checkDarkModeBtn(e.getX(), e.getY())) {
			darkModeClicked = false;	
			if (darkMode) {
				darkMode = false;
			} else {
				darkMode = true;
			}
		}						
		priceDragging = false;
		chartDragging = false;
		for (Chart c : charts) {
			c.drawChart();
		}
	}
	
	public void onMouseDragged(MouseEvent e) {	
		hsb.onMouseDragged(e);
		if (priceDragging) {
			double posDiff = e.getY() - priceInitPos;
			if (posDiff < 0) {
				if (chtDataMargin + posDiff > CHT_MARGIN + fontSize) {
					chtDataMargin += posDiff;
				}
			} else if (chtDataMargin + posDiff < chartHeight * CHT_DATA_MARGIN_COEF) {
				chtDataMargin += posDiff;
			}
		}
		priceInitPos = e.getY();
		if (chartDragging) {
			double posDiff = e.getX() - chartInitPos;
			hsb.setPosition(hsb.xPos() - (posDiff * hsb.hsbMove() / 10), false);
		}
		chartInitPos = e.getX();
		onMouseMoved(e);
	}
	
	public void onScroll(ScrollEvent e) {	
		//TODO Re-factor/optimise. Remove hard coded values
		if (drawCandlesticks) {
			if (e.getDeltaY() > 0) {
				if (numCandlesticks - 5 >= 10) {
					numCandlesticks -= 5;
					setCandleStickVars(numCandlesticks);
				}
			} else {
				if (numCandlesticks + 5 <= data.m1CandlesDataSize(this.replayMode) - 5) {
					numCandlesticks += 5;
					setCandleStickVars(numCandlesticks);
				}				
			}
			double newHSBPos = (width - hsb.sbWidth() - PRICE_MARGIN) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode) - numCandlesticks * END_MARGIN_COEF));		
			int si = (int)((newHSBPos / (width - hsb.sbWidth() - PRICE_MARGIN)) * (data.m1CandlesDataSize(this.replayMode) - numCandlesticks * END_MARGIN_COEF));
			if (si < startIndex) {
				roundUp = true;
			} else {
				roundUp = false;
			}
			if (newHSBPos > width - hsb.sbWidth() - PRICE_MARGIN) {
				newHSBPos = width - hsb.sbWidth() - PRICE_MARGIN;
			}
			hsb.setPosition(newHSBPos, false);
		} else {
			if (e.getDeltaY() > 0) {
				setNumDataPoints(numDataPoints - 100);
			} else {
				setNumDataPoints(numDataPoints + 100);
			}
			double xDiff = chartWidth / (double)numDataPoints;
			if (xDiff * (data.tickDataSize(this.replayMode) - 1) < chartWidth) {
				setNumDataPoints(data.tickDataSize(this.replayMode) - 1);
			} else if (numDataPoints < 100) {
				setNumDataPoints(100);
			}
			double newHSBPos = (width - hsb.sbWidth() - PRICE_MARGIN) * ((double)startIndex /(data.tickDataSize(this.replayMode) - (numDataPoints - 1) * END_MARGIN_COEF));
			if (newHSBPos > width - hsb.sbWidth() - PRICE_MARGIN) {
				newHSBPos = width - hsb.sbWidth() - PRICE_MARGIN;
			}
			hsb.setPosition(newHSBPos, false);
		}
		for (Chart c : charts) {
			c.drawChart();
		}
	}		
	
	public boolean onChart(double x, double y) {
		if (y <= chartHeight + CHT_MARGIN && y >= CHT_MARGIN) {
			if (x <= chartWidth + CHT_MARGIN && x >= CHT_MARGIN) {
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
		if (drawCandlesticks) {
			lowest = data.m1Candles().get(beginIndex).low();
			highest = data.m1Candles().get(beginIndex).high();				
			
			for (int i = beginIndex; i < endIndex; i++) {			
				double low = data.m1Candles().get(i).low();
				double high = data.m1Candles().get(i).high();				
				if (high > highest) {
					highest = high;
				} 
				if (low < lowest) {					
					lowest = low;
				}
			}			
			range = highest - lowest;
		} else {
			lowest = data.tickData().get(beginIndex).price();
			highest = data.tickData().get(beginIndex).price();				
			
			for (int i = beginIndex; i < endIndex; i++) {			
				double val = data.tickData().get(i).price();
				if (val > highest) {
					highest = val;
				} else if (val < lowest) {
					lowest = val;
				}
			}			
			range = highest - lowest;
		}
	}
	
	public double roundToNearestTick(double price) {
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
	
	private void drawLines() {
		double trueLowest = lowest - dataMarginSize;
		double trueHighest = highest + dataMarginSize;
		for (Double d : data.lines()) {
			if (d >= trueLowest && d <= trueHighest) {
				double fontSize = gc.getFont().getSize();
				double trueRange = trueHighest - trueLowest;
				double y = chartHeight + CHT_MARGIN - (((d - trueLowest) / trueRange) * chartHeight);
				gc.strokeLine(CHT_MARGIN, y, chartWidth + CHT_MARGIN, y);
				
				gc.setFill(Color.GRAY);
				gc.fillRect(chartWidth + CHT_MARGIN, y - fontSize/2, 100, fontSize);
				gc.setStroke(Color.WHITE);
				gc.strokeText(((Double)(roundToNearestTick(d))).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, y + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
				gc.setFill(Color.BLACK);
				if (darkMode) {
					gc.setStroke(Color.WHITE);
				} else {
					gc.setStroke(Color.BLACK);
				}
			}
		}
	}
	
	public void drawCandleStick(DataSet.Candlestick candle, double xPos, double yPos) {
		int num = 0;
		if (darkMode) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		if (candle.open() < candle.close()) {
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.close() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.close()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.close() - candle.open()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.close() - candle.low()) / conversionVar);
			gc.setFill(Color.CORNFLOWERBLUE);
			gc.fillRect(xPos, yPos, candlestickWidth - num, (candle.close() - candle.open()) / conversionVar - num);
		} else {
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.open() - candle.close()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.open() - candle.close()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.open() - candle.low()) / conversionVar);
			gc.setFill(Color.ORANGE);
			gc.fillRect(xPos + num, yPos + num, candlestickWidth - num, (candle.open() - candle.close()) / conversionVar - num);
		}
	}
	
	private void fillNewChtBtn() {
		if (newCHT_BTN_Clicked) {
			gc.setFill(Color.DIMGRAY);
			gc.fillRect(CHT_MARGIN + chartWidth + 1, CHT_MARGIN + chartHeight + 1, PRICE_MARGIN / 3 - 2, HSB_HEIGHT + CHT_MARGIN - 2);
		} else if (newCHT_BTN_Hover) {
			gc.setFill(Color.GRAY);
			gc.fillRect(CHT_MARGIN + chartWidth + 1, CHT_MARGIN + chartHeight + 1, PRICE_MARGIN / 3 - 2, HSB_HEIGHT + CHT_MARGIN - 2);
		}					
	}
	
	private void fillChartTypeBtn() {
		if (drawCandlesticksClicked) {
			gc.setFill(Color.DIMGRAY);
			gc.fillRect(width - PRICE_MARGIN * 2 / 3, CHT_MARGIN + chartHeight + 1, PRICE_MARGIN / 3 - 2, HSB_HEIGHT + CHT_MARGIN - 2);
		} else if (drawCandlesticksHover) {
			gc.setFill(Color.GRAY);
			gc.fillRect(width - PRICE_MARGIN * 2 / 3, CHT_MARGIN + chartHeight + 1, PRICE_MARGIN / 3 - 2, HSB_HEIGHT + CHT_MARGIN - 2);
		}					
	}

	private void fillDarkModeBtn() {		
		if (darkModeClicked) {
			gc.setFill(Color.DIMGRAY);
			gc.fillRect(width - PRICE_MARGIN / 3, CHT_MARGIN + chartHeight + 1, PRICE_MARGIN / 3 - 1, HSB_HEIGHT + CHT_MARGIN - 2);
		} else if (darkModeHover) {
			gc.setFill(Color.GRAY);
			gc.fillRect(width - PRICE_MARGIN / 3, CHT_MARGIN + chartHeight + 1, PRICE_MARGIN / 3 - 1, HSB_HEIGHT + CHT_MARGIN - 2);
		}					
	}
	
	private void calculateIndices() {
		if (drawCandlesticks) {
			startIndex = (int)((hsb.xPos() / (width - HSB_WIDTH - PRICE_MARGIN)) * (data.m1CandlesDataSize(this.replayMode) - numCandlesticks * END_MARGIN_COEF));
			if (roundUp) {
				if (startIndex + numCandlesticks < data.m1CandlesDataSize(this.replayMode)) {
					startIndex += 1;
				}
			}
			endIndex = startIndex + numCandlesticks;
			if (endIndex > data.m1CandlesDataSize(this.replayMode)) {
				endIndex = data.m1CandlesDataSize(this.replayMode);
			}
		} else {
			startIndex = (int)((hsb.xPos() / (width - HSB_WIDTH - PRICE_MARGIN)) * (data.tickDataSize(this.replayMode) - (numDataPoints - 1) * END_MARGIN_COEF));
			endIndex = startIndex + numDataPoints;
			if (endIndex >= data.tickDataSize(this.replayMode)) {
				endIndex = data.tickDataSize(this.replayMode) - 1;
			}
		}		
	}
	
	private void drawFrame() {
		gc.clearRect(0, 0, width, height);		
		if (darkMode) {			
			gc.setFill(Color.BLACK);
			gc.fillRect(0, 0, width, height);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		gc.strokeRect(CHT_MARGIN, CHT_MARGIN, chartWidth, chartHeight);
		if (drawCandlesticks) {
			gc.strokeText(data.name() + "  M1", CHT_MARGIN + INFO_MARGIN, CHT_MARGIN + fontSize);
		} else {
			gc.strokeText(data.name() + "  T1", CHT_MARGIN + INFO_MARGIN, CHT_MARGIN + fontSize);
		}
		gc.strokeRect(CHT_MARGIN + chartWidth, CHT_MARGIN + chartHeight, PRICE_MARGIN, HSB_HEIGHT + CHT_MARGIN);
		gc.strokeLine(width - PRICE_MARGIN / 3 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN / 3 - 1, height);
		gc.strokeLine(width - PRICE_MARGIN * 2 / 3 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN * 2 / 3 - 1, height);
	}	
	
	private void setPreDrawVars() {
		if (drawCandlesticks) {
			tickSizeOnChart = (chartHeight - chtDataMargin * 2) / (range / tickSize);
			dataMarginSize = (chtDataMargin / tickSizeOnChart) * tickSize;
			conversionVar = tickSize / tickSizeOnChart;	
		} else {
			xDiff = chartWidth / (double)numDataPoints;	
			tickSizeOnChart = (chartHeight - chtDataMargin * 2) / (range / tickSize);
			dataMarginSize = (chtDataMargin / tickSizeOnChart) * tickSize;
			conversionVar = tickSize / tickSizeOnChart;	
		}
	}
	
	private void drawLineChart() {
		endMargin = false;
		double startY = chartHeight - chtDataMargin + CHT_MARGIN - (((data.tickData().get(startIndex).price() - lowest) / range) * (chartHeight - chtDataMargin * 2));		
		double prevY = startY - ((data.tickData().get(startIndex + 1).price() - data.tickData().get(startIndex).price()) / conversionVar);
		if (darkMode) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		gc.strokeLine(CHT_MARGIN, startY, xDiff + CHT_MARGIN, prevY);		
		for (int i = 1; i < numDataPoints; i++) {
			if (startIndex + i > data.tickDataSize(this.replayMode) - 2) {
				endMargin = true;
				break;
			}
			gc.strokeLine((i * xDiff)+CHT_MARGIN, prevY, ((i + 1) * xDiff)+CHT_MARGIN, prevY - ((data.tickData().get(startIndex + i + 1).price() - data.tickData().get(startIndex + i).price()) / conversionVar));
			prevY = prevY - ((data.tickData().get(startIndex + i + 1).price() - data.tickData().get(startIndex + i).price()) / conversionVar);			
		}	
	}
	
	private void drawCandlestickChart() {
		endMargin = false;
		for (int i = 0; i < numCandlesticks; i++) {
			if (startIndex + i > data.m1CandlesDataSize(this.replayMode) - 1) {
				endMargin = true;
				break;
			}
			DataSet.Candlestick c = data.m1Candles().get(startIndex + i); 
			double xPos = CHT_MARGIN + (candlestickWidth + candlestickSpacing) * i;
			double yPos;
			if (c.open() < c.close()) {
				yPos = ((highest - c.close()) / range) * (chartHeight - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
			} else {
				yPos = ((highest - c.open()) / range) * (chartHeight - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
			}
			drawCandleStick(c, xPos, yPos);			
		}
	}
	
	private void drawPriceDashes() {
		double spacing = tickSizeOnChart * (int)(PRICE_DASH_SPACING / tickSizeOnChart);
		if (spacing == 0) {
			spacing = tickSizeOnChart;
		}
		double index = chartHeight - chtDataMargin + CHT_MARGIN;
		int i = 0;
		while (true) {
			if (index + spacing < chartHeight + CHT_MARGIN - gc.getFont().getSize() / 2) {
				index += spacing;
				i -= 1;
			} else {
				break;
			}			
		}
		double priceDashPos = chartWidth + CHT_MARGIN;
		double pricePos = priceDashPos + PRICE_DASH_SIZE + PRICE_DASH_MARGIN;
		int pricePosYMargin = (int)(gc.getFont().getSize() / 3);
		double diff = (spacing / tickSizeOnChart) * tickSize;		
		if (darkMode) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		while (index > CHT_MARGIN + gc.getFont().getSize() / 3) {
			gc.strokeLine(priceDashPos, index, priceDashPos + PRICE_DASH_SIZE, index);
			gc.strokeText(((Double)(lowest + (diff * i))).toString(), pricePos, index + pricePosYMargin, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
			index -= spacing;
			i++;
		}
		if (!data.lines().isEmpty()) {
			drawLines();
		}
		crossHair.drawCrossHair();
	}	
	
	public void drawChart() {		
		calculateIndices();
		drawFrame();		
		fillNewChtBtn();
		fillChartTypeBtn();
		fillDarkModeBtn();
		hsb.drawHSB();
		calculateRange(startIndex, endIndex);
		setPreDrawVars();/*
		Random r = new Random();
		int x = r.nextInt(0, 250);
		int y = r.nextInt(0, 250);
		gc.setFill(Color.ALICEBLUE);
		gc.fillRect(x, y, 50, 50);
		System.out.println(x+ " "+y);*/
		if (drawCandlesticks) {
			drawCandlestickChart();
		} else {		
			drawLineChart();
		}
		drawPriceDashes();
	}
	
	public void setNumDataPoints(int numDataPoints) {		
		this.numDataPoints = numDataPoints;
		hsb.setSBMove(data.tickDataSize(this.replayMode), numDataPoints);
	}	
	
	public void setCandleStickVars(int numCandlesticks) {		
		candlestickWidth = (chartWidth / numCandlesticks) / (1 + CNDL_SPAC_COEF);
		candlestickSpacing = candlestickWidth * CNDL_SPAC_COEF;
		if (drawCandlesticks) {
			hsb.setSBMove(data.m1CandlesDataSize(this.replayMode), numCandlesticks);
		}
	}	
	
	public boolean drawCandlesticks() {
		return this.drawCandlesticks;
	}
	
	public void disableRoundUp() {
		roundUp = false;
	}
	
	public boolean focusedChart() {
		return this.focusedChart;		
	}
	
	public double fontSize() {
		return this.fontSize;
	}
	
	public static void drawCharts() {
		for (Chart c : charts) {
			c.drawChart();
		}
	}
}
