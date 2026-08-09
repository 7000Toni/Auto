package com.github._7000toni.auto.marketreplay;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.github._7000toni.auto.Main;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.TextBox;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalMRPaneScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartPane;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MarketReplayNode extends CanvasNode implements IScrollBarOwner {
	private BooleanProperty draggable = new SimpleBooleanProperty(true);
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private Stage stage;
	private MarketReplay mr;
	private GraphicsContext gc;
	private HorizontalMRPaneScrollBar hsb;
	private String name;	
	private TNode<ICanvasNode> mrn;
	
	private static ArrayList<MarketReplayNode> nodes = new ArrayList<MarketReplayNode>();
	
	private CanvasButton newChart;
	private CanvasButton pausePlay;
	private CanvasButton back;
	private CanvasButton forward;
	private CanvasButton live;
	
	private TextBox txtMoveTicks;
	private TextBox txtSpeed;
	
	private double dragXOrigin = 0;
	private double dragYOrigin = 0;
	
	public MarketReplayNode(Chart chart, int index, GraphicsContext gc, Stage stage, double x, double y, double width, double height, Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> parent, boolean draggable, double minX, double maxX, double minY, double maxY) {
		constructorStuff(chart, index, null, gc, stage, x, y, width, height, sceneGraph, parent, draggable, minX, maxX, minY, maxY);
	}
	
	public MarketReplayNode(Chart chart, MarketReplay mr, GraphicsContext gc, Stage stage, double x, double y, double width, double height, Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> parent, boolean draggable, double minX, double maxX, double minY, double maxY) {
		constructorStuff(chart, 0, mr, gc, stage, x, y, width, height, sceneGraph, parent, draggable, minX, maxX, minY, maxY);
	}
	
	private void constructorStuff(Chart chart, int index, MarketReplay mr, GraphicsContext gc, Stage stage, double x, double y, double width, double height, Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> parent, boolean draggable, double minX, double maxX, double minY, double maxY) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.draggable.set(draggable);
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.stage = stage;				
		name = chart.chartNode().name();
		stage.setTitle(name + " Replay");
		if (mr == null) {
			this.mr = new MarketReplay(chart, this, index);
		} else {
			this.mr = mr;
		}
		this.gc = gc;
		
		hsb = new HorizontalMRPaneScrollBar(this, x, x+399, 50, 10, y+90);
		
		MarketReplayNodeVanGoghs mrpvg = new MarketReplayNodeVanGoghs();
		newChart = new CanvasButton(gc, 40, 20, x+349, y+10, null, 0, 0);
		newChart.setVanGogh(mrpvg.newChartVG(newChart));
		pausePlay = new CanvasButton(gc, 40, 40, x+10, y+40, null, 0, 0);
		pausePlay.setVanGogh(mrpvg.pausePlayVG(pausePlay, this.mr.paused()));
		back = new CanvasButton(gc, 40, 40, x+60, y+40, null, 0, 0);
		back.setVanGogh(mrpvg.backVG(back));
		forward = new CanvasButton(gc, 40, 40, x+210, y+40, null, 0, 0);
		forward.setVanGogh(mrpvg.forwardVG(forward));
		live = new CanvasButton(gc, 40, 40, x+349, y+40, null, 0, 0);
		live.setVanGogh(mrpvg.liveVG(live, this.mr.live()));
	
		txtMoveTicks = new TextBox(stage, gc, 106, 40, x+102, y+40, "1000", TextBox.InputType.ABS_INT, false, true, true);
		txtMoveTicks.setFont((Font.font("Verdana", FontWeight.BOLD, txtMoveTicks.height() - txtMoveTicks.margin()*2)));
		txtSpeed = new TextBox(stage, gc, 79, 40, x+260, y+40, ((Integer)this.mr.speed()).toString(), TextBox.InputType.ABS_INT, false, true, true);
		txtSpeed.setFont((Font.font("Verdana", FontWeight.BOLD, txtMoveTicks.height() - txtMoveTicks.margin()*2)));
		setTextBoxEvents();
		
		hsb.setOnMouseDragged(e -> {
			hsb.defaultOnMouseDragged(e);
			this.mr.setIndex((int)(((hsb.x() - hsb.minPos()) / (hsb.maxPos() - hsb.sbWidth() - hsb.minPos())) * this.mr.tickDataSize().get()), false);
			updateHSBPos();
		});
		newChart.setOnMouseClicked(e -> {
			Stage s = new Stage();
			if (Main.icon() != null) {
				s.getIcons().add(Main.icon());
			}
			s.setTitle(this.mr.data().name());
			ChartPane c = new ChartPane(s, 1280, 720, this.mr.data(), true, this.mr);
			Scene scene = new Scene(c);	
			scene.addEventFilter(KeyEvent.ANY, ev -> c.getChart().canvasEventFilter().canvasEventFilter(ev));
			s.setScene(scene);
			s.show();
		});
		pausePlay.setOnMouseClicked(e -> {
			this.mr.togglePause();
			if (!this.mr.charts().isEmpty()) {
				this.mr.charts().getFirst().draw();
			}
		});
		back.setOnMouseClicked(e -> {
			this.mr.setIndex(-Integer.parseInt(txtMoveTicks.text()), true);
			updateHSBPos();
			if (!this.mr.charts().isEmpty()) {
				this.mr.charts().getFirst().draw();
			}
		});
		forward.setOnMouseClicked(e -> {
			this.mr.setIndex(Integer.parseInt(txtMoveTicks.text()), true);
			updateHSBPos();
			if (!this.mr.charts().isEmpty()) {
				this.mr.charts().getFirst().draw();
			}	
		});
		live.setOnMouseClicked(e -> {
			this.mr.toggleLive();
			if (!this.mr.charts().isEmpty()) {
				this.mr.charts().getFirst().draw();
			}
		});
		mrn = new TNode<ICanvasNode>(this, parent);
		sceneGraph.addNode(mrn);
		sceneGraph.addNode(new TNode<ICanvasNode>(hsb, mrn));
		sceneGraph.addNode(new TNode<ICanvasNode>(newChart, mrn));
		sceneGraph.addNode(new TNode<ICanvasNode>(pausePlay, mrn));
		sceneGraph.addNode(new TNode<ICanvasNode>(back, mrn));
		sceneGraph.addNode(new TNode<ICanvasNode>(forward, mrn));
		sceneGraph.addNode(new TNode<ICanvasNode>(live, mrn));	
		sceneGraph.addNode(new TNode<ICanvasNode>(txtMoveTicks, mrn));
		sceneGraph.addNode(new TNode<ICanvasNode>(txtSpeed, mrn));
		
		setOnMousePressed(e -> {
			dragXOrigin = e.getX();
			dragYOrigin = e.getY();
			if (e.getButton() == MouseButton.SECONDARY) {
				this.draggable.set(!this.draggable.get());
			}
		});
		setOnMouseDragged(e -> {
			if (this.draggable.get()) {
				setX(this.x + e.getX() - dragXOrigin);
				setY(this.y + e.getY() - dragYOrigin);
				dragXOrigin = e.getX();
				dragYOrigin = e.getY();
				chart.chartNode().setMRNDragged(true);
			}
		});
		
		if (mr == null) {
			this.mr.run();
		}
		nodes.add(this);
		draw();
	}
	
	private void setTextBoxEvents() {
		txtMoveTicks.setOnKeyPressed(e -> {
			if (txtMoveTicks.text().equals("")) {
				txtMoveTicks.setText("0");
			}
			updateMoveTicksText(txtMoveTicks.text());
		});
		
		txtMoveTicks.setOnKeyTyped(e -> {
			int val = Integer.parseInt(txtMoveTicks.text());
			if (val > 1000) {
				txtMoveTicks.setText("1000");
			}
			updateMoveTicksText(txtMoveTicks.text());
		});
		
		txtSpeed.setOnKeyPressed(e -> {
			if (!txtSpeed.text().equals("") && !txtSpeed.text().equals("0")) {
				mr.setSpeed(Integer.parseInt(txtSpeed.text()));
			}
			updateSpeedText(txtSpeed.text());			
		});
		
		txtSpeed.setOnKeyTyped(e -> {
			if (txtSpeed.text().equals("") || txtSpeed.text().equals("0")) {
				updateSpeedText(txtSpeed.text());
				return;
			}
			int val = Integer.parseInt(txtSpeed.text());
			if (val > 999) {
				txtSpeed.setText("999");
			}
			updateSpeedText(txtSpeed.text());
			mr.setSpeed(Integer.parseInt(txtSpeed.text()));
		});
	}	
	
	public TNode<ICanvasNode> node() {
		return mrn;
	}
	
	public void updateHSBPos() {		
		for (MarketReplayNode n : nodes) {
			if (n != this && n.name.equals(name)) {				
				double x = n.hsb.minPos() + (mr.index().get() / (double)mr.tickDataSize().get()) * (n.hsb.maxPos() - n.hsb.sbWidth() - n.hsb.minPos());
				n.hsb.setX(x);
			}
		}
	}
	
	private void updateSpeedText(String text) {
		for (MarketReplayNode n : nodes) {
			if (n != this && n.name.equals(name)) {				
				n.txtSpeed.setText(txtSpeed.text());
			}
		}
	}
	
	private void updateMoveTicksText(String text) {
		for (MarketReplayNode n : nodes) {
			if (n != this && n.name.equals(name)) {				
				n.txtMoveTicks.setText(txtMoveTicks.text());
			}
		}
	}
	
	public String name() {
		return this.name;
	}
	
	public HorizontalMRPaneScrollBar hsb() {
		return this.hsb;
	}
	
	public static void drawReplayNodes() {
		for (MarketReplayNode n : nodes) {
			n.draw();
		}
	}
	
	public ReadOnlyBooleanProperty draggable() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(draggable);
	}
	
	public void setDraggable(boolean draggable) {
		this.draggable.set(draggable);
	}
	
	public double minX() {
		return minX;
	}
	
	public void setMinX(double minX) {
		this.minX = minX;
		setX(x);
	}
	
	public double maxX() {
		return maxX;
	}
	
	public void setMaxX(double maxX) {
		this.maxX = maxX;
		setX(x);
	}
	
	public double minY() {
		return minY;
	}
	
	public void setMinY(double minY) {
		this.minY = minY;
		setY(y);
	}
	
	public double maxY() {
		return maxY;
	}
	
	public void setMaxY(double maxY) {
		this.maxY = maxY;
		setY(y);
	}
	
	@Override
	public void draw() {
		if (Platform.isFxApplicationThread()) {
			for (MarketReplayNode n : nodes) {
				if (n.name().equals(name)) {
					drawNode();
				}
			}
		} else {
			Platform.runLater(() -> {
				drawNode();
			});
		}		
	}
	
	private void drawNode() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, 20));
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
		} else {			
			gc.setStroke(Color.BLACK);
		}
		if (hover) {
			gc.setStroke(Color.GRAY);
		}
		if (pressed) {
			if (draggable.get()) {
				gc.setStroke(Color.DIMGRAY);
			} else {
				gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
			}
		}
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
		gc.fillRoundRect(x-1.5, y-1.5, 400+3, 100+3, CanvasButton.ARC_W, CanvasButton.ARC_H);
		gc.strokeRoundRect(x-1.5, y-1.5, 400+3, 100+3, CanvasButton.ARC_W, CanvasButton.ARC_H);
		int percent = (int)(mr.index().get() * 100 / (double)(mr.maxSize().get() - 1));
		if (percent > 100) {
			percent = 100;
		} else if (percent < 0) {
			percent = 0;
		}		
		int index = mr.data().tickDataSize(true).get() - 1;
		LocalDateTime tick = null;
		String time = "";
		if (index > -1) {
			tick = mr.data().tickData().get(index).dateTime();
			time = tick.minusNanos(tick.getNano()).toString().replace('T', ' ');
		}
		if (Chart.darkMode().get()) {
			gc.setFill(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
		}
		gc.fillText(percent + "%  " + time, x + 10, y + 25, 240);
		gc.fillText("SPEED", x + 260, y + 25);			
		hsb.draw();
		newChart.draw();
		pausePlay.draw();
		back.draw();
		forward.draw();
		live.draw();
		txtMoveTicks.draw();
		txtSpeed.draw();	
		gc.setFont(oldFont);
	}
	
	@Override
	public void setX(double x) {
		x = x>maxX?maxX:x;
		x = x<minX?minX:x;
		
		double hsbDiff = hsb.x() - hsb.minPos();		
		hsb.setMinPos(x);
		hsb.setMaxPos(x+399);
		hsb.setX(x + hsbDiff);
		newChart.setX(x+349);
		pausePlay.setX(x+10);
		back.setX(x+60);
		forward.setX(x+210);
		live.setX(x+349);
		txtMoveTicks.setX(x+102);
		txtSpeed.setX(x+260);
		
		this.x = x;
	}
	
	@Override
	public void setY(double y) {
		y = y>maxY?maxY:y;
		y = y<minY?minY:y;
		
		hsb.setY(y+90);
		newChart.setY(y+10);
		pausePlay.setY(y+40);
		back.setY(y+40);
		forward.setY(y+40);
		live.setY(y+40);
		txtMoveTicks.setY(y+40);
		txtSpeed.setY(y+40);
		
		this.y = y;
	}
	
	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}
	
	private void removeNodes(String name) {
		for (int i = nodes.size()-1; i > -1; i--) {
			MarketReplayNode mrn = nodes.get(i);
			if (mrn.name().equals(name)) {
				nodes.remove(i);
			}
		}
	}
	
	public void endReplay() {
		removeNodes(name);
		Chart.closeAll(name, true);
		mr.data().setReplayM1CandlesDataSize(0);
		mr.data().setReplayTickDataSize(0);
		mr.stop();
		stage.close();
	}	
}
