package com.github._7000toni.auto.marketreplay;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.github._7000toni.auto.Main;
import com.github._7000toni.auto.canvasnode.CanvasEventFilter;
import com.github._7000toni.auto.canvasnode.CanvasWrapper;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasWindow;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.CanvasNumberChooser;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalMRPaneScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartPane;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MarketReplayPane extends GridPane implements IScrollBarOwner, ICanvasWindow {
	private Stage stage;
	private MarketReplay mr;
	private Canvas canvas;
	private GraphicsContext gc;
	private HorizontalMRPaneScrollBar hsb;
	private ArrayList<CanvasNumberChooser> numbers;
	private BooleanProperty bPlay = new SimpleBooleanProperty(true);
	private BooleanProperty bLive = new SimpleBooleanProperty(true);
	private String name;	
	
	private Tree<ICanvasNode> sceneGraph;
	private CanvasWrapper cw;
	private TNode<ICanvasNode> lastNode = null;
	private boolean dragging = false;
	
	private static ArrayList<MarketReplayPane> panes = new ArrayList<MarketReplayPane>();
	private final ReentrantLock varLock = new ReentrantLock();
	
	private CanvasButton newChart;
	private CanvasButton pausePlay;
	private CanvasButton back;
	private CanvasButton forward;
	private CanvasButton live;
	
	private CanvasNumberChooser bf1;
	private CanvasNumberChooser bf2;
	private CanvasNumberChooser bf3;
	private CanvasNumberChooser bf4;
	
	private CanvasNumberChooser s1;
	private CanvasNumberChooser s2;
	private CanvasNumberChooser s3;
	
	public MarketReplayPane(Chart chart, int index, Stage stage) {
		this.stage = stage;				
		name = chart.chartNode().name();
		stage.setTitle(name + " Replay");
		mr = new MarketReplay(chart, this, index);	
		canvas = new Canvas(399, 100);
		gc = canvas.getGraphicsContext2D();
		Font f = Menu.menu().graphicsContext().getFont();
		gc.setFont(Font.font(f.getFamily(), FontWeight.NORMAL, 20));
		hsb = new HorizontalMRPaneScrollBar(this, 0, 399, 50, 10, 90);
		numbers = new ArrayList<CanvasNumberChooser>();
		
		MarketReplayPaneVanGoghs mrpvg = new MarketReplayPaneVanGoghs();
		newChart = new CanvasButton(gc, 40, 20, 349, 10, null, 0, 0);
		newChart.setVanGogh(mrpvg.newChartVG(newChart));
		pausePlay = new CanvasButton(gc, 40, 40, 10, 40, null, 0, 0);
		pausePlay.setVanGogh(mrpvg.pausePlayVG(pausePlay, bPlay));
		back = new CanvasButton(gc, 40, 40, 60, 40, null, 0, 0);
		back.setVanGogh(mrpvg.backVG(back));
		forward = new CanvasButton(gc, 40, 40, 210, 40, null, 0, 0);
		forward.setVanGogh(mrpvg.forwardVG(forward));
		live = new CanvasButton(gc, 40, 40, 349, 40, null, 0, 0);
		live.setVanGogh(mrpvg.liveVG(live, bLive));
		
		double h = CanvasNumberChooser.getHeightForDesiredNumberHight(40);
		double y = 40 - CanvasNumberChooser.buttonHeight(h);
		bf1 = new CanvasNumberChooser(gc, 25, h, 102, y);
		bf2 = new CanvasNumberChooser(gc, 25, h, 129, y);
		bf3 = new CanvasNumberChooser(gc, 25, h, 156, y);
		bf4 = new CanvasNumberChooser(gc, 25, h, 183, y);
		numbers.add(bf1);	
		numbers.add(bf2);
		numbers.add(bf3);
		numbers.add(bf4);
		
		s1 = new CanvasNumberChooser(gc, 25, h, 260, y);
		s2 = new CanvasNumberChooser(gc, 25, h, 287, y);
		s3 = new CanvasNumberChooser(gc, 25, h, 314, y);
		s3.setValue(1);
		numbers.add(s1);
		numbers.add(s2);
		numbers.add(s3);
		
		sceneGraph = new Tree<ICanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<ICanvasNode>(cw, null));
		
		hsb.setOnMouseDragged(e -> {
			hsb.defaultOnMouseDragged(e);
			mr.setIndex((int)(((hsb.x() - hsb.minPos()) / (hsb.maxPos() - hsb.sbWidth() - hsb.minPos())) * mr.tickDataSize().get()), false);
		});
		newChart.setOnMouseClicked(e -> {
			Stage s = new Stage();
			if (Main.icon() != null) {
				s.getIcons().add(Main.icon());
			}
			s.setTitle(mr.data().name());
			ChartPane c = new ChartPane(s, 1280, 720, mr.data(), true, mr, this);
			Scene scene = new Scene(c);	
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		});
		pausePlay.setOnMouseClicked(e -> {
			if (bPlay.get()) {
				bPlay.set(false);
				mr.togglePause();
			} else {
				bPlay.set(true);
				mr.togglePause();
			}
		});
		back.setOnMouseClicked(e -> {
			mr.setIndex(-moveNumber(), true);
			mr.charts().getFirst().draw();
		});
		forward.setOnMouseClicked(e -> {
			mr.setIndex(moveNumber(), true);
			mr.charts().getFirst().draw();		
		});
		live.setOnMouseClicked(e -> {
			if (bLive.get()) {
				bLive.set(false);
				mr.toggleLive();
			} else {
				bLive.set(true);
				mr.toggleLive();
			}
		});
		s1.setOnMouseClicked(e -> {
			if (speedNumber() == 0) {
				s3.incrementValue();
			}
			mr.setSpeed(speedNumber());
		});
		s2.setOnMouseClicked(e -> {
			if (speedNumber() == 0) {
				s3.incrementValue();
			}
			mr.setSpeed(speedNumber());
		});
		s3.setOnMouseClicked(e -> {
			if (speedNumber() == 0) {
				s3.incrementValue();
			}
			mr.setSpeed(speedNumber());
		});

		sceneGraph.addNode(new TNode<ICanvasNode>(hsb, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(newChart, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(pausePlay, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(back, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(forward, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(live, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(bf1, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(bf2, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(bf3, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(bf4, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(s1, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(s2, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(s3, sceneGraph.root()));			
		
		canvas.addEventFilter(Event.ANY, e -> {
			(new CanvasEventFilter(this)).canvasEventFilter(e);
		});
		
		this.add(canvas, 0, 0);
		mr.run();
		draw();
		panes.add(this);
	}
	
	@Override
	public ReentrantLock varLock() {
		return varLock;
	}
	
	@Override
	public boolean onWindow(double x, double y) {
		return x <= 399 && x >= 0 && y <= 100 && y >= 0; 
	}
	
	public Tree<ICanvasNode> sceneGraph() {
		return sceneGraph;
	}
	
	public TNode<ICanvasNode> lastNode() {
		return lastNode;
	}
	
	public void setLastNode(TNode<ICanvasNode> lastNode) {
		this.lastNode = lastNode;
	}
	
	public Canvas canvas() {
		return canvas;
	}
	
	public String name() {
		return this.name;
	}
	
	public HorizontalMRPaneScrollBar hsb() {
		return this.hsb;
	}
	
	public static void drawReplayPanes() {
		for (MarketReplayPane m : panes) {
			m.draw();
		}
	}
	
	@Override
	public void draw() {
		if (Platform.isFxApplicationThread()) {
			draw(gc, 0, 0);
		} else {
			Platform.runLater(() -> {
				draw(gc, 0, 0);
			});
		}		
	}
	
	private void draw(GraphicsContext gc, double x, double y) {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, 20));
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
		} else {			
			gc.setStroke(Color.BLACK);
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
		gc.setFont(oldFont);
		int i = 0;
		for (TNode<ICanvasNode> tn : sceneGraph.postOrderArray()) {
			ICanvasNode d = tn.element();
			GraphicsContext g = d.graphicsContext();
			d.setGraphicsContext(gc);
			double x2 = d.x();
			double y2 = d.y();	
			if (i == 0) {
				HorizontalMRPaneScrollBar sb = (HorizontalMRPaneScrollBar)sceneGraph.postOrderArray().get(0).element();		
				double minPos = sb.minPos();
				double maxPos = sb.maxPos();
				sb.setMinPos(x + minPos);
				sb.setMaxPos(x + maxPos);
				sb.setX(x + minPos + x2);
				sb.setY(y + y2);				
				sb.draw();
				sb.setMinPos(minPos);
				sb.setMaxPos(maxPos);
				sb.setX(x2);
				sb.setY(y2);
				i++;
			} else {											
				d.setX(x + x2);
				d.setY(y + y2);
				d.draw();							
				d.setX(x2);
				d.setY(y2);
			}		
			d.setGraphicsContext(g);
		}				
	}
	
	public void drawPane(GraphicsContext gc, double x, double y) {
		draw(gc, x, y);
	}
	
	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}
	
	public void endReplay() {
		panes.remove(this);
		Chart.closeAll(name, true);
		mr.data().setReplayM1CandlesDataSize(0);
		mr.data().setReplayTickDataSize(0);
		mr.stop();
		stage.close();
	}
	
	private int moveNumber() {
		CanvasNumberChooser[] c = {bf1, bf2, bf3, bf4};
		return CanvasNumberChooser.number(c);
	}
	
	private int speedNumber() {
		CanvasNumberChooser[] c = {s1, s2, s3};
		return CanvasNumberChooser.number(c);
	}
	
	@Override
	public boolean dragging() {
		return dragging;
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;		
	}
}
