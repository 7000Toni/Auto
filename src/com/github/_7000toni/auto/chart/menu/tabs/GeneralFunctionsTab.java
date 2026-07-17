package com.github._7000toni.auto.chart.menu.tabs;
import com.github._7000toni.auto.Main;
import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.chart.ChartPane;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.marketreplay.MarketReplay;
import com.github._7000toni.auto.marketreplay.trade.Trade;
import com.github._7000toni.auto.miscellaneous.Round;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GeneralFunctionsTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel generalFunctions;
	private CanvasLabel marketReplay;
	
	private CanvasButton newChart;
	private CanvasButton chartType;
	private CanvasButton darkMode;
	private CanvasButton chartTypeShortcut;	
	private CanvasButton initHst;
	private CanvasButton toggleHst;
	private CanvasButton toggleSkipDraw;
	private CanvasButton toggleCrosshair;
	
	private CanvasButton saveHst;
	private CanvasButton replayShortcut;
	private CanvasButton toggleShortReport;
	private CanvasButton saveMRHst;
	
	private BooleanProperty mrRecentlySaved = new SimpleBooleanProperty(false);
	private boolean replayMode = false;
	private boolean resetSceneGraph = false;
	
	public GeneralFunctionsTab(double x, double y, double width, double height, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		this.cmbvg = cmbvg;
				
		initGeneralFunctionsMenu();
		setReplayMode(false);
	}
	
	private void initGeneralFunctionsMenu() {
		generalFunctions = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "GENERAL FUNCTIONS");
		generalFunctions.setVanGogh((x2, y2, gc2) -> {
			generalFunctions.defaultDraw(gc.getFont());
		});		
		
		newChart = new CanvasButton(gc, 290, 20, x + 5, y + 85, "NEW CHART");
		newChart.setVanGogh((x2, y2, gc2) -> {
			newChart.defaultDraw(gc.getFont());
		});
		newChart.setOnMouseClicked(e -> {
			Stage s = new Stage();
			if (Main.icon() != null) {
				s.getIcons().add(Main.icon());
			}
			s.setTitle(chart.chartNode().name());
			ChartPane cpane = new ChartPane(s, chart.width(), chart.height(), chart.chartNode().data(), chart.chartNode().replayMode(), chart.chartNode().mr(), chart.chartNode().mrp());			
			Scene scene = new Scene(cpane);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> cpane.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		});
		
		chartType = new CanvasButton(gc, 290, 20, x + 5, y + 110, "CANDLESTICK CHART");
		chartType.setVanGogh(cmbvg.toggleVG(chartType, chart.chartNode().drawCandlesticks(), "LINE CHART", "CANDLESTICK CHART"));
		chartType.setOnMouseClicked(e -> {
			chart.chartNode().toggleChartType();
		});
		
		darkMode = new CanvasButton(gc, 290, 20, x + 5, y + 135, "DARK MODE");
		darkMode.setVanGogh(cmbvg.toggleVG(darkMode, Chart.darkMode(), "LIGHT MODE", "DARK MODE"));
		darkMode.setOnMouseClicked(e -> {
			Chart.toggleDarkMode();
		});
		
		chartTypeShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 160, "CHART TYPE SHORTCUT");
		chartTypeShortcut.setVanGogh((x2, y2, gc2) -> {
			chartTypeShortcut.defaultDraw(gc.getFont());
		});
		chartTypeShortcut.setOnMouseClicked(e -> {
			chart.chartNode().toggleChartTypeShortcut();
		});
		
		initHst = new CanvasButton(gc, 290, 20, x + 5, y + 185, "LOAD HISTORY");
		initHst.setVanGogh(cmbvg.initHstDraw(initHst, chart));
		initHst.setOnMouseClicked(e -> {
			chart.chartNode().initHst();
		});;
		
		toggleHst = new CanvasButton(gc, 290, 20, x + 5, y + 210, "SHOW TRADE HISTORY");
		toggleHst.setVanGogh(cmbvg.toggleVG(toggleHst, chart.chartNode().plotHst(), "HIDE TRADE HISTORY", "SHOW TRADE HISTORY"));
		toggleHst.setOnMouseClicked(e -> {
			chart.chartNode().toggleHst();
		});;
		
		toggleSkipDraw = new CanvasButton(gc, 290, 20, x + 5, y + 235, "SKIP DRAW");
		toggleSkipDraw.setVanGogh(cmbvg.toggleVG(toggleSkipDraw, chart.chartNode().skipDraw(), "DON'T SKIP DRAW", "SKIP DRAW"));
		toggleSkipDraw.setOnMouseClicked(e -> {
			chart.chartNode().toggleSkipDraw();
		});;
		
		toggleCrosshair = new CanvasButton(gc, 290, 20, x + 5, y + 260, "DON'T DRAW CROSSHAIR");
		toggleCrosshair.setVanGogh(cmbvg.toggleVG(toggleCrosshair, ChartNode.drawCrosshair(), "DON'T DRAW CROSSHAIR", "DRAW CROSSHAIR"));
		toggleCrosshair.setOnMouseClicked(e -> {
			ChartNode.toggleDrawCrosshair();
		});;
		
		marketReplay = new CanvasLabel(gc, 290, 20, x + 5, y + 285, "MARKET REPLAY");
		marketReplay.setVanGogh((x2, y2, gc2) -> {
			marketReplay.defaultDraw(gc.getFont());
		});
		
		replayShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 310, "REPLAY SHORTCUT");
		replayShortcut.setVanGogh((x2, y2, gc2) -> {
			replayShortcut.defaultDraw(gc.getFont());
		});
		replayShortcut.setOnMouseClicked(e -> {
			chart.chartNode().toggleMRPShortcut();
		});;		
		
		saveHst = new CanvasButton(gc, 290, 20, x + 5, y + 335, "DON'T SAVE TRADE HISTORY");
		saveHst.setVanGogh(cmbvg.toggleVG(saveHst, MarketReplay.writeToFile(), "DON'T SAVE TRADE HISTORY", "SAVE TRADE HISTORY"));
		saveHst.setOnMouseClicked(e -> {
			MarketReplay.toggleWriteToFile();
		});;
		
		toggleShortReport = new CanvasButton(gc, 290, 20, x + 5, y + 360, "WRITE LONG REPORT");
		toggleShortReport.setVanGogh(cmbvg.toggleVG(toggleShortReport, Trade.shortReport(), "WRITE LONG REPORT", "WRITE SHORT REPORT"));
		toggleShortReport.setOnMouseClicked(e -> {
			Trade.toggleShortReport();
		});;
		
		saveMRHst = new CanvasButton(gc, 290, 20, x + 5, y + 385, "SAVE LOADABLE HISTORY");
		saveMRHst.setVanGogh(cmbvg.toggleVG(saveMRHst, mrRecentlySaved, "SAVED", "SAVE LOADABLE HISTORY"));
		saveMRHst.setOnMouseClicked(e -> {
			chart.chartNode().marketReplay().trade().writeHistoryToFile(chart.chartNode().name());
			mrRecentlySaved.set(true);
			new AnimationTimer() {
				private long init = 0;				
				@Override
				public void handle(long now) {
					if (init == 0) {
						init = now;
					}
					if ((now - init) / HorizontalScrollBar.NANO_TO_MILLI > 1500) {
						mrRecentlySaved.set(false);
						chart.draw();
						this.stop();
					}
				}
			}.start();
		});;				
	}
	
	private void drawGeneralFunctionsMenu() {
		generalFunctions.draw();
		newChart.draw();
		chartType.draw();
		darkMode.draw();
		chartTypeShortcut.draw();		
		initHst.draw();
		toggleHst.draw();
		toggleSkipDraw.draw();
		toggleCrosshair.draw();
		if (replayMode) {
			marketReplay.draw();
			replayShortcut.draw();
			saveHst.draw();
			toggleShortReport.draw();
			saveMRHst.draw();
		}
		drawNetProfit();
	}
	
	private void drawNetProfit() {
		if (!chart.chartNode().replayMode()) {
			return;
		}
		if (Chart.darkMode().get()) {
			gc.setFill(Color.WHITE);			
		} else {
			gc.setFill(Color.BLACK);
		}
		gc.fillText("NET PROFIT: " + Round.round(Trade.net(), 2), x + 7, y + 425);
	}
	
	public void setGeneralFunctionsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(generalFunctions, menuNode)); 
		sceneGraph.addNode(new TNode<ICanvasNode>(newChart, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(chartType, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(darkMode, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(chartTypeShortcut, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(initHst, menuNode)); 
		sceneGraph.addNode(new TNode<ICanvasNode>(toggleHst, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(toggleSkipDraw, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(toggleCrosshair, menuNode));
		if (replayMode) {
			sceneGraph.addNode(new TNode<ICanvasNode>(marketReplay, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(replayShortcut, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(saveHst, menuNode)); 
			sceneGraph.addNode(new TNode<ICanvasNode>(toggleShortReport, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(saveMRHst, menuNode));
		}
	}
	
	private void disableReplayButtons() {
		replayShortcut.disable();
		saveHst.disable();
		toggleShortReport.disable();
		saveMRHst.disable();
	}
	
	private void enableReplayButtons() {
		replayShortcut.enable();
		saveHst.enable();
		toggleShortReport.enable();
		saveMRHst.enable();
	}
	
	public void setReplayMode(boolean replayMode) {
		this.replayMode = replayMode;
		this.resetSceneGraph = true;
		if (replayMode) {
			enableReplayButtons();			
		} else {
			disableReplayButtons();
		}
	}
	
	public Chart chart() {
		return chart;
	}
	
	public CanvasButton newChart() {
		return newChart;
	}
	
	public CanvasButton chartType() {
		return chartType;
	}
	
	public CanvasButton darkMode() {
		return darkMode;
	}
	
	public CanvasButton chartTypeShortcut() {
		return chartTypeShortcut;
	}
	
	public CanvasButton replayShortcut() {
		return replayShortcut;
	}

	@Override
	public void draw() {	
		if (chart.chartMenu().functions() && resetSceneGraph) {
			chart.chartMenu().setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
			resetSceneGraph = false;
		}
		drawGeneralFunctionsMenu();
	}

	@Override
	public void setX(double x) {	
		generalFunctions.setX(x + 5);
		
		newChart.setX(x + 5);
		chartType.setX(x + 5);
		darkMode.setX(x + 5);
		chartTypeShortcut.setX(x + 5);
		initHst.setX(x + 5);
		toggleHst.setX(x + 5);
		toggleSkipDraw.setX(x + 5);
		toggleCrosshair.setX(x + 5);
		marketReplay.setX(x + 5);
		replayShortcut.setX(x + 5);
		saveHst.setX(x + 5);
		toggleShortReport.setX(x + 5);
		saveMRHst.setX(x + 5);	
		
		this.x = x;
	}

	@Override
	public void setY(double y) {				
		generalFunctions.setY(y + 35);			
		
		newChart.setY(y + 85);
		chartType.setY(y + 110);
		darkMode.setY(y + 135);
		chartTypeShortcut.setY(y + 160);		
		initHst.setY(y + 185);
		toggleHst.setY(y + 210);
		toggleSkipDraw.setY(y + 235);
		toggleCrosshair.setY(y + 260);
		marketReplay.setY(y + 285);
		replayShortcut.setY(y + 310);
		saveHst.setY(y + 335);
		toggleShortReport.setY(y + 360);
		saveMRHst.setY(y + 385);
		
		this.y = y;
	}
}
