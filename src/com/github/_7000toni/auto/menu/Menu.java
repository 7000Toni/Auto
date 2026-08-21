package com.github._7000toni.auto.menu;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.github._7000toni.auto.Main;
import com.github._7000toni.auto.canvasnode.CanvasEventFilter;
import com.github._7000toni.auto.canvasnode.CanvasWrapper;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasWindow;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.DatasetButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.canvasnode.scrollbar.VerticalMenuScrollBar;
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
import com.github._7000toni.auto.settings.MiscellaneousSettings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Menu implements ICanvasWindow, IScrollBarOwner {
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
	private CanvasButton setOnStart;
	private CanvasButton loadOnStart;
	private CanvasButton addOnStart;
	private CanvasButton toggleOnStart;
	private CanvasButton toggleReaders_OnStart;
	private double width;
	private double height;
	private Stage stage;
	private boolean excess = false;
	private ArrayList<Dataset> datasets = new ArrayList<Dataset>();
	private ArrayList<DatasetButton> dsButtons = new ArrayList<DatasetButton>();
	private ArrayList<MarketReplayNode> replays = new ArrayList<MarketReplayNode>();
	private VerticalMenuScrollBar vsb;
	private TNode<ICanvasNode> vsbNode;
	private double offset;
	private ITickDataFileReader reader = null;	
	private static Menu menu = null;	
	private BooleanProperty showReaders = new SimpleBooleanProperty(true);
	private BooleanProperty onStartEnabled = new SimpleBooleanProperty(true);
	
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
		} else {
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.TEXT_AND_STUFF));
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
		gc.fillRoundRect(x, y, optimize.width(), optimize.height(), MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		optimize.setColoursText();
		optimize.calculateOffsets(gc.getFont());
		gc.fillText(optimize.text(), x + optimize.textXOffset(), y + optimize.textYOffset());		
		gc.setFont(oldFont);
	};
	
	public Menu(double width, double height, Stage stage) {		
		this.canvas = new Canvas(width, height);
		this.stage = stage;
		setCanvasDragDropEvents();;
		this.gc = canvas.getGraphicsContext2D();
		gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
		this.width = width;
		this.height = height;			
		vsb = new VerticalMenuScrollBar(this, 0, height, 10, 100, width);
		vsb.setOnScroll(e -> {
			onScroll(e);
		});
		sceneGraph = new Tree<ICanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<ICanvasNode>(cw, null));
		vsbNode = new TNode<ICanvasNode>(vsb, sceneGraph.root());
		
		initDefaultButtons();
		initOnStartButtons();		
		resetSceneGraph();
		
		cw.setOnScroll(e -> {	
			onScroll(e);
		});
		
		cef = new CanvasEventFilter(this);
		canvas.addEventFilter(Event.ANY, e -> {
			cef.canvasEventFilter(e);
		});
		
		menu = this;
		
		openChartsOnStart();
		draw();
	}	
	
	private void initDefaultButtons() {
		this.loadData = new CanvasButton(gc, 100, 48, MARGIN, MARGIN, "LOAD", 2, 37);
		this.loadData.setVanGogh((x, y, gc) -> {
			Font oldFont = gc.getFont();
			gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, 37));
			loadData.defaultDraw(gc.getFont());
			gc.setFont(oldFont);
		});
		this.loadData.setOnMouseClicked(e -> {
			DatasetLoader dsl = new DatasetLoader(datasets, dsButtons, replays, reader, loadingSets);
			dsl.load();
		});
		
		this.optimize = new CanvasButton(gc, 100, 48, MARGIN, MARGIN + 58, "OPTIMIZE", 2, 32);
		this.optimize.setVanGogh(optimizeVG);
		this.optimize.setOnMouseClicked(e -> {
			File init = new File(MiscellaneousSettings.initFileDir());
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
		
		this.marketTickReader = new CanvasButton(gc, 100, 29.3, MARGIN, MARGIN + 58*3, "MT READER", 2, 24);
		this.marketTickReader.setVanGogh(readerVG(marketTickReader, 18, false));
		this.marketTickReader.setOnMouseClicked(e -> {			
			marketTickOReader.setOn(false);
			originalReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			marketTickReader.setOn(true);
			reader = new MarketTickFileReader();
		});
		
		this.marketTickOReader = new CanvasButton(gc, 100, 29.3, MARGIN, MARGIN + 58*3 + 33.3, "MTO READER", 2, 23);
		this.marketTickOReader.setVanGogh(readerVG(marketTickOReader, 16, false));
		this.marketTickOReader.setOnMouseClicked(e -> {
			marketTickReader.setOn(false);
			originalReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			marketTickOReader.setOn(true);
			reader = new OptimizedMarketTickFileReader();
		});
		
		this.originalReader = new CanvasButton(gc, 100, 29.3, MARGIN, MARGIN + 58*3 + 66.6, "OG READER", 2, 24);
		this.originalReader.setVanGogh(readerVG(originalReader, 18, false));
		this.originalReader.setOnMouseClicked(e -> {
			marketTickReader.setOn(false);
			marketTickOReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			originalReader.setOn(true);
			reader = new OriginalTickFileReader();
		});
		
		this.dukasNodeReader = new CanvasButton(gc, 100, 29.3, MARGIN, MARGIN + 58*3 + 99.9, "DN READER", 2, 24);
		this.dukasNodeReader.setVanGogh(readerVG(dukasNodeReader, 18, false));
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
		this.auto.setVanGogh(readerVG(auto, 15, false));
		this.auto.setOnMouseClicked(e -> {
			marketTickReader.setOn(false);
			marketTickOReader.setOn(false);
			originalReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(true);
			reader = null;
		});
		this.auto.toggleOn();		
	}
	
	private void initOnStartButtons() {
		setOnStart = new CanvasButton(gc, 100, 29.3, MARGIN, MARGIN + 58*3, "SET ONSTART");
		setOnStart.setVanGogh(readerVG(setOnStart, 15, true));
		setOnStart.setOnMouseClicked(e -> {	
			setOnStart(false);
		});
		
		loadOnStart = new CanvasButton(gc, 100, 29.3, MARGIN, MARGIN + 58*3 + 33.3, "LD ONSTART");
		loadOnStart.setVanGogh(readerVG(loadOnStart, 16, true));
		loadOnStart.setOnMouseClicked(e -> {
			openChartsOnStart();
		});
		
		addOnStart = new CanvasButton(gc, 100, 29.3, MARGIN, MARGIN + 58*3 + 66.6, "ADD ONSTART");
		addOnStart.setVanGogh(readerVG(addOnStart, 14, true));
		addOnStart.setOnMouseClicked(e -> {
			setOnStart(true);
		});
		
		toggleOnStart = new CanvasButton(gc, 100, 29.3, MARGIN, MARGIN + 58*3 + 99.9, "ONSTART OFF");
		toggleOnStart.setVanGogh((x, y, gc) -> {
			if (onStartEnabled.get()) {
				toggleOnStart.setText("ONSTART OFF");
			} else {
				toggleOnStart.setText("ONSTART ON");
			}
			readerVG(toggleOnStart, 15, true).draw(x, y, gc);
		});
		toggleOnStart.setOnMouseClicked(e -> {
			onStartEnabled.set(!onStartEnabled.get());
			toggleOnStart();
		});
		
		toggleReaders_OnStart = new CanvasButton(gc, 100, 27.8, MARGIN, MARGIN + 58*3 + 137.2, "ONSTART");
		toggleReaders_OnStart.setVanGogh((x, y, gc) -> {
			if (showReaders.get()) {
				toggleReaders_OnStart.setText("ONSTART");
			} else {
				toggleReaders_OnStart.setText("READERS");
			}
			readerVG(toggleReaders_OnStart, 22, true).draw(x, y, gc);
		});
		toggleReaders_OnStart.setOnMouseClicked(e -> {
			showReaders.set(!showReaders.get());
			resetSceneGraph();
		});
	}
	
	public void resetSceneGraph() {
		sceneGraph.root().removeAllChildren();		
		
		if (excess) {
			sceneGraph.addNode(vsbNode);
		}
		
		sceneGraph.addNode(new TNode<ICanvasNode>(loadData, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(optimize, sceneGraph.root()));
		if (showReaders.get()) {
			sceneGraph.addNode(new TNode<ICanvasNode>(marketTickReader, sceneGraph.root()));
			sceneGraph.addNode(new TNode<ICanvasNode>(marketTickOReader, sceneGraph.root()));
			sceneGraph.addNode(new TNode<ICanvasNode>(originalReader, sceneGraph.root()));
			sceneGraph.addNode(new TNode<ICanvasNode>(dukasNodeReader, sceneGraph.root()));
		} else {
			sceneGraph.addNode(new TNode<ICanvasNode>(setOnStart, sceneGraph.root()));
			sceneGraph.addNode(new TNode<ICanvasNode>(loadOnStart, sceneGraph.root()));
			sceneGraph.addNode(new TNode<ICanvasNode>(addOnStart, sceneGraph.root()));
			sceneGraph.addNode(new TNode<ICanvasNode>(toggleOnStart, sceneGraph.root()));
		}
		for (DatasetButton dsb : dsButtons) {
			if (dsb == null) {
				continue;
			}
			TNode<ICanvasNode> dsbNode = new TNode<ICanvasNode>(dsb, sceneGraph.root());
			TNode<ICanvasNode> mrNode = new TNode<ICanvasNode>(dsb.mrButton(), dsbNode);
			TNode<ICanvasNode> closeNode = new TNode<ICanvasNode>(dsb.closeButton(), dsbNode);
			sceneGraph.addNode(dsbNode);
			sceneGraph.addNode(mrNode);
			sceneGraph.addNode(closeNode);	
		}
		sceneGraph.addNode(new TNode<ICanvasNode>(toggleReaders_OnStart, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(darkMode, sceneGraph.root()));
		sceneGraph.addNode(new TNode<ICanvasNode>(auto, sceneGraph.root()));	
	}
	
	public void onScroll(ScrollEvent e) {
		if (!excess) {
			return;
		}
		double delta = e.getDeltaY() * -0.5;
		vsb.setPosition(delta, true);
		adjustDatasetPositions();
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
	
	private void openChartsOnStart() {	
		ArrayList<String> chartsOnStart = Main.getChartsOnStart(false);
		for (int i = 0; i < chartsOnStart.size(); i++) {
			File f = new File(chartsOnStart.get(i));
			if (f.exists()) {				
				DatasetLoader dsl = new DatasetLoader(f, datasets, dsButtons, replays, reader, loadingSets);
				dsl.load();	
			} else {
				System.out.println(chartsOnStart.get(i) + " does not exist");
			}
		}
	}
	
	private void toggleOnStart() {
		ArrayList<String> chartsOnStart = Main.getChartsOnStart(true);
		for (int i = 0; i < chartsOnStart.size(); i++) {
			String s = chartsOnStart.get(i);
			if (s.charAt(0) == '-') {
				chartsOnStart.set(i, s.substring(1));
			}
		}
		writeChartsOnStart(chartsOnStart);
	}
	
	private void setOnStart(boolean add) {
		ArrayList<String> chartsOnStart;
		if (add) {
			chartsOnStart = Main.getChartsOnStart(false);
		} else {
			chartsOnStart = new ArrayList<String>();
		}
		for (Dataset d : datasets) {
			boolean cont = false;
			for (String s : chartsOnStart) {
				if (s.equals(d.dir())) {
					cont = true;
					break;
				}
			}
			if (cont) {
				continue;
			}
			chartsOnStart.add(d.dir());
		}
		writeChartsOnStart(chartsOnStart);
	}
	
	private void writeChartsOnStart(ArrayList<String> chartsOnStart) {
		try (PrintWriter pw = new PrintWriter(new File("./onstart.txt"))) {
			for (String s : chartsOnStart) {
				if (!onStartEnabled.get() && s.charAt(0) != '-') {
					s = "-" + s;
				}
				pw.println(s);
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void setCanvasDragDropEvents() {
		canvas.setOnDragOver(e -> {
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
			Dragboard db = e.getDragboard();
			
			if (db.hasFiles()) {
				for (File f : db.getFiles()) {
					if (!f.isFile()) {
						continue;
					}
					DatasetLoader dsl = new DatasetLoader(f, datasets, dsButtons, replays, reader, loadingSets);
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
	
	private IVanGogh readerVG(CanvasButton cb, int fontSize, boolean defaultColours) {
		return (x, y, gc) -> {
			Font oldFont = gc.getFont();
			gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, fontSize));	
			if (defaultColours) {
				cb.setColoursRect();
			} else {			
				if (cb.on()) {
					gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				} else {
					gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.TEXT_AND_STUFF));
				}
				if (cb.hover()) {
					gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				} 
				if (cb.pressed()) {				
					gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
				}	
			}
			gc.fillRoundRect(x, y, cb.width(), cb.height(), MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
			cb.setColoursText();
			cb.calculateOffsets(gc.getFont());
			gc.fillText(cb.text(), x + cb.textXOffset(), y + cb.textYOffset());
			gc.setFont(oldFont);
		};
	}
	
	@Override
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
			gc.strokeRoundRect(120 + 0.5, 10 + 58*j + 0.5, 509, 47, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());					
		}
	}
	
	private void addVSB() {
		if (!excess) {
			stage.setWidth(stage.getWidth() + 10);
			width += 10;
			canvas.setWidth(width);
			excess = true;
			sceneGraph.addNode(vsbNode);
		}
	}
	
	private void removeVSB() {
		if (excess) {
			stage.setWidth(stage.getWidth() - 10);
			width -= 10;
			canvas.setWidth(width);
			excess = false;
			sceneGraph.removeNode(vsbNode);
		}		
	}
	
	private void vsbCheck() {		
		if (datasets.size() < 7) {
			removeVSB();
		} else {
			addVSB();
		}	
	}
	
	private void drawUI() {		
		varLock.lock();
		try {					
			vsbCheck();
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MENU_BACKGROUND));
			gc.fillRect(0, 0, width, height);
			
			if (excess) {
				vsb.draw();
			}			
			
			loadData.draw();
			optimize.draw();
			if (showReaders.get()) {
				marketTickReader.draw();
				marketTickOReader.draw();
				originalReader.draw();
				dukasNodeReader.draw();
			} else {
				setOnStart.draw();
				loadOnStart.draw();
				addOnStart.draw();
				toggleOnStart.draw();
			}
			toggleReaders_OnStart.draw();
			darkMode.draw();
			auto.draw();
			drawLoadingSets();
			drawDraggedFiles();
			drawDSButtons();			
		} finally {
			varLock.unlock();
		}
	}
	
	public void draw() {	
		if (Platform.isFxApplicationThread()) {
			drawUI();
		} else {
			Platform.runLater(() -> {
				drawUI();
			});
		}
	}
	
	private boolean visible(double y) {
		if (y > -48 && y < height) {
			return true;
		}
		return false;
	}
	
	private void drawLoadingSets() {
		for (LoadingDataset l : loadingSets) {
			if (!visible(l.y())) {
				continue;
			}
			if (l.validFullSignature()) {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				gc.fillRoundRect(120, l.y(), 510 * l.progress().get() / 100.0, 48, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
			} else {
				l.draw();
			}
		}
	}	
	
	private void drawDSButtons() {
		for (DatasetButton dsb : dsButtons) {
			if (dsb == null || !visible(dsb.y())) {
				continue;
			}
			dsb.draw();
		}
	}
	
	public void recalculateVSBPos() {
		double extra = (datasets.size() - 6) * 58;
		vsb.setY((offset / extra) * (height - 100));
	}
	
	public void adjustDatasetPositions() {		
		varLock.lock();
		try {			
			if (excess) {
				double extra = (datasets.size() - 6) * 58;
				offset = extra * (vsb.y() / (height - 100));
			} else {
				offset = 0;
			}
			for (int i = 0; i < dsButtons.size(); i++) {
				DatasetButton d = dsButtons.get(i);
				if (d == null) {
					continue;
				}
				d.setY(i * 58 + 10 - offset);
			}
			for (LoadingDataset l : loadingSets) {
				l.setY(l.addIndex().get() * 58 + 10 - offset);
			}
		} finally {
			varLock.unlock();
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