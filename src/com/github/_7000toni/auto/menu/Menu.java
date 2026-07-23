package com.github._7000toni.auto.menu;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.github._7000toni.auto.canvasnode.button.DatasetButton;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.DatasetLoader;
import com.github._7000toni.auto.dataset.LoadingDataset;
import com.github._7000toni.auto.dataset.OptimizeTask;
import com.github._7000toni.auto.dataset.Signature;
import com.github._7000toni.auto.dataset.reader.DukascopyNodeReader;
import com.github._7000toni.auto.dataset.reader.ITickDataFileReader;
import com.github._7000toni.auto.dataset.reader.MarketTickFileReader;
import com.github._7000toni.auto.dataset.reader.OptimizedMarketTickFileReader;
import com.github._7000toni.auto.dataset.reader.OriginalTickFileReader;
import com.github._7000toni.auto.marketreplay.MarketReplayNode;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
	private ArrayList<Dataset> datasets = new ArrayList<Dataset>();
	private ArrayList<DatasetButton> dsButtons = new ArrayList<DatasetButton>();
	private ArrayList<MarketReplayNode> replays = new ArrayList<MarketReplayNode>();
	private ITickDataFileReader reader = null;	
	private static Menu menu = null;
		
	private static ArrayList<String> chartsOnStart = new ArrayList<String>();
	
	private ArrayList<LoadingDataset> loadingSets = new ArrayList<LoadingDataset>();
	private IntegerProperty numJobs = new SimpleIntegerProperty();
	private final ReentrantLock varLock = new ReentrantLock();
	private boolean dragging = false;
	
	private Tree<ICanvasNode> sceneGraph;
	private CanvasWrapper cw;
	private TNode<ICanvasNode> lastNode = null;
	private ICanvasNode lastFocused = null;
	private CanvasEventFilter cef;
	private int draggedFilesSize = 0;
	
	private IVanGogh optimizeVG = (x, y, gc) -> {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, 22));
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
		optimize.calculateOffsets(gc.getFont());
		gc.fillText(optimize.text(), x + optimize.textXOffset(), y + optimize.textYOffset());		
		gc.setFont(oldFont);
	};
	
	public Menu(double width, double height) {		
		this.canvas = new Canvas(width, height);
		setCanvasDragDropEvents();;
		this.gc = canvas.getGraphicsContext2D();
		gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
		this.width = width;
		this.height = height;				
		
		this.loadData = new CanvasButton(gc, 100, 48, MARGIN, MARGIN, "LOAD", 2, 37);
		this.loadData.setVanGogh((x, y, gc) -> {
			Font oldFont = gc.getFont();
			gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, 37));
			loadData.defaultDraw(gc.getFont());
			gc.setFont(oldFont);
		});
		this.loadData.setOnMouseClicked(e -> {
			DatasetLoader dsl = new DatasetLoader(datasets, dsButtons, replays, reader, loadingSets, sceneGraph);
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
			Font oldFont = gc.getFont();
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
			gc.setFont(Font.font(oldFont.getName(), FontWeight.findByName(oldFont.getStyle()), fontSize));
			darkMode.defaultDraw(gc.getFont());
			gc.setFont(oldFont);
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
		
		cef = new CanvasEventFilter(this);
		canvas.addEventFilter(Event.ANY, e -> {
			cef.canvasEventFilter(e);
		});
		
		menu = this;
		
		if (!chartsOnStart.isEmpty()) {
			openChartsOnStart();
		}
		
		draw();
	}	
	
	@Override
	public CanvasEventFilter canvasEventFilter() {
		return cef;
	}
	
	@Override
	public Tree<ICanvasNode> sceneGraph() {
		return sceneGraph;
	}
	
	@Override
	public TNode<ICanvasNode> lastNode() {
		return lastNode;
	}
	
	@Override
	public void setLastNode(TNode<ICanvasNode> lastNode) {
		this.lastNode = lastNode;
	}
	
	@Override
	public ICanvasNode lastFocused() {
		return lastFocused;
	}
	
	@Override
	public void setLastFocused(ICanvasNode lastFocused) {
		this.lastFocused = lastFocused;
	}
	
	public static void setChartsOnStart(ArrayList<String> chartsOnStart) {
		Menu.chartsOnStart = chartsOnStart;
	}
	
	private void openChartsOnStart() {	
		for (int i = 0; i < chartsOnStart.size(); i++) {
			if (i >= 6) {
				break;
			}
			File f = new File(chartsOnStart.get(i));
			if (f.exists()) {				
				DatasetLoader dsl = new DatasetLoader(f, datasets, dsButtons, replays, reader, loadingSets, sceneGraph);
				dsl.load();	
			} else {
				System.out.println(chartsOnStart.get(i) + " does not exist");
			}
		}
	}
	
	private void setCanvasDragDropEvents() {
		canvas.setOnDragOver(e -> {
			if (datasets.size() == 6) {
				return;
			}
			Dragboard db = e.getDragboard();
			
			if (db.hasFiles()) {			
				int s = 0;
				for (File f : db.getFiles()) {
					if (!f.isFile()) {
						continue;
					}
					try	(FileInputStream fis = new FileInputStream(f);
							BufferedInputStream bis = new BufferedInputStream(fis);
							BufferedReader br = new BufferedReader(new InputStreamReader(bis))) {
						String signature = br.readLine();
						if (Signature.validFull(signature) || Signature.validPartial(signature)) {
							s++;
						}
					} catch(IOException ex) {
						ex.printStackTrace();
					}
				}
				if (s > 0) {
					draggedFilesSize = s;				
					e.acceptTransferModes(TransferMode.COPY);
				}
			}			
		});
		canvas.setOnDragDropped(e -> {
			if (datasets.size() == 6) {
				return;
			}
			Dragboard db = e.getDragboard();
			
			if (db.hasFiles()) {
				for (File f : db.getFiles()) {
					if (!f.isFile()) {
						continue;
					}
					DatasetLoader dsl = new DatasetLoader(f, datasets, dsButtons, replays, reader, loadingSets, sceneGraph);
					dsl.load();	
				}
			}
		});	
		canvas.setOnDragExited(e -> {
			draggedFilesSize = 0;
			draw();
		});
		canvas.setOnMouseMoved(e -> {
			draggedFilesSize = 0;
		});
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
			Font oldFont = gc.getFont();
			gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, fontSize));		
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
			cb.calculateOffsets(gc.getFont());
			gc.fillText(cb.text(), x + cb.textXOffset(), y + cb.textYOffset());
			gc.setFont(oldFont);
		};
	}
	
	public GraphicsContext graphicsContext() {
		return gc;
	}
	
	public Canvas canvas() {
		return this.canvas;
	}
	
	public static Menu menu() {
		return menu;
	}
	
	private void drawDraggedFiles() {		
		for (int i = 0; i < draggedFilesSize; i++) {
			int j = dsButtons.size() + i;
			if (j > 5) {
				break;
			}
			gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
			gc.strokeRoundRect(120 + 0.5, 10 + 58*j + 0.5, 509, 47, CanvasButton.ARC_W, CanvasButton.ARC_H);					
		}
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
			drawDraggedFiles();
			for (DatasetButton dsb : dsButtons) {
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
		for (LoadingDataset l : loadingSets) {
			if (l.validFullSignature()) {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				gc.fillRoundRect(120, l.y(), 510 * l.progress().get() / 100.0, 48, CanvasButton.ARC_W, CanvasButton.ARC_H);
			} else {
				l.draw();
			}
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