import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ChartMenu implements CanvasNode {
	private double x;
	private double y;
	private double width;
	private double height;
	private GraphicsContext gc;
	private Chart chart;
	private boolean replayMode = false;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel general;
	private CanvasLabel timeFrames;
	
	private CanvasButton chartFunctions;
	private CanvasButton chartSettings;
	private CanvasButton newChart;
	private CanvasButton chartType;
	private CanvasButton darkMode;
	private CanvasButton chartTypeShortcut;
	private CanvasButton replayShortcut;
	
	private boolean functions;
	
	private ColourPicker colourPicker;
	private CanvasButton reset;
	private CanvasButton defaultColours;
	private CanvasButton save;
	private CanvasLabel saved;
	private boolean recentlySaved = false;
	private ArrayList<CanvasButton> colourButtons;
	
	private EventHandler<? super MouseEvent> onMouseDragged;
	private EventHandler<? super MouseEvent> onMouseEntered;
	private EventHandler<? super MouseEvent> onMouseExited;
	private EventHandler<? super MouseEvent> onMousePressed;
	private EventHandler<? super MouseEvent> onMouseClicked;
	private EventHandler<? super MouseEvent> onMouseReleased;
	private EventHandler<? super MouseEvent> onMouseMoved;
	private EventHandler<? super ScrollEvent> onScroll;
	
	public enum ColourButtonIndices {
		UP_CANDLESTICK_FILL(0, "UP CANDLESTICK FILL"),
		UCF_PREVIEW(0, null),
		UP_CANDLESTICK_STROKE(2, "UP CANDLESTICK STROKE"),
		UCS_PREVIEW(2, null),
		DOWN_CANDLESTICK_FILL(1, "DOWN CANDLESTICK FILL"),	
		DCF_PREVIEW(1, null),
		DOWN_CANDLESTICK_STROKE(3, "DOWN CANDLESTICK STROKE"),
		DCS_PREVIEW(3, null),
		LIGHT_MODE_LINE(4, "LIGHT MODE LINE"),
		LML_PREVIEW(4, null),
		DARK_MODE_LINE(5, "DARK MODE LINE"),
		DML_PREVIEW(5, null),
		LIGHT_MODE_MENU_BACKGROUND(6, "LIGHT MODE MENU BACKGROUND"),
		LMMB_PREVIEW(6, null),
		DARK_MODE_MENU_BACKGROUND(7, "DARK MODE MENU BACKGROUND"),
		DMMB_PREVIEW(7, null),
		LIGHT_MODE_CHART_BACKGROUND(8, "LIGHT MODE CHART BACKGROUND"),
		LMCB_PREVIEW(7, null),
		DARK_MODE_CHART_BACKGROUND(9, "DARK MODE CHART BACKGROUND"),
		DMCB_PREIVEW(7, null);
		
		public final int index;
		public final String text;
		
		private ColourButtonIndices(int index, String text) {
			this.index = index;
			this.text = text;
		}
	}
	
	public ChartMenu(double x, double y, double width, double height, GraphicsContext gc, Chart chart) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		cmbvg = new ChartMenuButtonVanGoghs();
		
		initFunctionsMenu();
		initSettingsMenu();		
	}
	
	private void initFunctionsMenu() {
		general = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "GENERAL");
		general.setVanGogh((x2, y2, gc2) -> {
			general.alternateDraw(gc.getFont());
		});		
		
		chartFunctions = new CanvasButton(gc, 142, 20, x + 5, y + 5, "FUNCTIONS", 39, 14);
		chartFunctions.setVanGogh(cmbvg.menuButtonVG(chartFunctions, gc.getFont().getSize()));
		chartFunctions.setOnMouseClicked(e -> {
			chartFunctions.setOn(true);
			chartSettings.setOn(false);
			functions = true;
			setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});				
		
		chartSettings = new CanvasButton(gc, 142, 20, x + 153, y + 5, "SETTINGS", 45, 14);
		chartSettings.setVanGogh(cmbvg.menuButtonVG(chartSettings, gc.getFont().getSize()));
		chartSettings.setOnMouseClicked(e -> {
			chartFunctions.setOn(false);
			chartSettings.setOn(true);
			functions = false;
			setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});
		
		newChart = new CanvasButton(gc, 290, 20, x + 5, y + 60, "NEW CHART", 114, 14);
		newChart.setVanGogh((x2, y2, gc2) -> {
			newChart.alternateDraw(gc.getFont());
		});
		newChart.setOnMouseClicked(e -> {
			Stage s = new Stage();
			ChartPane cpane = new ChartPane(s, chart.width(), chart.height(), chart.data(), replayMode, chart.mr(), chart.mrp());			
			Scene scene = new Scene(cpane);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> cpane.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		});
		
		chartType = new CanvasButton(gc, 290, 20, x + 5, y + 85, "CANDLESTICK CHART", 39, 14);
		chartType.setVanGogh(cmbvg.toggleVG(chartType, chart.drawCandlesticks(), "LINE CHART", "CANDLESTICK CHART", gc.getFont().getSize(), gc.getFont().getSize(), 116, 90, 14, 14));
		chartType.setOnMouseClicked(e -> {
			chart.toggleChartType();
		});
		
		darkMode = new CanvasButton(gc, 290, 20, x + 5, y + 110, "DARK MODE", 39, 14);
		darkMode.setVanGogh(cmbvg.toggleVG(darkMode, Chart.darkMode(), "LIGHT MODE", "DARK MODE", gc.getFont().getSize(), gc.getFont().getSize(), 113, 114, 14, 14));
		darkMode.setOnMouseClicked(e -> {
			Chart.toggleDarkMode();
		});
		
		chartTypeShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 135, "CHART TYPE SHORTCUT");
		chartTypeShortcut.setVanGogh((x2, y2, gc2) -> {
			chartTypeShortcut.alternateDraw(gc.getFont());
		});
		chartTypeShortcut.setOnMouseClicked(e -> {
			chart.toggleChartTypeShortcut();
		});
		
		replayShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 160, "REPLAY SHORTCUT", 96, 14);
		replayShortcut.setVanGogh((x2, y2, gc2) -> {
			replayShortcut.alternateDraw(gc.getFont());
		});
		replayShortcut.setOnMouseClicked(e -> {
			chart.toggleMRPShortcut();
		});
		
		timeFrames = new CanvasLabel(gc, 290, 20, x + 5, y + 185, "TIME FRAMES");
		timeFrames.setVanGogh((x2, y2, gc2) -> {
			timeFrames.alternateDraw(gc.getFont());
		});
				
		replayShortcut.disable();
		chartFunctions.setOn(true);
		functions = true;
	}
	
	private void initSettingsMenu() {
		colourPicker = new ColourPicker(x + 5, y + 35, 290, 165, gc);
		
		colourButtons = new ArrayList<CanvasButton>();
		initColourButtons();
		
		reset = new CanvasButton(gc, 142.5, 20, x + 5, y + 455, "RESET");
		reset.setVanGogh((x2, y2, gc2) -> {
			reset.alternateDraw(gc.getFont());
		});
		reset.setOnMouseClicked(e -> {
			Settings.loadSettings();
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		defaultColours = new CanvasButton(gc, 142.5, 20, x + 152.5, y + 455, "DEFAULT");
		defaultColours.setVanGogh((x2, y2, gc2) -> {
			defaultColours.alternateDraw(gc.getFont());
		});
		defaultColours.setOnMouseClicked(e -> {
			ColourSettings.defaultColours();
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		save = new CanvasButton(gc, 290, 20, x + 5, y + 480, "SAVE");
		save.setVanGogh((x2, y2, gc2) -> {
			save.alternateDraw(gc.getFont());
		});
		save.setOnMouseClicked(e -> {
			Settings.saveSettings();
			recentlySaved = true;
			new AnimationTimer() {
				private long init = 0;
				
				@Override
				public void handle(long now) {
					if (init == 0) {
						init = now;
					}
					if ((now - init) / HorizontalScrollBar.NANO_TO_MILLI > 1500) {
						recentlySaved = false;
						chart.draw();
						this.stop();
					}
				}
			}.start();
		});
		saved = new CanvasLabel(gc, 290, 20, x + 5, y + 505, "SAVED"); 
		saved.setVanGogh((x2, y2, gc2) -> {
			saved.alternateDraw(gc.getFont());
		});
	}
	
	private void initColourButtons() {
		for (int i = 0; i < ColourSettings.colours().size(); i++) {
			CanvasButton javaisannyoing = new CanvasButton(gc, 265, 20, x + 5, y + 205 + 25*i, ColourButtonIndices.values()[i*2].text);
			colourButtons.add(javaisannyoing);
			javaisannyoing.setVanGogh((x2, y2, gc2) -> {
				javaisannyoing.alternateDraw(gc2.getFont());
			});
			setMouseEvent(javaisannyoing, i);
			colourButtons.add(new CanvasButton(gc, 20, 20, x + 275, y + 205 + 25*i, null));
			colourButtons.get(i*2+1).setVanGogh(cmbvg.colourPreviewVG(colourButtons.get(i*2+1), i));
		}
	}
	
	private void setMouseEvent(CanvasButton cb, int index) {
		cb.setOnMouseClicked(e -> {
			ColourSettings.colours().set(index, colourPicker.finalColour());
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
	}
	
	public void setFunctionsMenuSceneGraph(Tree<CanvasNode> sceneGraph, TNode<CanvasNode> menuNode) {
		chart.varLock().lock();
		try {
			menuNode.removeAllChildren();
			sceneGraph.addNode(new TNode<CanvasNode>(chartFunctions, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(chartSettings, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(general, menuNode)); 
			sceneGraph.addNode(new TNode<CanvasNode>(newChart, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(chartType, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(darkMode, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(chartTypeShortcut, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(replayShortcut, menuNode)); 
			sceneGraph.addNode(new TNode<CanvasNode>(timeFrames, menuNode)); 
		} finally {
			chart.varLock().unlock();
		}
	}
	
	public void setSettingsMenuSceneGraph(Tree<CanvasNode> sceneGraph, TNode<CanvasNode> menuNode) {
		chart.varLock().lock();
		try {
			menuNode.removeAllChildren();
			sceneGraph.addNode(new TNode<CanvasNode>(chartFunctions, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(chartSettings, menuNode));
			TNode<CanvasNode> colourPickerNode = new TNode<CanvasNode>(colourPicker, menuNode);
			sceneGraph.addNode(colourPickerNode);
			sceneGraph.addNode(new TNode<CanvasNode>(colourPicker.hsb(), colourPickerNode));
			sceneGraph.addNode(new TNode<CanvasNode>(colourPicker.usb(), colourPickerNode));
			for (CanvasButton c : colourButtons) {
				sceneGraph.addNode(new TNode<CanvasNode>(c, menuNode));
			}
			sceneGraph.addNode(new TNode<CanvasNode>(reset, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(defaultColours, menuNode));
			sceneGraph.addNode(new TNode<CanvasNode>(save, menuNode));
		} finally {
			chart.varLock().unlock();
		}
	}
	
	public CanvasButton chartFunctions() {
		return chartFunctions;
	}
	
	public CanvasButton chartSettings() {
		return chartSettings;
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
	
	public ColourPicker colourPicker() {
		return colourPicker;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	public void setReplayMode(boolean replayMode) {
		this.replayMode = replayMode;
		if (this.replayMode) {
			replayShortcut.enable();
		} else {
			replayShortcut.disable();
		}
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
		if (onMouseDragged == null) {
			return;
		}
		onMouseDragged.handle(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onMouseEntered == null) {
			return;
		}
		onMouseEntered.handle(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		if (onMouseExited == null) {
			return;
		}
		onMouseExited.handle(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		if (onMousePressed == null) {
			return;
		}
		onMousePressed.handle(e);
	}

	@Override
	public void onMouseClicked(MouseEvent e) {			
		if (onMouseClicked == null) {
			return;
		}
		onMouseClicked.handle(e);	
	}
	
	@Override
	public void onMouseReleased(MouseEvent e) {	
		if (onMouseReleased == null) {
			return;
		}
		onMouseReleased.handle(e);		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		if (onMouseMoved == null) {
			return;
		}
		onMouseMoved.handle(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		if (onScroll == null) {
			return;
		}
		onScroll.handle(e);
	}

	@Override
	public void setOnMouseDragged(EventHandler<? super MouseEvent> e) {
		onMouseDragged = e;
	}

	@Override
	public void setOnMouseEntered(EventHandler<? super MouseEvent> e) {
		onMouseEntered = e;
	}

	@Override
	public void setOnMouseExited(EventHandler<? super MouseEvent> e) {
		onMouseExited = e;
	}

	@Override
	public void setOnMousePressed(EventHandler<? super MouseEvent> e) {
		onMousePressed = e;
	}

	@Override
	public void setOnMouseClicked(EventHandler<? super MouseEvent> e) {
		onMouseClicked = e;
	}

	@Override
	public void setOnMouseReleased(EventHandler<? super MouseEvent> e) {
		onMouseReleased = e;
	}

	@Override
	public void setOnMouseMoved(EventHandler<? super MouseEvent> e) {
		onMouseMoved = e;
	}

	@Override
	public void setOnScroll(EventHandler<? super ScrollEvent> e) {
		onScroll = e;
	}

	@Override
	public boolean onNode(double x, double y) {
		if (x >= this.x && x < this.x + width && y >= this.y && y < this.y + height) {
			return true;
		}
		return false;
	}

	@Override
	public void draw() {
		chartFunctions.draw();
		chartSettings.draw();
		if (functions) {		
			general.draw();
			newChart.draw();
			chartType.draw();
			darkMode.draw();
			chartTypeShortcut.draw();
			replayShortcut.draw();
			//timeFrames.draw();
		} else {
			colourPicker.draw();
			for (CanvasButton c : colourButtons) {
				c.draw();
			}	
			reset.draw();
			defaultColours.draw();
			save.draw();
			if (recentlySaved) {
				saved.draw();
			}
		}
	}

	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}

	@Override
	public void setGraphicsContext(GraphicsContext gc) {
		this.gc = gc;		
	}

	@Override
	public void setX(double x) {
		this.x = x;
		chartFunctions.setX(x + 5);
		chartSettings.setX(x + 152.5);
		general.setX(x + 5);
		newChart.setX(x + 5);
		chartType.setX(x + 5);
		darkMode.setX(x + 5);
		chartTypeShortcut.setX(x + 5);
		replayShortcut.setX(x + 5);
		timeFrames.setX(x + 5);
		
		colourPicker.setX(x + 5);
		int i = 0;
		while (i < colourButtons.size()) {
			colourButtons.get(i).setX(x + 5);
			colourButtons.get(i + 1).setX(x + 275);
			i += 2;
		}
		reset.setX(x + 5);
		defaultColours.setX(x + 152.5);
		save.setX(x + 5);
		saved.setX(x + 5);
	}

	@Override
	public void setY(double y) {
		this.y = y;
		chartFunctions.setY(y + 5);
		chartSettings.setY(y + 5);
		general.setY(y + 35);
		newChart.setY(y + 60);
		chartType.setY(y + 85);
		darkMode.setY(y + 110);
		chartTypeShortcut.setY(y + 135);
		replayShortcut.setY(y + 160);
		timeFrames.setY(y + 185);
		
		colourPicker.setY(y + 35);
		for (int i = 0; i < ColourSettings.colours().size(); i++) {
			colourButtons.get(i*2).setY(y + 205 + 25*i);
			colourButtons.get(i*2+1).setY(y + 205 + 25*i);
		}
		reset.setY(y + 455);
		defaultColours.setY(y + 455);
		save.setY(y + 480);
		saved.setY(y + 505);
	}

	@Override
	public double x() {
		return y;
	}

	@Override
	public double y() {
		return x;
	}

}
