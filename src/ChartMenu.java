import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

public class ChartMenu implements CanvasNode {
	private double x;
	private double y;
	private double width;
	private double height;
	private GraphicsContext gc;
	private Chart chart;
	private boolean replayMode = false;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasButton chartFunctions;
	private CanvasButton chartSettings;
	private CanvasButton newChart;
	private CanvasButton chartType;
	private CanvasButton darkMode;
	private CanvasButton replayShortcut;
	
	private boolean functions;
	
	private EventHandler<? super MouseEvent> onMouseDragged;
	private EventHandler<? super MouseEvent> onMouseEntered;
	private EventHandler<? super MouseEvent> onMouseExited;
	private EventHandler<? super MouseEvent> onMousePressed;
	private EventHandler<? super MouseEvent> onMouseClicked;
	private EventHandler<? super MouseEvent> onMouseReleased;
	private EventHandler<? super MouseEvent> onMouseMoved;
	private EventHandler<? super ScrollEvent> onScroll;
	
	public ChartMenu(double x, double y, double width, double height, GraphicsContext gc, Chart c) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		chart = c;
		cmbvg = new ChartMenuButtonVanGoghs();
		
		chartFunctions = new CanvasButton(gc, 142, 20, x + 5, y + 5, "FUNCTIONS", 39, 14);
		chartFunctions.setVanGogh(cmbvg.menuButtonVG(chartFunctions, gc.getFont().getSize()));
		chartFunctions.setOnMouseClicked(e -> {
			chartFunctions.setOn(true);
			chartSettings.setOn(false);
			functions = true;
		});				
		
		chartSettings = new CanvasButton(gc, 142, 20, x + 153, y + 5, "SETTINGS", 45, 14);
		chartSettings.setVanGogh(cmbvg.menuButtonVG(chartSettings, gc.getFont().getSize()));
		chartSettings.setOnMouseClicked(e -> {
			chartFunctions.setOn(false);
			chartSettings.setOn(true);
			functions = false;
		});
		
		newChart = new CanvasButton(gc, 290, 20, x + 5, y + 35, "NEW CHART", 114, 14);
		newChart.setVanGogh((x2, y2, gc2) -> {
			newChart.defaultDrawButtonAlternate();
		});
		newChart.setOnMouseClicked(e -> {
			Stage s = new Stage();
			ChartPane cpane = new ChartPane(s, chart.width(), chart.height(), chart.data(), replayMode, chart.mr(), chart.mrp());			
			Scene scene = new Scene(cpane);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> cpane.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		});
		
		chartType = new CanvasButton(gc, 290, 20, x + 5, y + 60, "CANDLESTICK CHART", 39, 14);
		chartType.setVanGogh(cmbvg.toggleVG(chartType, chart.drawCandlesticks(), "LINE CHART", "CANDLESTICK CHART", gc.getFont().getSize(), gc.getFont().getSize(), 116, 90, 14, 14));
		chartType.setOnMouseClicked(e -> {
			chart.toggleChartType();
		});
		
		darkMode = new CanvasButton(gc, 290, 20, x + 5, y + 85, "DARK MODE", 39, 14);
		darkMode.setVanGogh(cmbvg.toggleVG(darkMode, Chart.darkMode(), "LIGHT MODE", "DARK MODE", gc.getFont().getSize(), gc.getFont().getSize(), 113, 114, 14, 14));
		darkMode.setOnMouseClicked(e -> {
			Chart.toggleDarkMode();
		});
		
		replayShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 110, "REPLAY SHORTCUT", 96, 14);
		replayShortcut.setVanGogh((x2, y2, gc2) -> {
			replayShortcut.defaultDrawButtonAlternate();
		});
		replayShortcut.setOnMouseClicked(e -> {
			chart.toggleMRPShortcut();
		});
				
		replayShortcut.disable();
		chartFunctions.setOn(true);
		functions = true;
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
	
	public CanvasButton replayShortcut() {
		return replayShortcut;
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
			newChart.draw();
			chartType.draw();
			darkMode.draw();
			replayShortcut.draw();
		} else {
			
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
		newChart.setX(x + 5);
		chartType.setX(x + 5);
		darkMode.setX(x + 5);
		replayShortcut.setX(x + 5);
	}

	@Override
	public void setY(double y) {
		this.y = y;
		chartFunctions.setY(y + 5);
		chartSettings.setY(y + 5);
		newChart.setY(y + 5);
		chartType.setY(y + 5);
		darkMode.setY(y + 5);
		replayShortcut.setY(y + 5);
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
