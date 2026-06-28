package com.github._7000toni.auto.chart;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.drawing.Line;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.marketreplay.MarketReplay;
import com.github._7000toni.auto.marketreplay.MarketReplayPane;
import com.github._7000toni.auto.marketreplay.trade.PendingTrade;
import com.github._7000toni.auto.marketreplay.trade.PendingTradePair;
import com.github._7000toni.auto.marketreplay.trade.Trade;
import com.github._7000toni.auto.marketreplay.trade.history.LoadingHistory;
import com.github._7000toni.auto.marketreplay.trade.history.TradeHistory;
import com.github._7000toni.auto.marketreplay.trade.history.TradeHistoryPlotter;
import com.github._7000toni.auto.miscellaneous.Round;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Cursor;

public class ChartNode extends CanvasNode implements IScrollBarOwner {
	public final static double CNDL_MOVE_COEF = 0.001;
	public final static int CNDL_INDX_MOVE_COEF = 2;
	
	public final static double TICK_MOVE_COEF = 0.001;
	public final static int TICK_INDX_MOVE_COEF = 3;	
	
	public final static double LINE_PRESS_MARGIN = 5;
	
	public final static double CHT_MARGIN = 5;
	public final static double INFO_MARGIN = 5;
	public final static double CHT_DATA_MARGIN_COEF = 0.45;		
	public final static double END_MARGIN_COEF = 1/1.5;
	
	public final static double CNDL_WDTH_COEF = 0.005;
	public final static double CNDL_SPAC_COEF = 0.4;
	
	private Chart c;
	private Dataset data;
	private CrossHair crossHair;
	
	private BooleanProperty focusedChart = new SimpleBooleanProperty(false);
	private double range;	
	private double lowest;
	private double highest;
	private int numDataPoints = 1495;//299	
	private int startIndex;
	private int endIndex;
	private double tickSizeOnChart;
	private double conversionVar;
	private double dataMarginTickSize;
	private double xDiff;
	private double fontSize;
	private double chtDataMargin;
	private double chartInitPos;
	private boolean endMargin = false;
	private boolean replayMode = false;
	private boolean keepStartIndex = false;
	private MarketReplay mr;
	private MarketReplayPane mrp;
	private boolean drawMRP = false;
	private double dragDiffAccum = 0;	
	private double mrpx;
	private double mrpy;	
	
	private int lineHighlighted = -1;
	private boolean lineDragging = false;
	private boolean measuring = false;
	private double startPrice = 0;
	private double startX = 0;
	private double startY = 0;
	private double endX = 0;
	private double endY = 0;
	
	private BooleanProperty drawCandlesticks = new SimpleBooleanProperty(false);
	private double candlestickWidth;
	private double candlestickSpacing;
	private int numCandlesticks;
	
	private ChartButtonVanGoghs cbvg;
	private boolean mrpSBDragging = false;

	private CanvasButton chartTypeShortcut;
	private DateMargin dateMargin;
	private boolean drawChartTypeShortcut = true;
	private TNode<ICanvasNode> ctsNode;	
	private TNode<ICanvasNode> chartNode;	
	private ChartMarketReplayButtons cmrb;
	private BooleanProperty skipDraw = new SimpleBooleanProperty(false);
	private Dataset.Candlestick lastCandlestick;
	
	private boolean printSpeed = false;
	private double t = 0;
	private int c2 = 0;
	
	private TradeHistoryPlotter thp;
	private ArrayList<TradeHistory> hst;
	private BooleanProperty plotHst = new SimpleBooleanProperty(false);
	private LoadingHistory lhst;
	
	public ChartNode(double width, double height, Stage stage, Dataset data, Chart c, Tree<ICanvasNode> sceneGraph) throws Exception {
		constructorStuff(width, height, stage, data, c, sceneGraph);
	}
	
	private void constructorStuff(double widthParam, double heightParam, Stage stage, Dataset data, Chart c, Tree<ICanvasNode> sceneGraph) throws Exception {		
		this.width = widthParam;
		this.height = heightParam;
		this.data = data;
		this.c = c;
		gc = c.graphicsContext();		
		cbvg = new ChartButtonVanGoghs(this);	
		chartTypeShortcut = new CanvasButton(gc, 10, 10, CHT_MARGIN + width - 15, CHT_MARGIN + 5, null);
		chartTypeShortcut.setOnMouseMoved(e -> {
			setFocusedChart(false);
		});
		chartTypeShortcut.setOnMouseClicked(e -> {
			toggleChartType();			
		});
		dateMargin = new DateMargin(this);
		
		chartNode = new TNode<ICanvasNode>(this, sceneGraph.root());
		sceneGraph.addNode(chartNode);
		ctsNode = new TNode<ICanvasNode>(chartTypeShortcut, chartNode);
		sceneGraph.addNode(ctsNode);
		sceneGraph.addNode(new TNode<ICanvasNode>(dateMargin, chartNode));
		
		fontSize = gc.getFont().getSize();
		crossHair = new CrossHair(this);		
		x = CHT_MARGIN;
		y = CHT_MARGIN;
		width = c.width() - c.priceMargin().width() - CHT_MARGIN;
		height = c.height() - Chart.HSB_HEIGHT - CHT_MARGIN*2;
		candlestickWidth = width * CNDL_WDTH_COEF;
		candlestickSpacing = candlestickWidth * CNDL_SPAC_COEF;
		numCandlesticks = (int)(width / (candlestickWidth + candlestickSpacing));
		chtDataMargin = CHT_MARGIN + fontSize;
		setEventHandlers();		
		thp = new TradeHistoryPlotter(this);
	}	
	
