import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
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
	
	private CanvasButton saveHst;
	private CanvasButton replayShortcut;
	private CanvasButton toggleShortReport;
	private CanvasButton saveMRHst;
	
	private BooleanProperty mrRecentlySaved = new SimpleBooleanProperty(false);
	
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
			generalFunctions.alternateDraw(gc.getFont());
		});		
		
		newChart = new CanvasButton(gc, 290, 20, x + 5, y + 85, "NEW CHART");
		newChart.setVanGogh((x2, y2, gc2) -> {
			newChart.alternateDraw(gc.getFont());
		});
		newChart.setOnMouseClicked(e -> {
			Stage s = new Stage();
			if (Main.icon() != null) {
				s.getIcons().add(Main.icon());
			}
			s.setTitle(chart.name());
			ChartPane cpane = new ChartPane(s, chart.width(), chart.height(), chart.data(), chart.replayMode(), chart.mr(), chart.mrp());			
			Scene scene = new Scene(cpane);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> cpane.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		});
		
		chartType = new CanvasButton(gc, 290, 20, x + 5, y + 110, "CANDLESTICK CHART");
		chartType.setVanGogh(cmbvg.toggleVG(chartType, chart.drawCandlesticks(), "LINE CHART", "CANDLESTICK CHART"));
		chartType.setOnMouseClicked(e -> {
			chart.toggleChartType();
		});
		
		darkMode = new CanvasButton(gc, 290, 20, x + 5, y + 135, "DARK MODE");
		darkMode.setVanGogh(cmbvg.toggleVG(darkMode, Chart.darkMode(), "LIGHT MODE", "DARK MODE"));
		darkMode.setOnMouseClicked(e -> {
			Chart.toggleDarkMode();
		});
		
		chartTypeShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 160, "CHART TYPE SHORTCUT");
		chartTypeShortcut.setVanGogh((x2, y2, gc2) -> {
			chartTypeShortcut.alternateDraw(gc.getFont());
		});
		chartTypeShortcut.setOnMouseClicked(e -> {
			chart.toggleChartTypeShortcut();
		});
		
		initHst = new CanvasButton(gc, 290, 20, x + 5, y + 185, "LOAD HISTORY");
		initHst.setVanGogh(cmbvg.initHstDraw(initHst, chart));
		initHst.setOnMouseClicked(e -> {
			chart.initHst();
		});;
		
		toggleHst = new CanvasButton(gc, 290, 20, x + 5, y + 210, "SHOW TRADE HISTORY");
		toggleHst.setVanGogh(cmbvg.toggleVG(toggleHst, chart.plotHst(), "HIDE TRADE HISTORY", "SHOW TRADE HISTORY"));
		toggleHst.setOnMouseClicked(e -> {
			chart.toggleHst();
		});;
		
		marketReplay = new CanvasLabel(gc, 290, 20, x + 5, y + 235, "MARKET REPLAY");
		marketReplay.setVanGogh((x2, y2, gc2) -> {
			marketReplay.alternateDraw(gc.getFont());
		});
		
		replayShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 260, "REPLAY SHORTCUT");
		replayShortcut.setVanGogh((x2, y2, gc2) -> {
			replayShortcut.alternateDraw(gc.getFont());
		});
		replayShortcut.setOnMouseClicked(e -> {
			chart.toggleMRPShortcut();
		});;		
		
		saveHst = new CanvasButton(gc, 290, 20, x + 5, y + 285, "DON'T SAVE TRADE HISTORY");
		saveHst.setVanGogh(cmbvg.toggleVG(saveHst, MarketReplay.writeToFile(), "DON'T SAVE TRADE HISTORY", "SAVE TRADE HISTORY"));
		saveHst.setOnMouseClicked(e -> {
			MarketReplay.toggleWriteToFile();
		});;
		
		toggleShortReport = new CanvasButton(gc, 290, 20, x + 5, y + 310, "WRITE LONG REPORT");
		toggleShortReport.setVanGogh(cmbvg.toggleVG(toggleShortReport, Trade.shortReport(), "WRITE LONG REPORT", "WRITE SHORT REPORT"));
		toggleShortReport.setOnMouseClicked(e -> {
			Trade.toggleShortReport();
		});;
		
		saveMRHst = new CanvasButton(gc, 290, 20, x + 5, y + 335, "SAVE LOADABLE HISTORY");
		saveMRHst.setVanGogh(cmbvg.toggleVG(saveMRHst, mrRecentlySaved, "SAVED", "SAVE LOADABLE HISTORY"));
		saveMRHst.setOnMouseClicked(e -> {
			chart.marketReplay().trade().writeHistoryToFile(chart.name());
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
		marketReplay.draw();
		replayShortcut.draw();
		saveHst.draw();
		toggleShortReport.draw();
		saveMRHst.draw();
	}
	
	public void setGeneralFunctionsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(generalFunctions, menuNode)); 
		sceneGraph.addNode(new TNode<ICanvasNode>(newChart, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(chartType, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(darkMode, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(chartTypeShortcut, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(initHst, menuNode)); 
		sceneGraph.addNode(new TNode<ICanvasNode>(toggleHst, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(marketReplay, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(replayShortcut, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(saveHst, menuNode)); 
		sceneGraph.addNode(new TNode<ICanvasNode>(toggleShortReport, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(saveMRHst, menuNode));
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
		marketReplay.setX(x + 5);
		replayShortcut.setX(x + 5);
		saveHst.setX(x + 5);
		toggleShortReport.setX(x + 5);
		saveMRHst.setX(x + 5);	
		
		this.x = x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
		
		generalFunctions.setY(y + 35);			
		
		newChart.setY(y + 85);
		chartType.setY(y + 110);
		darkMode.setY(y + 135);
		chartTypeShortcut.setY(y + 185);		
		initHst.setY(y + 210);
		toggleHst.setY(y + 235);
		marketReplay.setY(y + 260);
		replayShortcut.setY(y + 285);
		saveHst.setY(y + 310);
		toggleShortReport.setY(y + 335);
		saveMRHst.setY(y + 360);
	}
}
