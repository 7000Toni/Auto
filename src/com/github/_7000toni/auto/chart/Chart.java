package com.github._7000toni.auto.chart;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.github._7000toni.auto.canvasnode.CanvasEventFilter;
import com.github._7000toni.auto.canvasnode.CanvasWrapper;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasWindow;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.CanvasNumberChooser;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalChartScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.drawing.Line;
import com.github._7000toni.auto.chart.listener.ChartHeightListener;
import com.github._7000toni.auto.chart.listener.ChartWidthListener;
import com.github._7000toni.auto.chart.menu.ChartMenu;
import com.github._7000toni.auto.dataset.DataSet;
import com.github._7000toni.auto.marketreplay.MarketReplay;
import com.github._7000toni.auto.marketreplay.MarketReplayPane;
import com.github._7000toni.auto.marketreplay.trade.PendingTrade;
import com.github._7000toni.auto.marketreplay.trade.PendingTradePair;
import com.github._7000toni.auto.marketreplay.trade.Trade;
import com.github._7000toni.auto.marketreplay.trade.history.LoadingHistory;
import com.github._7000toni.auto.marketreplay.trade.history.TradeHistory;
import com.github._7000toni.auto.marketreplay.trade.history.TradeHistoryPlotter;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.miscellaneous.Round;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ImageFunctions;
import com.github._7000toni.auto.settings.ImageSettings;
import com.github._7000toni.auto.settings.Settings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Cursor;

public class Chart implements IScrollBarOwner, ICanvasWindow {
	public final static double CNDL_MOVE_COEF = 0.001;
	public final static int CNDL_INDX_MOVE_COEF = 2;
	
	public final static double TICK_MOVE_COEF = 0.001;
	public final static int TICK_INDX_MOVE_COEF = 3;
	
	public final static double HSB_WIDTH = 100;
	public final static double HSB_HEIGHT = 10;	
	
	public final static double LINE_PRESS_MARGIN = 5;
	
	public final static double WIDTH_EXTRA = 16;
	public final static double HEIGHT_EXTRA = 39;
	
	public final static double MIN_WIDTH = 950; 
	public final static double MIN_HEIGHT = 590; 
	
	public final static double CHT_MARGIN = 5;
	public final static double INFO_MARGIN = 5;
	public final static double CHT_DATA_MARGIN_COEF = 0.45;		
	public final static double END_MARGIN_COEF = 1/1.5;
	
	public final static double PRICE_DASH_SPACING = 50;
	public final static double PRICE_DASH_SIZE = 5;
	public final static double PRICE_DASH_MARGIN = 5;
	
	public final static double CNDL_WDTH_COEF = 0.005;
	public final static double CNDL_SPAC_COEF = 0.4;
	
	private static ArrayList<Chart> charts = new ArrayList<Chart>();	
	private static BooleanProperty focusedOnChart = new SimpleBooleanProperty(false);	
	private static BooleanProperty darkMode = new SimpleBooleanProperty(false);
	
	private DataSet data;
	private CrossHair crossHair;
	
	private double priceMargin = 100;
	private int numDecimalPts;
	private double tickSize;
	private BooleanProperty focusedChart = new SimpleBooleanProperty(false);
	private Canvas canvas;	
	private double width;
	private double height;
	private double chartWidth;
	private double chartHeight;
	private GraphicsContext gc;
	private double range;	
	private double lowest;
	private double highest;
	private HorizontalChartScrollBar hsb;
	private int numDataPoints = 1495;//299	
	private int startIndex;
	private int endIndex;
	private double tickSizeOnChart;
	private double conversionVar;
	private double dataMarginTickSize;
	private double xDiff;
	private double fontSize;
	private double chtDataMargin;
	private boolean priceDragging = false;
	private double priceInitPos;
	private boolean chartDragging = false;
	private boolean chartDateMarginDragging = false;
	private double chartInitPos;
	private boolean endMargin = false;
	private Stage stage;
	private boolean replayMode = false;
	private boolean keepStartIndex = false;
	private MarketReplay mr;
	private MarketReplayPane mrp;
	private boolean drawMRP = false;
	private double dragDiffAccum = 0;
	private double x = 0;
	private double y = 0;	
	private double mrpx;
	private double mrpy;	
	
	private CanvasNumberChooser volUnits;
	private CanvasNumberChooser volTens;
	
	private int lineHighlighted = -1;
	private boolean lineDragging = false;
	private boolean rightPressed = false;
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
	private Tree<ICanvasNode> sceneGraph;
	private TNode<ICanvasNode> lastNode = null;
	private CanvasWrapper cw;
	private boolean dragging = false;
	private boolean mrpSBDragging = false;
	private final ReentrantLock varLock = new ReentrantLock();
	TNode<ICanvasNode> menuNode;
	
