package com.github._7000toni.auto.marketreplay;

import java.util.concurrent.locks.ReentrantLock;

import com.github._7000toni.auto.canvasnode.CanvasEventFilter;
import com.github._7000toni.auto.canvasnode.CanvasWrapper;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasWindow;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MarketReplayPane extends GridPane implements ICanvasWindow {
	private Canvas canvas;
	private MarketReplayNode mrNode;
	
	private Tree<ICanvasNode> sceneGraph;
	private CanvasWrapper cw;
	private CanvasEventFilter cef;
	private TNode<ICanvasNode> lastNode = null;
	private ICanvasNode lastFocused = null;
	private boolean dragging = false;
	private final ReentrantLock varLock = new ReentrantLock();
	
	public MarketReplayPane(Chart chart, int index, Stage stage) {
		stage.setTitle(chart.chartNode().name() + " Replay");		
		canvas = new Canvas(399, 100);
		Font f = Menu.menu().graphicsContext().getFont();
		canvas.getGraphicsContext2D().setFont(Font.font(f.getFamily(), FontWeight.NORMAL, 20));
		sceneGraph = new Tree<ICanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<ICanvasNode>(cw, null));
		cef = new CanvasEventFilter(this);
		canvas.addEventFilter(Event.ANY, e -> {
			cef.canvasEventFilter(e);
		});
		mrNode = new MarketReplayNode(chart, index, canvas.getGraphicsContext2D(), stage, 0, 0, 399, 100, sceneGraph, sceneGraph.root(), false, 0, 0, 0, 0);		
		this.add(canvas, 0, 0);	
	}
	
	public MarketReplayNode mrNode() {
		return mrNode;
	}
	
	public Canvas canvas() {
		return canvas;
	}
	
	@Override
	public ReentrantLock varLock() {
		return varLock;
	}
	
	@Override
	public boolean onWindow(double x, double y) {
		return x <= 399 && x >= 0 && y <= 100 && y >= 0; 
	}
	
	@Override
	public Tree<ICanvasNode> sceneGraph() {
		return sceneGraph;
	}
	
	@Override
	public CanvasEventFilter canvasEventFilter() {
		return cef;
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
	public boolean dragging() {
		return dragging;
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;		
	}

	@Override
	public void draw() {
		mrNode.draw();
	}
}
