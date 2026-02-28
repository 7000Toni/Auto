import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

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
import javafx.stage.Stage;
import javafx.scene.Cursor;

public class Chart implements ScrollBarOwner, CanvasWindow {
	public final static double CNDL_MOVE_COEF = 0.001;
	public final static int CNDL_INDX_MOVE_COEF = 2;
	
	public final static double TICK_MOVE_COEF = 0.001;
	public final static int TICK_INDX_MOVE_COEF = 3;
	
	public final static double HSB_WIDTH = 100;
	public final static double HSB_HEIGHT = 10;	
	
	public final static double LINE_PRESS_MARGIN = 5;
	
	public final static double WIDTH_EXTRA = 16;
	public final static double HEIGHT_EXTRA = 39;
	
	public final static double MIN_WIDTH = 640; 
	public final static double MIN_HEIGHT = 360; 
	
	public final static double CHT_MARGIN = 5;
	public final static double INFO_MARGIN = 5;
	public final static double CHT_DATA_MARGIN_COEF = 0.45;	
	public static double PRICE_MARGIN = 100;
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
	
	private CanvasButton buy;
	private CanvasButton sell;
	private CanvasNumberChooser volUnits;
	private CanvasNumberChooser volTens;
		
	private TradeButtons tradeButs;
	private CanvasButton limitOrder;
	private CanvasButton stopOrder;
	private boolean slDragging = false;
	private boolean tpDragging = false;	
	private boolean drawPending = false;
	private boolean limitDragging = false;
	private boolean stopDragging = false;
	private PendingTrade penTrade = null;
	private ArrayList<PendingTrade> pendingTrades;	
	private PendingTrade penOrderBeingDragged = null;
	private boolean penOrderDragging = false;
	
	private static int lineHighlighted = -1;
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
	private Tree<CanvasNode> sceneGraph;
	private TNode<CanvasNode> lastNode = null;
	private CanvasWrapper cw;
	private boolean dragging = false;
	private boolean mrpSBDragging = false;
	private final ReentrantLock varLock = new ReentrantLock();
	TNode<CanvasNode> menuNode;
	
	private boolean menuHidden = true;
	private CanvasButton btnMenu;
	private ChartMenu menu;
	
	private boolean printSpeed = false;
	
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
		PRICE_MARGIN = data.maxLength() * gc.getFont().getSize() / 2 + 20;
		if (PRICE_MARGIN < 35) {
			PRICE_MARGIN = 35;
		}
		hsb = new HorizontalChartScrollBar(this, 0, width - PRICE_MARGIN, HSB_WIDTH, HSB_HEIGHT, height - HSB_HEIGHT);
		
