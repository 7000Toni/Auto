package com.github._7000toni.auto.chart.menu.tabs;
import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class TimeFramesTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	//private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel timeFramesFunctions;
	
	public TimeFramesTab(double x, double y, double width, double height, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		//this.cmbvg = cmbvg;
		
		initTimeFramesMenu();
	}
	
	private void initTimeFramesMenu() {timeFramesFunctions = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "TIME FRAME FUNCTIONS");
		timeFramesFunctions.setVanGogh((x2, y2, gc2) -> {
			timeFramesFunctions.alternateDraw(gc.getFont());
		});					
	}
	
	private void drawTimeframeFunctionsMenu() {
		timeFramesFunctions.draw();
	}
	
	public void setTimeframeFunctionsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(timeFramesFunctions, menuNode)); 
	}
	
	public Chart chart() {
		return chart;
	}

	@Override
	public void draw() {		
		drawTimeframeFunctionsMenu();
	}

	@Override
	public void setX(double x) {	
		timeFramesFunctions.setX(x + 5);
		
		this.x = x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
		
		timeFramesFunctions.setY(y + 35);
	}
}