	private boolean menuHidden = true;
	private CanvasButton btnMenu;
	private ChartMenu menu;
	private CanvasButton chartTypeShortcut;
	private boolean drawChartTypeShortcut = true;
	private TNode<ICanvasNode> ctsNode;	
	
	private boolean printSpeed = false;
	private ChartMarketReplayButtons cmrb;
	private BooleanProperty skipDraw = new SimpleBooleanProperty(false);
	
	public Chart(double width, double height, Stage stage, DataSet data) throws Exception {
		constructorStuff(width, height, stage, data);
	}
	
	private void constructorStuff(double widthParam, double heightParam, Stage stage, DataSet data) throws Exception {		
		this.numDecimalPts = data.numDecimalPts();
		this.tickSize = data.tickSize();
		this.width = widthParam;
		this.height = heightParam;
		this.data = data;
		stage.setMinWidth(MIN_WIDTH);
		stage.setMinHeight(MIN_HEIGHT);
		stage.heightProperty().addListener(new ChartHeightListener(this));
		stage.widthProperty().addListener(new ChartWidthListener(this));	
		stage.setOnCloseRequest(ev -> {
			close();
		});
		this.stage = stage;
		canvas = new Canvas(width, height);
		gc = canvas.getGraphicsContext2D();
		priceMargin = data.maxLength() * gc.getFont().getSize() / 2 + 20;
		if (priceMargin < 35) {
			priceMargin = 35;
		}
		hsb = new HorizontalChartScrollBar(this, 0, width - priceMargin, HSB_WIDTH, HSB_HEIGHT, height - HSB_HEIGHT);
		
		cbvg = new ChartButtonVanGoghs(this);
		btnMenu = new CanvasButton(gc, priceMargin - 2, HSB_HEIGHT + CHT_MARGIN - 2, width - priceMargin + 1, height - HSB_HEIGHT - CHT_MARGIN + 1, "MENU", (priceMargin - 2 - 34) / 2, 11);
		btnMenu.setVanGogh(cbvg.menuButtonVG(btnMenu)); 
		btnMenu.setOnMouseClicked(e -> {
			new AnimationTimer() {
				int i = menuHidden?1:-1;
				double pm = menuHidden?priceMargin:priceMargin+300;
				double t = pm + 300*i;
				boolean changed = false;
				@Override
				public void handle(long now) {	
					if (menuHidden && !changed) {
						menuHidden = false;
						changed = true;
					}
					pm += 50*i;
					chartWidth -= 50*i;
					setCandleStickVars(numCandlesticks);
					btnMenu.setWidth(priceMargin - 2);
					btnMenu.setHeight(HSB_HEIGHT + CHT_MARGIN - 2);
					btnMenu.setX(CHT_MARGIN + chartWidth + 1);
					chartTypeShortcut.setX(CHT_MARGIN + chartWidth - 15);
					
					menu.setX(CHT_MARGIN + chartWidth + priceMargin);
					
					hsb.setMaxPos(width - pm); 					
					
					if (cmrb != null) {
						cmrb.resetButtons();
					}
					
					if (!drawCandlesticks.get()) {
						hsb.setPosition((width - hsb.sbWidth() - pm) * ((double)startIndex /(data.tickDataSize(replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF)), false);
					} else {
						hsb.setPosition((width - hsb.sbWidth() - pm) * ((double)startIndex /(data.m1CandlesDataSize(replayMode).get() - numCandlesticks * END_MARGIN_COEF)), false);
					}
					keepStartIndex = true;
					if (pm == t) {
						btnMenu.setHover(false);						
						if (!menuHidden && !changed) {
							menuHidden = true;
						}
						draw();
						this.stop();
					}
					draw();
				}
			}.start();
		});
		
		menu = new ChartMenu(CHT_MARGIN + chartWidth + priceMargin, 0, 300, chartHeight, gc, this);		
		chartTypeShortcut = new CanvasButton(gc, 10, 10, CHT_MARGIN + chartWidth - 15, CHT_MARGIN + 5, null);
		chartTypeShortcut.setOnMouseClicked(e -> {
			toggleChartType();
		});
		
		sceneGraph = new Tree<ICanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<ICanvasNode>(cw, null));
		
		sceneGraph.addNode(new TNode<ICanvasNode>(hsb, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(btnMenu, sceneGraph.root()));
		menuNode = new TNode<ICanvasNode>(menu, sceneGraph.root());
		ctsNode = new TNode<ICanvasNode>(chartTypeShortcut, sceneGraph.root());
		sceneGraph.addNode(menuNode);
		sceneGraph.addNode(ctsNode);
		
		canvas.addEventFilter(Event.ANY, e -> {
			(new CanvasEventFilter(this)).canvasEventFilter(e);
		});
		
		fontSize = gc.getFont().getSize();
		crossHair = new CrossHair(this);		
		chartWidth = width - priceMargin - CHT_MARGIN;
		chartHeight = height - hsb.sbHeight() - CHT_MARGIN*2;
		candlestickWidth = chartWidth * CNDL_WDTH_COEF;
		candlestickSpacing = candlestickWidth * CNDL_SPAC_COEF;
		numCandlesticks = (int)(chartWidth / (candlestickWidth + candlestickSpacing));
		chtDataMargin = CHT_MARGIN + fontSize;
		Chart.charts.add(this);
		setEventHandlers();	
		menu.setFunctionsMenuSceneGraph(sceneGraph, menuNode);		
		thp = new TradeHistoryPlotter(this);
		
		draw();
	}
	//TODO
	private TradeHistoryPlotter thp;
	private ArrayList<TradeHistory> hst;
	private BooleanProperty plotHst = new SimpleBooleanProperty(false);
	private LoadingHistory lhst;
	
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
	
	public LoadingHistory loadingHistory() {
		return lhst;
	}
	
	public ReadOnlyBooleanProperty plotHst() {
		return plotHst;
	}
	
	public static ArrayList<Chart> charts() {
		return charts;
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
	
	public void setChartHeight(double chartHeight) {
		this.chartHeight = chartHeight;
	}
	
	public void setChartWidth(double chartWidth) {
		this.chartWidth = chartWidth;
	}
	
	private void setEventHandlers() {
		cw.setOnMouseDragged(e -> onMouseDragged(e));
		canvas.setOnMouseEntered(e -> onMouseEntered());
		canvas.setOnMouseExited(e -> onMouseExited(e));
		canvas.setOnMousePressed(e -> onMousePressed(e));
		canvas.setOnMouseReleased(e -> onMouseReleased(e));
		canvas.setOnMouseClicked(e -> onMouseClicked(e));
		canvas.setOnMouseMoved(e -> onMouseMoved(e));
		canvas.setOnScroll(e -> onScroll(e));
	}
	
	public Canvas canvas() {
		return this.canvas;
	}
	
	public GraphicsContext graphicsContext() {
		return this.gc;
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
	
	public void setGraphicsContext(GraphicsContext gc) {
		this.gc = gc;
	}
	
	public double x() {
		return this.x;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public double y() {
		return this.y;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void toggleChartTypeShortcut() {
		drawChartTypeShortcut = !drawChartTypeShortcut;
		if (drawChartTypeShortcut) {
			sceneGraph.addNode(ctsNode);
		} else {
			sceneGraph.removeNode(ctsNode);
		}
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

	public double dataMarginTickSize() {
		return this.dataMarginTickSize;
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
	
	public int numDataPoints() {
		return this.numDataPoints;
	}
	
	public int numCandlesticks() {
		return this.numCandlesticks;
	}
	
	public static ReadOnlyBooleanProperty focusedOnChart() {
		return BooleanProperty.readOnlyBooleanProperty(focusedOnChart);
	}
	
	public ArrayList<DataSet.Candlestick> m1Candles() {
		return this.data.m1Candles();
	}
	
	public ArrayList<DataSet.DataPair> tickData() {
		return this.data.tickData();
	}
	
	public void close() {
		disableReplayMode();
		charts.remove(this);
		stage.close();
	}
	
	public static ReadOnlyBooleanProperty darkMode() {
		return BooleanProperty.readOnlyBooleanProperty(darkMode);
	}
	
	public void enableReplayMode(MarketReplay mr, MarketReplayPane mrp) {
		if (!this.replayMode) {
			this.replayMode = true;
			this.mr = mr;
			this.mrp = mrp;
			cmrb = new ChartMarketReplayButtons(this, mr, cbvg);
			
			if (mr.trade() == null) {
				Trade t = new Trade(data, 1, true, 1);
				t.close(1);
				mr.setTrade(t);
				cmrb.disableButtons();
			} else if (mr.trade().closed()) {
				cmrb.disableButtons();
			}
			
			for (PendingTrade pt : mr.pendingTrades()) {
				cmrb.addPenTradePair(new PendingTradePair(pt, this));
			}
			
			drawMRP = true;
			mr.addChart(this);
			menu.chartFunctionsMenu().generalFunctionstab().setReplayMode(true);
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
			menu.chartFunctionsMenu().generalFunctionstab().setReplayMode(false);
		}
	}
	
	public boolean replayMode() {
		return this.replayMode;
	}
	
	public static void closeAll(String name, boolean replayOnly) {
		Object[] chts = charts.toArray();
		for (Object c : chts) {
			Chart cht = (Chart)c;
			if (cht.name().equals(name)) {				
				if (replayOnly && !cht.replayMode()) {
					continue;
				}
				cht.close();
			}
		}
	}
	
	public HorizontalChartScrollBar hsb() {
		return this.hsb;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}
	
	public void setHeight(double height) {
		this.height = height;
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
	
	public static void drawCharts(String name) {
		for (Chart c : charts) {
			if (name == null || c.name().equals(name) && (c.focusedChart().get() || !c.skipDraw().get())) {
				c.draw();
			}
		}
	}
	
	public void onMouseExited(MouseEvent e) {
		hsb.onMouseExited(e);
		focusedChart.set(false);
		focusedOnChart.set(false);
		if (replayMode) {
			cmrb.disablePendingOrderButtons();
		}
		drawCharts(this.name());
	}
	
	public void onMouseEntered() {
		focusedChart.set(true);
		CrossHair.setIsForCandle(drawCandlesticks.get());
		CrossHair.setDateIndex(0);
		CrossHair.setName(data.name());
		drawCharts(this.name());
	}
	
	public void onMouseMoved(MouseEvent e) {
		if (CrossHair.dateIndex().get() >= data.m1CandlesDataSize(replayMode).get() && drawCandlesticks.get()) {
			CrossHair.setDateIndex(0);
		}
		if (!chartDateMarginDragging && !priceDragging) {
			stage.getScene().setCursor(Cursor.DEFAULT);
		}
		hsb.onMouseMoved(e);
		CrossHair.setX(e.getX());
		CrossHair.setY(e.getY());
		CrossHair.setPrice(yCoordToPrice(e.getY()));
		if (!onChart(e.getX(), e.getY(), true)) {
			measuring = false;
			if (e.getX() >= CHT_MARGIN + chartWidth && e.getX() <= CHT_MARGIN + chartWidth + priceMargin && e.getY() <= height - HSB_HEIGHT - CHT_MARGIN) {
				stage.getScene().setCursor(Cursor.N_RESIZE);
			}
		} else {
			if (e.getY() >= chartHeight + CHT_MARGIN - fontSize) {
				stage.getScene().setCursor(Cursor.E_RESIZE);
			}
		}
		if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100 && !mrpSBDragging) {
			fireMRPEvent(MouseEvent.MOUSE_MOVED, e);
		}
		drawCharts(this.name());
	}	
	
	private boolean onDateMargin(double x, double y) {
		if (x >= CHT_MARGIN && x <= CHT_MARGIN + chartWidth && y >= CHT_MARGIN + chartHeight - fontSize && y <= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}
	
	public void onMousePressed(MouseEvent e) {		
		hsb.onMousePressed(e);
		if (e.getButton() == MouseButton.MIDDLE) {
			if (lineHighlighted != -1) {
				data.lines().remove(lineHighlighted);
				lineHighlighted = -1;
			} else if (onChart(e.getX(), e.getY(), true)) {
				data.lines().add(new Line(roundToNearestTick(CrossHair.price())));
			}
		} else if (e.getButton() == MouseButton.SECONDARY) {
			rightPressed = true;			
			startPrice = roundToNearestTick(CrossHair.price());
			startX = e.getX();
			startY = e.getY();
		} else if (e.isPrimaryButtonDown()) {			
			if (e.getX() >= CHT_MARGIN + chartWidth && e.getX() <= CHT_MARGIN + chartWidth + priceMargin && e.getY() <= chartHeight + CHT_MARGIN) {
				priceDragging = true;
				priceInitPos = e.getY();
			}
			if (onChart(e.getX(), e.getY(), true)) {
				chartInitPos = e.getX();
				if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100) {
					fireMRPEvent(MouseEvent.MOUSE_PRESSED, e);
				} else if (onDateMargin(e.getX(), e.getY())) {
						chartDateMarginDragging = true;
				} else {
					chartDragging = true;
				}								
				double price = ((((chartHeight - (chtDataMargin*2)) - (e.getY() - Chart.CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest;
				double upperPrice = ((((chartHeight - (chtDataMargin*2)) - (e.getY() - LINE_PRESS_MARGIN - Chart.CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest;
				double lowerPrice = ((((chartHeight - (chtDataMargin*2)) - (e.getY() + LINE_PRESS_MARGIN - Chart.CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest;
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
		drawCharts(this.name());
	}
	
	public static void toggleDarkMode() {
		darkMode.set(!darkMode.get());
		Settings.saveDarkMode();
		for (Chart c : charts) {
			if (c.replayMode) {					
				c.setNumberChooserColours();				
			}
			c.draw();
		}
		Menu m = Menu.menu();
		if (m != null) {
			m.draw();
		}
		MarketReplayPane.drawReplayPanes();
	}
		
	public void onMouseReleased(MouseEvent e) {	
		hsb.onMouseReleased(e);	
		if (chartDateMarginDragging && !(onChart(e.getX(), e.getY(), true) && e.getY() >= chartHeight + CHT_MARGIN - fontSize)) {
			stage.getScene().setCursor(Cursor.DEFAULT);
		}
		if (priceDragging && !(e.getX() >= CHT_MARGIN + chartWidth && e.getY() <= height - HSB_HEIGHT - CHT_MARGIN)) {
			stage.getScene().setCursor(Cursor.DEFAULT);
		}
		//TODO
		if (measuring) {
			measuring = false;
			stage.getScene().cursorProperty().set(Cursor.DEFAULT);
		} else if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100) {
			fireMRPEvent(MouseEvent.MOUSE_RELEASED, e);
		}		
		lineDragging = false;
		priceDragging = false;
		chartDragging = false;
		chartDateMarginDragging = false;
		rightPressed = false;
		if (mrpSBDragging) {
			mrpSBDragging = false;
			fireMRPEvent(MouseEvent.MOUSE_RELEASED, e);
		}
		drawCharts(this.name());
	}
	
	public void onMouseClicked(MouseEvent e) {
		if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100 && !measuring) {
			fireMRPEvent(MouseEvent.MOUSE_CLICKED, e);
		}
		drawCharts(this.name());
	}
	
	private void fireMRPEvent(EventType<MouseEvent> type, MouseEvent e) {
		MouseEvent me = new MouseEvent(type, e.getX() - mrpx, e.getY() - mrpy, e.getScreenX(), e.getScreenY(), 
				e.getButton(), e.getClickCount(), e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(), 
				e.isPrimaryButtonDown(), e.isMiddleButtonDown(), e.isSecondaryButtonDown(), e.isBackButtonDown(), 
				e.isForwardButtonDown(), e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(), null);
		mrp.canvas().fireEvent(me);
	}
	
	public void onMouseDragged(MouseEvent e) {	
		hsb.onMouseDragged(e);
		if (priceDragging) {
			stage.getScene().setCursor(Cursor.N_RESIZE);
			double posDiff = e.getY() - priceInitPos;
			if (posDiff < 0) {
				if (chtDataMargin + posDiff > CHT_MARGIN + fontSize) {
					chtDataMargin = chtDataMargin + posDiff;
				}
			} else if (chtDataMargin + posDiff < chartHeight * CHT_DATA_MARGIN_COEF) {
				chtDataMargin = chtDataMargin + posDiff;
			}
		}
		if (lineDragging) {
			double price = roundToNearestTick(((((chartHeight - (chtDataMargin*2)) - (e.getY() - Chart.CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest); 
			data.lines().get(lineHighlighted).setPrice(price);
		}
		if (rightPressed) {
			stage.getScene().cursorProperty().set(Cursor.CROSSHAIR);
			measuring = true;
			endX = e.getX();
			endY = e.getY();
		}
		priceInitPos = e.getY();		
		if (chartDragging && !lineDragging) {
			double posDiff = e.getX() - chartInitPos;
			double newHSBPos = hsb.x();					
			int diff;
			dragDiffAccum += posDiff;
			if (drawCandlesticks.get()) {
				diff = (int)(dragDiffAccum / (candlestickWidth + candlestickSpacing));
				if (diff != 0) {
					startIndex = startIndex - diff;
					newHSBPos = (CHT_MARGIN + chartWidth - HSB_WIDTH) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
					dragDiffAccum = 0;
				}
			} else {
				diff = (int)(dragDiffAccum / xDiff);
				if (diff != 0) {
					startIndex = startIndex - diff;
					newHSBPos = (CHT_MARGIN + chartWidth - HSB_WIDTH) * ((double)startIndex /(data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
					dragDiffAccum = 0;
				}
			}
			if (startIndex < 0) {
				startIndex = 0;
			}
			if (dragDiffAccum == 0 && posDiff != 0) {
				if (newHSBPos < CHT_MARGIN + chartWidth - HSB_WIDTH) {
					keepStartIndex = true;
				} else {
					keepStartIndex = false;
				}
				hsb.setPosition(newHSBPos, false);
			}
		}
		if (chartDateMarginDragging && !lineDragging) {	
			stage.getScene().setCursor(Cursor.E_RESIZE);
			if (drawCandlesticks.get()) {
				zoomCandlesticks(e.getX() - chartInitPos, false);
			} else {
				zoomTicks(e.getX() - chartInitPos, false);
			}
		}
		if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100 || mrpSBDragging) {
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
	
	private void zoomCandlesticks(double delta, boolean scroll) {
		double multiplier = 1.002;
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
				newHSBPos = (CHT_MARGIN + chartWidth - HSB_WIDTH) * ((double)(startIndex) /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
			}
		} else { 
			newHSBPos = (CHT_MARGIN + chartWidth - HSB_WIDTH) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
		}
		if (newHSBPos < CHT_MARGIN + chartWidth - HSB_WIDTH || customSI) {
			keepStartIndex = true;
		} else {
			keepStartIndex = false;
		}
		hsb.setPosition(newHSBPos, false);
	}
	
	private void zoomTicks(double delta, boolean scroll) {
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
		double xDiff = chartWidth / (double)numDataPoints;
		if (xDiff * (data.tickDataSize(this.replayMode).get() - 1) < chartWidth) {
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
				newHSBPos = (CHT_MARGIN + chartWidth - HSB_WIDTH) * ((double)(startIndex) /(data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));				
			}
		} else {
			newHSBPos = (CHT_MARGIN + chartWidth - HSB_WIDTH) * ((double)startIndex /(data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
		}
		if (newHSBPos < CHT_MARGIN + chartWidth - HSB_WIDTH || customSI) {
			keepStartIndex = true;
		} else {
			keepStartIndex = false;
		}
		hsb.setPosition(newHSBPos, false);
	}
	
	public void onScroll(ScrollEvent e) {	
		if (drawCandlesticks.get()) {
			zoomCandlesticks(e.getDeltaY(), true);
		} else {
			zoomTicks(e.getDeltaY(), true);
		}
		drawCharts(this.name());
	}		
	
	private void setNumberChooserColours() {
		volTens.resetColours();
		volUnits.resetColours();
	}
	
	public boolean onChart(double x, double y, boolean setFocused) {
		if (y <= chartHeight + CHT_MARGIN && y >= CHT_MARGIN) {
			if (x <= chartWidth + CHT_MARGIN && x >= CHT_MARGIN) {
				if (focusedChart.get() && setFocused) {
					focusedOnChart.set(true);
				}
				return true;
			}
		}
		if (focusedChart.get() && setFocused) {
			focusedOnChart.set(false);
		}
		return false;
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
				DataSet.Candlestick c = data.makeLastReplayCandlestick(m1Candles().get(ei).firstTickIndex());
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
		int pow = (int)Math.pow(10, numDecimalPts);
		price *= pow;
		int intTick = (int)(pow * tickSize);		
		int quotient = (int)(price / intTick);
		double remainder = price % intTick;		
		if (remainder > intTick / 2.0) {
			return i * Round.round(intPart + (intTick * (quotient + 1)) / (double)pow, numDecimalPts + 1); 
		}
		return i * Round.round(intPart + (intTick * quotient) / (double)pow, numDecimalPts + 1);
	}
	
	private void drawLines() {
		double trueLowest = lowest - dataMarginTickSize;
		double trueHighest = highest + dataMarginTickSize;
		for (Line l : data.lines()) {
			if (l.price() >= trueLowest && l.price() <= trueHighest) {
				double trueRange = trueHighest - trueLowest;
				double y = chartHeight + CHT_MARGIN - (((l.price() - trueLowest) / trueRange) * chartHeight);
				if (l.highlighted()) {
					gc.setFill(Color.RED);
					gc.setStroke(Color.RED);
				} else {
					gc.setFill(Color.GRAY);
					gc.setStroke(Color.GRAY);
				}
				gc.strokeLine(CHT_MARGIN, y, chartWidth + CHT_MARGIN, y);				
				gc.fillRect(chartWidth + CHT_MARGIN, y - fontSize/2, priceMargin, fontSize);
				gc.setStroke(Color.WHITE);
				gc.strokeText(((Double)(roundToNearestTick(l.price()))).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, y + fontSize/3, priceMargin - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
			}
		}		
	}
	
	public void drawCandleStick(DataSet.Candlestick candle, double xPos, double yPos) {
		int num = 0;
		if (darkMode.get()) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}		
		if (candle.open() < candle.close()) {
			gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.UP_CANDLESTICK_STROKE));
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.close() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.close()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.close() - candle.open()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.close() - candle.low()) / conversionVar);
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.UP_CANDLESTICK_FILL));
			gc.fillRect(xPos, yPos, candlestickWidth - num, (candle.close() - candle.open()) / conversionVar - num);
		} else {
			gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.DOWN_CANDLESTICK_STROKE));
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.open() - candle.close()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.open() - candle.close()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.open() - candle.low()) / conversionVar);
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.DOWN_CANDLESTICK_FILL));
			gc.fillRect(xPos + num, yPos + num, candlestickWidth - num, (candle.open() - candle.close()) / conversionVar - num);
		}
	}
	
	private void calculateIndices() {
		if (drawCandlesticks.get()) {
			if (!keepStartIndex) {
				if (data.m1CandlesDataSize(this.replayMode).get() < numCandlesticks * END_MARGIN_COEF) {
					startIndex = 0;
				} else {
					startIndex = (int)((hsb.x() / (chartWidth + CHT_MARGIN - HSB_WIDTH)) * (data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
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
					startIndex = (int)((hsb.x() / (chartWidth + CHT_MARGIN - HSB_WIDTH)) * (data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
				}
			}
			endIndex = startIndex + numDataPoints;
			if (endIndex >= data.tickDataSize(this.replayMode).get()) {
				endIndex = data.tickDataSize(this.replayMode).get() - 1;
			}
		}
	}
	
	private void drawFrame() {
		//TODO
		gc.clearRect(0, 0, width, height);		
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
		gc.fillRect(0, 0, width, height);
		if (ImageSettings.draw().get()) {
			ImageFunctions.drawImage(gc, ImageSettings.image(), 0, 0, width, height);
		}
		if (darkMode.get()) {	
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}				
		gc.strokeRect(CHT_MARGIN, CHT_MARGIN, chartWidth, chartHeight);
		gc.strokeRect(CHT_MARGIN + chartWidth, CHT_MARGIN + chartHeight, priceMargin, HSB_HEIGHT + CHT_MARGIN);
	}	
	
	private void setPreDrawVars() {
		if (drawCandlesticks.get()) {
			tickSizeOnChart = (chartHeight - chtDataMargin * 2) / (range / tickSize);
			dataMarginTickSize = (chtDataMargin / tickSizeOnChart) * tickSize;
			conversionVar = tickSize / tickSizeOnChart;	
		} else {
			xDiff = chartWidth / (double)numDataPoints;	
			tickSizeOnChart = (chartHeight - chtDataMargin * 2) / (range / tickSize);
			dataMarginTickSize = (chtDataMargin / tickSizeOnChart) * tickSize;
			conversionVar = tickSize / tickSizeOnChart;	
		}
	}
	
	private void drawLineChart() {
		endMargin = false;
		double startY = chartHeight - chtDataMargin + CHT_MARGIN - (((data.tickData().get(startIndex).price() - lowest) / range) * (chartHeight - chtDataMargin * 2));		
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
			DataSet.Candlestick c;
			if (replayMode && startIndex + i == data.m1CandlesDataSize(this.replayMode).get() - 1) {
				c = data.makeLastReplayCandlestick(m1Candles().get(data.m1CandlesDataSize(replayMode).get() - 1).firstTickIndex());
			} else {
				c = data.m1Candles().get(startIndex + i);
			}		
			double yPos;
			double xPos = CHT_MARGIN + (candlestickWidth + candlestickSpacing) * i;
			if (c.open() < c.close()) {
				yPos = ((highest - c.close()) / range) * (chartHeight - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
			} else {
				yPos = ((highest - c.open()) / range) * (chartHeight - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
			}
			drawCandleStick(c, xPos, yPos);			
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
		double yPos = ((highest - price) / range) * (chartHeight - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
		gc.setFill(Color.SLATEBLUE);		
		gc.fillRect(chartWidth + CHT_MARGIN, yPos - fontSize/2, priceMargin, fontSize);
		gc.setStroke(Color.WHITE);
		gc.strokeText(((Double)(price)).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, yPos + fontSize/3, priceMargin - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
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
		double yPos = ((highest - price) / range) * (chartHeight - chtDataMargin * 2) + chtDataMargin + CHT_MARGIN;
		gc.setStroke(Color.SLATEBLUE);
		gc.strokeLine(CHT_MARGIN, yPos,  CHT_MARGIN + chartWidth, yPos);	
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
		if (darkMode.get()) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		while (index > CHT_MARGIN + gc.getFont().getSize() / 3) {
			gc.strokeLine(priceDashPos, index, priceDashPos + PRICE_DASH_SIZE, index);
			gc.strokeText(((Double)(Round.round(lowest + (diff * i), numDecimalPts + 1))).toString(), pricePos, index + pricePosYMargin, priceMargin - PRICE_DASH_SIZE - PRICE_DASH_MARGIN * 2);
			index -= spacing;
			i++;
		}			
	}	
	
	private void checkDrawLines() {
		if (!data.lines().isEmpty()) {
			drawLines();
		}
	}
	
	private void checkMeasuring() {		
		if (measuring) {
			double endPrice = ((((chartHeight - (chtDataMargin*2)) - (endY - Chart.CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest;
			if (darkMode.get()) {
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
				if (n100 > CHT_MARGIN && n100 < CHT_MARGIN + chartHeight - fontSize) {
					double ex = endX + 50;
					if (ex >= CHT_MARGIN + chartWidth) {
						ex -= 100;
						gc.strokeLine(ex, n100, endX, n100);
					} else {
						gc.strokeLine(endX, n100, ex, n100);
					}				
				}
			}
			
			double ex = endX;
			double ey = endY;
			DecimalFormat df = new DecimalFormat("#");
			df.setMaximumFractionDigits(numDecimalPts);
			String text = df.format(roundToNearestTick(endPrice - startPrice)) + " from: " + ((Double)startPrice).toString();
			Text t = new Text(text);
			double prc_msrmnt_length = t.getLayoutBounds().getWidth() + 5;
			boolean right = true;
			if (endX > CHT_MARGIN + chartWidth - prc_msrmnt_length) {
				ex -= prc_msrmnt_length + 5;
				right = false;
			}
			boolean dropped = false;
			if (endY < CHT_MARGIN + 2 + fontSize) {
				ey += fontSize + 3;
				if (right) {
					dropped = true;
					ex += 12;	
				}
			}
			if (endX > CHT_MARGIN + chartWidth - prc_msrmnt_length - 12 && dropped && right) {
				ex -= prc_msrmnt_length + 5 + 12;
			}
			if (endY >= chartHeight + CHT_MARGIN - fontSize) {
				ey = chartHeight + CHT_MARGIN - fontSize;
			}
			gc.setStroke(Color.SLATEBLUE);				
			gc.strokeText(text, ex + 1, ey - 2, prc_msrmnt_length);
		}
	}
	
	private void drawTopRightText() {
		if (darkMode.get()) {			
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		if (drawCandlesticks.get()) {
			String trt = data.name() + "  M1  ";
			if (crossHair.ohlc() != null) {
				trt += crossHair.ohlc();
			}
			gc.strokeText(trt, CHT_MARGIN + INFO_MARGIN, CHT_MARGIN + fontSize);
		} else {
			gc.strokeText(data.name() + "  T1", CHT_MARGIN + INFO_MARGIN, CHT_MARGIN + fontSize);
		}
		crossHair.resetOHLC();
	}
	
	public double yCoordToPrice(double y) {
		return ((((chartHeight - (chtDataMargin*2)) - (y - CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest;
	}
	
	public double priceToYCoord(double price) {
		return ((highest + dataMarginTickSize - price) / (range + dataMarginTickSize * 2)) * chartHeight + CHT_MARGIN;
	}
	
	private double t = 0;
	private int c = 0;
	
	private void drawUI() {	
		long b = 0;
		if (printSpeed) {
			b = System.nanoTime();
		}
		calculateIndices();			
		drawFrame();		
		hsb.draw();
		calculateRange();
		setPreDrawVars();			
		drawPriceDashes();
		drawTopRightText();	
		crossHair.drawCrossHair();
		if (drawCandlesticks.get()) {
			drawCandlestickChart();
		} else {		
			drawLineChart();
		}
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
		checkDrawLines();										
		checkMeasuring();			
		if (replayMode) {					
			drawCurrentPriceLine();
			drawCurrentPriceBox();
			cmrb.draw();
			if (drawMRP) {
				mrp.drawPane(gc, mrpx, mrpy);
			}
		}
		btnMenu.draw();
		if (!menuHidden) {
			menu.draw();
		}
		if (printSpeed) {
			double tm = (System.nanoTime() - b) / 1000000000.0;
			t += tm;
			c++;
			System.out.printf("REDRAW\ttime: %f\tave: %f\trange: %d\n", tm, t/c, endIndex - startIndex);
		}
	}
	
	public void draw() {		
		if (Thread.currentThread().getStackTrace()[2].getClassName().equals("com.github._7000toni.auto.canvasnode.CanvasEventFilter")) {
			return;
		}
		if (Platform.isFxApplicationThread()) {
			drawUI();
		} else {
			Platform.runLater(() -> {
				drawUI();
			});
		}
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
		c = 0;
	}	
	
	public void setCandleStickVars(int numCandlesticks) {	
		candlestickWidth = (chartWidth / numCandlesticks) / (1 + CNDL_SPAC_COEF);
		candlestickSpacing = candlestickWidth * CNDL_SPAC_COEF;
		t = 0;
		c = 0;
	}	
	
	public ReadOnlyBooleanProperty drawCandlesticks() {
		return BooleanProperty.readOnlyBooleanProperty(drawCandlesticks);
	}
	
	public ReadOnlyBooleanProperty focusedChart() {
		return BooleanProperty.readOnlyBooleanProperty(focusedChart);		
	}
	
	public double fontSize() {
		return this.fontSize;
	}
	
	public double priceMargin() {
		return priceMargin;
	}

	public ChartMenu chartMenu() {
		return menu;
	}
	
	public CanvasButton btnMenu() {
		return btnMenu;
	}
	
	public boolean menuHidden() {
		return menuHidden;
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
			newHSBPos = (CHT_MARGIN + chartWidth - HSB_WIDTH) * ((double)startIndex / (data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
			hsb.setPosition(newHSBPos, false);				
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
			newHSBPos = (CHT_MARGIN + chartWidth - HSB_WIDTH) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
			hsb.setPosition(newHSBPos, false);				
		}
		if (newHSBPos < CHT_MARGIN + chartWidth - HSB_WIDTH) {
			keepStartIndex = true;
		} else {
			keepStartIndex = false;
		}
	}
	
	public TNode<ICanvasNode> menuNode() {
		return menuNode;
	}
	
	@Override
	public Tree<ICanvasNode> sceneGraph() {
		return sceneGraph;
	}

	@Override
	public TNode<ICanvasNode> lastNode() {
		return lastNode;
	}

	@Override
	public void setLastNode(TNode<ICanvasNode> lastNode) {
		this.lastNode = lastNode;
	}

	@Override
	public ReentrantLock varLock() {
		return varLock;
	}
	
	@Override
	public boolean onWindow(double x, double y) {
		return x <= width && x >= 0 && y <= height && y >= 0; 
	}

	@Override
	public boolean dragging() {
		return dragging;
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;		
	}
}
