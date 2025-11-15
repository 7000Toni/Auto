import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

public class Chart implements ScrollBarOwner, Drawable {
	public final static double CNDL_MOVE_COEF = 0.001;
	public final static int CNDL_INDX_MOVE_COEF = 2;
	
	public final static double TICK_MOVE_COEF = 0.001;
	public final static int TICK_INDX_MOVE_COEF = 3;
	
	public final static double HSB_WIDTH = 100;
	public final static double HSB_HEIGHT = 10;
	
	public final static double PRC_MSRMNT_LENGTH = 100;
	
	public final static double LINE_PRESS_MARGIN = 5;
	
	//TODO consider a function to calculate the price given a y coordinate
	public final static double WIDTH_EXTRA = 16;
	public final static double HEIGHT_EXTRA = 39;
	
	public final static double MIN_WIDTH = 640; 
	public final static double MIN_HEIGHT = 360; 
	
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
	private static BooleanProperty focusedOnChart = new SimpleBooleanProperty(false);	
	private static boolean darkMode = false;
	
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
	private int lastTick = 0;
	
	//ChartButton
	private boolean newCHT_BTN_Hover = false;
	private boolean drawCandlesticksHover = false;
	private boolean darkModeHover = false;
	private boolean drawMRPHover = false;
	private boolean newCHT_BTN_Clicked = false;
	private boolean drawCandlesticksClicked = false;
	private boolean darkModeClicked = false;
	private boolean drawMRPClicked = false;
	private CanvasButton buy;
	private CanvasButton sell;
	private CanvasNumberChooser volUnits;
	private CanvasNumberChooser volTens;
	
	//TradeStuff
	private static Trade trade = null; 
	private TradeButtons tradeButs;
	private CanvasButton limitOrder;
	private CanvasButton stopOrder;
	private boolean slDragging = false;
	private boolean tpDragging = false;
	private static double slPrice = -1;
	private static double tpPrice = -1;
	private boolean drawPending = false;
	private boolean limitDragging = false;
	private boolean stopDragging = false;
	private PendingTrade penTrade = null;
	private ArrayList<PendingTrade> pendingTrades;
	private boolean writeToFile = false;
	private PendingTrade penOrderBeingDragged = null;
	private boolean penOrderDragging = false;
	
	//ChartActions
	private static int lineHighlighted = -1;
	private boolean lineDragging = false;
	private boolean rightPressed = false;
	private boolean measuring = false;
	private double startPrice = 0;
	private double startX = 0;
	private double startY = 0;
	private double endX = 0;
	private double endY = 0;
	
	//Candlestick static variables
	private boolean drawCandlesticks = false;
	private double candlestickWidth;
	private double candlestickSpacing;
	private int numCandlesticks;
	
	private class PendingTrade {
		boolean limit;
		boolean buy;
		double price;
		double volume;
		PendingTradeButtons pTradeButs;
		
