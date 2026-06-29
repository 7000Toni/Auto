package com.github._7000toni.auto.chart.menu.tabs;
import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.CanvasNumberChooser;
import com.github._7000toni.auto.canvasnode.button.TimeframeButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.timeframe.Timeframe;
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
	private ArrayList<TimeframeButton> tfButtons = new ArrayList<TimeframeButton>();
	
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
		add.setOnMouseClicked(e -> {
			addTimeframe();
		});
		
		added = new CanvasLabel(gc, 290, 20, x + 5, y + 260, "ADDED TIMEFRAMES");
		added.setVanGogh((x2, y2, gc2) -> {
			added.defaultDraw(gc.getFont());
		});	
		
		units.setValue(2);
		ticks.setOn(true);
		staticTF.setOn(true);
		dynamicTF.disable();
		addBaseTimeframe();
	}
	
	private void addBaseTimeframe() {
		TimeframeButton tfb = new TimeframeButton(gc, 142, 20, x + 5, y + 285, Dataset.BASE_TF_NAME, Dataset.BASE_TF_NAME);
		tfb.setVanGogh((x2, y2, gc) -> {
			tfb.calculateOffsets(gc.getFont());
			tfb.setColoursRect();
			gc.fillRoundRect(tfb.x(), tfb.y(), tfb.width(), tfb.height(), CanvasButton.ARC_W, CanvasButton.ARC_H);
			tfb.setColoursText();
			gc.fillText(tfb.text(), tfb.x() + 2, tfb.y() + tfb.textYOffset(), tfb.width() - 20);
		});
		tfb.removeButton().disable();
		tfb.setOnMouseClicked(e -> {
			chart.chartNode().setTimeframe(chart.chartNode().data().getTimeframe(Dataset.BASE_TF_NAME));
		});
		tfButtons.add(tfb);
	}
	
	private void addTimeframe() {
		Dataset dataset = chart.chartNode().data();
		int period = CanvasNumberChooser.number(tenThousands, thousands, hundreds, tens, units);
		if (dataset.addTimeframe(dataset, addTicks, addStaticTF, period)) {		
			String name = Timeframe.determineName(addTicks, period);
			int x = tfButtons.size() % 2 == 0?5:153;
			int y = 285 + (tfButtons.size() / 2) * 25;
			TimeframeButton tfb = new TimeframeButton(gc, 142, 20, this.x + x, this.y + y, name, name);
			tfButtons.add(tfb);
			if (tfButtons.size() == 20) {
				add.disable();
			}
			tfb.setOnMouseClicked(e -> {
				chart.chartNode().setTimeframe(dataset.getTimeframe(name));
			});
			tfb.removeButton().setOnMouseClicked(e -> {
				int index = tfButtons.indexOf(tfb);
				tfButtons.remove(tfb);
				dataset.removeTimeframe(name);
				chart.chartMenu().setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
				for (int i = index; i < tfButtons.size(); i++) {
					TimeframeButton t = tfButtons.get(i);
					if (i % 2 == 0) {
						t.setX(this.x + 5);
					} else {						
						t.setX(this.x + 153);
						t.setY(t.y() - 25);
					}
				}
				ChartNode cn = chart.chartNode();
				if (cn.timeframe().name().equals(name)) {
					cn.setTimeframe(dataset.getTimeframe(Dataset.BASE_TF_NAME));
				}
				add.enable();
			});
			TNode<ICanvasNode> tfNode = new TNode<ICanvasNode>(tfb, chart.menuNode());
			chart.sceneGraph().addNode(tfNode);
			chart.sceneGraph().addNode(new TNode<ICanvasNode>(tfb.removeButton(), tfNode));
		}
	}
	
	private void upDateTimeframes() {
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
		for (TimeframeButton tfb : tfButtons) {
			tfb.draw();
		}
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
		for (TimeframeButton tf : tfButtons) {
			TNode<ICanvasNode> tfNode = new TNode<ICanvasNode>(tf, menuNode);
			sceneGraph.addNode(tfNode);
			sceneGraph.addNode(new TNode<ICanvasNode>(tf.removeButton(), tfNode));
		}
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
		for (TimeframeButton tfb : tfButtons) {
			if (tfb != null) {
				tfb.setX(x + 5);
			}
		}
		
		this.x = x;
	}

	@Override
	public void setY(double y) {
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
		for (int i = 0; i < tfButtons.size(); i++) {
			TimeframeButton tfb = tfButtons.get(i);
			if (tfb != null) {
				tfb.setY(y + 285 * tfButtons.size());				
			}
		}
		
		this.y = y;
	}
}
