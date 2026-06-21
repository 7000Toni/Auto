package com.github._7000toni.auto.menu;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import com.github._7000toni.auto.canvasnode.CanvasEventFilter;
import com.github._7000toni.auto.canvasnode.CanvasWrapper;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasWindow;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.DataSetButton;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.dataset.DataSet;
import com.github._7000toni.auto.dataset.DataSetLoader;
import com.github._7000toni.auto.dataset.LoadingDataSet;
import com.github._7000toni.auto.dataset.OptimizeTask;
import com.github._7000toni.auto.dataset.reader.DukascopyNodeReader;
import com.github._7000toni.auto.dataset.reader.ITickDataFileReader;
import com.github._7000toni.auto.dataset.reader.MarketTickFileReader;
import com.github._7000toni.auto.dataset.reader.OptimizedMarketTickFileReader;
import com.github._7000toni.auto.dataset.reader.OriginalTickFileReader;
import com.github._7000toni.auto.marketreplay.MarketReplayPane;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

public class Menu implements ICanvasWindow {
	public static final double MARGIN = 10;
	
	private Canvas canvas;
	private GraphicsContext gc;
	private CanvasButton loadData;
	private CanvasButton optimize;
	private CanvasButton marketTickReader;
	private CanvasButton marketTickOReader;
	private CanvasButton originalReader;
	private CanvasButton dukasNodeReader;
	private CanvasButton darkMode;
	private CanvasButton auto;
	private double width;
	private double height;
	private ArrayList<DataSet> datasets = new ArrayList<DataSet>();
	private ArrayList<DataSetButton> dsButtons = new ArrayList<DataSetButton>();
	private ArrayList<MarketReplayPane> replays = new ArrayList<MarketReplayPane>();
	private ITickDataFileReader reader = null;	
	private static Menu menu = null;
		
	private boolean openChartOnStart = true;
	
	private ArrayList<LoadingDataSet> loadingSets = new ArrayList<LoadingDataSet>();
	private IntegerProperty numJobs = new SimpleIntegerProperty();
	private final ReentrantLock varLock = new ReentrantLock();
	private boolean dragging = false;
	
	private Tree<ICanvasNode> sceneGraph;
	private CanvasWrapper cw;
	private TNode<ICanvasNode> lastNode = null;
	
	private IVanGogh optimizeVG = (x, y, gc) -> {
		double oldFontSize = gc.getFont().getSize();
		gc.setFont(new Font(22));
		if (numJobs.get() > 0) {
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
		} else if (Chart.darkMode().get()) {
			gc.setFill(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
		}		
		if (optimize.hover()) {
			gc.setFill(Color.GRAY);
		}
		if (optimize.pressed()) {
			gc.setFill(Color.DIMGRAY);
		}
		if (!optimize.enabled()) {
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.fillRoundRect(x, y, optimize.width(), optimize.height(), CanvasButton.ARC_W, CanvasButton.ARC_H);
		optimize.setColoursText();
		gc.fillText(optimize.text(), x + optimize.textXOffset(), y + optimize.textYOffset());		
		gc.setFont(new Font(oldFontSize));
	};
	
	public Menu(double width, double height) {		
		this.canvas = new Canvas(width, height);		
		this.gc = canvas.getGraphicsContext2D();
		this.width = width;
		this.height = height;				
		
		this.loadData = new CanvasButton(gc, 100, 48, MARGIN, MARGIN, "LOAD", 2, 37);
		this.loadData.setVanGogh((x, y, gc) -> {
			double oldFontSize = gc.getFont().getSize();
			gc.setFont(new Font(37));
			loadData.defaultDraw(gc.getFont());
			gc.setFont(new Font(oldFontSize));
		});
		this.loadData.setOnMouseClicked(e -> {
			DataSetLoader dsl = new DataSetLoader(datasets, dsButtons, replays, reader, loadingSets, sceneGraph);
			dsl.load();
		});
		
		this.optimize = new CanvasButton(gc, 100, 48, MARGIN, MARGIN + 58, "OPTIMIZE", 2, 32);
		this.optimize.setVanGogh(optimizeVG);
		this.optimize.setOnMouseClicked(e -> {
			File init = new File("C:\\Users\\Toni C\\Desktop\\TC'S\\The Projects\\Java\\Auto\\res");
			FileChooser fc = new FileChooser();
			if (init.exists()) {
				fc.setInitialDirectory(init);
			} else {
				fc.setInitialDirectory(new File("./"));
			}	
			fc.setTitle("Select MarketTick Files");
			List<File> files = fc.showOpenMultipleDialog(null);		
			if (files != null) {
				for (File f : files) {						
					Thread t = new Thread(new OptimizeTask(f, numJobs));
					t.start();
				}
			}
		});
		
		this.marketTickReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3, "MT READER", 2, 24);
		this.marketTickReader.setVanGogh(readerVG(marketTickReader, 18));
		this.marketTickReader.setOnMouseClicked(e -> {			
			marketTickOReader.setOn(false);
			originalReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			marketTickReader.setOn(true);
			reader = new MarketTickFileReader();
		});
		
		this.marketTickOReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 42, "MTO READER", 2, 23);
		this.marketTickOReader.setVanGogh(readerVG(marketTickOReader, 16));
		this.marketTickOReader.setOnMouseClicked(e -> {
			marketTickReader.setOn(false);
			originalReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			marketTickOReader.setOn(true);
			reader = new OptimizedMarketTickFileReader();
		});
		
		this.originalReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 86, "OG READER", 2, 24);
		this.originalReader.setVanGogh(readerVG(originalReader, 18));
		this.originalReader.setOnMouseClicked(e -> {
			marketTickReader.setOn(false);
			marketTickOReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			originalReader.setOn(true);
			reader = new OriginalTickFileReader();
		});
		
