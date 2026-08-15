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
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class ChartFunctionsMenu extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenu chartMenu;
	
	private GeneralFunctionsTab gft;
	private DrawingsTab dt;
	private TimeframesTab tft;
	private MiscellaneousTab mt;
	
	private CanvasButton previousFunctions;
	private CanvasButton nextFunctions;
	
	private int functionsMenuIndex = 0;
	
	public ChartFunctionsMenu(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.chartMenu = chartMenu;
		this.gc = gc;
		this.chart = chart;
		gft = new GeneralFunctionsTab(x, y, width, height, gc, chart, cmbvg);
		dt = new DrawingsTab(x, y, width, height, gc, chart, cmbvg);
		tft = new TimeframesTab(x, y, width, height, gc, chart, cmbvg);
		mt = new MiscellaneousTab(x, y, width, height, gc, chart, cmbvg);
		
		initFunctionsMenu();
	}
	
	private void initFunctionsMenu() {		
		previousFunctions = new CanvasButton(gc, 142.5, 20, x + 5, y + 60, "PREVIOUS");
		previousFunctions.setVanGogh((x2, y2, gc2) -> {
			previousFunctions.defaultDraw(gc.getFont());
		});
		previousFunctions.setOnMouseClicked(e -> {
			functionsMenuIndex = functionsMenuIndex==0?3:Math.abs((functionsMenuIndex - 1) % 3);
			chartMenu.setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});
		
		nextFunctions = new CanvasButton(gc, 142.5, 20, x + 152.5, y + 60, "NEXT");	
		nextFunctions.setVanGogh((x2, y2, gc2) -> {
			nextFunctions.defaultDraw(gc.getFont());
		});
		nextFunctions.setOnMouseClicked(e -> {
			functionsMenuIndex = Math.abs((functionsMenuIndex + 1) % 4); 
			chartMenu.setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});				
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
		chart.varLock().lock();
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
			chart.varLock().unlock();
		}
	}		
	
	public Chart chart() {
		return chart;
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
		nextFunctions.setX(x + 152.5);
		
		gft.setX(x);
		dt.setX(x);
		tft.setX(x);
		mt.setX(x);
		
		this.x = x;
	}

	@Override
	public void setY(double y) {				
		previousFunctions.setY(y + 60);
		nextFunctions.setY(y + 60);				
		
		gft.setY(y);
		dt.setY(y);
		tft.setY(y);
		mt.setY(y);
		
		this.y = y;
	}
}
