import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Menu {
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
	private final ReentrantLock lock = new ReentrantLock();
	
	private Tree<CanvasNode> sceneGraph;
	private CanvasWrapper cw;
	private TNode<CanvasNode> lastNode = null;
	
	private ButtonVanGogh optimizeVG = (x, y, gc) -> {
		gc.setFont(new Font(22));
		if (numJobs.get() > 0) {
			gc.setStroke(Color.RED);
			gc.setFill(Color.RED);
		} else if (Chart.darkMode()) {
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
	};
	
	public Menu(double width, double height) {		
		this.canvas = new Canvas(width, height);		
		this.gc = canvas.getGraphicsContext2D();
		this.width = width;
		this.height = height;				
		
		this.loadData = new CanvasButton(gc, 100, 48, MARGIN, MARGIN, "LOAD", 2, 37);
		this.loadData.setVanGogh((x, y, gc) -> {
			gc.setFont(new Font(37));
			loadData.defaultDrawButton();			
		});
		this.loadData.setOnMouseReleased(e -> {
			DataSetLoader dsl = new DataSetLoader(datasets, dsButtons, replays, reader, loadingSets, sceneGraph);
			dsl.load();
		});
		
		this.optimize = new CanvasButton(gc, 100, 48, MARGIN, MARGIN + 58, "OPTIMIZE", 2, 32);
		this.optimize.setVanGogh(optimizeVG);
		this.optimize.setOnMouseReleased(e -> {
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
		this.marketTickReader.setOnMouseReleased(e -> {			
			marketTickOReader.setOn(false);
			originalReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			marketTickReader.setOn(true);
			reader = new MarketTickFileReader();
		});
		
		this.marketTickOReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 42, "MTO READER", 2, 23);
		this.marketTickOReader.setVanGogh(readerVG(marketTickOReader, 16));
		this.marketTickOReader.setOnMouseReleased(e -> {
			marketTickReader.setOn(false);
			originalReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			marketTickOReader.setOn(true);
			reader = new OptimizedMarketTickFileReader();
		});
		
		this.originalReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 86, "OG READER", 2, 24);
		this.originalReader.setVanGogh(readerVG(originalReader, 18));
		this.originalReader.setOnMouseReleased(e -> {
			marketTickReader.setOn(false);
			marketTickOReader.setOn(false);
			dukasNodeReader.setOn(false);
			auto.setOn(false);
			originalReader.setOn(true);
			reader = new OriginalTickFileReader();
		});
		
		this.dukasNodeReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 129, "DN READER", 2, 24);
		this.dukasNodeReader.setVanGogh(readerVG(dukasNodeReader, 18));
		this.dukasNodeReader.setOnMouseReleased(e -> {
			marketTickReader.setOn(false);
			marketTickOReader.setOn(false);
			originalReader.setOn(false);
			auto.setOn(false);
			dukasNodeReader.setOn(true);
			reader = new DukascopyNodeReader();
		});
		
		this.darkMode = new CanvasButton(gc, 100, 22, MARGIN, MARGIN + 58*2, "DARK", 2, 0);
		this.darkMode.setVanGogh((x, y, gc) -> {
			int fontSize;
			if (Chart.darkMode()) {
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
		});
		this.darkMode.setOnMouseReleased(e -> {
			Chart.toggleDarkMode();	
		});
		
		this.auto = new CanvasButton(gc, 100, 22, MARGIN, MARGIN + 58*2 + 26, "AUTO READER", 2, 17);
		this.auto.setVanGogh(readerVG(auto, 15));
		this.auto.setOnMouseReleased(e -> {
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
			canvasEventFilter(e);
		});
		
		if (openChartOnStart) {
			openChartOnStart();
		}
		
		draw();
		menu = this;
	}	
	
	private void canvasEventFilter(Event e) {
		boolean onNode = false;
		MouseEvent me = null;
		ScrollEvent se = null;
		for (TNode<CanvasNode> t : sceneGraph.postOrderArray()) {	
			CanvasNode cn = t.element();
			if (e instanceof MouseEvent) {
				me = (MouseEvent)e;
				if (!cn.onNode(me.getX(), me.getY())) {
					continue;
				}
				if (!(cn instanceof CanvasWrapper)) {
					onNode = true;
				}
				if (lastNode == null) {
					lastNode = t;
					cn.onMouseEntered(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
					cn.onMouseDragged(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
					cn.onMouseExited(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
					cn.onMousePressed(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
					cn.onMouseReleased(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
					cn.onMouseMoved(me);
				}
				break;
			} else if (e instanceof ScrollEvent) {
				se = (ScrollEvent)e;
				if (!cn.onNode(se.getX(), se.getY())) {
					continue;
				}
				if (!(cn instanceof CanvasWrapper)) {
					onNode = true;
				}
				if (e.getEventType() == ScrollEvent.SCROLL) {
					cn.onScroll(se);
				}
				break;
			}
		}		
		if (me != null) {
			if (!onNode && lastNode != null) {
				lastNode.element().onMouseExited(me);
				lastNode = null;
			}
			sceneGraph.root().element().onMouseMoved(me);
		}
		draw();
	}
	
	private void openChartOnStart() {
		File f = new File("res/20221229_Optimized.csv");
		if (f.exists()) {				
			try (FileInputStream fis = new FileInputStream(f);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {	
				String signature = br.readLine();
				if (!Signature.validFull(signature)) {
					System.err.println("file has invalid signature (regex: [0-9]+\s[A-Za-z0-9]+\s[0-9]*\\.[0-9]+\s[0-9]+)");
					return;
				}
				String datum = br.readLine();
				TickDataFileReader thisReader = reader;
				if (reader == null) {
					MarketTickFileReader mtfr = new MarketTickFileReader();
					OriginalTickFileReader otfr = new OriginalTickFileReader();
					OptimizedMarketTickFileReader omtfr = new OptimizedMarketTickFileReader();
					DukascopyNodeReader dnr = new DukascopyNodeReader();
					if (mtfr.validDatum(datum)) {
						thisReader = mtfr;
					} else if (otfr.validDatum(datum)) {
						thisReader = otfr;
					} else if (omtfr.validDatum(datum)) {
						thisReader = omtfr;
					} else {
						thisReader = dnr;
					}
				}
				datasets.add(new DataSet(f, thisReader));
				DataSet ds = datasets.get(datasets.size() - 1);
				DataSetButton dsb = new DataSetButton(gc, 510, 48, 120, MARGIN + dsButtons.size() * 58, "Name: " + ds.name() + " Size: " + ds.tickData().size(), 2, 37);		
				dsb.setVanGogh((x, y, gc) -> {
					gc.setFont(new Font(37));
					dsb.defaultDrawButton();		
				});
				Stage s = new Stage();
				ChartPane c = new ChartPane(s, 1280, 720, datasets.get(0), false, null, null);
				Scene scene = new Scene(c);
				scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
				s.setScene(scene);
				s.show();
				dsButtons.add(dsb);						
			} catch(IOException e) {
				e.printStackTrace();
			}				
		}
	}
	
	private ButtonVanGogh readerVG(CanvasButton cb, int fontSize) {
		return (x, y, gc) -> {
			gc.setFont(new Font(fontSize));
			if (Chart.darkMode()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
				gc.setFill(Color.BLACK);
			}
			if (cb.on) {
				if (cb.hover) {
					gc.setStroke(Color.DARKORANGE);
					gc.setFill(Color.DARKORANGE);
				} else {
					gc.setStroke(Color.ORANGE);
					gc.setFill(Color.ORANGE);
				}
			} else if (cb.hover) {
				gc.setStroke(Color.GRAY);
				gc.setFill(Color.GRAY);
			}
			gc.strokeRect(x, y, cb.width, cb.height);
			gc.fillText(cb.text, x + cb.textXOffset, y + cb.textYOffset);
		};
	}
	
	public Canvas canvas() {
		return this.canvas;
	}
	
	public static Menu menu() {
		return menu;
	}
	
	private void drawUI() {
		lock.lock();
		try {		
			if (datasets.size() < 6) {
				loadData.enable();
			} else {
				loadData.disable();
			}
			if (Chart.darkMode()) {
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
			lock.unlock();
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
		if (Chart.darkMode()) {
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
	
	protected void mergeFiles(List<File> files) {
		ArrayList<String> nf = new ArrayList<String>();
		for (File f : files) {										
			try (FileInputStream fis = new FileInputStream(f);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
				String s = br.readLine();
				while (s != null) {
					nf.add(s);
					s = br.readLine();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		try (FileOutputStream fos = new FileOutputStream(new File("./merged"))) {
			fos.write(("size name tickSize numDecimalPts\n").getBytes());
			for (String s : nf) {
				if (!Signature.validFull(s)) {
					fos.write((s + '\n').getBytes());
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}