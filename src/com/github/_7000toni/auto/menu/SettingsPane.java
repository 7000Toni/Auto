package com.github._7000toni.auto.menu;

import java.util.concurrent.locks.ReentrantLock;

import com.github._7000toni.auto.canvasnode.CanvasEventFilter;
import com.github._7000toni.auto.canvasnode.CanvasWrapper;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasWindow;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenu;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SettingsPane extends GridPane implements ICanvasWindow {
	private static double[] xP = new double[4];
	private static double[] yP = new double[4];
	
	private Tree<ICanvasNode> sceneGraph;
	private TNode<ICanvasNode> lastNode = null;
	private ICanvasNode lastFocused = null;
	private CanvasWrapper cw;
	private boolean dragging = false;
	private final ReentrantLock varLock = new ReentrantLock();
	private TNode<ICanvasNode> menuNode;
	private CanvasEventFilter cef;
	
	private Stage stage;
	private Canvas canvas;
	private ChartMenu menu;
	private double width;
	private double height;
	
	public SettingsPane(Stage stage, double width, double height) {
		this.stage = stage;
		this.width = width;
		this.height = height;
		canvas = new Canvas(width, height);
		
		sceneGraph = new Tree<ICanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);		
		sceneGraph.addNode(new TNode<ICanvasNode>(cw, null));
		cef = new CanvasEventFilter(this);
		canvas.addEventFilter(Event.ANY, e -> {
			cef.canvasEventFilter(e);
		});
		
		menu = new ChartMenu(0, 0, width, height, canvas.getGraphicsContext2D(), this);
		menuNode = new TNode<ICanvasNode>(menu, sceneGraph.root());
		sceneGraph.addNode(menuNode);
		stage.widthProperty().addListener((observable, oldValue, newValue) -> {
			this.width = newValue.doubleValue() - Chart.WIDTH_EXTRA;
		});
		stage.heightProperty().addListener((observable, oldValue, newValue) -> {
			this.height = newValue.doubleValue() - Chart.HEIGHT_EXTRA;
		});
		menu.setFunctionsMenuSceneGraph(sceneGraph, menuNode);
		draw();
		
		this.add(canvas, 0, 0);
	}
	
	public static void drawIcon(GraphicsContext gc, double centerX, double centerY, double radius) {
		double[] xP = new double[5];
		double[] yP = new double[5];
		
		double off = -Math.PI/10;
		for (int i = 0; i < 5; i++) {
			xP[i] = centerX + radius*Math.cos(off+(i*2*Math.PI)/5);
			yP[i] = centerY + radius*Math.sin(off+(i*2*Math.PI)/5);
		}
		
		gc.setFill(Color.GRAY);
		gc.fillOval(centerX - radius, centerY - radius, radius*2, radius*2);
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MENU_BACKGROUND));
		gc.fillOval(centerX - radius/2, centerY - radius/2, radius, radius);
				
		double rotation = 2*Math.PI/5;
		for (int i = 0; i < 5; i++) {			
			drawTooth(gc, rotation, 70, 40, xP[4], yP[4]+10, centerX, centerY);
			rotation += 2*Math.PI/5;
		}
	}
	
	private static void drawTooth(GraphicsContext gc, double rotation, double width, double height, double topToothX, double topToothY, double centerX, double centerY) {
		initTopTooth(width, height, topToothX, topToothY);
		double[] x = new double[4];
		double[] y = new double[4];
		for (int i = 0; i < 4; i++) {
			x[i] = centerX + (xP[i] - centerX)*Math.cos(rotation) - (yP[i] - centerY)*Math.sin(rotation);
			y[i] = centerY + (xP[i] - centerX)*Math.sin(rotation) + (yP[i] - centerY)*Math.cos(rotation);
		}
		gc.setFill(Color.GRAY);
		gc.fillPolygon(x, y, 4);
	}
	
	private static void initTopTooth(double width, double height, double topToothX, double topToothY) {
		double topWidth = width * 3/4;
		xP[0] = topToothX - width/2;
		xP[1] = xP[0] + (width - topWidth) / 2;
		xP[2] = xP[1] + topWidth;
		xP[3] = xP[0] + width;
		
		yP[0] = topToothY;
		yP[1] = topToothY - height;
		yP[2] = topToothY - height;
		yP[3] = topToothY;
	}

	public Stage stage() {
		return stage;
	}
	
	public TNode<ICanvasNode> menuNode() {
		return menuNode;
	}
	
	@Override
	public void draw() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(ColourSettings.colour(ColourIndex.CHART_BACKGROUND));
		gc.fillRect(0, 0, width, height);
		menu.draw();
	}

	@Override
	public Tree<ICanvasNode> sceneGraph() {
		return sceneGraph;
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
	public TNode<ICanvasNode> lastNode() {
		return lastNode;
	}

	@Override
	public void setLastNode(TNode<ICanvasNode> lastNode) {
		this.lastNode = lastNode;
	}

	@Override
	public ICanvasNode lastFocused() {
		return lastFocused;
	}

	@Override
	public void setLastFocused(ICanvasNode lastFocused) {
		this.lastFocused = lastFocused;
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	@Override
	public CanvasEventFilter canvasEventFilter() {
		return cef;
	}
 }
