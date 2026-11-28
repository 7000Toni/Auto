package com.github._7000toni.auto.chart.menu;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.tabs.DrawingsTab;
import com.github._7000toni.auto.chart.menu.tabs.GeneralFunctionsTab;
import com.github._7000toni.auto.chart.menu.tabs.MiscellaneousTab;
import com.github._7000toni.auto.chart.menu.tabs.TimeframesTab;
import com.github._7000toni.auto.menu.SettingsPane;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class ChartFunctionsMenu extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private SettingsPane sp;
	private ChartMenu chartMenu;
	
	private GeneralFunctionsTab gft;
	private DrawingsTab dt;
	private TimeframesTab tft;
	private MiscellaneousTab mt;
	
	private CanvasButton previousFunctions;
	private CanvasButton nextFunctions;
	
	private int functionsMenuIndex = 0;
	
	public ChartFunctionsMenu(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, SettingsPane sp, ChartMenuButtonVanGoghs cmbvg) {
		constructorStuff(x, y, width, height, chartMenu, gc, null, sp, cmbvg);
	}
	
	public ChartFunctionsMenu(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		constructorStuff(x, y, width, height, chartMenu, gc, chart, null, cmbvg);
	}
	
	private void constructorStuff(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, SettingsPane sp, ChartMenuButtonVanGoghs cmbvg) {
		this.x.set(x);
		this.y.set(y);
		this.width.set(width);
		this.height.set(height);
		this.chartMenu = chartMenu;
		this.gc = gc;
		this.sp = sp;
		this.chart = chart;
		if (chart != null) {
			gft = new GeneralFunctionsTab(x, y, width, height, gc, chart, cmbvg);
			dt = new DrawingsTab(x, y, width, height, gc, chart, cmbvg);
			tft = new TimeframesTab(x, y, width, height, gc, chart, cmbvg);
			mt = new MiscellaneousTab(x, y, width, height, gc, chart, cmbvg);
		} else {
			gft = new GeneralFunctionsTab(x, y, width, height, gc, sp, cmbvg);
			mt = new MiscellaneousTab(x, y, width, height, gc, sp, cmbvg);
		}
		
		initFunctionsMenu();
	}
	
	private void initFunctionsMenu() {		
		previousFunctions = new CanvasButton(gc, 142, 20, x.get() + 5, y.get() + 60, "PREVIOUS");
		previousFunctions.setVanGogh((x2, y2, gc2) -> {
			previousFunctions.defaultDraw(gc.getFont());
		});
		previousFunctions.setOnMouseClicked(e -> {
			if (chart != null) {
				functionsMenuIndex = functionsMenuIndex==0?3:(functionsMenuIndex - 1);
			} else {
				functionsMenuIndex = functionsMenuIndex==0?3:0;
			}
			setSceneGraph();
		});
		
		nextFunctions = new CanvasButton(gc, 142, 20, x.get() + 153, y.get() + 60, "NEXT");	
		nextFunctions.setVanGogh((x2, y2, gc2) -> {
			nextFunctions.defaultDraw(gc.getFont());
		});
		nextFunctions.setOnMouseClicked(e -> {
			if (chart != null) {
				functionsMenuIndex = Math.abs((functionsMenuIndex + 1) % 4);
			} else {
				functionsMenuIndex = functionsMenuIndex==0?3:0;
			}
			setSceneGraph();
		});				
	}
	
	private void setSceneGraph() {
		if (chart != null) {
			chartMenu.setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		} else {
			chartMenu.setFunctionsMenuSceneGraph(sp.sceneGraph(), sp.menuNode());
		}
	}
	
	private void drawFunctionsMenu() {
		previousFunctions.draw();
		nextFunctions.draw();
		if (functionsMenuIndex == 0) {
			gft.draw();
		} else if (functionsMenuIndex == 1) {
			dt.draw();
		} else if (functionsMenuIndex == 2) {			
			tft.draw();
		} else {
			mt.draw();
		}
	}
	
	public void setFunctionsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		lock();
		try {
			sceneGraph.addNode(new TNode<ICanvasNode>(previousFunctions, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(nextFunctions, menuNode));
			if (functionsMenuIndex == 0) {
				gft.setGeneralFunctionsSceneGraph(sceneGraph, menuNode);
			} else if (functionsMenuIndex == 1) {
				dt.setDrawingFunctionsSceneGraph(sceneGraph, menuNode);
			} else if (functionsMenuIndex == 2) {
				tft.setTimeframeFunctionsSceneGraph(sceneGraph, menuNode);
			} else if (functionsMenuIndex == 3) {
				mt.setMiscellaneousFunctionsSceneGraph(sceneGraph, menuNode);
			}
		} finally {
			unlock();
		}
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
	
	public Chart chart() {
		return chart;
	}
	
	public SettingsPane settingsPane() {
		return sp;
	}
	
	public GeneralFunctionsTab generalFunctionstab() {
		return gft;
	}
	
	public DrawingsTab drawingsTab() {
		return dt;
	}
	
	public TimeframesTab timeFramesTab() {
		return tft;
	}

	public MiscellaneousTab miscellaneousTab() {
		return mt;
	}
	
	@Override
	public void draw() {		
		drawFunctionsMenu();
	}

	@Override
	public void setX(double x) {	
		previousFunctions.setX(x + 5);
		nextFunctions.setX(x + 153);
		
		gft.setX(x);
		dt.setX(x);
		tft.setX(x);
		mt.setX(x);
		
		this.x.set(x);
	}

	@Override
	public void setY(double y) {				
		previousFunctions.setY(y + 60);
		nextFunctions.setY(y + 60);				
		
		gft.setY(y);
		dt.setY(y);
		tft.setY(y);
		mt.setY(y);
		
		this.y.set(y);
	}
}
