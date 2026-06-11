package com.github._7000toni.auto.chart;
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
import com.github._7000toni.auto.chart.listener.ChartHeightListener;
import com.github._7000toni.auto.chart.listener.ChartWidthListener;
import com.github._7000toni.auto.chart.menu.ChartMenu;
import com.github._7000toni.auto.dataset.DataSet;
import com.github._7000toni.auto.marketreplay.MarketReplayPane;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ImageFunctions;
import com.github._7000toni.auto.settings.ImageSettings;
import com.github._7000toni.auto.settings.Settings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Chart implements IScrollBarOwner, ICanvasWindow {
	public final static double WIDTH_EXTRA = 16;
	public final static double HEIGHT_EXTRA = 39;
	
	public final static double MIN_WIDTH = 950; 
	public final static double MIN_HEIGHT = 590; 

	public final static double HSB_WIDTH = 100;
	public final static double HSB_HEIGHT = 10;	
	
	private static ArrayList<Chart> charts = new ArrayList<Chart>();	
	private static BooleanProperty darkMode = new SimpleBooleanProperty(false);
	
	private ChartNode cn;
	
	private Canvas canvas;	
	private double width;
	private double height;
	private GraphicsContext gc;
	private HorizontalChartScrollBar hsb;
	private PriceMargin priceMargin;
	private Stage stage;	
	
	private CanvasNumberChooser volUnits;
	private CanvasNumberChooser volTens;
	
	private Tree<ICanvasNode> sceneGraph;
	private TNode<ICanvasNode> lastNode = null;
	private CanvasWrapper cw;
	private boolean dragging = false;
	private final ReentrantLock varLock = new ReentrantLock();
	TNode<ICanvasNode> menuNode;
	
	private boolean menuHidden = true;
	private CanvasButton btnMenu;
	private ChartMenu menu;
	
	public Chart(double width, double height, Stage stage, DataSet data) throws Exception {
		constructorStuff(width, height, stage, data);
	}
	
	private void constructorStuff(double widthParam, double heightParam, Stage stage, DataSet data) throws Exception {		
		this.width = widthParam;
		this.height = heightParam;
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
		
		sceneGraph = new Tree<ICanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<ICanvasNode>(cw, null));
		priceMargin = new PriceMargin(this, data.maxLength());
		hsb = new HorizontalChartScrollBar(this, 0, width - priceMargin.width(), HSB_WIDTH, HSB_HEIGHT, height - HSB_HEIGHT);	
		cn = new ChartNode(width - priceMargin.width() - ChartNode.CHT_MARGIN, height - HSB_HEIGHT - ChartNode.CHT_MARGIN*2, stage, data, this, sceneGraph);
		menu = new ChartMenu(ChartNode.CHT_MARGIN + cn.width() + priceMargin.width(), 0, 300, cn.height(), gc, this);				
		btnMenu = new CanvasButton(gc, priceMargin.width() - 2, HSB_HEIGHT + ChartNode.CHT_MARGIN - 2, width - priceMargin.width() + 1, height - HSB_HEIGHT - ChartNode.CHT_MARGIN + 1, "MENU", (priceMargin.width() - 2 - 34) / 2, 11);
		
		sceneGraph.addNode(new TNode<ICanvasNode>(hsb, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(btnMenu, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(priceMargin, sceneGraph.root()));
		menuNode = new TNode<ICanvasNode>(menu, sceneGraph.root());
		sceneGraph.addNode(menuNode);
		
		canvas.addEventFilter(Event.ANY, e -> {
			(new CanvasEventFilter(this)).canvasEventFilter(e);
		});
				
		btnMenu.setVanGogh(cn.chartButtonVanGoghs().menuButtonVG(btnMenu)); 
		btnMenu.setOnMouseClicked(e -> {
			new AnimationTimer() {
				int i = menuHidden?1:-1;
				double pm = menuHidden?priceMargin.width():priceMargin.width()+300;
				double t = pm + 300*i;
				boolean changed = false;
				@Override
				public void handle(long now) {	
					if (menuHidden && !changed) {
						menuHidden = false;
						changed = true;
					}
					pm += 50*i;
					cn.setWidth(cn.width() - 50*i);
					cn.setCandleStickVars(cn.numCandlesticks());
					btnMenu.setWidth(priceMargin.width() - 2);
					btnMenu.setHeight(HSB_HEIGHT + ChartNode.CHT_MARGIN - 2);
					btnMenu.setX(ChartNode.CHT_MARGIN + cn.width() + 1);
					cn.chartTypeShortcut().setX(ChartNode.CHT_MARGIN + cn.width() - 15);
					
					menu.setX(ChartNode.CHT_MARGIN + cn.width() + priceMargin.width());
					
					hsb.setMaxPos(width - pm); 					
					
					if (cn.tradeButtons() != null) {
						cn.tradeButtons().resetButtons();
					}
					
					if (!cn.drawCandlesticks().get()) {
						hsb.setPosition((width - hsb.sbWidth() - pm) * ((double)cn.startIndex() /(data.tickDataSize(cn.replayMode()).get() - (cn.numDataPoints() - 1) * ChartNode.END_MARGIN_COEF)), false);
					} else {
						hsb.setPosition((width - hsb.sbWidth() - pm) * ((double)cn.startIndex() /(data.m1CandlesDataSize(cn.replayMode()).get() - cn.numCandlesticks() * ChartNode.END_MARGIN_COEF)), false);
					}
					cn.setKeepStartIndex(true);
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
		
		Chart.charts.add(this);
		menu.setFunctionsMenuSceneGraph(sceneGraph, menuNode);	
		
		draw();
	}
	
	public Stage stage() {
		return stage;
	}
	
	public static ArrayList<Chart> charts() {
		return charts;
	}
	
	public Canvas canvas() {
		return this.canvas;
	}
	
	public GraphicsContext graphicsContext() {
		return this.gc;
	}
	
	public double width() {
		return width;
	}
	
	public double height() {
		return height;
	}
	
	public void close() {
		cn.disableReplayMode();
		charts.remove(this);
		stage.close();
	}
	
	public static ReadOnlyBooleanProperty darkMode() {
		return BooleanProperty.readOnlyBooleanProperty(darkMode);
	}
	
	public static void closeAll(String name, boolean replayOnly) {
		Object[] chts = charts.toArray();
		for (Object c : chts) {
			ChartNode cht = ((Chart)c).chartNode();
			if (cht.name().equals(name)) {				
				if (replayOnly && !cht.replayMode()) {
					continue;
				}
				cht.chart().close();
			}
		}
	}
	
	public static void addChart(Chart c) {
		charts.add(c);
	}
	
	public ChartMenu menu() {
		return menu;
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
	
	public static void drawCharts(String name) {
		for (Chart c : charts) {
			if (name == null || c.chartNode().name().equals(name) && (c.chartNode().focusedChart().get() || !c.chartNode().skipDraw().get())) {
				c.drawChart();
			}
		}
	}
	
	public ChartNode chartNode() {
		return cn;
	}
	
	public static void toggleDarkMode() {
		darkMode.set(!darkMode.get());
		Settings.saveDarkMode();
		for (Chart c : charts) {
			if (c.chartNode().replayMode()) {					
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
	
	private void setNumberChooserColours() {
		volTens.resetColours();
		volUnits.resetColours();
	}
	
	private void drawFrame() {
		gc.clearRect(0, 0, width, height);		
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
		gc.fillRect(0, 0, width, height);
		if (ImageSettings.draw().get()) {
			ImageFunctions.drawImage(gc, ImageSettings.image(), 0, 0, width, height);
		}
		if (Chart.darkMode().get()) {	
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}				
		gc.strokeRect(ChartNode.CHT_MARGIN, ChartNode.CHT_MARGIN, cn.width(), cn.height());
		gc.strokeRect(ChartNode.CHT_MARGIN + cn.width(), ChartNode.CHT_MARGIN + cn.height(), priceMargin.width(), Chart.HSB_HEIGHT + ChartNode.CHT_MARGIN);
	}
	
	private void drawChart() {
		drawFrame();
		cn.drawChart();
		btnMenu.draw();
		if (!menuHidden) {
			menu.draw();
		}
	}
	
	@Override
	public void draw() {		
		drawCharts(cn.name());
	}	
	
	public PriceMargin priceMargin() {
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