		public PendingTrade(boolean limit, boolean buy,	double price, double volume) {
			this.limit = limit;
			this.buy = buy;
			this.price = price;
			this.volume = volume;
			this.pTradeButs = new PendingTradeButtons();
			this.pTradeButs.order = new CanvasButton(gc, 100, fontSize*2, CHT_MARGIN + chartWidth / 2 - 100, 0, "", 5, fontSize/3);
			this.pTradeButs.close = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3);			
			this.pTradeButs.setSL = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 + 10, 0, "SL", 6, fontSize/3);
			this.pTradeButs.setTP = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 + 20 + fontSize*2, 0, "TP", 6, fontSize/3);
			this.pTradeButs.order.setVanGogh(orderVG(pTradeButs.order, this));
			this.pTradeButs.close.setVanGogh(closeVG(pTradeButs.close));			
			this.pTradeButs.setSL.setVanGogh(setSlVG(pTradeButs.setSL));
			this.pTradeButs.setTP.setVanGogh(setTpVG(pTradeButs.setTP));
			String text = "STOP";
			if (limit) {
				text = "LIMIT";
			}
			pTradeButs.order.setText(((Double)(trade.volume)).toString() + '\t' + text);
		}
	}
	
	private class PendingTradeButtons {
		CanvasButton order;
		CanvasButton close;
		CanvasButton setSL;
		CanvasButton setTP;
		
		public ArrayList<CanvasButton> buttons() {
			ArrayList<CanvasButton> b = new ArrayList<CanvasButton>();
			b.add(order);
			b.add(close);
			b.add(setSL);
			b.add(setTP);
			return b;
		}
	}
	
	private class TradeButtons extends PendingTradeButtons {
		CanvasButton cancelTP;
		CanvasButton cancelSL;
		CanvasButton sl;
		CanvasButton tp;
		
		@Override
		public ArrayList<CanvasButton> buttons() {
			ArrayList<CanvasButton> b = new ArrayList<CanvasButton>();
			b.add(close);
			b.add(cancelTP);
			b.add(cancelSL);
			b.add(sl);
			b.add(tp);
			b.add(setSL);
			b.add(setTP);
			return b;
		}
	}
	
	private ButtonVanGogh buyVG = (x, y, gc) -> {
		gc.setStroke(Color.WHITE);
		gc.setFill(Color.DODGERBLUE);
		if (buy.hover) {
			gc.setFill(Color.STEELBLUE);
		}
		if (buy.pressed) {
			gc.setFill(Color.DARKBLUE);
		}
		if (!buy.enabled) {
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.fillRect(x, y, buy.width, buy.height);
		gc.strokeText(buy.text, x + buy.textXOffset, y + buy.textYOffset);
	};
	
	private ButtonVanGogh sellVG = (x, y, gc) -> {
		gc.setStroke(Color.WHITE);
		gc.setFill(Color.ORANGERED);
		if (sell.hover) {
			gc.setFill(Color.INDIANRED);
		}
		if (sell.pressed) {
			gc.setFill(Color.DARKRED);
		}
		if (!sell.enabled) {
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.fillRect(x, y, sell.width, sell.height);
		gc.strokeText(sell.text, x + sell.textXOffset, y + sell.textYOffset);
	};
	
	private ButtonVanGogh closeVG(CanvasButton close) {
		return (x, y, gc) -> {
			Color textColour = Color.RED;
			Color boxColour = Color.RED;
			if (close.hover) {
				textColour = Color.DARKRED;
				boxColour = Color.DARKRED;
			}
			if (close.pressed) {
				textColour = Color.MAROON;
				boxColour = Color.MAROON;
			}
			if (!close.enabled) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(x, y, close.width, fontSize * 2, close.textXOffset, close.text, textColour, boxColour);
		};
	}
	
	private ButtonVanGogh cancelTpVG(CanvasButton cancelTP) {
		return (x, y, gc) -> {
			Color textColour = Color.CORNFLOWERBLUE;
			Color boxColour = Color.CORNFLOWERBLUE;
			if (cancelTP.hover) {
				textColour = Color.STEELBLUE;
				boxColour = Color.STEELBLUE;
			}
			if (cancelTP.pressed) {
				textColour = Color.NAVY;
				boxColour = Color.NAVY;
			}
			if (!cancelTP.enabled) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(x, y, cancelTP.width, fontSize * 2, cancelTP.textXOffset, cancelTP.text, textColour, boxColour);
		};
	}
	
	private ButtonVanGogh cancelSlVG(CanvasButton cancelSL) {
		return (x, y, gc) -> {
			Color textColour = Color.ORANGE;
			Color boxColour = Color.ORANGE;
			if (cancelSL.hover) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (cancelSL.pressed) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (!cancelSL.enabled) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(x, y, cancelSL.width, fontSize * 2, cancelSL.textXOffset, cancelSL.text, textColour, boxColour);
		};
	}
	
	private ButtonVanGogh slVG(CanvasButton sl) {
		return (x, y, gc) -> {
			Color textColour = Color.ORANGE;
			Color boxColour = Color.ORANGE;
			if (sl.hover) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (sl.pressed) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (!sl.enabled) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(x, y, sl.width, 90, sl.textXOffset, sl.text, textColour, boxColour);
		};
	}
	
	private ButtonVanGogh tpVG(CanvasButton tp) {
		return (x, y, gc) -> {
			Color textColour = Color.CORNFLOWERBLUE;
			Color boxColour = Color.CORNFLOWERBLUE;
			if (tp.hover) {
				textColour = Color.STEELBLUE;
				boxColour = Color.STEELBLUE;
			}
			if (tp.pressed) {
				textColour = Color.NAVY;
				boxColour = Color.NAVY;
			}
			if (!tp.enabled) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(x, y, tp.width, 90, tp.textXOffset, tp.text, textColour, boxColour);
		};
	}
	
	private ButtonVanGogh setSlVG(CanvasButton setSL) {
		return (x, y, gc) -> {
			Color textColour = Color.ORANGE;
			Color boxColour = Color.ORANGE;
			if (setSL.hover) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (setSL.pressed) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (!setSL.enabled) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(x, y, setSL.width, fontSize * 2, setSL.textXOffset, setSL.text, textColour, boxColour);
		};
	}
	
	private ButtonVanGogh setTpVG(CanvasButton setTP) {
		return (x, y, gc) -> {
			Color textColour = Color.CORNFLOWERBLUE;
			Color boxColour = Color.CORNFLOWERBLUE;
			if (setTP.hover) {
				textColour = Color.STEELBLUE;
				boxColour = Color.STEELBLUE;
			}
			if (setTP.pressed) {
				textColour = Color.NAVY;
				boxColour = Color.NAVY;
			}
			if (!setTP.enabled) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(x, y, setTP.width, fontSize * 2, setTP.textXOffset, setTP.text, textColour, boxColour);
		};
	}
	
	private ButtonVanGogh orderVG(CanvasButton order, PendingTrade trade) {
		return (x, y, gc) -> {
			Color textColour = Color.GRAY;
			Color boxColour = Color.GRAY;
			if (order.hover) {
				boxColour = Color.DARKGRAY;
			}
			if (order.pressed) {
				boxColour = Color.DIMGRAY;
			}
			if (!order.enabled) {
				boxColour = Color.LIGHTGRAY;
			}
			if (trade.buy) {
				textColour = Color.FORESTGREEN;
			} else {
				textColour = Color.RED;
			}
			drawTradeBox(x, y, order.width, 90, order.textXOffset, order.text, textColour, boxColour);
		};
	}
	
	private ButtonVanGogh pendingVG(CanvasButton btn) {		
		return (x, y, gc) -> {
			if (Chart.darkMode()) {
				gc.setFill(Color.WHITE);
			} else {
				gc.setFill(Color.BLACK);
			}
			gc.fillRect(x, y, btn.width, btn.height);
			if (Chart.darkMode()) {
				gc.setStroke(Color.BLACK);
			} else {
				gc.setStroke(Color.WHITE);
			}	
			if (btn.hover) {
				gc.setStroke(Color.ORANGE);
			}
			if (btn.pressed) {
				gc.setStroke(Color.DARKORANGE);
			}
			if (!btn.enabled) {
				gc.setStroke(Color.LIGHTGRAY);
			}
			gc.strokeText(btn.text, x + btn.textXOffset, y + btn.textYOffset);
		};
	}
	
	private class WidthListener implements ChangeListener<Number> {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {			
			double newHSBPos = (hsb.x() / (width - hsb.sbWidth() - PRICE_MARGIN)) * (newValue.doubleValue() - WIDTH_EXTRA - hsb.sbWidth() - PRICE_MARGIN);	
			width = newValue.doubleValue() - WIDTH_EXTRA;
			canvas.setWidth(width);
			chartWidth = width - PRICE_MARGIN - CHT_MARGIN;
			setCandleStickVars(numCandlesticks);
			hsb.setMaxPos(width - PRICE_MARGIN);
			hsb.setPosition(newHSBPos, false);
			mrpx = CHT_MARGIN + 5;			
			
			if (replayMode) {
				tradeButs.close.setX(CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2);
				tradeButs.cancelTP.setX(CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2);
				tradeButs.cancelSL.setX(CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2);
				tradeButs.sl.setX(CHT_MARGIN + chartWidth / 2 - 100);
				tradeButs.tp.setX(CHT_MARGIN + chartWidth / 2 - 100);
				tradeButs.setSL.setX(CHT_MARGIN + chartWidth / 2 + 10);
				tradeButs.setTP.setX(CHT_MARGIN + chartWidth / 2 + 20 + fontSize*2);
				if (penTrade != null) {
					pendingTrades.add(penTrade);
				}
				for (PendingTrade p : pendingTrades) {
					p.pTradeButs.order.setX(CHT_MARGIN + chartWidth / 2 - 100);
					p.pTradeButs.close.setX(CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2);
					p.pTradeButs.setSL.setX(CHT_MARGIN + chartWidth / 2 + 10);
					p.pTradeButs.setTP.setX(CHT_MARGIN + chartWidth / 2 + 20 + fontSize*2);
				}
				if (penTrade != null) {
					pendingTrades.remove(penTrade);
				}
				limitOrder.setX( width - PRICE_MARGIN - fontSize*2-2);
				stopOrder.setX(width - PRICE_MARGIN - fontSize*4-4);
			}
			
			draw();
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
			hsb.setY(height - HSB_HEIGHT);
			mrpy = height - HSB_HEIGHT - CHT_MARGIN - 105 - fontSize;
			draw();
		}		
	}
	
	public Chart(double width, double height, Stage stage, DataSet data) throws Exception {
		constructorStuff(width, height, stage, data);
	}
	
	private void constructorStuff(double width, double height, Stage stage, DataSet data) throws Exception {
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
			close();
		});
		this.stage = stage;
		canvas = new Canvas(width, height);
		gc = canvas.getGraphicsContext2D();	
		hsb = new HorizontalChartScrollBar(this, data.tickDataSize(this.replayMode).get(), 0, width - PRICE_MARGIN, HSB_WIDTH, HSB_HEIGHT, height - HSB_HEIGHT);				
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
		draw();
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
	
	@Override
	public GraphicsContext graphicsContext() {
		return this.gc;
	}
	
	@Override
	public void setGraphicsContext(GraphicsContext gc) {
		this.gc = gc;
	}
	
	@Override
	public double x() {
		return this.x;
	}
	
	@Override
	public void setX(double x) {
		this.x = x;
	}
	
	@Override
	public double y() {
		return this.y;
	}
	
	@Override
	public void setY(double y) {
		this.y = y;
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
	
	public static boolean darkMode() {
		return Chart.darkMode;
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
			buy.setVanGogh(buyVG);
			sell = new CanvasButton(gc, bw, bh, initx + bw + mgn + ncw + mgn + ncw + mgn, inity, "SELL", 9, fontSize + 7);
			sell.setVanGogh(sellVG);
			double h = CanvasNumberChooser.getHeightForDesiredNumberHight(bh);
			double y = bh - CanvasNumberChooser.buttonHeight(h);
			volTens = new CanvasNumberChooser(gc, ncw, h, initx + bw + mgn, y);
			volUnits = new CanvasNumberChooser(gc, ncw, h, initx + bw + mgn + ncw + mgn, y);
			volUnits.setValue(1);
			setNumberChooserColours();
			
			pendingTrades = new ArrayList<PendingTrade>();
			tradeButs = new TradeButtons();
			tradeButs.close = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3);
			tradeButs.close.setVanGogh(closeVG(tradeButs.close));
			tradeButs.cancelTP = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3);
			tradeButs.cancelTP.setVanGogh(cancelTpVG(tradeButs.cancelTP));
			tradeButs.cancelSL = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3);
			tradeButs.cancelSL.setVanGogh(cancelSlVG(tradeButs.cancelSL));
			tradeButs.sl = new CanvasButton(gc, 100, fontSize*2, CHT_MARGIN + chartWidth / 2 - 100, 0, "", 5, fontSize/3);
			tradeButs.sl.setVanGogh(slVG(tradeButs.sl));
			tradeButs.tp = new CanvasButton(gc, 100, fontSize*2, CHT_MARGIN + chartWidth / 2 - 100, 0, "", 5, fontSize/3);
			tradeButs.tp.setVanGogh(tpVG(tradeButs.tp));
			tradeButs.setSL = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 + 10, 0, "SL", 6, fontSize/3);
			tradeButs.setSL.setVanGogh(setSlVG(tradeButs.setSL));
			tradeButs.setTP = new CanvasButton(gc, fontSize*2, fontSize*2, CHT_MARGIN + chartWidth / 2 + 20 + fontSize*2, 0, "TP", 6, fontSize/3);
			tradeButs.setTP.setVanGogh(setTpVG(tradeButs.setTP));
			limitOrder = new CanvasButton(gc, fontSize*2+2, fontSize, width - PRICE_MARGIN - fontSize*2-2, 0, "LMT", 0, fontSize-2);
			limitOrder.setVanGogh(pendingVG(this.limitOrder));
			stopOrder = new CanvasButton(gc, fontSize*2+2, fontSize, width - PRICE_MARGIN - fontSize*4-4, 0, "STP", 1, fontSize-2);			
			stopOrder.setVanGogh(pendingVG(this.stopOrder));
			if (trade == null) {
				Chart.trade = new Trade(data, 1, true, 1);
				Chart.trade.close(1);
				disableTradeButtons();
			}			
			
			mr.addChart(this);
		}
	}
	
	private void enableTradeButtons() {
		this.tradeButs.close.enable();
		this.tradeButs.cancelTP.enable();
		this.tradeButs.cancelSL.enable();
		this.tradeButs.sl.enable();
		this.tradeButs.tp.enable();
		this.tradeButs.setSL.enable();
		this.tradeButs.setTP.enable();
	}
	
	private void disableTradeButtons() {
		this.tradeButs.close.disable();
		this.tradeButs.cancelTP.disable();
		this.tradeButs.cancelSL.disable();
		this.tradeButs.sl.disable();
		this.tradeButs.tp.disable();
		this.tradeButs.setSL.disable();
		this.tradeButs.setTP.disable();
	}
	
	private int tradeVolume() {
		CanvasNumberChooser[] c = {volTens, volUnits};
		return CanvasNumberChooser.number(c);
	}
	
	public void disableReplayMode() {
		if (this.replayMode) {
			this.replayMode = false;
			mr.removeChart(this);
			this.mr = null;
			this.mrp = null;
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
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
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
		if (drawCandlesticks && startIndex >= data.m1CandlesDataSize(false).get()) {
			this.startIndex = data.m1CandlesDataSize(replayMode).get() - 1;
			return;
		}
		if (!drawCandlesticks && startIndex >= data.tickDataSize(false).get()) {
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
	
	public void onMouseExited() {
		hsb.onMouseExited();
		focusedChart.set(false);
		focusedOnChart.set(false);
		newCHT_BTN_Hover = false;
		drawCandlesticksHover = false;
		darkModeHover = false;
		drawMRPHover = false;
		drawPending = false;
		drawCharts(this.name());
	}
	
	public void onMouseEntered() {
		focusedChart.set(true);
		CrossHair.setIsForCandle(drawCandlesticks);
		CrossHair.setDateIndex(0);
		CrossHair.setName(data.name());
		drawCharts(this.name());
	}
	
	private void tradeButtonHoverChecks(double x, double y) {
		ButtonChecks.mouseButtonHoverCheck(buy, x, y);
		ButtonChecks.mouseButtonHoverCheck(sell, x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.sl, x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.setSL, x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.tp, x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.setTP, x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.cancelSL, x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.cancelTP, x, y);
		ButtonChecks.mouseButtonHoverCheck(tradeButs.close, x, y);
		for (PendingTrade p : pendingTrades) {
			for (CanvasButton c : p.pTradeButs.buttons()) {
				ButtonChecks.mouseButtonHoverCheck(c, x, y);
			}
		}
		ButtonChecks.mouseNumberChooserDownHoverCheck(volTens, x, y);
		ButtonChecks.mouseNumberChooserUpHoverCheck(volTens, x, y);
		ButtonChecks.mouseNumberChooserDownHoverCheck(volUnits, x, y);
		ButtonChecks.mouseNumberChooserUpHoverCheck(volUnits, x, y);
	}
	
	public void onMouseMoved(MouseEvent e) {
		if (CrossHair.dateIndex().get() >= data.m1CandlesDataSize(replayMode).get() && drawCandlesticks) {
			CrossHair.setDateIndex(0);
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
		} else if (replayMode && !limitDragging && !stopDragging) {				
			limitOrder.setY(e.getY() - fontSize/2); 
			stopOrder.setY(e.getY() - fontSize/2);
			ButtonChecks.mouseButtonHoverCheck(limitOrder, e.getX(), e.getY());
			ButtonChecks.mouseButtonHoverCheck(stopOrder, e.getX(), e.getY());
			drawPending = true;							
		}
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
		if (checkDrawMRPBtn(e.getX(), e.getY())) {
			if (!drawMRPClicked) {
				drawMRPHover = true;
			}
		} else {
			drawMRPClicked = false;
			drawMRPHover = false;
		}
		if (replayMode) {
			tradeButtonHoverChecks(e.getX(), e.getY());
		}
		if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100) {
			MouseEvent me = new MouseEvent(MouseEvent.MOUSE_MOVED, e.getX() - mrpx, e.getY() - mrpy, e.getScreenX(), e.getScreenY(), 
					e.getButton(), e.getClickCount(), e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(), 
					e.isPrimaryButtonDown(), e.isMiddleButtonDown(), e.isSecondaryButtonDown(), e.isBackButtonDown(), 
					e.isForwardButtonDown(), e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(), null);
			mrp.onMouseMoved(me);
		} else if (drawMRP) {
			mrp.hsb().onMouseReleased();
			mrp.onMouseExited();
		}
		drawCharts(this.name());
	}				
	
	private boolean checkNewChtBtn(double x, double y) {
		if (replayMode) {
			if (x >= CHT_MARGIN + chartWidth && x <= CHT_MARGIN + chartWidth + PRICE_MARGIN / 4 - 1 && y >= CHT_MARGIN + chartHeight) {
				return true;
			}
			return false;
		} else if (x >= CHT_MARGIN + chartWidth && x <= CHT_MARGIN + chartWidth + PRICE_MARGIN / 3 - 1 && y >= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}
	
	private boolean checkChartTypeBtn(double x, double y) {
		if (replayMode) {
			if (x >= CHT_MARGIN + chartWidth  + PRICE_MARGIN / 4 + 1 && x <= CHT_MARGIN + chartWidth + PRICE_MARGIN * 2 / 4 - 1 && y >= CHT_MARGIN + chartHeight) {
				return true;
			}
			return false;
		} else if (x >= CHT_MARGIN + chartWidth  + PRICE_MARGIN / 3 + 1 && x <= CHT_MARGIN + chartWidth + PRICE_MARGIN * 2 / 3 - 1 && y >= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}

	private boolean checkDarkModeBtn(double x, double y) {
		if (replayMode) {
			if (x >= CHT_MARGIN + chartWidth  + PRICE_MARGIN * 2 / 4 + 1  && x <= CHT_MARGIN + chartWidth + PRICE_MARGIN * 3 / 4 - 1 && y >= CHT_MARGIN + chartHeight) {
				return true;
			}
			return false;
		} else if (x >= CHT_MARGIN + chartWidth  + PRICE_MARGIN * 2 / 3 + 1 && y >= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}
	
	private boolean checkDrawMRPBtn(double x, double y) {
		if (x >= CHT_MARGIN + chartWidth  + PRICE_MARGIN * 3 / 4 + 1 && y >= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}
	
	private boolean onDateMargin(double x, double y) {
		if (x >= CHT_MARGIN && x <= CHT_MARGIN + chartWidth && y >= CHT_MARGIN + chartHeight - fontSize && y <= CHT_MARGIN + chartHeight) {
			return true;
		}
		return false;
	}
	
	private boolean tradeButtonPressChecks(double x, double y) {
		boolean pressed = false;
		if (sell.onButton(x, y) && sell.enabled()) {
			sell.setPressed(true);
			pressed = true;
		} else if (buy.onButton(x, y) && buy.enabled()) {
			buy.setPressed(true);
			pressed = true;
		} else if (tradeButs.tp.onButton(x, y) && tradeButs.tp.enabled()) {
			tradeButs.tp.setPressed(true);
			tpDragging = true;
			pressed = true;
		} else if (tradeButs.sl.onButton(x, y) && tradeButs.sl.enabled()) {
			tradeButs.sl.setPressed(true);
			slDragging = true;
			pressed = true;
		} else if (tradeButs.setTP.onButton(x, y) && tradeButs.setTP.enabled()) {
			tradeButs.setTP.setPressed(true);
			tpDragging = true;
			pressed = true;
		} else if (tradeButs.setSL.onButton(x, y) && tradeButs.setSL.enabled()) {
			tradeButs.setSL.setPressed(true);
			slDragging = true;
			pressed = true;
		} else if (tradeButs.cancelTP.onButton(x, y) && tradeButs.cancelTP.enabled()) {
			tradeButs.cancelTP.setPressed(true);
			pressed = true;
		} else if (tradeButs.cancelSL.onButton(x, y) && tradeButs.cancelSL.enabled()) {
			tradeButs.cancelSL.setPressed(true);
			pressed = true;
		} else if (tradeButs.close.onButton(x, y) && tradeButs.close.enabled()) {
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
		} else if (limitOrder.onButton(x, y) && limitOrder.enabled()) {
			limitOrder.setPressed(true);
			limitDragging = true;			
			boolean buy = true;
			double currentPrice = tickData().get(data.tickDataSize(true).get() - 1).price();
			double crossHairPrice = roundToNearestTick(yCoordToPrice(y));
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = false;
				}
				penTrade = new PendingTrade(true, buy, crossHairPrice, tradeVolume());
			}	
			pressed = true;
		} else if (stopOrder.onButton(x, y) && stopOrder.enabled()) {
			stopOrder.setPressed(true);
			stopDragging = true;	
			boolean buy = false;
			double currentPrice = tickData().get(data.tickDataSize(true).get()).price();
			double crossHairPrice = roundToNearestTick(yCoordToPrice(y));
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = true;
				}
				penTrade = new PendingTrade(false, buy, crossHairPrice, tradeVolume());
			}	
			pressed = true;
		} else {
			for (PendingTrade p : pendingTrades) {
				if (p.pTradeButs.order.onButton(x, y) && p.pTradeButs.order.enabled()) {
					p.pTradeButs.order.setPressed(true);
					penOrderBeingDragged = p;
					penOrderDragging = true;
					pressed = true;
					break;
				} else if (p.pTradeButs.setTP.onButton(x, y) && p.pTradeButs.setTP.enabled()) {
					p.pTradeButs.setTP.setPressed(true);
					penOrderBeingDragged = p;
					tpDragging = true;
					pressed = true;
					break;
				} else if (p.pTradeButs.setSL.onButton(x, y) && p.pTradeButs.setSL.enabled()) {
					p.pTradeButs.setSL.setPressed(true);
					penOrderBeingDragged = p;
					slDragging = true;
					pressed = true;
					break;
				} else if (p.pTradeButs.close.onButton(x, y) && p.pTradeButs.close.enabled()) {
					p.pTradeButs.close.setPressed(true);
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
			if (e.getX() >= width - PRICE_MARGIN && e.getY() <= chartHeight + CHT_MARGIN) {
				priceDragging = true;
				priceInitPos = e.getY();
			}
			if (onChart(e.getX(), e.getY(), true)) {
				chartInitPos = e.getX();
				if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100) {
					MouseEvent me = new MouseEvent(MouseEvent.MOUSE_PRESSED, e.getX() - mrpx, e.getY() - mrpy, e.getScreenX(), e.getScreenY(), 
							e.getButton(), e.getClickCount(), e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(), 
							e.isPrimaryButtonDown(), e.isMiddleButtonDown(), e.isSecondaryButtonDown(), e.isBackButtonDown(), 
							e.isForwardButtonDown(), e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(), null);
					mrp.onMousePressed(me);
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
			if (checkNewChtBtn(e.getX(), e.getY())) {
				newCHT_BTN_Clicked = true;
				newCHT_BTN_Hover = false;
			} else if (checkChartTypeBtn(e.getX(), e.getY())) {
				drawCandlesticksClicked = true;	
				drawCandlesticksHover = false;
			} else if (checkDarkModeBtn(e.getX(), e.getY())) {
				darkModeClicked = true;
				darkModeHover = false;
			} else if (checkDrawMRPBtn(e.getX(), e.getY())) {
				drawMRPClicked = true;
				drawMRPHover = false;
			}
		} 					
		drawCharts(this.name());
	}
	
	private void tradeButtonReleaseChecks(double x, double y) {
		if (sell.pressed()) {
			if (sell.onButton(x, y)) {
				if (trade.closed()) {
					trade = new Trade(data, data.tickDataSize(true).get() - 1, false, tradeVolume());
					for (Chart c : charts) {
						c.enableTradeButtons();
					}
				} else {
					if (trade.buy()) {
						trade.scaleOut(tradeVolume(), data.tickDataSize(true).get() - 1);
						closedTradeProc();
						if (trade.closed()) {
							for (Chart c : charts) {
								c.disableTradeButtons();
							}
						}
					} else {
						trade.scaleIn(tradeVolume(), data.tickDataSize(true).get() - 1);
					}
				}
			}
			sell.setPressed(false);
		} else if (buy.pressed()) {
			if (buy.onButton(x, y)) {
				if (trade.closed()) {
					trade = new Trade(data, data.tickDataSize(true).get() - 1, true, tradeVolume());
					for (Chart c : charts) {
						c.enableTradeButtons();
					}
				} else {
					if (trade.buy()) {
						trade.scaleIn(tradeVolume(), data.tickDataSize(true).get() - 1);
					} else {
						trade.scaleOut(tradeVolume(), data.tickDataSize(true).get() - 1);
						closedTradeProc();
						if (trade.closed()) {
							for (Chart c : charts) {
								c.disableTradeButtons();
							}
						}
					}
				}
			}
			buy.setPressed(false);
		} else if (volTens.upPressed()) {
			if (volTens.onUp(x, y)) {
				volTens.incrementValue();
				if (tradeVolume() == 0) {
					volUnits.incrementValue();
				}
			}
			volTens.setUpPressed(false);
		} else if (volTens.downPressed()) {
			if (volTens.onDown(x, y)) {
				volTens.decrementValue();
				if (tradeVolume() == 0) {
					volUnits.incrementValue();
				}
			}
			volTens.setDownPressed(false);
		} else if (volUnits.upPressed()) {
			if (volUnits.onUp(x, y)) {
				volUnits.incrementValue();
				if (tradeVolume() == 0) {
					volUnits.incrementValue();
				}
			}
			volUnits.setUpPressed(false);
		} else if (volUnits.downPressed()) {
			if (volUnits.onDown(x, y)) {
				volUnits.decrementValue();
				if (tradeVolume() == 0) {
					volUnits.incrementValue();
				}
			}
			volUnits.setDownPressed(false);
		} else {
			for (PendingTrade p : pendingTrades) {
				if (p.pTradeButs.close.pressed()) {
					if (p.pTradeButs.close.onButton(x, y)) {
						pendingTrades.remove(penOrderBeingDragged);
						penOrderBeingDragged = null;
					}
					break;
				} else if (p.pTradeButs.setSL.pressed()) {
					p.pTradeButs.setSL.setPressed(false);
					break;
				} else if (p.pTradeButs.setTP.pressed()) {
					p.pTradeButs.setTP.setPressed(false);
					break;
				}
			}					
			
			if (penOrderBeingDragged != null) {
				if (trade.closed()) {		
					if (penOrderBeingDragged.buy) {
						if (slPrice >= penOrderBeingDragged.price) {
							slPrice = -1;
						}
						if (tpPrice <= penOrderBeingDragged.price) {
							tpPrice = -1;
						}
					} else {
						if (slPrice <= penOrderBeingDragged.price) {
							slPrice = -1;
						}
						if (tpPrice >= penOrderBeingDragged.price) {
							tpPrice = -1;
						}
					}
				}				
				
				penOrderBeingDragged.pTradeButs.order.setPressed(false);
				penOrderDragging = false;
			}
		}
		
		if (!trade.closed()) {
			if (tradeButs.tp.pressed()) {
				tradeButs.tp.setPressed(false);
			} else if (tradeButs.sl.pressed()) {
				tradeButs.sl.setPressed(false);
			} else if (tradeButs.cancelTP.pressed()) {
				if (tradeButs.cancelTP.onButton(x, y)) {
					trade.cancelTP();
					tpPrice = -1;
				}
				tradeButs.cancelTP.setPressed(false);
			} else if (tradeButs.cancelSL.pressed()) {
				if (tradeButs.cancelSL.onButton(x, y)) {
					trade.cancelSL();
					slPrice = -1;
				}
				tradeButs.cancelSL.setPressed(false);
			} else if (tradeButs.setTP.pressed()) {			
				tradeButs.setTP.setPressed(false);
			} else if (tradeButs.setSL.pressed()) {			
				tradeButs.setSL.setPressed(false);
			} else if (tradeButs.close.pressed()) {
				if (tradeButs.close.onButton(x, y)) {
					trade.close(data.tickDataSize(true).get() - 1);
					closedTradeProc();
					for (Chart c : charts) {
						c.disableTradeButtons();
					}
					tpPrice = -1;
					slPrice = -1;
				}
				tradeButs.close.setPressed(false);
			} 
			
			if (slDragging) {		
				if (!trade.closed()) {
					trade.setSL(slPrice);
					slPrice = trade.sl();
				}
			} else if (tpDragging) {
				if (!trade.closed()) {
					trade.setTP(tpPrice);
					tpPrice = trade.tp();
				}
			} 
			
			
			for (Chart c : charts) {
				c.tradeButs.sl.setText(trade.volume() + "\t$" + trade.hypotheticalProfit(slPrice));
				c.tradeButs.tp.setText(trade.volume() + "\t$" + trade.hypotheticalProfit(tpPrice));
			}			
		}
		
		limitOrder.setPressed(false);
		stopOrder.setPressed(false);
		if (penTrade != null) {
			if (limitDragging) {
				orderReleasedStuff(x, y);
				pendingTrades.add(new PendingTrade(penTrade.limit, penTrade.buy, penTrade.price, penTrade.volume));
			} else if (stopDragging) {
				orderReleasedStuff(x, y);
				pendingTrades.add(new PendingTrade(penTrade.limit, penTrade.buy, penTrade.price, penTrade.volume));
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
	
	public static void toggleDarkMode(boolean redraw) {
		darkMode = !darkMode;
		for (Chart c : charts) {
			if (c.replayMode) {					
				c.setNumberChooserColours();				
			}
			if (redraw) {
				c.draw();
			}
		}
		Menu.drawMenus();
		MarketReplayPane.drawReplayPanes();
	}
	
	public void onMouseReleased(MouseEvent e) {	
		hsb.onMouseReleased();		
		if (newCHT_BTN_Clicked && checkNewChtBtn(e.getX(), e.getY())) {
			newCHT_BTN_Clicked = false;
			Stage s = new Stage();
			ChartPane c = new ChartPane(s, width, height, data, replayMode, mr, mrp);			
			Scene scene = new Scene(c);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		} else if (drawCandlesticksClicked && checkChartTypeBtn(e.getX(), e.getY())) {
			drawCandlesticksClicked = false;
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
				newHSBPos = (width - hsb.sbWidth() - PRICE_MARGIN) * ((double)startIndex / (data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
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
				newHSBPos = (width - hsb.sbWidth() - PRICE_MARGIN) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
				hsb.setPosition(newHSBPos, false);				
			}
			if (newHSBPos < width - PRICE_MARGIN - HSB_WIDTH) {
				keepStartIndex = true;
			} else {
				keepStartIndex = false;
			}
		} else if (darkModeClicked && checkDarkModeBtn(e.getX(), e.getY())) {
			darkModeClicked = false;	
			toggleDarkMode(false);
		} else if (drawMRPClicked && checkDrawMRPBtn(e.getX(), e.getY())) {
			drawMRPClicked = false;
			drawMRP = !drawMRP;
		} else if (measuring) {
			measuring = false;
		} else if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100) {
			MouseEvent me = new MouseEvent(MouseEvent.MOUSE_RELEASED, e.getX() - mrpx, e.getY() - mrpy, e.getScreenX(), e.getScreenY(), 
					e.getButton(), e.getClickCount(), e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(), 
					e.isPrimaryButtonDown(), e.isMiddleButtonDown(), e.isSecondaryButtonDown(), e.isBackButtonDown(), 
					e.isForwardButtonDown(), e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(), null);
			mrp.onMouseReleased(me);
		} else {
			if (replayMode) {
				tradeButtonReleaseChecks(e.getX(), e.getY());
			}
		}
		lineDragging = false;
		priceDragging = false;
		chartDragging = false;
		chartDateMarginDragging = false;
		rightPressed = false;
		drawCharts(this.name());
	}
	
	private void updatePendingTrade(PendingTrade pent, double y) {
		if (pent == null) {
			return;
		}
		if (pent.limit) {		
			boolean buy = true;
			double currentPrice = tickData().get(data.tickDataSize(true).get()).price();
			double crossHairPrice = roundToNearestTick(yCoordToPrice(y));
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = false;
				}
				pent.buy = buy;
				pent.price = crossHairPrice;
			}	
		} else {	
			boolean buy = false;
			double currentPrice = tickData().get(data.tickDataSize(true).get()).price();
			double crossHairPrice = roundToNearestTick(yCoordToPrice(y));
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = true;
				}
				pent.buy = buy;
				pent.price = crossHairPrice;
			}	
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
		if (lineDragging) {
			double price = roundToNearestTick(((((chartHeight - (chtDataMargin*2)) - (e.getY() - Chart.CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest); 
			data.lines().get(lineHighlighted).setPrice(price);
		}
		if (rightPressed) {
			measuring = true;
			endX = e.getX();
			endY = e.getY();
		}
		if (tpDragging) {
			tpPrice = roundToNearestTick(yCoordToPrice(e.getY()));
			for (Chart c : charts) {
				if (trade.closed()) {
					c.tradeButs.tp.setText(trade.volume() + "\t$" + Trade.hypotheticalProfit2(penOrderBeingDragged.price, roundToNearestTick(yCoordToPrice(e.getY())), penOrderBeingDragged.buy, penOrderBeingDragged.volume));
				} else {
					c.tradeButs.tp.setText(trade.volume() + "\t$" + trade.hypotheticalProfit(roundToNearestTick(yCoordToPrice(e.getY()))));
				}				
			}
		}
		if (slDragging) {
			slPrice = roundToNearestTick(yCoordToPrice(e.getY()));
			for (Chart c : charts) {
				if (trade.closed()) {
					c.tradeButs.sl.setText(trade.volume() + "\t$" + Trade.hypotheticalProfit2(penOrderBeingDragged.price, roundToNearestTick(yCoordToPrice(e.getY())), penOrderBeingDragged.buy, penOrderBeingDragged.volume));
				} else {
					c.tradeButs.sl.setText(trade.volume() + "\t$" + trade.hypotheticalProfit(roundToNearestTick(yCoordToPrice(e.getY()))));
				}				
			}
		}
		if (limitDragging || stopDragging) {
			updatePendingTrade(penTrade, e.getY());
		}
		if (penOrderDragging) {
			updatePendingTrade(penOrderBeingDragged, e.getY());
		}
		priceInitPos = e.getY();		
		if (chartDragging && !lineDragging) {
			double posDiff = e.getX() - chartInitPos;
			double newHSBPos = hsb.x();					
			int diff;
			dragDiffAccum += posDiff;
			if (drawCandlesticks) {
				diff = (int)(dragDiffAccum / (candlestickWidth + candlestickSpacing));
				if (diff != 0) {
					startIndex = startIndex - diff;
					newHSBPos = (width - hsb.sbWidth() - PRICE_MARGIN) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
					dragDiffAccum = 0;
				}
			} else {
				diff = (int)(dragDiffAccum / xDiff);
				if (diff != 0) {
					startIndex = startIndex - diff;
					newHSBPos = (width - hsb.sbWidth() - PRICE_MARGIN) * ((double)startIndex /(data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
					dragDiffAccum = 0;
				}
			}
			if (startIndex < 0) {
				startIndex = 0;
			}
			if (dragDiffAccum == 0 && posDiff != 0) {
				if (newHSBPos < width - PRICE_MARGIN - HSB_WIDTH) {
					keepStartIndex = true;
				} else {
					keepStartIndex = false;
				}
				hsb.setPosition(newHSBPos, false);
			}
		}
		if (chartDateMarginDragging && !lineDragging) {			
			if (drawCandlesticks) {
				zoomCandlesticks(e.getX() - chartInitPos);
			} else {
				zoomTicks(e.getX() - chartInitPos);
			}
		}
		if (drawMRP && e.getX() >= mrpx && e.getX() <= mrpx + 399 && e.getY() >= mrpy && e.getY() <= mrpy + 100) {
			MouseEvent me = new MouseEvent(MouseEvent.MOUSE_DRAGGED, e.getX() - mrpx, e.getY() - mrpy, e.getScreenX(), e.getScreenY(), 
					e.getButton(), e.getClickCount(), e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(), 
					e.isPrimaryButtonDown(), e.isMiddleButtonDown(), e.isSecondaryButtonDown(), e.isBackButtonDown(), 
					e.isForwardButtonDown(), e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(), null);
			mrp.onMouseDragged(me);
		}
		chartInitPos = e.getX();
		onMouseMoved(e);
	}
	
	private void zoomCandlesticks(double delta) {
		if (delta > 0) {
			if (numCandlesticks - 5 >= 10) {
				numCandlesticks -= 5;
				setCandleStickVars(numCandlesticks);
			}
		} else {
			if (numCandlesticks + 5 <= data.m1CandlesDataSize(this.replayMode).get() - 5) {
				numCandlesticks += 5;
				setCandleStickVars(numCandlesticks);
			}				
		}
		double newHSBPos = (width - hsb.sbWidth() - PRICE_MARGIN) * ((double)startIndex /(data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
		if (newHSBPos < width - PRICE_MARGIN - HSB_WIDTH) {
			keepStartIndex = true;
		} else {
			keepStartIndex = false;
		}
		hsb.setPosition(newHSBPos, false);
	}
	
	private void zoomTicks(double delta) {
		if (delta > 0) {
			setNumDataPoints(numDataPoints - 100);
		} else {
			setNumDataPoints(numDataPoints + 100);
		}
		double xDiff = chartWidth / (double)numDataPoints;
		if (xDiff * (data.tickDataSize(this.replayMode).get() - 1) < chartWidth) {
			setNumDataPoints(data.tickDataSize(this.replayMode).get() - 1);
		} else if (numDataPoints < 100) {
			setNumDataPoints(100);
		}
		double newHSBPos = (width - hsb.sbWidth() - PRICE_MARGIN) * ((double)startIndex /(data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
		if (newHSBPos < width - PRICE_MARGIN - HSB_WIDTH) {
			keepStartIndex = true;
		} else {
			keepStartIndex = false;
		}
		hsb.setPosition(newHSBPos, false);
	}
	
	public void onScroll(ScrollEvent e) {	
		if (drawCandlesticks) {
			zoomCandlesticks(e.getDeltaY());
		} else {
			zoomTicks(e.getDeltaY());
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
		if (drawCandlesticks) {
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
		int intPart = (int)price;
		price = price - intPart;
		int pow = (int)Math.pow(10, numDecimalPts);
		price *= pow;
		int intTick = (int)(pow * tickSize);		
		int quotient = (int)(price / intTick);
		double remainder = price % intTick;		
		if (remainder > intTick / 2.0) {
			return Round.round(intPart + (intTick * (quotient + 1)) / (double)pow, numDecimalPts + 1); 
		}
		return Round.round(intPart + (intTick * quotient) / (double)pow, numDecimalPts + 1);
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
				gc.fillRect(chartWidth + CHT_MARGIN, y - fontSize/2, 100, fontSize);
				gc.setStroke(Color.WHITE);
				gc.strokeText(((Double)(roundToNearestTick(l.price()))).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, y + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
			}
		}		
	}
	
	public void drawCandleStick(DataSet.Candlestick candle, double xPos, double yPos) {
		int num = 0;
		if (darkMode) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}		;
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
	
	private void fillNewChtBtn() {
		if (!newCHT_BTN_Clicked && !newCHT_BTN_Hover) {
			return;
		}
		double x = CHT_MARGIN + chartWidth + 1;
		double w;
		if (replayMode) { 
			w = PRICE_MARGIN / 4 - 2;
		} else {
			w = PRICE_MARGIN / 3 - 2;
		}
		if (newCHT_BTN_Clicked) {
			gc.setFill(Color.DIMGRAY);
			gc.fillRect(x, CHT_MARGIN + chartHeight + 1, w, HSB_HEIGHT + CHT_MARGIN - 2);
		} else if (newCHT_BTN_Hover) {
			gc.setFill(Color.GRAY);
			gc.fillRect(x, CHT_MARGIN + chartHeight + 1, w, HSB_HEIGHT + CHT_MARGIN - 2);
		}	
	}
	
	private void fillChartTypeBtn() {
		if (!drawCandlesticksClicked && !drawCandlesticksHover) {
			return;
		}
		double x;
		double w;
		if (replayMode) { 
			x =  width - PRICE_MARGIN * 3 / 4;
			w = PRICE_MARGIN / 4 - 2;
		} else {
			x =  width - PRICE_MARGIN * 2 / 3;
			w = PRICE_MARGIN / 3 - 2;
		}
		if (drawCandlesticksClicked) {
			gc.setFill(Color.DIMGRAY);
			gc.fillRect(x, CHT_MARGIN + chartHeight + 1, w, HSB_HEIGHT + CHT_MARGIN - 2);
		} else if (drawCandlesticksHover) {
			gc.setFill(Color.GRAY);
			gc.fillRect(x, CHT_MARGIN + chartHeight + 1, w, HSB_HEIGHT + CHT_MARGIN - 2);
		}
	}

	private void fillDarkModeBtn() {
		if (!darkModeClicked && !darkModeHover) {
			return;
		}
		double x;
		double w;
		if (replayMode) { 
			x = width - PRICE_MARGIN * 2 / 4;
			w = PRICE_MARGIN / 4 - 2;
		} else {
			x = width - PRICE_MARGIN / 3;
			w = PRICE_MARGIN / 3 - 1;
		}	
		if (darkModeClicked) {
			gc.setFill(Color.DIMGRAY);
			gc.fillRect(x, CHT_MARGIN + chartHeight + 1, w, HSB_HEIGHT + CHT_MARGIN - 2);
		} else if (darkModeHover) {
			gc.setFill(Color.GRAY);
			gc.fillRect(x, CHT_MARGIN + chartHeight + 1, w, HSB_HEIGHT + CHT_MARGIN - 2);
		}	
	}
	
	private void fillDrawMRPBtn() {		
		if (!drawMRPClicked && !drawMRPHover) {
			return;
		}
		double x = width - PRICE_MARGIN / 4;
		double w = PRICE_MARGIN / 4 - 1;
		if (drawMRPClicked) {
			gc.setFill(Color.DIMGRAY);
			gc.fillRect(x, CHT_MARGIN + chartHeight + 1, w, HSB_HEIGHT + CHT_MARGIN - 2);
		} else if (drawMRPHover) {
			gc.setFill(Color.GRAY);
			gc.fillRect(x, CHT_MARGIN + chartHeight + 1, w, HSB_HEIGHT + CHT_MARGIN - 2);
		}					
	}
	
	private void calculateIndices() {
		if (drawCandlesticks) {
			if (!keepStartIndex) {
				if (data.m1CandlesDataSize(this.replayMode).get() < numCandlesticks * END_MARGIN_COEF) {
					startIndex = 0;
				} else {
					startIndex = (int)((hsb.x() / (width - HSB_WIDTH - PRICE_MARGIN)) * (data.m1CandlesDataSize(this.replayMode).get() - numCandlesticks * END_MARGIN_COEF));
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
					startIndex = (int)((hsb.x() / (width - HSB_WIDTH - PRICE_MARGIN)) * (data.tickDataSize(this.replayMode).get() - (numDataPoints - 1) * END_MARGIN_COEF));
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
		if (darkMode) {			
			gc.setFill(Color.BLACK);
			gc.fillRect(0, 0, width, height);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		gc.strokeRect(CHT_MARGIN, CHT_MARGIN, chartWidth, chartHeight);
		gc.strokeRect(CHT_MARGIN + chartWidth, CHT_MARGIN + chartHeight, PRICE_MARGIN, HSB_HEIGHT + CHT_MARGIN);
		if (replayMode) {
			gc.strokeLine(width - PRICE_MARGIN * 3 / 4 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN * 3 / 4 - 1, height);
			gc.strokeLine(width - PRICE_MARGIN * 2 / 4 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN * 2 / 4 - 1, height);
			gc.strokeLine(width - PRICE_MARGIN / 4 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN / 4 - 1, height);
		} else {
			gc.strokeLine(width - PRICE_MARGIN / 3 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN / 3 - 1, height);
			gc.strokeLine(width - PRICE_MARGIN * 2 / 3 - 1, CHT_MARGIN + chartHeight, width - PRICE_MARGIN * 2 / 3 - 1, height);
		}		
	}	
	
	private void setPreDrawVars() {
		if (drawCandlesticks) {
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
		if (darkMode) {
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
		gc.strokeLine(CHT_MARGIN, yPos, width - PRICE_MARGIN, yPos);	
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
			gc.strokeText(((Double)(Round.round(lowest + (diff * i), numDecimalPts + 1))).toString(), pricePos, index + pricePosYMargin, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN * 2);
			index -= spacing;
			i++;
		}
		if (!data.lines().isEmpty()) {
			drawLines();
		}	
	}	
	
	private void checkMeasuring() {
		if (measuring) {
			double endPrice = ((((chartHeight - (chtDataMargin*2)) - (endY - Chart.CHT_MARGIN - chtDataMargin)) / (double)(chartHeight - (chtDataMargin*2))) * range) + lowest;
			if (darkMode) {
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}
			gc.strokeLine(startX, startY, endX, endY);
			double ex = endX;
			double ey = endY;
			if (endX > CHT_MARGIN + chartWidth - PRC_MSRMNT_LENGTH) {
				ex -= PRC_MSRMNT_LENGTH + 5;
			}
			if (endY < CHT_MARGIN + 2 + fontSize) {
				ey += fontSize + 3;
				if (ex == endX) {
					ex += 15;
				}
			}
			gc.setStroke(Color.SLATEBLUE);	
			DecimalFormat df = new DecimalFormat("#");
			df.setMaximumFractionDigits(numDecimalPts);
			gc.strokeText(df.format(roundToNearestTick(endPrice - startPrice)) + " from: " + ((Double)startPrice).toString(), ex + 1, ey - 2, PRC_MSRMNT_LENGTH);
		}
	}
	
	private void drawTopRightText() {
		if (darkMode) {			
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		if (drawCandlesticks) {
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
	
	private void drawTradeBox(double xPos, double yPos, double width, double textMaxWidth, double textMargin, String text, Color textColour, Color boxColour) {
		if (darkMode) {
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
		double x2 = width - PRICE_MARGIN;
		if (trade.closed()) {
			double slY = priceToYCoord(slPrice);
			double tpY = priceToYCoord(tpPrice);			
			if (onChart(CHT_MARGIN + 1, tpY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, tpY - fontSize - 3, false)) {
				gc.setStroke(Color.CORNFLOWERBLUE);
				gc.strokeLine(x1, tpY, x2, tpY);
				drawPriceBox(tpY, tpPrice, Color.WHITE, Color.CORNFLOWERBLUE);
				tradeButs.tp.enable();
				tradeButs.cancelTP.enable();
				tradeButs.tp.setY(tpY - fontSize);
				tradeButs.cancelTP.setY(tpY - fontSize);
				tradeButs.tp.draw();
				tradeButs.cancelTP.draw();
			} else {
				tradeButs.tp.disable();
				tradeButs.cancelTP.disable();
			}
			if (onChart(CHT_MARGIN + 1, slY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, slY - fontSize - 3, false)) {
				gc.setStroke(Color.ORANGE);
				gc.strokeLine(x1, slY, x2, slY);
				drawPriceBox(slY, slPrice, Color.WHITE, Color.ORANGE);
				tradeButs.sl.enable();
				tradeButs.cancelSL.enable();
				tradeButs.sl.setY(slY - fontSize);
				tradeButs.cancelSL.setY(slY - fontSize);
				tradeButs.sl.draw();
				tradeButs.cancelSL.draw();
			} else {
				tradeButs.sl.disable();
				tradeButs.cancelSL.disable();
			}
		}
		for (PendingTrade trade : pendingTrades) {			
			double entryY = priceToYCoord(roundToNearestTick(trade.price));				
			if (onChart(CHT_MARGIN + 1, entryY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, entryY - fontSize - 3, false)) {
				Color boxColour = Color.GRAY;
				gc.setStroke(Color.GRAY);
				gc.strokeLine(x1, entryY, x2, entryY);
				
				drawPriceBox(entryY, roundToNearestTick(trade.price), Color.WHITE, boxColour);
				trade.pTradeButs.order.setY(priceToYCoord(trade.price) - fontSize);
				trade.pTradeButs.order.draw();						
				if (Chart.trade.closed()) {
					trade.pTradeButs.setSL.enable();
					trade.pTradeButs.setTP.enable();
					trade.pTradeButs.setSL.setY(entryY - fontSize);
					trade.pTradeButs.setTP.setY(entryY - fontSize);
					trade.pTradeButs.setSL.draw();
					trade.pTradeButs.setTP.draw();
				} else {
					trade.pTradeButs.setSL.disable();
					trade.pTradeButs.setTP.disable();
				}
				trade.pTradeButs.close.setY(entryY - fontSize);				
				trade.pTradeButs.close.draw();
			}	
		}	
		if (penTrade != null) {
			pendingTrades.remove(penTrade);
		}
	}
	
	private void drawTrade() {
		if (trade == null || trade.closed()) {
			return;
		}
		double x1 = CHT_MARGIN + chartWidth / 2;
		double x2 = width - PRICE_MARGIN;	
		
		double entryY = priceToYCoord(roundToNearestTick(trade.entryPrice()));
		double slY = priceToYCoord(slPrice);
		double tpY = priceToYCoord(tpPrice);
		if (onChart(CHT_MARGIN + 1, tpY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, tpY - fontSize - 3, false)) {
			gc.setStroke(Color.CORNFLOWERBLUE);
			gc.strokeLine(x1, tpY, x2, tpY);
			drawPriceBox(tpY, tpPrice, Color.WHITE, Color.CORNFLOWERBLUE);
			tradeButs.tp.setY(tpY - fontSize);
			tradeButs.cancelTP.setY(tpY - fontSize);
			tradeButs.tp.draw();
			tradeButs.cancelTP.draw();
		}
		if (onChart(CHT_MARGIN + 1, slY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, slY - fontSize - 3, false)) {
			gc.setStroke(Color.ORANGE);
			gc.strokeLine(x1, slY, x2, slY);
			drawPriceBox(slY, slPrice, Color.WHITE, Color.ORANGE);
			tradeButs.sl.setY(slY - fontSize);
			tradeButs.cancelSL.setY(slY - fontSize);
			tradeButs.sl.draw();
			tradeButs.cancelSL.draw();
		}
		if (onChart(CHT_MARGIN + 1, entryY + fontSize + 3, false) && onChart(CHT_MARGIN + 1, entryY - fontSize - 3, false)) {
			Color boxColour;
			Color textColour;
			if (trade.buy()) {
				boxColour = Color.FORESTGREEN;
				gc.setStroke(Color.FORESTGREEN);
			} else {
				boxColour = Color.RED;
				gc.setStroke(Color.RED);
			}
			double profit = trade.profit();
			if (profit > 0) {
				textColour = Color.FORESTGREEN;
			} else if (profit < 0) {
				textColour = Color.RED;
			} else {
				textColour = Color.GRAY;
			}
			gc.strokeLine(x1, entryY, x2, entryY);
			
			drawPriceBox(entryY, roundToNearestTick(trade.entryPrice()), Color.WHITE, boxColour);
			drawTradeBox(x1 - 100, entryY - fontSize, 100, 90, 5, ((Double)(trade.volume())).toString() + "\t$" + ((Double)(trade.profit())).toString(), textColour, boxColour);
			tradeButs.setSL.setY(entryY - fontSize);
			tradeButs.setTP.setY(entryY - fontSize);
			tradeButs.close.setY(entryY - fontSize);
			tradeButs.setSL.draw();
			tradeButs.setTP.draw();
			tradeButs.close.draw();
		}	
	}
	
	public void draw() {		
		calculateIndices();
		drawFrame();		
		fillNewChtBtn();
		fillChartTypeBtn();
		fillDarkModeBtn();		
		hsb.draw();
		calculateRange();
		setPreDrawVars();
		if (drawCandlesticks) {
			drawCandlestickChart();
		} else {		
			drawLineChart();
		}		
		drawPriceDashes();
		drawTopRightText();
		checkMeasuring();	
		crossHair.drawCrossHair();
		if (replayMode) {				
			fillDrawMRPBtn();			
			drawCurrentPriceLine();
			drawTradeButtons();
			drawTrade();
			drawPendingTrades();
			drawCurrentPriceBox();
			if (drawPending) {
				limitOrder.draw();
				stopOrder.draw();
			}
			if (drawMRP) {
				mrp.drawPane(gc, mrpx, mrpy);
			}
		}
	}
	
	private boolean executePendingOrder(PendingTrade p, int i, boolean newTrade) {
		boolean nt = false;
		if (trade.closed()) {
			trade = new Trade(data, i, p.buy, p.volume, slPrice, tpPrice);
			for (Chart c : charts) {
				c.enableTradeButtons();
			}
			nt = true;
		} else {			
			if (trade.buy() && p.buy || !trade.buy() && !p.buy) {
				trade.scaleIn(p.volume, i);
			} else {
				trade.scaleOut(p.volume, i);
				closedTradeProc();
			}
			nt = false;
		} 		
		if (nt) {
			trade.updateTrade(data.tickDataSize(true).get() - 1);
		}
		return nt;
	}
	
	private void checkPendingOrders() {						
		for (int i = lastTick; i < data.tickDataSize(true).get(); i++) {
			boolean newTrade = false;
			double currentPrice = data.tickData().get(i).price();
			int j = 0;
			Object[] pt = pendingTrades.toArray();
			for (Object obj : pt) {
				PendingTrade p = (PendingTrade) obj;
				if (p.buy) {
					if (currentPrice >= p.price && !p.limit) {
						newTrade = executePendingOrder(p, i, newTrade);
						pendingTrades.remove(j);
						j--;
					} else if (currentPrice <= p.price && p.limit) {
						newTrade = executePendingOrder(p, i, newTrade);
						pendingTrades.remove(j);
						j--;
					}
				} else {
					if (currentPrice <= p.price && !p.limit) {
						newTrade = executePendingOrder(p, i, newTrade);
						pendingTrades.remove(j);
						j--;
					} else if (currentPrice >= p.price && p.limit) {
						newTrade = executePendingOrder(p, i, newTrade);
						pendingTrades.remove(j);
						j--;
					}
				}					
				j++;
			}
		}
	}
	
	private void closedTradeProc() {
		System.out.println(trade.toString() + '\n');	
		if (writeToFile) {
			trade.writeToFile(new File("./trades.txt"));
		}
	}
	
	public void tick() {		
		if (!trade.closed()) {
			trade.updateTrade(data.tickDataSize(true).get() - 1);			
			if (trade.closed()) {
				closedTradeProc();
				for (Chart c : charts) {
					c.disableTradeButtons();
				}
				slPrice = -1;
				tpPrice = -1;
			}
		}		
		lastTick = data.tickDataSize(true).get() - 1;
		checkPendingOrders();		
	}
	
	public void setNumDataPoints(int numDataPoints) {		
		this.numDataPoints = numDataPoints;
	}	
	
	public void setCandleStickVars(int numCandlesticks) {		
		candlestickWidth = (chartWidth / numCandlesticks) / (1 + CNDL_SPAC_COEF);
		candlestickSpacing = candlestickWidth * CNDL_SPAC_COEF;
	}	
	
	public boolean drawCandlesticks() {
		return this.drawCandlesticks;
	}
	
	public ReadOnlyBooleanProperty focusedChart() {
		return BooleanProperty.readOnlyBooleanProperty(focusedChart);		
	}
	
	public double fontSize() {
		return this.fontSize;
	}
}