	public void initHst() {
		hst = null;
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File("./"));
		fc.setTitle("Select History File");
		File file = fc.showOpenDialog(null);
		if (lhst != null) {
			lhst.stop();
		}
		lhst = new LoadingHistory(this);
		lhst.loadApproxHistory(file, data.tickData());
	}
	
	public void setHistory(ArrayList<TradeHistory> hst) {
		this.hst = hst;
	}
	
	public TNode<ICanvasNode> chartNode() {
		return chartNode;
	}
	
	public LoadingHistory loadingHistory() {
		return lhst;
	}
	
	public ReadOnlyBooleanProperty plotHst() {
		return plotHst;
	}
	
	public Chart chart() {
		return c;
	}
	
	public void toggleHst() {
		plotHst.set(!plotHst.get());;
	}
	
	public CanvasButton chartTypeShortcut() {
		return chartTypeShortcut;
	}
	
	public ChartButtonVanGoghs chartButtonVanGoghs() {
		return cbvg;
	}
	
	public void setMRPY(double y) {
		mrpy = y;
	}
	
	public void setMRPX(double x) {
		mrpx = x;
	}
	
	public void setChtDataMargin(double chtDataMargin) {
		this.chtDataMargin = chtDataMargin;
	}
	
	private void setEventHandlers() {
		setOnMouseDragged(e -> onMouseDragged(e));
		setOnMouseEntered(e -> onMouseEntered(e));
		setOnMouseExited(e -> onMouseExited(e));
		setOnMousePressed(e -> onMousePressed(e));
		setOnMouseReleased(e -> onMouseReleased(e));
		setOnMouseClicked(e -> onMouseClicked(e));
		setOnMouseMoved(e -> onMouseMoved(e));
		setOnScroll(e -> onScroll(e));
	}
	
	public ReadOnlyBooleanProperty skipDraw() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(skipDraw);
	}
	
	public void toggleSkipDraw() {
		skipDraw.set(!skipDraw.get());
	}
	
	public void setSkipDraw(boolean skipDraw) {
		this.skipDraw.set(skipDraw);
	}
	
	public void toggleChartTypeShortcut() {
		drawChartTypeShortcut = !drawChartTypeShortcut;
		if (drawChartTypeShortcut) {
			c.sceneGraph().addNode(ctsNode);
		} else {
			c.sceneGraph().removeNode(ctsNode);
		}
	}
	
	public Dataset data() {
		return this.data;
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

	public double dataMarginTickSize() {
		return this.dataMarginTickSize;
	}
	
	public double candlestickWidth() {
		return this.candlestickWidth;
	}
	
	public double candlestickSpacing() {
		return this.candlestickSpacing;
	}
	
	public Dataset.Candlestick lastCandlestick() {
		return lastCandlestick;
	}
	
	public String name() {
		return this.data.name();
	}
	
	public int numDataPoints() {
		return this.numDataPoints;
	}
	
	public int numCandlesticks() {
		return this.numCandlesticks;
	}
	
	public ArrayList<Dataset.Candlestick> m1Candles() {
		return this.data.m1Candles();
	}
	
	public ArrayList<Dataset.DataPair> tickData() {
		return this.data.tickData();
	}
	
	public boolean drawChartTypeShortcut() {
		return drawChartTypeShortcut;
	}
	
	public void enableReplayMode(MarketReplay mr, MarketReplayPane mrp) {
		if (!this.replayMode) {
			this.replayMode = true;
			this.mr = mr;
			this.mrp = mrp;
			cmrb = new ChartMarketReplayButtons(this, mr, cbvg);
			cmrb.disableButtons();
			
			for (PendingTrade pt : mr.pendingTrades()) {
				cmrb.addPenTradePair(new PendingTradePair(pt, this));
			}
			
			drawMRP = true;
			mr.addChart(this);
			c.menu().chartFunctionsMenu().generalFunctionstab().setReplayMode(true);
		}
	}
	
	public MarketReplay marketReplay() {
		return mr;
	}
	
	public ChartMarketReplayButtons tradeButtons() {
		return cmrb;
	}
	
	public void disableReplayMode() {
		if (this.replayMode) {
			replayMode = false;
			mr.removeChart(this);
			cmrb = null;
			mr = null;
			mrp = null;
			c.menu().chartFunctionsMenu().generalFunctionstab().setReplayMode(false);
		}
	}
	
	public boolean replayMode() {
		return this.replayMode;
	}
	
	public void setKeepStartIndex(boolean keepStartIndex) {
		this.keepStartIndex = keepStartIndex;
	}
	
	public boolean keepStartIndex() {
		return this.keepStartIndex;
	}
	
	public void setStartIndex(int startIndex) {
		if (startIndex < 0) {
			this.startIndex = 0;
			return;
		}
		if (drawCandlesticks.get() && startIndex >= data.m1CandlesDataSize(false).get()) {
			this.startIndex = data.m1CandlesDataSize(replayMode).get() - 1;
			return;
		}
		if (!drawCandlesticks.get() && startIndex >= data.tickDataSize(false).get()) {
			this.startIndex = data.tickDataSize(replayMode).get() - 1;
			return;
		}
		this.startIndex = startIndex;
	}
	
	public boolean endMargin() {
		return this.endMargin;
	}
	
	public double xDiff() {
		return this.xDiff;
	}
	
	public static boolean onSomeChart(String name) {
		for (Chart c : Chart.charts()) {
			if (c.chartNode().focusedChart().get() && c.chartNode().name().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public void onMouseExited(MouseEvent e) {			
		c.stage().getScene().cursorProperty().set(Cursor.DEFAULT);
		if (replayMode) {
			if (!onPendingButtonArea(e.getX(), e.getY())) {				
				cmrb.disablePendingOrderButtons();
				setFocusedChart(false);
			}
		} else {
			setFocusedChart(false);
		}
	}
	
	private boolean onPendingButtonArea(double x, double y) {
		if (y < CHT_MARGIN || y > CHT_MARGIN + height) {
			return false;
		}
		if (x < CHT_MARGIN + width - (fontSize*4+6)*2 || x > CHT_MARGIN + width) {
			return false;
		}
		return true;
	}
	
	public void onMouseEntered(MouseEvent e) {
		setFocusedChart(true);
		CrossHair.setIsForCandle(drawCandlesticks.get());
		CrossHair.setDateIndex(0);
		CrossHair.setName(data.name());
		CrossHair.setX(e.getX());
		CrossHair.setY(e.getY());
	}
	
	private boolean onMRP(double x, double y) {
		return x >= mrpx && x <= mrpx + 399 && y >= mrpy && y <= mrpy + 100;
	}
	
	public void onMouseMoved(MouseEvent e) {
		if (CrossHair.dateIndex().get() >= data.m1CandlesDataSize(replayMode).get() && drawCandlesticks.get()) {
			CrossHair.setDateIndex(0);
		}		
		CrossHair.setX(e.getX());
		CrossHair.setY(e.getY());
		CrossHair.setPrice(yCoordToPrice(e.getY()));
		if (replayMode) {
			cmrb.enablePendingOrderButtons();
		}
		if (!onChart(e.getX(), e.getY())) {
			measuring = false;
			setFocusedChart(false);
		} else {
			setFocusedChart(true);
		}
		if (drawMRP && onMRP(e.getX(), e.getY()) && !mrpSBDragging) {
			fireMRPEvent(MouseEvent.MOUSE_MOVED, e);
		}		
	}	
	
	public void onMousePressed(MouseEvent e) {		
		if (e.getButton() == MouseButton.MIDDLE) {
			if (lineHighlighted != -1) {
				data.lines().remove(lineHighlighted);
				lineHighlighted = -1;
			} else if (onChart(e.getX(), e.getY())) {
				data.lines().add(new Line(roundToNearestTick(CrossHair.price())));
			}
		} else if (e.getButton() == MouseButton.SECONDARY) {
			startPrice = roundToNearestTick(CrossHair.price());
			startX = e.getX();
			startY = e.getY();
		} else if (e.isPrimaryButtonDown()) {
			if (onChart(e.getX(), e.getY())) {
				chartInitPos = e.getX();
				if (drawMRP && onMRP(e.getX(), e.getY())) {
					fireMRPEvent(MouseEvent.MOUSE_PRESSED, e);
				}								
				double price = ((((height - (chtDataMargin*2)) - (e.getY() - ChartNode.CHT_MARGIN - chtDataMargin)) / (double)(height - (chtDataMargin*2))) * range) + lowest;
				double upperPrice = ((((height - (chtDataMargin*2)) - (e.getY() - LINE_PRESS_MARGIN - ChartNode.CHT_MARGIN - chtDataMargin)) / (double)(height - (chtDataMargin*2))) * range) + lowest;
				double lowerPrice = ((((height - (chtDataMargin*2)) - (e.getY() + LINE_PRESS_MARGIN - ChartNode.CHT_MARGIN - chtDataMargin)) / (double)(height - (chtDataMargin*2))) * range) + lowest;
				int i = -1;
				int j = 0;
				double minDiff = Double.MAX_VALUE;
				int lh = lineHighlighted;
				lineHighlighted = -1;
				for (Line l : data.lines()) {
					double diff = Math.abs(price - l.price());
					if (l.price() >= lowerPrice && l.price() <= upperPrice && diff < minDiff) {
						i = j;						
						minDiff = diff;
					}
					l.setHighlighted(false);					
					j++;
				}
				if (i != -1) {
					data.lines().get(i).setHighlighted(true);
					lineHighlighted = i;
					if (i == lh) {
						lineDragging = true;
					}
				}
				if (i != lh) {
					lineDragging = false;
				}
			}
		} 					
	}
		
	public void onMouseReleased(MouseEvent e) {
		if (measuring) {
			measuring = false;		
			c.stage().getScene().cursorProperty().set(Cursor.DEFAULT);
		} else if (drawMRP && onMRP(e.getX(), e.getY())) {
			fireMRPEvent(MouseEvent.MOUSE_RELEASED, e);
		}		
		lineDragging = false;
		if (mrpSBDragging) {
			mrpSBDragging = false;
			fireMRPEvent(MouseEvent.MOUSE_RELEASED, e);
		}
	}
	
	public void onMouseClicked(MouseEvent e) {
		if (drawMRP && onMRP(e.getX(), e.getY()) && !measuring) {
			fireMRPEvent(MouseEvent.MOUSE_CLICKED, e);
		}
	}
	
	private void fireMRPEvent(EventType<MouseEvent> type, MouseEvent e) {
		MouseEvent me = new MouseEvent(type, e.getX() - mrpx, e.getY() - mrpy, e.getScreenX(), e.getScreenY(), 
				e.getButton(), e.getClickCount(), e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(), 
				e.isPrimaryButtonDown(), e.isMiddleButtonDown(), e.isSecondaryButtonDown(), e.isBackButtonDown(), 
				e.isForwardButtonDown(), e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(), null);
		mrp.canvas().fireEvent(me);
	}
	
	public void onMouseDragged(MouseEvent e) {	
		if (lineDragging) {
			double price = roundToNearestTick(((((height - (chtDataMargin*2)) - (e.getY() - ChartNode.CHT_MARGIN - chtDataMargin)) / (double)(height - (chtDataMargin*2))) * range) + lowest); 
			data.lines().get(lineHighlighted).setPrice(price);
		}
		if (e.getButton() == MouseButton.SECONDARY) {
			c.stage().getScene().cursorProperty().set(Cursor.CROSSHAIR);
			measuring = true;
			endX = e.getX();
			endY = e.getY();
		}		
		if (!lineDragging && !measuring && e.isPrimaryButtonDown() && !onMRP(e.getX(), e.getY())) {
			double posDiff = e.getX() - chartInitPos;
			double newHSBPos = c.hsb().x();					
			int diff;
			dragDiffAccum += posDiff;
			if (drawCandlesticks.get()) {
				diff = (int)(dragDiffAccum / (candlestickWidth + candlestickSpacing));
				if (diff != 0) {
					startIndex = startIndex - diff;
					newHSBPos = (CHT_MARGIN + width - Chart.HSB_WIDTH) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
					dragDiffAccum = 0;
				}
			} else {
				diff = (int)(dragDiffAccum / xDiff);
				if (diff != 0) {
					startIndex = startIndex - diff;
					newHSBPos = (CHT_MARGIN + width - Chart.HSB_WIDTH) * ((double)startIndex /(data.tickDataSize(this.replayMode).get() - numDataPoints * END_MARGIN_COEF));
					dragDiffAccum = 0;
				}
			}
			if (startIndex < 0) {
				startIndex = 0;
			}
			if (dragDiffAccum == 0 && posDiff != 0) {
				if (newHSBPos < CHT_MARGIN + width - Chart.HSB_WIDTH) {
					keepStartIndex = true;
				} else {
					keepStartIndex = false;
				}
				c.hsb().setPosition(newHSBPos, false);
			}
		}
		if (drawMRP && onMRP(e.getX(), e.getY()) || mrpSBDragging) {
			MouseEvent me = new MouseEvent(MouseEvent.MOUSE_DRAGGED, e.getX() - mrpx, e.getY() - mrpy, e.getScreenX(), e.getScreenY(), 
					e.getButton(), e.getClickCount(), e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(), 
					e.isPrimaryButtonDown(), e.isMiddleButtonDown(), e.isSecondaryButtonDown(), e.isBackButtonDown(), 
					e.isForwardButtonDown(), e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(), null);
			mrp.canvas().fireEvent(me);
			mrpSBDragging = true;
		}
		chartInitPos = e.getX();
		onMouseMoved(e);
	}
	
	public void zoomCandlesticks(double delta, boolean scroll) {
		double multiplier = 1.005;
		boolean customSI = false;
		if (scroll) {
			multiplier = 1.05;
		}
		if (delta > 0) {
			if ((int)(numCandlesticks * 0.999 * (1 / multiplier)) - 1 >= 10) {
				numCandlesticks = (int)(numCandlesticks * 0.999 * (1 / multiplier)) - 1;
				setCandleStickVars(numCandlesticks);
			} else {
				return;
			}
		} else if (delta < 0) {
			if ((int)(numCandlesticks * 1.001 * multiplier) + 1 <= data.m1CandlesDataSize(this.replayMode).get() - 5) {
				numCandlesticks = (int)(numCandlesticks * 1.001 * multiplier) + 1;
				setCandleStickVars(numCandlesticks);
			} else {
				return;
			}
		}
		double newHSBPos;
		if (replayMode) {
			if (mr.live().get() && !mr.paused() || endIndex >= data.m1CandlesDataSize(true).get() - 1) {
				newHSBPos = Double.MAX_VALUE;
			} else {
				startIndex = endIndex - numCandlesticks;
				customSI = true;
				if (startIndex < 0) {
					startIndex = 0;
				}
				newHSBPos = (CHT_MARGIN + width - Chart.HSB_WIDTH) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
			}
		} else { 
			newHSBPos = (CHT_MARGIN + width - Chart.HSB_WIDTH) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
		}
		if (newHSBPos < CHT_MARGIN + width - Chart.HSB_WIDTH || customSI) {
			keepStartIndex = true;
		} else {
			keepStartIndex = false;
		}
		c.hsb().setPosition(newHSBPos, false);
	}
	
	public void zoomTicks(double delta, boolean scroll) {
		double multiplier = 1.01;
		boolean customSI = false;
		if (scroll) {
			multiplier = 1.05;
		}
		if (delta > 0) {
			setNumDataPoints((int)(numDataPoints * 0.99 * (1 / multiplier)));
		} else if (delta < 0) {
			setNumDataPoints((int)(numDataPoints * 1.01 * multiplier));
		}
		double xDiff = width / (double)numDataPoints;
		if (xDiff * (data.tickDataSize(this.replayMode).get() - 1) < width) {
			setNumDataPoints(data.tickDataSize(this.replayMode).get() - 1);
		} else if (numDataPoints < 100) {
			setNumDataPoints(100);
		}
		double newHSBPos;
		if (replayMode) {
			if (mr.live().get() && !mr.paused() || endIndex >= data.tickDataSize(true).get() - 1) {
				newHSBPos = Double.MAX_VALUE;
			} else {
				startIndex = endIndex - numDataPoints;
				customSI = true;
				if (startIndex < 0) {
					startIndex = 0;
				}
				newHSBPos = (CHT_MARGIN + width - Chart.HSB_WIDTH) * ((double)startIndex /(data.tickDataSize(this.replayMode).get() - numDataPoints * END_MARGIN_COEF));				
			}
		} else {
			newHSBPos = (CHT_MARGIN + width - Chart.HSB_WIDTH) * ((double)startIndex /(data.tickDataSize(this.replayMode).get() - numDataPoints * END_MARGIN_COEF));
		}
		if (newHSBPos < CHT_MARGIN + width - Chart.HSB_WIDTH || customSI) {
			keepStartIndex = true;
		} else {
			keepStartIndex = false;
		}
		c.hsb().setPosition(newHSBPos, false);
	}
	
	public void onScroll(ScrollEvent e) {	
		if (drawCandlesticks.get()) {
			zoomCandlesticks(e.getDeltaY(), true);
		} else {
			zoomTicks(e.getDeltaY(), true);
		}
	}		
	
	public boolean onChart(double x, double y) {
		return onNode(x, y);
	}
	
	
	
	private void calculateRange() {	
		if (drawCandlesticks.get()) {
			lowest = data.m1Candles().get(startIndex).low();
			highest = data.m1Candles().get(startIndex).high();
			int ei = endIndex;
			if (replayMode) {
				ei--;
			}
			for (int i = startIndex; i < ei; i++) {			
				double low = data.m1Candles().get(i).low();
				double high = data.m1Candles().get(i).high();				
				if (high > highest) {
					highest = high;	
				} 
				if (low < lowest) {					
					lowest = low;	
				}				
			}
			if (replayMode) {
				Dataset.Candlestick c = data.makeLastReplayCandlestick(m1Candles().get(ei).firstTickIndex());
				if (c.high() > highest) {
					highest = c.high();	
				} 
				if (c.low() < lowest) {					
					lowest = c.low();
				}
			}
			range = highest - lowest;
		} else {
			lowest = data.tickData().get(startIndex).price();
			highest = data.tickData().get(startIndex).price();	
			for (int i = startIndex; i < endIndex + 1; i++) {			
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
		int i = 1;
		if (price < 0) {
			price *= -1;
			i = -1;
		}
		int intPart = (int)price;
		price = price - intPart;
		int pow = (int)Math.pow(10, data.numDecimalPts());
		price *= pow;
		int intTick = (int)(pow * data.tickSize());		
		int quotient = (int)(price / intTick);
		double remainder = price % intTick;		
		if (remainder > intTick / 2.0) {
			return i * Round.round(intPart + (intTick * (quotient + 1)) / (double)pow, data.numDecimalPts() + 1); 
		}
		return i * Round.round(intPart + (intTick * quotient) / (double)pow, data.numDecimalPts() + 1);
	}
	
	private void drawLines() {
		double trueLowest = lowest - dataMarginTickSize;
		double trueHighest = highest + dataMarginTickSize;
		for (Line l : data.lines()) {
			if (l.price() >= trueLowest && l.price() <= trueHighest) {
				double trueRange = trueHighest - trueLowest;
				double y = height + CHT_MARGIN - (((l.price() - trueLowest) / trueRange) * height);
				if (l.highlighted()) {
					gc.setFill(Color.RED);
					gc.setStroke(Color.RED);
				} else {
					gc.setFill(Color.GRAY);
					gc.setStroke(Color.GRAY);
				}
				gc.strokeLine(CHT_MARGIN, y, width + CHT_MARGIN, y);				
				gc.fillRoundRect(width + CHT_MARGIN, y - fontSize/2, c.priceMargin().width(), fontSize, CanvasButton.ARC_W, CanvasButton.ARC_H);
				gc.setFill(Color.WHITE);
				gc.fillText(((Double)(roundToNearestTick(l.price()))).toString(), width + CHT_MARGIN + PriceMargin.EXTRA_SPACE/2, y + fontSize/3, c.priceMargin().width() - PriceMargin.EXTRA_SPACE);
			}
		}		
	}
	
	public void drawCandleStick(Dataset.Candlestick candle, double xPos, double yPos) {
		if (candle.open() < candle.close()) {
			gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.UP_CANDLESTICK_STROKE));
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.close() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.close()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.close() - candle.open()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.close() - candle.low()) / conversionVar);
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.UP_CANDLESTICK_FILL));
			gc.fillRect(xPos, yPos, candlestickWidth, (candle.close() - candle.open()) / conversionVar);
		} else if (candle.open() > candle.close()) {
			gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.DOWN_CANDLESTICK_STROKE));
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.open() - candle.close()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.open() - candle.close()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.open() - candle.low()) / conversionVar);
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.DOWN_CANDLESTICK_FILL));
			gc.fillRect(xPos, yPos, candlestickWidth, (candle.open() - candle.close()) / conversionVar);
		} else {
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}
			gc.strokeLine(xPos, yPos, xPos + candlestickWidth, yPos);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.open() - candle.close()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.open() - candle.low()) / conversionVar);
		}
	}
	
	private void calculateIndices() {
		if (drawCandlesticks.get()) {
			if (!keepStartIndex) {
				if (data.m1CandlesDataSize(this.replayMode).get() < numCandlesticks * END_MARGIN_COEF) {
					startIndex = 0;
				} else {
					startIndex = (int)((c.hsb().x() / (width + CHT_MARGIN - Chart.HSB_WIDTH)) * (data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
				}
			}
			endIndex = startIndex + numCandlesticks;
			if (endIndex > data.m1CandlesDataSize(this.replayMode).get()) {
				endIndex = data.m1CandlesDataSize(this.replayMode).get();
			}
		} else {
			if (!keepStartIndex) {
				if (data.tickDataSize(this.replayMode).get() < (numDataPoints - 1) * END_MARGIN_COEF) {
					startIndex = 0;
				} else {
					startIndex = (int)((c.hsb().x() / (width + CHT_MARGIN - Chart.HSB_WIDTH)) * (data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
				}
			}
			endIndex = startIndex + numDataPoints;
			if (endIndex >= data.tickDataSize(this.replayMode).get()) {
				endIndex = data.tickDataSize(this.replayMode).get() - 1;
			}
		}
	}
	
	private void setPreDrawVars() {
		if (drawCandlesticks.get()) {
			tickSizeOnChart = (height - chtDataMargin * 2) / (range / data.tickSize());
			dataMarginTickSize = (chtDataMargin / tickSizeOnChart) * data.tickSize();
			conversionVar = data.tickSize() / tickSizeOnChart;	
		} else {
			xDiff = width / (double)numDataPoints;	
			tickSizeOnChart = (height - chtDataMargin * 2) / (range / data.tickSize());
			dataMarginTickSize = (chtDataMargin / tickSizeOnChart) * data.tickSize();
			conversionVar = data.tickSize() / tickSizeOnChart;	
		}
	}
	
	private void drawLineChart() {
		endMargin = false;
		double startY = height - chtDataMargin + CHT_MARGIN - (((data.tickData().get(startIndex).price() - lowest) / range) * (height - chtDataMargin * 2));		
		double prevY = startY - ((data.tickData().get(startIndex + 1).price() - data.tickData().get(startIndex).price()) / conversionVar);
		gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.LINE_CHART));
		gc.strokeLine(CHT_MARGIN, startY, xDiff + CHT_MARGIN, prevY);		
		for (int i = 1; i < numDataPoints; i++) {
			if (startIndex + i > data.tickDataSize(this.replayMode).get() - 2) {
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
			if (startIndex + i > data.m1CandlesDataSize(this.replayMode).get() - 1) {
				endMargin = true;
				break;
			}
			if (replayMode && startIndex + i == data.m1CandlesDataSize(this.replayMode).get() - 1) {
				lastCandlestick = data.makeLastReplayCandlestick(m1Candles().get(data.m1CandlesDataSize(replayMode).get() - 1).firstTickIndex());
			} else {
				lastCandlestick = data.m1Candles().get(startIndex + i);
			}		
			double yPos;
			double xPos = CHT_MARGIN + (candlestickWidth + candlestickSpacing) * i;
			if (lastCandlestick.open() < lastCandlestick.close()) {
				yPos = ((highest - lastCandlestick.close()) / range) * (height - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
			} else {
				yPos = ((highest - lastCandlestick.open()) / range) * (height - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
			}
			drawCandleStick(lastCandlestick, xPos, yPos);			
		}		
	}
	
	private void drawCurrentPriceBox() {
		if (data.tickDataSize(true).get() < 2) {
			return;
		}
		int i = data.tickDataSize(true).get();
		double price = tickData().get(i - 1).price();
		if (price > highest || price < lowest) {
			return;
		}	
		double yPos = ((highest - price) / range) * (height - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
		gc.setFill(Color.SLATEBLUE);		
		gc.fillRoundRect(width + CHT_MARGIN, yPos - fontSize/2, c.priceMargin().width(), fontSize, CanvasButton.ARC_W, CanvasButton.ARC_H);
		gc.setFill(Color.WHITE);
		gc.fillText(((Double)(price)).toString(), width + CHT_MARGIN + PriceMargin.EXTRA_SPACE/2, yPos + fontSize/3, c.priceMargin().width() - PriceMargin.EXTRA_SPACE);
	}
	
	private void drawCurrentPriceLine() {		
		if (data.tickDataSize(true).get() < 2) {
			return;
		}
		int i = data.tickDataSize(true).get();
		double price = tickData().get(i - 1).price();
		if (price > highest || price < lowest) {
			return;
		}		
		double yPos = ((highest - price) / range) * (height - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
		gc.setStroke(Color.SLATEBLUE);
		gc.strokeLine(CHT_MARGIN, yPos,  CHT_MARGIN + width, yPos);	
	}
	
	private void checkDrawLines() {
		if (!data.lines().isEmpty()) {
			drawLines();
		}
	}
	
	private void checkMeasuring() {		
		if (measuring) {
			double endPrice = ((((height - (chtDataMargin*2)) - (endY - ChartNode.CHT_MARGIN - chtDataMargin)) / (double)(height - (chtDataMargin*2))) * range) + lowest;
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}
			gc.strokeLine(startX, startY, endX, endY);
			double n100 = Math.abs(endY - startY);
			if (startY < endY) {
				n100 = endY + n100;				
			} else if (endY < startY) {
				n100 = endY - n100;
			} else {
				n100 = -1;
			}
			
			if (n100 != -1) {
				if (n100 > CHT_MARGIN && n100 < CHT_MARGIN + height - fontSize) {
					double ex = endX + 50;
					if (ex >= CHT_MARGIN + width) {
						ex -= 100;
						gc.strokeLine(ex, n100 + 0.5, endX, n100 + 0.5);
					} else {
						gc.strokeLine(endX, n100 + 0.5, ex, n100 + 0.5);
					}				
				}
			}
			
			double ex = endX;
			double ey = endY;
			DecimalFormat df = new DecimalFormat("#");
			df.setMaximumFractionDigits(data.numDecimalPts());
			String text = df.format(roundToNearestTick(endPrice - startPrice)) + " from: " + ((Double)startPrice).toString();
			Text t = new Text(text);
			double prc_msrmnt_length = t.getLayoutBounds().getWidth() + 5;
			boolean right = true;
			if (endX > CHT_MARGIN + width - prc_msrmnt_length) {
				ex -= prc_msrmnt_length + 5;
				right = false;
			}
			boolean dropped = false;
			if (endY < CHT_MARGIN + 2 + fontSize) {
				ey += fontSize + 3;
				if (right) {
					dropped = true;
				}
			}
			if (endX > CHT_MARGIN + width - prc_msrmnt_length && dropped && right) {
				ex -= prc_msrmnt_length + 5;
			}
			if (endY >= height + CHT_MARGIN - fontSize) {
				ey = height + CHT_MARGIN - fontSize;
			}
			gc.setFill(Color.SLATEBLUE);				
			gc.fillText(text, ex + 1, ey - 2, prc_msrmnt_length);
		}
	}
	
	private void drawTopRightText() {		
		if (Chart.darkMode().get()) {			
			gc.setFill(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
		}
		String midDot = " " + (char)183 + " ";
		if (drawCandlesticks.get()) {			
			String trt1 = data.name() + midDot + "M1";
			gc.fillText(trt1, CHT_MARGIN + INFO_MARGIN, CHT_MARGIN + fontSize);
			boolean useLast = false;
			if (replayMode && (CrossHair.dateIndex().get() == -1 || (!focusedChart.get() && crossHair.ohlc() == null) || CrossHair.dateIndex().get() == data.m1CandlesDataSize(replayMode).get() - 1)) {				
				crossHair.setOHLC(lastCandlestick);
				useLast = true;
			}
			if (crossHair.ohlc() != null) {				
				String trt2 = crossHair.ohlc();
				Dataset.Candlestick c;
				if (useLast) {
					c = lastCandlestick;
				} else if (CrossHair.isForCandle().get()) {
					c = data.m1Candles().get(CrossHair.dateIndex().get());
				} else {
					c = data.m1Candles().get(data.tickData().get(CrossHair.dateIndex().get()).candleIndex());
				}
				Text t = new Text(trt1);
				t.setFont(gc.getFont());
				gc.fillText(midDot, CHT_MARGIN + INFO_MARGIN + t.getLayoutBounds().getWidth(), CHT_MARGIN + fontSize);
				t.setText(trt1 + midDot);
				if (c.open() < c.close()) {		
					if (ColourSettings.colour(ColourIndex.UP_CANDLESTICK_FILL).equals(ColourSettings.colour(ColourIndex.DOWN_CANDLESTICK_FILL))) {
						gc.setFill(ColourSettings.colour(ColourIndex.UP_CANDLESTICK_STROKE));
					} else {
						gc.setFill(ColourSettings.colour(ColourIndex.UP_CANDLESTICK_FILL));
					}
				} else if (c.open() > c.close()) {
					if (ColourSettings.colour(ColourIndex.UP_CANDLESTICK_FILL).equals(ColourSettings.colour(ColourIndex.DOWN_CANDLESTICK_FILL))) {
						gc.setFill(ColourSettings.colour(ColourIndex.DOWN_CANDLESTICK_STROKE));
					} else {
						gc.setFill(ColourSettings.colour(ColourIndex.DOWN_CANDLESTICK_FILL));
					}
				}				
				gc.fillText(trt2, CHT_MARGIN + INFO_MARGIN + t.getLayoutBounds().getWidth(), CHT_MARGIN + fontSize);
			}			
		} else {
			gc.fillText(data.name() + midDot + "T1", CHT_MARGIN + INFO_MARGIN, CHT_MARGIN + fontSize);
		}
		crossHair.resetOHLC();
	}
	
	public double yCoordToPrice(double y) {
		return ((((height - (chtDataMargin*2)) - (y - CHT_MARGIN - chtDataMargin)) / (double)(height - (chtDataMargin*2))) * range) + lowest;
	}
	
	public double priceToYCoord(double price) {
		return ((highest + dataMarginTickSize - price) / (range + dataMarginTickSize * 2)) * height + CHT_MARGIN;
	}
	
	
	
	private void drawUI() {	
		long b = 0;
		if (printSpeed) {
			b = System.nanoTime();
		}
		calculateIndices();		
		c.hsb().draw();
		calculateRange();
		setPreDrawVars();			
		c.priceMargin().draw();		
		checkDrawLines();		
		crossHair.drawCrossHair();
		if (drawCandlesticks.get()) {
			drawCandlestickChart();
		} else {		
			drawLineChart();
		}
		drawTopRightText();
		if (plotHst.get()) {
			if (replayMode) {
				thp.plotHistory(Trade.history(data.name()));
			} else if (hst != null) {
				thp.plotHistory(hst);
			}
		}
		if (drawChartTypeShortcut) {
			chartTypeShortcut.draw();
		}									
		checkMeasuring();			
		if (replayMode) {					
			drawCurrentPriceLine();
			drawCurrentPriceBox();
			cmrb.draw();
			if (drawMRP) {
				mrp.drawPane(gc, mrpx, mrpy);
			}
		}
		if (printSpeed) {
			double tm = (System.nanoTime() - b) / 1000000000.0;
			t += tm;
			c2++;
			System.out.printf("REDRAW\ttime: %f\tave: %f\trange: %d\n", tm, t/c2, endIndex - startIndex);
		}
	}
	
	public void drawChart() {
		if (Platform.isFxApplicationThread()) {
			drawUI();
		} else {
			Platform.runLater(() -> {
				drawUI();
			});
		}
	}
	
	@Override
	public void draw() {
		c.draw();
	}
	
	public MarketReplay mr() {
		return mr;
	}
	
	public MarketReplayPane mrp() {
		return mrp;
	}
	
	public void toggleMRPShortcut() {
		drawMRP = !drawMRP;
	}
	
	public void setNumDataPoints(int numDataPoints) {	
		this.numDataPoints = numDataPoints;
		t = 0;
		c2 = 0;
	}	
	
	public void setCandleStickVars(int numCandlesticks) {	
		candlestickWidth = (width / numCandlesticks) / (1 + CNDL_SPAC_COEF);
		candlestickSpacing = candlestickWidth * CNDL_SPAC_COEF;
		t = 0;
		c2 = 0;
	}	
	
	public ReadOnlyBooleanProperty drawCandlesticks() {
		return BooleanProperty.readOnlyBooleanProperty(drawCandlesticks);
	}
	
	public ReadOnlyBooleanProperty focusedChart() {
		return BooleanProperty.readOnlyBooleanProperty(focusedChart);		
	}
	
	public void setFocusedChart(boolean focusedChart) {
		this.focusedChart.set(focusedChart);
		if (focusedChart) {
			for (Chart c : Chart.charts()) {
				if (!c.chartNode().equals(this)) {
					c.chartNode().setFocusedChart(false);
				}
			}
			int i = Chart.charts().indexOf(c);
			Chart c = Chart.charts().getFirst();
			Chart.charts().set(i, c);
			Chart.charts().set(0, this.c);
		}
	}
	
	public double tickSizeOnChart() {
		return tickSizeOnChart;
	}
	
	public double fontSize() {
		return this.fontSize;
	}
	
	public void toggleChartType() {
		double newHSBPos;
		if (drawCandlesticks.get()) {
			drawCandlesticks.set(false);
			CrossHair.setIsForCandle(false);
			CrossHair.setDateIndex(0);
			if (replayMode) {
				if (endIndex >= m1Candles().size()) {
					startIndex = tickData().size() - numDataPoints;
				} else {
					startIndex = m1Candles().get(endIndex).firstTickIndex() - 1 - numDataPoints;
				}
				if (startIndex < 0) {
					startIndex = 0;
				}
			} else {
				startIndex = m1Candles().get(startIndex).firstTickIndex();
			}
			newHSBPos = (CHT_MARGIN + width - Chart.HSB_WIDTH) * ((double)startIndex / (data.tickDataSize(this.replayMode).get() - numDataPoints * END_MARGIN_COEF));
			c.hsb().setPosition(newHSBPos, false);				
		} else {
			if (m1Candles().isEmpty()) {
				return;
			}
			drawCandlesticks.set(true);	
			CrossHair.setIsForCandle(true);	
			CrossHair.setDateIndex(0);
			if (replayMode) {
				if (endIndex >= tickData().size()) {
					startIndex = m1Candles().size() - numCandlesticks;
				} else {
					startIndex = tickData().get(endIndex).candleIndex() + 1 - numCandlesticks;
				}
				if (startIndex < 0) {
					startIndex = 0;
				}
			} else {
				startIndex = tickData().get(startIndex).candleIndex();
			}
			newHSBPos = (CHT_MARGIN + width - Chart.HSB_WIDTH) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
			c.hsb().setPosition(newHSBPos, false);				
		}
		if (newHSBPos < CHT_MARGIN + width - Chart.HSB_WIDTH) {
			keepStartIndex = true;
		} else {
			keepStartIndex = false;
		}
	}
}