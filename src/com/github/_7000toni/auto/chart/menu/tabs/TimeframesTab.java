package com.github._7000toni.auto.chart.menu.tabs;
import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.TextBox;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.TimeframeButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.timeframe.Timeframe;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.GraphicsContext;

public class TimeframesTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel timeFramesFunctions;
	private CanvasButton ticks;
	private CanvasButton minutes;
	private CanvasLabel enterPeriod;
	private TextBox txtPeriod;
	private CanvasButton add;
	private CanvasLabel added;
	
	private BooleanProperty addTicks = new SimpleBooleanProperty(true);
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
			addTicks.set(true);
		});		
		
		minutes = new CanvasButton(gc, 142, 20, x + 153, y + 85, "MINUTES");
		minutes.setVanGogh(cmbvg.menuButtonVG(minutes, gc.getFont().getSize()));
		minutes.setOnMouseClicked(e -> {
			minutes.setOn(true);
			ticks.setOn(false);
			addTicks.set(false);
		});		
		
		enterPeriod = new CanvasLabel(gc, 290, 20, x + 5, y + 110, "ENTER PERIOD");
		enterPeriod.setVanGogh(cmbvg.toggleVG(enterPeriod, addTicks, "ENTER THE NUMBER OF TICKS", "ENTER THE NUMBER OF MINUTES"));	
		
		txtPeriod = new TextBox(chart.stage(), gc, 143, 20, x + 5, y + 135, "2", TextBox.InputType.INT, false, true, false);
		setTextEvents();
		
		add = new CanvasButton(gc, 142, 20, x + 5, y + 135, "ADD");
		add.setVanGogh((x2, y2, gc2) -> {
			add.defaultDraw(gc.getFont());
		});
		add.setOnMouseClicked(e -> {
			addTimeframe(false);
			add.disable();
		});
		
		added = new CanvasLabel(gc, 290, 20, x + 5, y + 160, "ADDED TIMEFRAMES");
		added.setVanGogh((x2, y2, gc2) -> {
			added.defaultDraw(gc.getFont());
		});	
		
		ticks.setOn(true);
		addBaseTimeframe(true);
	}
	
	private void setTextEvents() {
		txtPeriod.setOnKeyPressed(e -> {
			checkNumber();
		});
			
		txtPeriod.setOnKeyTyped(e -> {
			checkNumber();
		});
	}
	
	private void addBaseTimeframe(boolean ctor) {
		TimeframeButton tfb = new TimeframeButton(gc, 142, 20, x + 5, y + 185, Dataset.BASE_TF_NAME, Dataset.BASE_TF_NAME);
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
		checkTimeframes(ctor);
	}
	
	private void addTimeframe(boolean ctor) {		
		Dataset dataset = chart.chartNode().data();
		int period = Integer.parseInt(txtPeriod.text());
		String name = Timeframe.determineName(addTicks.get(), period);
		if (dataset.addTimeframe(dataset, addTicks.get(), period)) {		
			addTimeframe(name, dataset, ctor);
			for (Chart c : Chart.charts(chart.chartNode().name())) {
				if (!c.equals(chart)) {
					c.menu().chartFunctionsMenu().timeFramesTab().addTimeframe(name, ctor);
				}
			}
		}
	}
	
	public void addTimeframe(String name, boolean ctor) {
		addTimeframe(name, chart.chartNode().data(), ctor);
		if (tfButtons.size() == 28) {
			add.disable();
		}
	}
	
	public void removeTimeframe(String name) {		
		for (int i = 1; i < tfButtons.size(); i++) {
			if (tfButtons.get(i).timeframeName().equals(name)) {
				tfButtons.remove(i);
				resetTFButtonsPos(i);
				break;
			}
		}
		chart.chartMenu().setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
	}
	
	private void checkTimeframes(boolean ctor) {
		Dataset dataset = chart.chartNode().data();
		ArrayList<Timeframe> timeframes = dataset.timeframes();
		for (int i = 1; i < timeframes.size(); i++) {
			addTimeframe(timeframes.get(i).name(), dataset, ctor);
		}
	}
	
	private void addTimeframe(String name, Dataset dataset, boolean ctor) {
		int x = tfButtons.size() % 2 == 0?5:153;
		int y = 185 + (tfButtons.size() / 2) * 25;
		TimeframeButton tfb = new TimeframeButton(gc, 142, 20, this.x + x, this.y + y, name, name);
		tfButtons.add(tfb);
		if (tfButtons.size() == 28) {
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
			resetTFButtonsPos(index);
			for (Chart c : Chart.charts(chart.chartNode().name())) {
				ChartNode cn = c.chartNode();
				if (cn.timeframe().name().equals(name)) {
					cn.setTimeframe(dataset.getTimeframe(Dataset.BASE_TF_NAME));
				}
				c.menu().chartFunctionsMenu().timeFramesTab().removeTimeframe(name);
			}
			checkNumber();
		});		
		if (!ctor) {
			TNode<ICanvasNode> tfNode = new TNode<ICanvasNode>(tfb, chart.menuNode());
			chart.sceneGraph().addNode(tfNode);
			chart.sceneGraph().addNode(new TNode<ICanvasNode>(tfb.removeButton(), tfNode));
		}
	}
	
	private void resetTFButtonsPos(int index) {
		for (int i = index; i < tfButtons.size(); i++) {
			TimeframeButton tb = tfButtons.get(i);
			if (i % 2 == 0) {
				tb.setX(this.x + 5);
			} else {						
				tb.setX(this.x + 153);
				tb.setY(tb.y() - 25);
			}
		}
	}
	
	private void setTFButtonsXPos(double newx) {
		for (int i = 0; i < tfButtons.size(); i++) {
			TimeframeButton tb = tfButtons.get(i);
			if (i % 2 == 0) {
				tb.setX(newx + 5);
			} else {						
				tb.setX(newx + 153);
			}
		}
	}
	
	private void setTFButtonsYPos(double newy) {
		for (int i = 0; i < tfButtons.size(); i++) {
			int y = 285 + (i / 2) * 25;
			TimeframeButton tb = tfButtons.get(i);
			tb.setY(newy + y);
		}
	}
	
	private void checkNumber() {
		int p;
		if (txtPeriod.text().equals("") || (p = Integer.parseInt(txtPeriod.text())) < 2) {
			add.disable();
			return;
		}
		for (Timeframe tf : chart.chartNode().data().timeframes()) {
			if (tf.tickBased() == addTicks.get() && tf.period() == p) {
				add.disable();
				return;
			}
		}
		add.enable();
	}	
	
	private void drawTimeframeFunctionsMenu() {
		timeFramesFunctions.draw();
		ticks.draw();
		minutes.draw();
		enterPeriod.draw();
		txtPeriod.draw();
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
		sceneGraph.addNode(new TNode<ICanvasNode>(enterPeriod, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(txtPeriod, menuNode));
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
		enterPeriod.setX(x + 5);
		txtPeriod.setX(x + 5);
		add.setX(x + 153);
		added.setX(x + 5);
		setTFButtonsXPos(x);
		
		this.x = x;
	}

	@Override
	public void setY(double y) {
		timeFramesFunctions.setY(y + 35);
		ticks.setY(y + 85);
		minutes.setY(y + 85);
		enterPeriod.setY(y + 110);
		txtPeriod.setY(y + 135);
		add.setY(y + 135);
		added.setY(y + 160);
		setTFButtonsYPos(y);
		
		this.y = y;
	}
}
