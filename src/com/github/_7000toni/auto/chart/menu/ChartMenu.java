package com.github._7000toni.auto.chart.menu;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.menu.SettingsPane;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class ChartMenu extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private SettingsPane sp;
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
		MISC1_PREVIEW(7, null),
		MISCELLANEOUS_2(8, "MISCELLANEOUS 2"),
		MISC2_PREIVEW(8, null),
		TEXT_AND_STUFF(9, "TEXT AND STUFF"),
		TAS_PREVIEW(9, null),
		CURRENT_PRICE_LINE(10, "CURRENT PRICE LINE"),
		CPL_PREVIEW(10, null),
		BUY_HISTORY(11, "BUY HISTORY"),
		BH_PREVIEW(11, null),
		SELL_HISTORY(12, "SELL HISTORY"),
		SH_PREVIEW(12, null);
		
		public final int index;
		public final String text;
		
		private ColourButtonIndices(int index, String text) {
			this.index = index;
			this.text = text;
		}
	}
	
	public ChartMenu(double x, double y, double width, double height, GraphicsContext gc, SettingsPane sp) {
		constructorStuff(x, y, width, height, gc, null, sp);
	}
	
	public ChartMenu(double x, double y, double width, double height, GraphicsContext gc, Chart chart) {
		constructorStuff(x, y, width, height, gc, chart, null);
	}
	
	private void constructorStuff(double x, double y, double width, double height, GraphicsContext gc, Chart chart, SettingsPane sp) {
		this.x.set(x);
		this.y.set(y);
		this.width.set(width);
		this.height.set(height);
		this.gc = gc;
		this.chart = chart;
		this.sp = sp;		
		cmbvg = new ChartMenuButtonVanGoghs();
		
		if (chart != null) {
			csm = new ChartSettingsMenu(x, y, width, height, this, gc, chart, cmbvg);
			cfm = new ChartFunctionsMenu(x, y, width, height, this, gc, chart, cmbvg);
		} else {
			csm = new ChartSettingsMenu(x, y, width, height, this, gc, sp, cmbvg);
			cfm = new ChartFunctionsMenu(x, y, width, height, this, gc, sp, cmbvg);
		}
		init();
	}
	
	private void init() {		
		chartFunctions = new CanvasButton(gc, 142, 20, x.get() + 5, y.get() + 5, "FUNCTIONS");
		chartFunctions.setVanGogh(cmbvg.menuButtonVG(chartFunctions, gc.getFont().getSize()));
		chartFunctions.setOnMouseClicked(e -> {
			chartFunctions.setOn(true);
			chartSettings.setOn(false);
			functions = true;
			if (chart != null) {
				setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
			} else {
				setFunctionsMenuSceneGraph(sp.sceneGraph(), sp.menuNode());
			}
		});				
		
		chartSettings = new CanvasButton(gc, 142, 20, x.get() + 153, y.get() + 5, "SETTINGS");
		chartSettings.setVanGogh(cmbvg.menuButtonVG(chartSettings, gc.getFont().getSize()));
		chartSettings.setOnMouseClicked(e -> {
			chartFunctions.setOn(false);
			chartSettings.setOn(true);
			functions = false;
			if (chart != null) {
				setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
			} else {
				setSettingsMenuSceneGraph(sp.sceneGraph(), sp.menuNode());
			}
		});			
				
		chartFunctions.setOn(true);
		functions = true;
	}
	
	private void lock() {
		if (chart != null) {
			chart.varLock().lock();
		} else {
			sp.varLock().lock();
		}
	}
	
	private void unlock() {
		if (chart != null) {
			chart.varLock().unlock();
		} else {
			sp.varLock().unlock();
		}
	}
	
	public void setFunctionsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		lock();
		try {			
			menuNode.removeAllChildren();
			sceneGraph.addNode(new TNode<ICanvasNode>(chartFunctions, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(chartSettings, menuNode));
			cfm.setFunctionsMenuSceneGraph(sceneGraph, menuNode);
		} finally {
			unlock();
		}
	}
	
	public void setSettingsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		lock();
		try {
			menuNode.removeAllChildren();
			sceneGraph.addNode(new TNode<ICanvasNode>(chartFunctions, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(chartSettings, menuNode));
			csm.setSettingsMenuSceneGraph(sceneGraph, menuNode);
		} finally {
			unlock();
		}
	}
	
	public boolean functions() {
		return functions;
	}
	
	public Chart chart() {
		return chart;
	}
	
	public SettingsPane settingsPane() {
		return sp;
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
		chartSettings.setX(x + 153);		
		
		cfm.setX(x);
		csm.setX(x);
		this.x.set(x);
	}

	@Override
	public void setY(double y) {
		this.y.set(y);
		chartFunctions.setY(y + 5);
		chartSettings.setY(y + 5);
		
		cfm.setY(y);
		csm.setY(y);
	}
}