		this.dukasNodeReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 129, "DN READER", 2, 24);
		this.dukasNodeReader.setVanGogh(readerVG(dukasNodeReader, 18));
		this.dukasNodeReader.setOnMouseClicked(e -> {
			marketTickReader.setOn(false);
			marketTickOReader.setOn(false);
			originalReader.setOn(false);
			auto.setOn(false);
			dukasNodeReader.setOn(true);
			reader = new DukascopyNodeReader();
		});
		
		this.darkMode = new CanvasButton(gc, 100, 22, MARGIN, MARGIN + 58*2, "DARK", 2, 0);
		this.darkMode.setVanGogh((x, y, gc) -> {
			double oldFontSize = gc.getFont().getSize();
			int fontSize;
			if (Chart.darkMode().get()) {
				darkMode.setText("LIGHT MODE");	
				darkMode.setTextYOffset(16);
				fontSize = 16;
			} else {
				darkMode.setText("DARK MODE");
				darkMode.setTextYOffset(17);
				fontSize = 17;
			}
			gc.setFont(new Font(fontSize));
			darkMode.defaultDraw(gc.getFont());
			gc.setFont(new Font(oldFontSize));
		});
		this.darkMode.setOnMouseClicked(e -> {
			Chart.toggleDarkMode();	
		});
		
		this.auto = new CanvasButton(gc, 100, 22, MARGIN, MARGIN + 58*2 + 26, "AUTO READER", 2, 17);
		this.auto.setVanGogh(readerVG(auto, 15));
		this.auto.setOnMouseClicked(e -> {
			marketTickReader.setOn(false);
			marketTickOReader.setOn(false);
			originalReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(true);
			reader = null;
		});
		this.auto.toggleOn();
		
		sceneGraph = new Tree<ICanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<ICanvasNode>(cw, null));
		
		sceneGraph.addNode(new TNode<ICanvasNode>(loadData, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(optimize, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(marketTickReader, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(marketTickOReader, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(originalReader, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(dukasNodeReader, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(darkMode, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(auto, sceneGraph.root()));
		
		canvas.addEventFilter(Event.ANY, e -> {
			(new CanvasEventFilter(this)).canvasEventFilter(e);
		});
		
		menu = this;
		
		if (openChartOnStart) {
			openChartOnStart();
		}
		
		draw();
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
	
	private void openChartOnStart() {	
		File f = new File("res/20220901_DBG.csv");
		if (f.exists()) {				
			DataSetLoader dsl = new DataSetLoader(f, datasets, dsButtons, replays, reader, loadingSets, sceneGraph);
			dsl.load();	
		}
	}
	
	@Override
	public ReentrantLock varLock() {
		return varLock;
	}
	
	@Override
	public boolean onWindow(double x, double y) {
		return x <= width && x >= 0 && y <= height && y >= 0; 
	}
	
	private IVanGogh readerVG(CanvasButton cb, int fontSize) {
		return (x, y, gc) -> {
			double oldFontSize = gc.getFont().getSize();
			gc.setFont(new Font(fontSize));			
			if (cb.on()) {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
			} else if (Chart.darkMode().get()) {
				gc.setFill(Color.WHITE);
			} else {
				gc.setFill(Color.BLACK);
			}
			if (cb.hover()) {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
			} 
			if (cb.pressed()) {				
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
			}	
			gc.fillRoundRect(x, y, cb.width(), cb.height(), CanvasButton.ARC_W, CanvasButton.ARC_H);
			cb.setColoursText();
			gc.fillText(cb.text(), x + cb.textXOffset(), y + cb.textYOffset());
			gc.setFont(new Font(oldFontSize));
		};
	}
	
	public Canvas canvas() {
		return this.canvas;
	}
	
	public static Menu menu() {
		return menu;
	}
	
	private void drawUI() {		
		varLock.lock();
		try {					
			if (datasets.size() < 6) {
				loadData.enable();
			} else {
				loadData.disable();
			}
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MENU_BACKGROUND));
			gc.fillRect(0, 0, width, height);
			loadData.draw();
			optimize.draw();
			marketTickReader.draw();
			marketTickOReader.draw();
			originalReader.draw();
			dukasNodeReader.draw();
			darkMode.draw();
			auto.draw();
			drawLoadingSets();
			for (DataSetButton dsb : dsButtons) {
				if (dsb == null) {
					continue;
				}
				dsb.draw();
			}
		} finally {
			varLock.unlock();
		}
	}
	
	public void draw() {	
		if (Platform.isFxApplicationThread()) {
			drawUI();
		} else {
			final CountDownLatch latch = new CountDownLatch(1);
			Platform.runLater(() -> {
				try {
					drawUI();
				} finally {
					latch.countDown();
				}
			});
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void drawLoadingSets() {
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
		for (LoadingDataSet l : loadingSets) {
			gc.fillRoundRect(120, l.y(), 510 * l.progress().get() / 100.0, 48, CanvasButton.ARC_W, CanvasButton.ARC_H);
		}
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