		cbvg = new ChartButtonVanGoghs(this);
		btnMenu = new CanvasButton(gc, PRICE_MARGIN - 2, HSB_HEIGHT + CHT_MARGIN - 2, width - PRICE_MARGIN + 1, height - HSB_HEIGHT - CHT_MARGIN + 1, "MENU", (PRICE_MARGIN - 2 - 34) / 2, 11);
		btnMenu.setVanGogh(cbvg.menuButtonVG(btnMenu)); 
		btnMenu.setOnMouseClicked(e -> {
			new AnimationTimer() {
				int i = menuHidden?1:-1;
				double pm = menuHidden?PRICE_MARGIN:PRICE_MARGIN+300;
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
					btnMenu.setWidth(PRICE_MARGIN - 2);
					btnMenu.setHeight(HSB_HEIGHT + CHT_MARGIN - 2);
					btnMenu.setX(CHT_MARGIN + chartWidth + 1);
					btnMenu.setY(height - HSB_HEIGHT - CHT_MARGIN + 1);
					
					menu.setX(CHT_MARGIN + chartWidth + PRICE_MARGIN);
					
					hsb.setMaxPos(width - pm); 					
					
					if (!drawCandlesticks.get()) {
						hsb.setPosition((width - hsb.sbWidth() - pm) * ((double)startIndex /(data.tickDataSize(replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF)), false);
					} else {
						hsb.setPosition((width - hsb.sbWidth() - pm) * ((double)startIndex /(data.m1CandlesDataSize(replayMode).get() - numCandlesticks * END_MARGIN_COEF)), false);
					}
					keepStartIndex = true;
					//TODO					
					if (pm == t) {
						btnMenu.setHover(false);
						if (replayMode) {
							limitOrder.setX(CHT_MARGIN + chartWidth - fontSize*2-2);
							stopOrder.setX(CHT_MARGIN + chartWidth - fontSize*4-4);
						}		
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
		
		menu = new ChartMenu(CHT_MARGIN + chartWidth + PRICE_MARGIN, 0, 300, chartHeight, gc, this);		
		
		sceneGraph = new Tree<CanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<CanvasNode>(cw, null));
		
		sceneGraph.addNode(new TNode<CanvasNode>(hsb, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(btnMenu, sceneGraph.root()));
		menuNode = new TNode<CanvasNode>(menu, sceneGraph.root());
		sceneGraph.addNode(menuNode);
		
		canvas.addEventFilter(Event.ANY, e -> {
			(new CanvasEventFilter(this)).canvasEventFilter(e);
		});
		
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
		menu.setFunctionsMenuSceneGraph(sceneGraph, menuNode);
		draw();
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
	
	public CanvasButton limitOrder() {
		return limitOrder;
	}
	
	public CanvasButton stopOrder() {
		return stopOrder;
	}
	
	public PendingTrade penTrade() {
		return penTrade;
	}
	
	public TradeButtons tradeButs() {
		return tradeButs;
	}
	
	public CanvasButton buyButton() {
		return buy;
	}
	
	public CanvasButton sellButton() {
		return sell;
	}
	
	private void setEventHandlers() {
		canvas.setOnMouseDragged(e -> onMouseDragged(e));
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
	
	public ArrayList<PendingTrade> pendingTrades() {
		return pendingTrades;
	}
	
	public void setPendingTrades(ArrayList<PendingTrade> pendingTrades) {
		this.pendingTrades = new ArrayList<PendingTrade>();
		for (PendingTrade p : mr.pendingTrades()) {
			PendingTrade p2 = new PendingTrade(p.limit(), p.buy(), p.price(), p.volume(), this);
			p2.pTradeButs().order.setText(p.pTradeButs().order.text());
			p2.pTradeButs().close.setText(p.pTradeButs().close.text());
			p2.pTradeButs().setSL.setText(p.pTradeButs().setSL.text());
			p2.pTradeButs().setTP.setText(p.pTradeButs().setTP.text());
			this.pendingTrades.add(p2);
		}
		if (mr.trade().closed() && pendingTrades.size() == 1) {
			PendingTrade p = this.pendingTrades.get(0);
			tradeButs.sl().setText(p.volume() + "  $" + Trade.hypotheticalProfit2(p.price(), mr.slPrice().get(), p.buy(), p.volume()));
			tradeButs.tp().setText(p.volume() + "  $" + Trade.hypotheticalProfit2(p.price(), mr.tpPrice().get(), p.buy(), p.volume()));
		} else if (mr.trade().closed()) {
			tradeButs.sl().setText("SL");
			tradeButs.tp().setText("TP");
		} else {
			tradeButs.sl().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.slPrice().get()));
			tradeButs.tp().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.tpPrice().get()));
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
			double bw = 40;
			double ncw = 20;
			double bh = 30;
			double mgn = 5;
			double initx = CHT_MARGIN + INFO_MARGIN;
			double inity = 30;			
			buy = new CanvasButton(gc, bw, bh, initx, inity, "BUY", 9, fontSize + 7);
			buy.setVanGogh(cbvg.buyVG(buy));
			sell = new CanvasButton(gc, bw, bh, initx + bw + mgn + ncw + mgn + ncw + mgn, inity, "SELL", 9, fontSize + 7);
			sell.setVanGogh(cbvg.sellVG(sell));
			double h = CanvasNumberChooser.getHeightForDesiredNumberHight(bh);
			double y = bh - CanvasNumberChooser.buttonHeight(h);
			volTens = new CanvasNumberChooser(gc, ncw, h, initx + bw + mgn, y);
			volUnits = new CanvasNumberChooser(gc, ncw, h, initx + bw + mgn + ncw + mgn, y);
			volUnits.setValue(1);
			setNumberChooserColours();
			menu.setReplayMode(true);
			
			tradeButs = new TradeButtons();
			tradeButs.close = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3);
			tradeButs.close.setVanGogh(cbvg.closeVG(tradeButs.close));
			tradeButs.setCancelTP(new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3));
			tradeButs.cancelTP().setVanGogh(cbvg.cancelTpVG(tradeButs.cancelTP()));
			tradeButs.setCancelSL(new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3));
			tradeButs.cancelSL().setVanGogh(cbvg.cancelSlVG(tradeButs.cancelSL()));
			tradeButs.setSL(new CanvasButton(gc, 100, fontSize*2, CHT_MARGIN + chartWidth / 2 - 100, 0, "", 5, fontSize/3));
			tradeButs.sl().setVanGogh(cbvg.slVG(tradeButs.sl()));
			tradeButs.setTP(new CanvasButton(gc, 100, fontSize*2, CHT_MARGIN + chartWidth / 2 - 100, 0, "", 5, fontSize/3));
			tradeButs.tp().setVanGogh(cbvg.tpVG(tradeButs.tp()));
			tradeButs.setSetSL(new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 + 10, 0, "SL", 6, fontSize/3));
			tradeButs.setSL().setVanGogh(cbvg.setSlVG(tradeButs.setSL));
			tradeButs.setSetTP(new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 + 20 + fontSize*2, 0, "TP", 6, fontSize/3));
			tradeButs.setTP().setVanGogh(cbvg.setTpVG(tradeButs.setTP));
			limitOrder = new CanvasButton(gc, fontSize*2+2, fontSize, CHT_MARGIN + chartWidth - fontSize*2-2, 0, "LMT", 0, fontSize-2);
			limitOrder.setVanGogh(cbvg.pendingVG(limitOrder));
			stopOrder = new CanvasButton(gc, fontSize*2+2, fontSize, CHT_MARGIN + chartWidth - fontSize*4-4, 0, "STP", 1, fontSize-2);			
			stopOrder.setVanGogh(cbvg.pendingVG(stopOrder));			
			if (mr.trade() == null) {
				mr.setTrade(new Trade(data, 1, true, 1));
				mr.trade().close(1);
				disableTradeButtons();
			}
			setPendingTrades(mr.pendingTrades());
			
			drawMRP = true;
			mr.addChart(this);
		}
	}
	
	public void enableTradeButtons() {
		this.tradeButs.close().enable();
		this.tradeButs.cancelTP().enable();
		this.tradeButs.cancelSL().enable();
		this.tradeButs.sl().enable();
		this.tradeButs.tp().enable();
		this.tradeButs.setSL().enable();
		this.tradeButs.setTP().enable();
	}
	
	public void disableTradeButtons() {
		this.tradeButs.close().disable();
		this.tradeButs.cancelTP().disable();
		this.tradeButs.cancelSL().disable();
		this.tradeButs.sl().disable();
		this.tradeButs.tp().disable();
		this.tradeButs.setSL().disable();
		this.tradeButs.setTP().disable();
	}
	
	public MarketReplay marketReplay() {
		return mr;
	}
	
	public TradeButtons tradeButtons() {
		return tradeButs;
	}
	
	private double tradeVolume() {
		CanvasNumberChooser[] c = {volTens, volUnits};
		return CanvasNumberChooser.number(c);
	}
	
	public void disableReplayMode() {
		if (this.replayMode) {
			this.replayMode = false;
			mr.removeChart(this);
			this.mr = null;
			this.mrp = null;
			menu.setReplayMode(false);
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
			if (c.name().equals(name)) {
				c.draw();
			}
		}
	}
	
	public void onMouseExited(MouseEvent e) {
		hsb.onMouseExited(e);
		focusedChart.set(false);
		focusedOnChart.set(false);
		drawPending = false;
		drawCharts(this.name());
	}
	
	public void onMouseEntered() {
		focusedChart.set(true);
		CrossHair.setIsForCandle(drawCandlesticks.get());
		CrossHair.setDateIndex(0);
		CrossHair.setName(data.name());
		drawCharts(this.name());
	}
	
	private void tradeButtonHoverChecks(double x, double y) {
		ButtonChecks.mouseButtonHoverCheck(buy, x, y);
		ButtonChecks.mouseButtonHoverCheck(sell, x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.sl(), x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.setSL(), x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.tp(), x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.setTP(), x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.cancelSL(), x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.cancelTP(), x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.close(), x, y);
		for (PendingTrade p : pendingTrades) {
			for (CanvasButton c : p.pTradeButs().buttons()) {
				ButtonChecks.mouseButtonHoverCheck(c, x, y);
			}
		}
		ButtonChecks.mouseNumberChooserDownHoverCheck(volTens, x, y);
		ButtonChecks.mouseNumberChooserUpHoverCheck(volTens, x, y);
		ButtonChecks.mouseNumberChooserDownHoverCheck(volUnits, x, y);
		ButtonChecks.mouseNumberChooserUpHoverCheck(volUnits, x, y);
	}
	
	public void onMouseMoved(MouseEvent e) {
		if (CrossHair.dateIndex().get() >= data.m1CandlesDataSize(replayMode).get() && drawCandlesticks.get()) {
			CrossHair.setDateIndex(0);
		}
		if (!chartDateMarginDragging && !priceDragging) {
			stage.getScene().setCursor(Cursor.DEFAULT);
		}
		hsb.onMouseMoved(e);
		if (!limitDragging && !stopDragging) {
			CrossHair.setX(e.getX());
			CrossHair.setY(e.getY());
		}
		if (!onChart(e.getX(), e.getY(), true)) {
			measuring = false;
			if (!limitDragging && !stopDragging) {
				drawPending = false;
			}
			if (e.getX() >= CHT_MARGIN + chartWidth && e.getX() <= CHT_MARGIN + chartWidth + PRICE_MARGIN && e.getY() <= height - HSB_HEIGHT - CHT_MARGIN) {
				stage.getScene().setCursor(Cursor.N_RESIZE);
			}
		} else {
			if (replayMode && !limitDragging && !stopDragging) {				
				limitOrder.setY(e.getY() - fontSize/2); 
				stopOrder.setY(e.getY() - fontSize/2);
				ButtonChecks.mouseButtonHoverCheck(limitOrder, e.getX(), e.getY());
				ButtonChecks.mouseButtonHoverCheck(stopOrder, e.getX(), e.getY());
				drawPending = true;	
			}
			if (e.getY() >= chartHeight + CHT_MARGIN - fontSize) {
				stage.getScene().setCursor(Cursor.E_RESIZE);
			}
		}
		if (replayMode) {
			tradeButtonHoverChecks(e.getX(), e.getY());
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
	
	private boolean tradeButtonPressChecks(double x, double y) {
		boolean pressed = false;
		if (sell.onNode(x, y) && sell.enabled()) {
			sell.setPressed(true);
			pressed = true;
		} else if (buy.onNode(x, y) && buy.enabled()) {
			buy.setPressed(true);
			pressed = true;
		} else if (tradeButs.tp().onNode(x, y) && tradeButs.tp().enabled()) {
			tradeButs.tp().setPressed(true);
			tpDragging = true;
			pressed = true;
		} else if (tradeButs.sl().onNode(x, y) && tradeButs.sl().enabled()) {
			tradeButs.sl().setPressed(true);
			slDragging = true;
			pressed = true;
		} else if (tradeButs.setTP.onNode(x, y) && tradeButs.setTP.enabled()) {
			tradeButs.setTP.setPressed(true);
			tpDragging = true;
			pressed = true;
		} else if (tradeButs.setSL.onNode(x, y) && tradeButs.setSL.enabled()) {
			tradeButs.setSL.setPressed(true);
			slDragging = true;
			pressed = true;
		} else if (tradeButs.cancelTP().onNode(x, y) && tradeButs.cancelTP().enabled()) {
			tradeButs.cancelTP().setPressed(true);
			pressed = true;
		} else if (tradeButs.cancelSL().onNode(x, y) && tradeButs.cancelSL().enabled()) {
			tradeButs.cancelSL().setPressed(true);
			pressed = true;
		} else if (tradeButs.close.onNode(x, y) && tradeButs.close.enabled()) {
			tradeButs.close.setPressed(true);
			pressed = true;
		} else if (volTens.onUp(x, y) && volTens.enabled()) {
			volTens.setUpPressed(true);
			pressed = true;
		} else if (volTens.onDown(x, y) && volTens.enabled()) {
			volTens.setDownPressed(true);
			pressed = true;
		} else if (volUnits.onUp(x, y) && volUnits.enabled()) {
			volUnits.setUpPressed(true);
			pressed = true;
		} else if (volUnits.onDown(x, y) && volUnits.enabled()) {
			volUnits.setDownPressed(true);
			pressed = true;
		} else if (limitOrder.onNode(x, y) && limitOrder.enabled()) {
			limitOrder.setPressed(true);
			limitDragging = true;			
			boolean buy = true;
			double currentPrice = tickData().get(data.tickDataSize(true).get() - 1).price();
			double crossHairPrice = roundToNearestTick(yCoordToPrice(y));
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = false;
				}
				penTrade = new PendingTrade(true, buy, crossHairPrice, tradeVolume(), this);
			}	
			pressed = true;
		} else if (stopOrder.onNode(x, y) && stopOrder.enabled()) {
			stopOrder.setPressed(true);
			stopDragging = true;	
			boolean buy = false;
			double currentPrice = tickData().get(data.tickDataSize(true).get()).price();
			double crossHairPrice = roundToNearestTick(yCoordToPrice(y));
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = true;
				}
				penTrade = new PendingTrade(false, buy, crossHairPrice, tradeVolume(), this);
			}	
			pressed = true;
		} else {
			for (PendingTrade p : pendingTrades) {
				if (p.pTradeButs().order.onNode(x, y) && p.pTradeButs().order.enabled()) {
					p.pTradeButs().order.setPressed(true);
					penOrderBeingDragged = p;
					penOrderDragging = true;
					pressed = true;
					break;
				} else if (p.pTradeButs().setTP.onNode(x, y) && p.pTradeButs().setTP.enabled()) {
					p.pTradeButs().setTP.setPressed(true);
					penOrderBeingDragged = p;
					tpDragging = true;
					pressed = true;
					break;
				} else if (p.pTradeButs().setSL.onNode(x, y) && p.pTradeButs().setSL.enabled()) {
					p.pTradeButs().setSL.setPressed(true);
					penOrderBeingDragged = p;
					slDragging = true;
					pressed = true;
					break;
				} else if (p.pTradeButs().close.onNode(x, y) && p.pTradeButs().close.enabled()) {
					p.pTradeButs().close.setPressed(true);
					penOrderBeingDragged = p;
					pressed = true;
					break;
				}
			}
		}
		return pressed;
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
			if (e.getX() >= CHT_MARGIN + chartWidth && e.getX() <= CHT_MARGIN + chartWidth + PRICE_MARGIN && e.getY() <= chartHeight + CHT_MARGIN) {
				priceDragging = true;
				priceInitPos = e.getY();
			}
			if (onChart(e.getX(), e.getY(), true)) {
				chartInitPos = e.getX();
				if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100) {
					fireMRPEvent(MouseEvent.MOUSE_PRESSED, e);
				} else if (!replayMode || !tradeButtonPressChecks(e.getX(), e.getY())) {
					if (onDateMargin(e.getX(), e.getY())) {
						chartDateMarginDragging = true;
					} else {
						chartDragging = true;
					}				
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
	
	private void tradeButtonReleaseChecks(double x, double y) {
		if (sell.pressed()) {
			sell.setPressed(false);
			if (sell.onNode(x, y)) {				
				if (mr.trade().closed()) {
					mr.setTrade(new Trade(data, data.tickDataSize(true).get() - 1, false, tradeVolume()));
					mr.setSlPrice(-1);
					mr.setTpPrice(-1);
					for (Chart c : charts) {
						if (c.mr == null || !c.mr.equals(mr)) {
							continue;
						}
						c.enableTradeButtons();
					}
				} else {
					if (mr.trade().buy()) {
						mr.trade().scaleOut(tradeVolume(), data.tickDataSize(true).get() - 1);
						mr.closedTradeProc();
						if (mr.trade().closed()) {
							mr.setSlPrice(-1);
							mr.setTpPrice(-1);
							for (Chart c : charts) {
								if (c.mr == null || !c.mr.equals(mr)) {
									continue;
								}
								c.disableTradeButtons();
							}
						}
					} else {
						mr.trade().scaleIn(tradeVolume(), data.tickDataSize(true).get() - 1);
					}
				}
				for (Chart c : charts) {
					if (c.mr == null || !c.mr.equals(mr)) {
						continue;
					}
					c.tradeButs.sl().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.slPrice().get()));
					c.tradeButs.tp().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.tpPrice().get()));
				}	
			}
		} else if (buy.pressed()) {
			buy.setPressed(false);
			if (buy.onNode(x, y)) {				
				if (mr.trade().closed()) {
					mr.setTrade(new Trade(data, data.tickDataSize(true).get() - 1, true, tradeVolume()));
					mr.setSlPrice(-1);
					mr.setTpPrice(-1);
					for (Chart c : charts) {
						if (c.mr == null || !c.mr.equals(mr)) {
							continue;
						}
						c.enableTradeButtons();
					}
				} else {
					if (mr.trade().buy()) {
						mr.trade().scaleIn(tradeVolume(), data.tickDataSize(true).get() - 1);
					} else {
						mr.trade().scaleOut(tradeVolume(), data.tickDataSize(true).get() - 1);
						mr.closedTradeProc();
						if (mr.trade().closed()) {
							mr.setSlPrice(-1);
							mr.setTpPrice(-1);
							for (Chart c : charts) {
								if (c.mr == null || !c.mr.equals(mr)) {
									continue;
								}
								c.disableTradeButtons();
							}
						}
					}
				}
				for (Chart c : charts) {
					if (c.mr == null || !c.mr.equals(mr)) {
						continue;
					}
					c.tradeButs.sl().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.slPrice().get()));
					c.tradeButs.tp().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.tpPrice().get()));
				}	
			}			
		} else if (volTens.upPressed()) {
			volTens.setUpPressed(false);
			if (volTens.onUp(x, y)) {
				volTens.incrementValue();
				if (tradeVolume() == 0) {
					volUnits.incrementValue();
				}
			}			
		} else if (volTens.downPressed()) {
			volTens.setDownPressed(false);
			if (volTens.onDown(x, y)) {
				volTens.decrementValue();
				if (tradeVolume() == 0) {
					volUnits.incrementValue();
				}
			}			
		} else if (volUnits.upPressed()) {
			volUnits.setUpPressed(false);
			if (volUnits.onUp(x, y)) {
				volUnits.incrementValue();
				if (tradeVolume() == 0) {
					volUnits.incrementValue();
				}
			}			
		} else if (volUnits.downPressed()) {
			volUnits.setDownPressed(false);
			if (volUnits.onDown(x, y)) {
				volUnits.decrementValue();
				if (tradeVolume() == 0) {
					volUnits.incrementValue();
				}
			}			
		} else {
			for (PendingTrade p : pendingTrades) {
				if (p.pTradeButs().close.pressed()) {
					p.pTradeButs().close.setPressed(false);
					if (p.pTradeButs().close.onNode(x, y)) {
						int i = pendingTrades.indexOf(penOrderBeingDragged);
						for (Chart c : charts) {
							if (c.mr == null || !c.mr.equals(mr)) {
								continue;
							}							
							c.pendingTrades.remove(i);							
							if (mr.trade().closed() && pendingTrades.size() == 1) {
								c.tradeButs.sl().setText(p.volume() + "  $" + Trade.hypotheticalProfit2(p.price(), roundToNearestTick(yCoordToPrice(y)), p.buy(), p.volume()));
								c.tradeButs.tp().setText(p.volume() + "  $" + Trade.hypotheticalProfit2(p.price(), roundToNearestTick(yCoordToPrice(y)), p.buy(), p.volume()));
							}
						}						
						mr.setPendingTrades(pendingTrades);
						penOrderBeingDragged = null;
						if (mr.trade().closed() && pendingTrades.isEmpty()) {
							mr.setSlPrice(-1);
							mr.setTpPrice(-1);
						}
					}
					break;
				} else if (p.pTradeButs().setSL.pressed()) {
					p.pTradeButs().setSL.setPressed(false);
					break;
				} else if (p.pTradeButs().setTP.pressed()) {
					p.pTradeButs().setTP.setPressed(false);
					break;
				}
			}					
			
			if (penOrderBeingDragged != null) {
				penOrderBeingDragged.pTradeButs().order.setPressed(false);
				penOrderDragging = false;
				
				if (mr.trade().closed()) {		
					if (penOrderBeingDragged.buy()) {
						if (mr.slPrice().get() >= penOrderBeingDragged.price()) {
							mr.setSlPrice(-1);
						}
						if (mr.tpPrice().get() <= penOrderBeingDragged.price()) {
							mr.setTpPrice(-1);
						}
					} else {
						if (mr.slPrice().get() <= penOrderBeingDragged.price()) {
							mr.setSlPrice(-1);
						}
						if (mr.tpPrice().get() >= penOrderBeingDragged.price()) {
							mr.setTpPrice(-1);
						}
					}
				}												
			}
		}
		
		if (!mr.trade().closed() || !pendingTrades.isEmpty()) {
			if (tradeButs.tp().pressed()) {
				tradeButs.tp().setPressed(false);
			} else if (tradeButs.sl().pressed()) {
				tradeButs.sl().setPressed(false);
			} else if (tradeButs.cancelTP().pressed()) {
				tradeButs.cancelTP().setPressed(false);
				if (tradeButs.cancelTP().onNode(x, y)) {
					mr.trade().cancelTP();
					mr.setTpPrice(-1);
				}				
			} else if (tradeButs.cancelSL().pressed()) {
				tradeButs.cancelSL().setPressed(false);
				if (tradeButs.cancelSL().onNode(x, y)) {
					mr.trade().cancelSL();
					mr.setSlPrice(-1);
				}				
			}
		}
		
		if (!mr.trade().closed()) {
			if (tradeButs.setTP.pressed()) {			
				tradeButs.setTP.setPressed(false);
			} else if (tradeButs.setSL.pressed()) {			
				tradeButs.setSL.setPressed(false);
			} else if (tradeButs.close.pressed()) {
				tradeButs.close.setPressed(false);
				if (tradeButs.close.onNode(x, y)) {
					mr.trade().close(data.tickDataSize(true).get() - 1);
					mr.closedTradeProc();
					for (Chart c : charts) {
						if (c.mr == null || !c.mr.equals(mr)) {
							continue;
						}
						c.disableTradeButtons();
					}
					mr.setTpPrice(-1);
					mr.setSlPrice(-1);
				}				
			} 
			
			if (slDragging) {		
				if (!mr.trade().closed()) {
					mr.trade().setSL(mr.slPrice().get());
					mr.setSlPrice(mr.trade().sl());
				}
				for (Chart c : charts) {
					if (c.mr == null || !c.mr.equals(mr)) {
						continue;
					}
					c.tradeButs.sl().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.slPrice().get()));
					c.tradeButs.tp().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.tpPrice().get()));
				}	
			} else if (tpDragging) {
				if (!mr.trade().closed()) {
					mr.trade().setTP(mr.tpPrice().get());
					mr.setTpPrice(mr.trade().tp());
				}
				for (Chart c : charts) {
					if (c.mr == null || !c.mr.equals(mr)) {
						continue;
					}
					c.tradeButs.sl().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.slPrice().get()));
					c.tradeButs.tp().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(mr.tpPrice().get()));
				}	
			} 		
		}
		
		limitOrder.setPressed(false);
		stopOrder.setPressed(false);
		if (penTrade != null) {
			if (limitDragging || stopDragging) {	
				orderReleasedStuff(x, y);
				for (Chart c : charts) {
					if (c.mr == null || !c.mr.equals(mr)) {
						continue;
					}										
					c.pendingTrades.add(new PendingTrade(penTrade.limit(), penTrade.buy(), penTrade.price(), penTrade.volume(), c));
					if (pendingTrades.size() > 1) {
						if (mr.trade().closed()) {
							c.tradeButs.tp().setText("TP");
							c.tradeButs.sl().setText("SL");
						}
					}
				}	
				mr.setPendingTrades(pendingTrades);
			}			
		}
		limitDragging = false;
		stopDragging = false;
		penTrade = null;
		slDragging = false;
		tpDragging = false;		
		penOrderBeingDragged = null;
	}
	
	private void orderReleasedStuff(double x, double y) {
		CrossHair.setX(x);
		CrossHair.setY(y);
		limitOrder.setY(y - fontSize/2); 
		stopOrder.setY(y - fontSize/2);
		ButtonChecks.mouseButtonHoverCheck(limitOrder, x, y);
		ButtonChecks.mouseButtonHoverCheck(stopOrder, x, y);
		if (onChart(x, y, true)) {
			drawPending = true;	
		} else {
			drawPending = false;
		}
	}
	
	public static void toggleDarkMode() {
		darkMode.set(!darkMode.get());
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
		/*if (newCHT_BTN_Pressed && checkNewChtBtn(e.getX(), e.getY())) {
			newCHT_BTN_Pressed = false;
			Stage s = new Stage();
			ChartPane c = new ChartPane(s, width, height, data, replayMode, mr, mrp);			
			Scene scene = new Scene(c);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		} else if (drawCandlesticksPressed && checkChartTypeBtn(e.getX(), e.getY())) {						
			drawCandlesticksPressed = false;
			double newHSBPos;
			if (drawCandlesticks) {
				drawCandlesticks = false;
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
				drawCandlesticks = true;	
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
		} else if (darkModePressed && checkDarkModeBtn(e.getX(), e.getY())) {
			darkModePressed = false;	
			toggleDarkMode();
		} else if (drawMRPPressed && checkDrawMRPBtn(e.getX(), e.getY())) {
			drawMRPPressed = false;
			drawMRP = !drawMRP;
		} else*/ if (measuring) {
			measuring = false;
			stage.getScene().cursorProperty().set(Cursor.DEFAULT);
		} else if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100) {
			fireMRPEvent(MouseEvent.MOUSE_RELEASED, e);
		} else if (replayMode) {
			tradeButtonReleaseChecks(e.getX(), e.getY());
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
	
	private void updatePendingTrade(PendingTrade pent, double currentPrice, double crossHairPrice) {
		if (pent == null) {
			return;
		}
		if (pent.limit()) {		
			boolean buy = true;			
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = false;
				}
				pent.setBuy(buy);
				pent.setPrice(crossHairPrice);
			}	
		} else {	
			boolean buy = false;			
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = true;
				}
				pent.setBuy(buy);
				pent.setPrice(crossHairPrice);
			}	
		}
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
		if (tpDragging) {
			mr.setTpPrice(roundToNearestTick(yCoordToPrice(e.getY())));
			for (Chart c : charts) {
				if (c.mr == null || !c.mr.equals(mr)) {
					continue;
				}
				if (mr.trade().closed()) {
					if (pendingTrades.size() == 1) {
						PendingTrade p = pendingTrades.get(0);
						c.tradeButs.tp().setText(p.volume() + "  $" + Trade.hypotheticalProfit2(p.price(), roundToNearestTick(yCoordToPrice(e.getY())), p.buy(), p.volume()));
					} else {
						c.tradeButs.tp().setText("TP");
						c.tradeButs.sl().setText("SL");
					}
				} else {
					c.tradeButs.tp().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(roundToNearestTick(yCoordToPrice(e.getY()))));
				}				
			}
		}
		if (slDragging) {
			mr.setSlPrice(roundToNearestTick(yCoordToPrice(e.getY())));
			for (Chart c : charts) {
				if (c.mr == null || !c.mr.equals(mr)) {
					continue;
				}
				if (mr.trade().closed()) {
					if (pendingTrades.size() == 1) {
						PendingTrade p = pendingTrades.get(0);
						c.tradeButs.sl().setText(p.volume() + "  $" + Trade.hypotheticalProfit2(p.price(), roundToNearestTick(yCoordToPrice(e.getY())), p.buy(), p.volume()));
					} else {
						c.tradeButs.sl().setText("SL");
						c.tradeButs.tp().setText("TP");
					}
				} else {
					c.tradeButs.sl().setText(mr.trade().volume() + "  $" + mr.trade().hypotheticalProfit(roundToNearestTick(yCoordToPrice(e.getY()))));
				}				
			}
		}
		if (limitDragging || stopDragging) {
			double currentPrice = tickData().get(data.tickDataSize(true).get()).price();
			double crossHairPrice = roundToNearestTick(yCoordToPrice(e.getY()));
			updatePendingTrade(penTrade, currentPrice, crossHairPrice);
		}
		if (penOrderDragging) {
			double currentPrice = tickData().get(data.tickDataSize(true).get()).price();
			double crossHairPrice = roundToNearestTick(yCoordToPrice(e.getY()));
			int i = pendingTrades.indexOf(penOrderBeingDragged);
			for (Chart c: charts) {
				if (c.mr == null || !c.mr.equals(mr)) {
					continue;
				}
				c.updatePendingTrade(c.pendingTrades.get(i), currentPrice, crossHairPrice);
			}
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
		double multiplier = 1;
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
		double multiplier = 1;
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
				gc.fillRect(chartWidth + CHT_MARGIN, y - fontSize/2, PRICE_MARGIN, fontSize);
				gc.setStroke(Color.WHITE);
				gc.strokeText(((Double)(roundToNearestTick(l.price()))).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, y + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
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
			gc.setStroke(Color.CORNFLOWERBLUE);
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.close() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.close()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.close() - candle.open()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.close() - candle.low()) / conversionVar);
			gc.setFill(Color.CORNFLOWERBLUE);
			gc.fillRect(xPos, yPos, candlestickWidth - num, (candle.close() - candle.open()) / conversionVar - num);
		} else {
			gc.setStroke(Color.ORANGE);
			gc.strokeRect(xPos, yPos, candlestickWidth, (candle.open() - candle.close()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos, xPos + candlestickWidth / 2, yPos - (candle.high() - candle.open()) / conversionVar);
			gc.strokeLine(xPos + candlestickWidth / 2, yPos + (candle.open() - candle.close()) / conversionVar, xPos + candlestickWidth / 2, yPos + (candle.open() - candle.low()) / conversionVar);
			gc.setFill(Color.ORANGE);
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
			if (endIndex >= data.tickDataSize(false).get()) {
				endIndex = data.tickDataSize(false).get() - 1;
			} else if (replayMode && endIndex >= data.tickDataSize(true).get()) {
				endIndex = data.tickDataSize(true).get();
			}
		}
	}
	
	private void drawFrame() {
		gc.clearRect(0, 0, width, height);		
		if (darkMode.get()) {			
			gc.setFill(Color.BLACK);
			gc.fillRect(0, 0, width, height);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		gc.strokeRect(CHT_MARGIN, CHT_MARGIN, chartWidth, chartHeight);
		gc.strokeRect(CHT_MARGIN + chartWidth, CHT_MARGIN + chartHeight, PRICE_MARGIN, HSB_HEIGHT + CHT_MARGIN);
		/*if (replayMode) {
			gc.strokeLine(width - PRICE_MARGIN * 3 / 4 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN * 3 / 4 - 1, height);
			gc.strokeLine(width - PRICE_MARGIN * 2 / 4 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN * 2 / 4 - 1, height);
			gc.strokeLine(width - PRICE_MARGIN / 4 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN / 4 - 1, height);
		} else {
			gc.strokeLine(width - PRICE_MARGIN / 3 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN / 3 - 1, height);
			gc.strokeLine(width - PRICE_MARGIN * 2 / 3 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN * 2 / 3 - 1, height);
		}*/	
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
		if (darkMode.get()) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
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
		gc.fillRect(chartWidth + CHT_MARGIN, yPos - fontSize/2, PRICE_MARGIN, fontSize);
		gc.setStroke(Color.WHITE);
		gc.strokeText(((Double)(price)).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, yPos + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
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
			gc.strokeText(((Double)(Round.round(lowest + (diff * i), numDecimalPts + 1))).toString(), pricePos, index + pricePosYMargin, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN * 2);
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
	
	private void drawTradeButtons() {
		buy.draw();
		sell.draw();
		volTens.draw();
		volUnits.draw();
	}
	
	public double yCoordToPrice(double y) {
		return ((((chartHeight - (chtDataMargin*2)) - (y - CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest;
	}
	
	public double priceToYCoord(double price) {
		return ((highest + dataMarginTickSize - price) / (range + dataMarginTickSize * 2)) * chartHeight + CHT_MARGIN;
	}
	
	private void drawPriceBox(double yPos, double price, Color textColour, Color boxColour) {
		gc.setStroke(textColour);
		gc.setFill(boxColour);
		gc.fillRect(chartWidth + CHT_MARGIN, yPos - fontSize/2, PRICE_MARGIN, fontSize);
		gc.strokeText(((Double)(roundToNearestTick(price))).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, yPos + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
	}
	
	public void drawTradeBox(double xPos, double yPos, double width, double textMaxWidth, double textMargin, String text, Color textColour, Color boxColour) {
		if (darkMode.get()) {
			gc.setFill(Color.BLACK);
		} else {
			gc.setFill(Color.WHITE);
		}		
		gc.fillRect(xPos, yPos, width, fontSize * 2);
		gc.setStroke(boxColour);
		gc.strokeRect(xPos, yPos, width, fontSize * 2);
		gc.setStroke(textColour);	
		gc.strokeText(text, xPos + textMargin, yPos + 4*fontSize/3, textMaxWidth);
	}
	
	private void drawPendingTrades() {
		if (penTrade != null) {
			pendingTrades.add(penTrade);
		}
		double x1 = CHT_MARGIN + chartWidth / 2;
		double x2 = CHT_MARGIN + chartWidth;
		if (mr.trade().closed()) {
			double slY = priceToYCoord(mr.slPrice().get());
			double tpY = priceToYCoord(mr.tpPrice().get());			
			if (onChart(CHT_MARGIN + 1, tpY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, tpY - fontSize - 3, false)) {
				gc.setStroke(Color.CORNFLOWERBLUE);
				gc.strokeLine(x1, tpY, x2, tpY);
				drawPriceBox(tpY, mr.tpPrice().get(), Color.WHITE, Color.CORNFLOWERBLUE);
				tradeButs.tp().enable();
				tradeButs.cancelTP().enable();
				tradeButs.tp().setY(tpY - fontSize);
				tradeButs.cancelTP().setY(tpY - fontSize);
				if (mr.tpPrice().get() != -1) {
					tradeButs.tp().draw();
				}				
				tradeButs.cancelTP().draw();
			} else {
				tradeButs.tp().disable();
				tradeButs.cancelTP().disable();
			}
			if (onChart(CHT_MARGIN + 1, slY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, slY - fontSize - 3, false)) {
				gc.setStroke(Color.ORANGE);
				gc.strokeLine(x1, slY, x2, slY);
				drawPriceBox(slY, mr.slPrice().get(), Color.WHITE, Color.ORANGE);
				tradeButs.sl().enable();
				tradeButs.cancelSL().enable();
				tradeButs.sl().setY(slY - fontSize);
				tradeButs.cancelSL().setY(slY - fontSize);
				if (mr.slPrice().get() != -1) {
					tradeButs.sl().draw();
				}
				tradeButs.cancelSL().draw();
			} else {
				tradeButs.sl().disable();
				tradeButs.cancelSL().disable();
			}
		}
		for (PendingTrade trade : pendingTrades) {	
			double entryY = priceToYCoord(roundToNearestTick(trade.price()));				
			if (onChart(CHT_MARGIN + 1, entryY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, entryY - fontSize - 3, false)) {
				Color boxColour = Color.GRAY;
				gc.setStroke(Color.GRAY);
				gc.strokeLine(x1, entryY, x2, entryY);
				
				drawPriceBox(entryY, roundToNearestTick(trade.price()), Color.WHITE, boxColour);
				trade.pTradeButs().order.setY(priceToYCoord(trade.price()) - fontSize);
				trade.pTradeButs().order.draw();						
				if (mr.trade().closed()) {
					trade.pTradeButs().setSL.enable();
					trade.pTradeButs().setTP.enable();
					trade.pTradeButs().setSL.setY(entryY - fontSize);
					trade.pTradeButs().setTP.setY(entryY - fontSize);
					trade.pTradeButs().setSL.draw();
					trade.pTradeButs().setTP.draw();
				} else {
					trade.pTradeButs().setSL.disable();
					trade.pTradeButs().setTP.disable();
				}
				trade.pTradeButs().close.setY(entryY - fontSize);				
				trade.pTradeButs().close.draw();
			}	
		}	
		if (penTrade != null) {
			pendingTrades.remove(penTrade);
		}
	}
	
	private void drawTrade() {
		if (mr.trade() == null || mr.trade().closed()) {
			return;
		}
		double x1 = CHT_MARGIN + chartWidth / 2;
		double x2 = CHT_MARGIN + chartWidth;	
		
		double entryY = priceToYCoord(roundToNearestTick(mr.trade().entryPrice()));
		double slY = priceToYCoord(mr.slPrice().get());
		double tpY = priceToYCoord(mr.tpPrice().get());
		if (onChart(CHT_MARGIN + 1, tpY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, tpY - fontSize - 3, false)) {
			gc.setStroke(Color.CORNFLOWERBLUE);
			gc.strokeLine(x1, tpY, x2, tpY);
			drawPriceBox(tpY, mr.tpPrice().get(), Color.WHITE, Color.CORNFLOWERBLUE);
			tradeButs.tp().setY(tpY - fontSize);
			tradeButs.cancelTP().setY(tpY - fontSize);
			tradeButs.tp().draw();
			tradeButs.cancelTP().draw();
		}
		if (onChart(CHT_MARGIN + 1, slY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, slY - fontSize - 3, false)) {
			gc.setStroke(Color.ORANGE);
			gc.strokeLine(x1, slY, x2, slY);
			drawPriceBox(slY, mr.slPrice().get(), Color.WHITE, Color.ORANGE);
			tradeButs.sl().setY(slY - fontSize);
			tradeButs.cancelSL().setY(slY - fontSize);
			tradeButs.sl().draw();
			tradeButs.cancelSL().draw();
		}
		if (onChart(CHT_MARGIN + 1, entryY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, entryY - fontSize - 3, false)) {
			Color boxColour;
			Color textColour;
			if (mr.trade().buy()) {
				boxColour = Color.FORESTGREEN;
				gc.setStroke(Color.FORESTGREEN);
			} else {
				boxColour = Color.RED;
				gc.setStroke(Color.RED);
			}
			double profit = mr.trade().profit();
			if (profit > 0) {
				textColour = Color.FORESTGREEN;
			} else if (profit < 0) {
				textColour = Color.RED;
			} else {
				textColour = Color.GRAY;
			}
			gc.strokeLine(x1, entryY, x2, entryY);
			
			drawPriceBox(entryY, mr.trade().entryPrice(), Color.WHITE, boxColour);
			drawTradeBox(x1 - 100, entryY - fontSize, 100, 90, 5, ((Double)(mr.trade().volume())).toString() + "\t$" + ((Double)(mr.trade().profit())).toString(), textColour, boxColour);
			tradeButs.setSL.setY(entryY - fontSize);
			tradeButs.setTP.setY(entryY - fontSize);
			tradeButs.close.setY(entryY - fontSize);
			tradeButs.setSL.draw();
			tradeButs.setTP.draw();
			tradeButs.close.draw();
		}	
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
		checkDrawLines();										
		checkMeasuring();			
		if (replayMode) {				
			drawCurrentPriceLine();
			drawTradeButtons();			
			drawPendingTrades();
			drawTrade();
			drawCurrentPriceBox();
			if (drawPending) {
				limitOrder.draw();
				stopOrder.draw();
			}
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
	
	public TNode<CanvasNode> menuNode() {
		return menuNode;
	}
	
	@Override
	public Tree<CanvasNode> sceneGraph() {
		return sceneGraph;
	}

	@Override
	public TNode<CanvasNode> lastNode() {
		return lastNode;
	}

	@Override
	public void setLastNode(TNode<CanvasNode> lastNode) {
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
