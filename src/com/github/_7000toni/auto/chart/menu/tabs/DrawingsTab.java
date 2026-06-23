package com.github._7000toni.auto.chart.menu.tabs;
import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class DrawingsTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	//private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel drawingFunctions;
	
	public DrawingsTab(double x, double y, double width, double height, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		//this.cmbvg = cmbvg;
		
		initDrawingsMenu();
	}
	
	private void initDrawingsMenu() {
		drawingFunctions = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "DRAWING FUNCTIONS");
		drawingFunctions.setVanGogh((x2, y2, gc2) -> {
			drawingFunctions.defaultDraw(gc.getFont());
		});				
	}
	
	private void drawDrawingFunctionsMenu() {
		drawingFunctions.draw();
	}
	
	public void setDrawingFunctionsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(drawingFunctions, menuNode)); 
	}
	
	public Chart chart() {
		return chart;
	}

	@Override
	public void draw() {		
		drawDrawingFunctionsMenu();
	}

	@Override
	public void setX(double x) {	
		drawingFunctions.setX(x + 5);
		
		this.x = x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
		
		drawingFunctions.setY(y + 35);
	}
}
