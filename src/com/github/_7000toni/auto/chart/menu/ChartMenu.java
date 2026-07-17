package com.github._7000toni.auto.chart.menu;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class ChartMenu extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasButton chartFunctions;
	private CanvasButton chartSettings;
	
	private boolean functions;
	
	private ChartSettingsMenu csm;
	private ChartFunctionsMenu cfm;
	
	public enum ColourButtonIndices {
		UP_CANDLESTICK_FILL(0, "UP CANDLESTICK FILL"),
		UCF_PREVIEW(0, null),
		UP_CANDLESTICK_STROKE(1, "UP CANDLESTICK STROKE"),
		UCS_PREVIEW(1, null),
		DOWN_CANDLESTICK_FILL(2, "DOWN CANDLESTICK FILL"),	
		DCF_PREVIEW(2, null),
		DOWN_CANDLESTICK_STROKE(3, "DOWN CANDLESTICK STROKE"),
		DCS_PREVIEW(3, null),
		LINE_CHART(4, "LINE CHART"),
		LC_PREVIEW(4, null),
		MENU_BACKGROUND(5, "MENU BACKGROUND"),
		MB_PREVIEW(5, null),
		CHART_BACKGROUND(6, "CHART BACKGROUND"),
		CB_PREVIEW(6, null),
		MISCELLANEOUS_1(7, "MISCELLANEOUS 1"),
		MISC1_PREIVEW(7, null),
		MISCELLANEOUS_2(8, "MISCELLANEOUS 2"),
		MISC2_PREIVEW(8, null);
		
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
		
		csm = new ChartSettingsMenu(x, y, width, height, this, gc, chart, cmbvg);
		cfm = new ChartFunctionsMenu(x, y, width, height, this, gc, chart, cmbvg);	
		init();
	}
	
	private void init() {		
		chartFunctions = new CanvasButton(gc, 142, 20, x + 5, y + 5, "FUNCTIONS");
		chartFunctions.setVanGogh(cmbvg.menuButtonVG(chartFunctions, gc.getFont().getSize()));
		chartFunctions.setOnMouseClicked(e -> {
			chartFunctions.setOn(true);
			chartSettings.setOn(false);
			functions = true;
			setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});				
		
		chartSettings = new CanvasButton(gc, 142, 20, x + 153, y + 5, "SETTINGS");
		chartSettings.setVanGogh(cmbvg.menuButtonVG(chartSettings, gc.getFont().getSize()));
		chartSettings.setOnMouseClicked(e -> {
			chartFunctions.setOn(false);
			chartSettings.setOn(true);
			functions = false;
			setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});			
				
		chartFunctions.setOn(true);
		functions = true;
	}
	
	public void setFunctionsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		chart.varLock().lock();
		try {			
			menuNode.removeAllChildren();
			sceneGraph.addNode(new TNode<ICanvasNode>(chartFunctions, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(chartSettings, menuNode));
			cfm.setFunctionsMenuSceneGraph(sceneGraph, menuNode);
		} finally {
			chart.varLock().unlock();
		}
	}
	
	public void setSettingsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		chart.varLock().lock();
		try {
			menuNode.removeAllChildren();
			sceneGraph.addNode(new TNode<ICanvasNode>(chartFunctions, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(chartSettings, menuNode));
			csm.setSettingsMenuSceneGraph(sceneGraph, menuNode);
		} finally {
			chart.varLock().unlock();
		}
	}
	
	public boolean functions() {
		return functions;
	}
	
	public Chart chart() {
		return chart;
	}
	
	public ChartSettingsMenu chartSettingsMenu() {
		return csm;
	}
	
	public ChartFunctionsMenu chartFunctionsMenu() {
		return cfm;
	}

	@Override
	public void draw() {
		chartFunctions.draw();
		chartSettings.draw();
		if (functions) {		
			cfm.draw();
		} else {
			csm.draw();					
		}
	}

	@Override
	public void setX(double x) {
		chartFunctions.setX(x + 5);
		chartSettings.setX(x + 152.5);		
		
		cfm.setX(x);
		csm.setX(x);
		this.x = x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
		chartFunctions.setY(y + 5);
		chartSettings.setY(y + 5);
		
		cfm.setY(y);
		csm.setY(y);
	}
}
