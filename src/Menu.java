import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

public class Menu implements CanvasWindow {
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
	private TickDataFileReader reader = null;	
	private static Menu menu = null;
	
	private boolean openChartOnStart = false;
	
	private ArrayList<LoadingDataSet> loadingSets = new ArrayList<LoadingDataSet>();
	private IntegerProperty numJobs = new SimpleIntegerProperty();
	private final ReentrantLock varLock = new ReentrantLock();
	
	private Tree<CanvasNode> sceneGraph;
	private CanvasWrapper cw;
	private TNode<CanvasNode> lastNode = null;
	
	private ButtonVanGogh optimizeVG = (x, y, gc) -> {
		double oldFontSize = gc.getFont().getSize();
		gc.setFont(new Font(22));
		if (numJobs.get() > 0) {
			gc.setStroke(Color.RED);
			gc.setFill(Color.RED);
		} else if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}		
		if (optimize.hover) {
			gc.setStroke(Color.GRAY);
			gc.setFill(Color.GRAY);
		}
		if (optimize.pressed) {
			gc.setStroke(Color.DIMGRAY);
			gc.setFill(Color.DIMGRAY);
		}
		if (!optimize.enabled) {
			gc.setStroke(Color.LIGHTGRAY);
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.strokeRect(x, y, optimize.width, optimize.height);
		gc.fillText(optimize.text, x + optimize.textXOffset, y + optimize.textYOffset);		
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
			loadData.defaultDrawButton();
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
			darkMode.defaultDrawButton();
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
		
		sceneGraph = new Tree<CanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<CanvasNode>(cw, null));
		
		sceneGraph.addNode(new TNode<CanvasNode>(loadData, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(optimize, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(marketTickReader, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(marketTickOReader, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(originalReader, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(dukasNodeReader, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(darkMode, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(auto, sceneGraph.root()));
		
		canvas.addEventFilter(Event.ANY, e -> {
			(new CanvasEventFilter(this)).canvasEventFilter(e);
		});
		
		menu = this;
		
		if (openChartOnStart) {
			openChartOnStart();
		}
		
		draw();
	}	
	
	public Tree<CanvasNode> sceneGraph() {
		return sceneGraph;
	}
	
	public TNode<CanvasNode> lastNode() {
		return lastNode;
	}
	
	public void setLastNode(TNode<CanvasNode> lastNode) {
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
	
	private ButtonVanGogh readerVG(CanvasButton cb, int fontSize) {
		return (x, y, gc) -> {
			double oldFontSize = gc.getFont().getSize();
			gc.setFont(new Font(fontSize));
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
				gc.setFill(Color.BLACK);
			}
			if (cb.on) {
				gc.setStroke(Color.ORANGE);
				gc.setFill(Color.ORANGE);
			} 
			if (cb.hover) {
				gc.setStroke(Color.ORANGE);
				gc.setFill(Color.ORANGE);
			} 
			if (cb.pressed) {				
				gc.setStroke(Color.DARKORANGE);
				gc.setFill(Color.DARKORANGE);
			}	
			gc.strokeRect(x, y, cb.width, cb.height);
			gc.fillText(cb.text, x + cb.textXOffset, y + cb.textYOffset);
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
			if (Chart.darkMode().get()) {
				gc.setFill(Color.BLACK);
			} else {
				gc.setFill(Color.WHITE);
			}
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
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}		
		gc.setFill(Color.ORANGE);
		for (LoadingDataSet l : loadingSets) {
			gc.strokeRect(120, l.y(), 510, 48);	
			gc.fillRect(121, l.y() + 1, 508 * l.progress().get() / 100.0, 46);
		}
	}	
}