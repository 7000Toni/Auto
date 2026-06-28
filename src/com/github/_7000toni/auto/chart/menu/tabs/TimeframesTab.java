package com.github._7000toni.auto.chart.menu.tabs;
import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.CanvasNumberChooser;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class TimeframesTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel timeFramesFunctions;
	private CanvasButton ticks;
	private CanvasButton minutes;
	private CanvasButton staticTF;
	private CanvasButton dynamicTF;
	private CanvasNumberChooser tenThousands;
	private CanvasNumberChooser thousands;
	private CanvasNumberChooser hundreds;
	private CanvasNumberChooser tens;
	private CanvasNumberChooser units;
	private CanvasButton add;
	private CanvasLabel added;
	
	private boolean addTicks = true;
	private boolean addStaticTF = true;
	
	public TimeframesTab(double x, double y, double width, double height, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		this.cmbvg = cmbvg;
		
		initTimeFramesMenu();
	}
	
	private void initTimeFramesMenu() {
		timeFramesFunctions = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "TIMEFRAME FUNCTIONS");
		timeFramesFunctions.setVanGogh((x2, y2, gc2) -> {
			timeFramesFunctions.defaultDraw(gc.getFont());
		});	
		
		ticks = new CanvasButton(gc, 142, 20, x + 5, y + 85, "TICKS");
		ticks.setVanGogh(cmbvg.menuButtonVG(ticks, gc.getFont().getSize()));
		ticks.setOnMouseClicked(e -> {
			ticks.setOn(true);
			minutes.setOn(false);
			addTicks = true;
		});		
		
		minutes = new CanvasButton(gc, 142, 20, x + 153, y + 85, "MINUTES");
		minutes.setVanGogh(cmbvg.menuButtonVG(minutes, gc.getFont().getSize()));
		minutes.setOnMouseClicked(e -> {
			minutes.setOn(true);
			ticks.setOn(false);
			addTicks = false;
		});		
		
		staticTF = new CanvasButton(gc, 142, 20, x + 5, y + 110, "STATIC");
		staticTF.setVanGogh(cmbvg.menuButtonVG(staticTF, gc.getFont().getSize()));
		staticTF.setOnMouseClicked(e -> {
			staticTF.setOn(true);
			dynamicTF.setOn(false);
			addStaticTF = true;
		});		
		
		dynamicTF = new CanvasButton(gc, 142, 20, x + 153, y + 110, "DYNAMIC");
		dynamicTF.setVanGogh(cmbvg.menuButtonVG(dynamicTF, gc.getFont().getSize()));
		dynamicTF.setOnMouseClicked(e -> {
			dynamicTF.setOn(true);
			staticTF.setOn(false);
			addStaticTF = false;
		});		
		
		tenThousands = new CanvasNumberChooser(gc, 54, 90, x + 5, y + 135);
		tenThousands.setOnMouseClicked(e -> {
			checkNumber();
		});
		thousands = new CanvasNumberChooser(gc, 54, 90, x + 64, y + 135);
		thousands.setOnMouseClicked(e -> {
			checkNumber();
		});
		hundreds = new CanvasNumberChooser(gc, 54, 90, x + 123, y + 135);
		hundreds.setOnMouseClicked(e -> {
			checkNumber();
		});
		tens = new CanvasNumberChooser(gc, 54, 90, x + 182, y + 135);
		tens.setOnMouseClicked(e -> {
			checkNumber();
		});
		units = new CanvasNumberChooser(gc, 54, 90, x + 241, y + 135);
		units.setOnMouseClicked(e -> {
			checkNumber();
		});
		
		add = new CanvasButton(gc, 290, 20, x + 5, y + 235, "ADD");
		add.setVanGogh((x2, y2, gc2) -> {
			add.defaultDraw(gc.getFont());
		});
		added = new CanvasLabel(gc, 290, 20, x + 5, y + 260, "ADDED TIMEFRAMES");
		added.setVanGogh((x2, y2, gc2) -> {
			added.defaultDraw(gc.getFont());
		});	
		
		units.setValue(2);
		ticks.setOn(true);
		staticTF.setOn(true);
	}
	
	private void checkNumber() {
		if (CanvasNumberChooser.number(tenThousands, thousands, hundreds, tens, units) < 2) {
			units.setValue(2);
		}
	}
	
	private void drawTimeframeFunctionsMenu() {
		timeFramesFunctions.draw();
		ticks.draw();
		minutes.draw();
		staticTF.draw();
		dynamicTF.draw();
		tenThousands.draw();
		thousands.draw();
		hundreds.draw();
		tens.draw();
		units.draw();
		add.draw();
		added.draw();
	}
	
	public void setTimeframeFunctionsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(timeFramesFunctions, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(ticks, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(minutes, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(staticTF, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(dynamicTF, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(tenThousands, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(thousands, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(hundreds, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(tens, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(units, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(add, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(added, menuNode));
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
		ticks.setX(x + 5);
		minutes.setX(x + 153);
		staticTF.setX(x + 5);
		dynamicTF.setX(x + 153);
		tenThousands.setX(x + 5);
		thousands.setX(x + 64);
		hundreds.setX(x + 123);
		tens.setX(x + 182);
		units.setX(x + 241);
		add.setX(x + 5);
		added.setX(x + 5);
		
		this.x = x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
		
		timeFramesFunctions.setY(y + 35);
		ticks.setY(y + 85);
		minutes.setY(y + 85);
		staticTF.setY(y + 110);
		dynamicTF.setY(y + 110);
		tenThousands.setY(y + 135);
		thousands.setY(y + 135);
		hundreds.setY(y + 135);
		tens.setY(y + 135);
		units.setY(y + 135);
		add.setY(y + 235);
		added.setY(y + 260);
	}
}
