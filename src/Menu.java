import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Menu {
	private final double MARGIN = 10;
	
	private Canvas canvas;
	private GraphicsContext gc;
	private CanvasButton loadData;
	private CanvasButton optimize;
	private CanvasButton marketTickReader;
	private CanvasButton marketTickOReader;
	private CanvasButton originalReader;
	private CanvasButton dukasNodeReader;
	private CanvasButton darkMode;
	private double width;
	private double height;
	private ArrayList<DataSet> datasets = new ArrayList<DataSet>();
	private ArrayList<DataSetButton> dsButtons = new ArrayList<DataSetButton>();
	private ArrayList<MarketReplayPane> replays = new ArrayList<MarketReplayPane>();
	private TickDataFileReader reader;	
	private static Menu menu = null;
	
	private boolean openChartOnStart = false;
	private boolean sahilMode = false;
	
	private ArrayList<LoadingDataSet> loadingSets = new ArrayList<LoadingDataSet>();
	private IntegerProperty numJobs = new SimpleIntegerProperty();
	private final ReentrantLock lock = new ReentrantLock();
	
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
		this.optimize = new CanvasButton(gc, 100, 48, MARGIN, MARGIN + 58, "OPTIMIZE", 2, 32);
		this.optimize.setVanGogh(optimizeVG);
		this.marketTickReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3, "MT READER", 2, 24);
		this.marketTickReader.setVanGogh(readerVG(marketTickReader, 18));
		this.marketTickOReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 42, "MTO READER", 2, 23);
		this.marketTickOReader.setVanGogh(readerVG(marketTickOReader, 16));
		this.originalReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 86, "OG READER", 2, 24);
		this.originalReader.setVanGogh(readerVG(originalReader, 18));
		this.dukasNodeReader = new CanvasButton(gc, 100, 35, MARGIN, MARGIN + 58*3 + 129, "DN READER", 2, 24);
		this.dukasNodeReader.setVanGogh(readerVG(dukasNodeReader, 18));
		this.darkMode = new CanvasButton(gc, 100, 48, MARGIN, MARGIN + 58*2, "DARK", 2, 0);
		this.darkMode.setVanGogh((x, y, gc) -> {
			int fontSize;
			if (Chart.darkMode()) {
				darkMode.setText("LIGHT");	
				darkMode.setTextYOffset(36);
				fontSize = 35;
			} else {
				darkMode.setText("DARK");
				darkMode.setTextYOffset(37);
				fontSize = 37;
			}
			gc.setFont(new Font(fontSize));
			darkMode.defaultDrawButton();
		});
		if (sahilMode) {
			originalReader.setPressed(true);
			reader = new OriginalTickFileReader();
		} else {
			marketTickOReader.setPressed(true);
			reader = new OptimizedMarketTickFileReader();
		}
		canvas.setOnMousePressed(e -> onMousePressed(e));
		canvas.setOnMouseReleased(e -> onMouseReleased(e));
		canvas.setOnMouseMoved(e -> onMouseMoved(e));
		canvas.setOnMouseDragged(e -> onMouseDragged(e));
		canvas.setOnMouseExited(e -> onMouseExited(e));
		
		if (openChartOnStart) {
			datasets.add(new DataSet(new File("res/20240624_Optimized.csv"), new OptimizedMarketTickFileReader()));
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
		}
		
		draw();
		menu = this;
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
			if (cb.pressed) {
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
	
	public void draw() {	
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
	
	private void onMousePressed(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		if (loadData.onButton(x, y)) {
			loadData.setPressed(true);
		} else if (optimize.onButton(x, y)) {
			optimize.setPressed(true);
		} else if (marketTickReader.onButton(x, y)) {
			marketTickReader.setPressed(true);			
		} else if (marketTickOReader.onButton(x, y)) {
			marketTickOReader.setPressed(true);			
		} else if (originalReader.onButton(x, y)) {	
			originalReader.setPressed(true);			
		} else if (dukasNodeReader.onButton(x, y)) {
			dukasNodeReader.setPressed(true);
		} else if (darkMode.onButton(x, y)) {
			darkMode.setPressed(true);
		} else {
			for (DataSetButton dsb : dsButtons) {
				if (dsb == null) {
					continue;
				}
				CanvasButton close = dsb.closeButton();
				CanvasButton mr = dsb.mrButton();
				if (close.onButton(x, y)) {
					close.setPressed(true);
					break;
				} else if (mr.onButton(x, y)) {
					mr.setPressed(true);
				} else if (dsb.onButton(x, y)) {
					dsb.setPressed(true);
					break;
				} 
			}
		}
		draw();
	}
	
	private void onMouseReleased(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		if (loadData.onButton(x, y)) {
			if (loadData.pressed() && loadData.enabled()) {
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("./"));
				File file = fc.showOpenDialog(null);		
				if (file != null) {
					try (FileInputStream fis = new FileInputStream(file);
							BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
						String in = br.readLine();
						boolean add = true;
						if (!Signature.validFull(in)) {
							System.err.println("file has invalid signature (regex: [0-9]+\s[A-Za-z0-9]+\s[0-9]*\\.[0-9]+\s[0-9]+)");
							add = false;
						}
						for (DataSet d : datasets) {
							if (in.equals(d.signature())) {
								add = false;
								break;
							}
						}				
						if (add) {							
							LoadingDataSet l = new LoadingDataSet(MARGIN + (datasets.size() + loadingSets.size()) * 58, datasets.size());
							loadingSets.add(l);
							Task<Void> task = new Task<Void>() {
								@Override
								public Void call() {	
									datasets.add(null);
									dsButtons.add(null);
									DataSet ds = l.load(in, file, reader);
									loadingSets.remove(l);
									if (ds == null) {
										dsButtons.remove(l.addIndex().get());
										for (int j = l.addIndex().get() + 1; j < dsButtons.size(); j++) {		
											DataSetButton dsb = dsButtons.get(j);
											if (dsb == null) {
												continue;
											}
											dsButtons.get(j).setY(dsb.y() - 58);				
										}
										for (LoadingDataSet l2 : loadingSets) {
											if (l2.addIndex().get() > l.addIndex().get()) {
												l2.setAddIndex(l2.addIndex().get() - 1);
											}
											l2.setY(l2.y() - 58);				
										}
										datasets.remove(l.addIndex().get());
										draw();
										return null;
									}			
									datasets.set(l.addIndex().get(), ds);
									DataSetButton dsb = new DataSetButton(gc, 510, 48, 120, l.y(), "Name: " + ds.name() + " Size: " + ds.tickData().size(), 2, 37);
									dsb.setVanGogh((x2, y2, gc) -> {
										gc.setFont(new Font(37));
										dsb.defaultDrawButton();		
									});
									dsButtons.set(l.addIndex().get(), dsb);									
									draw();
									return null;
								}
							};				
							new Thread(task).start();							
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}				
				}
			}
			loadData.setPressed(false);
		} else if (marketTickReader.onButton(x, y)) {
			if (marketTickReader.pressed()) {
				reader = new MarketTickFileReader();
				marketTickOReader.setPressed(false);
				originalReader.setPressed(false);
				dukasNodeReader.setPressed(false);
			}			
		} else if (marketTickOReader.onButton(x, y)) {
			if (marketTickOReader.pressed()) {
				reader = new OptimizedMarketTickFileReader();
				marketTickReader.setPressed(false);
				originalReader.setPressed(false);
				dukasNodeReader.setPressed(false);
			}
		} else if (originalReader.onButton(x, y)) {
			if (originalReader.pressed()) {
				reader = new OriginalTickFileReader();
				marketTickReader.setPressed(false);
				marketTickOReader.setPressed(false);
				dukasNodeReader.setPressed(false);
			}
		} else if (dukasNodeReader.onButton(x, y)) {
			if (dukasNodeReader.pressed()) {
				reader = new DukascopyNodeReader();
				marketTickReader.setPressed(false);
				marketTickOReader.setPressed(false);
				originalReader.setPressed(false);
			}
		} else if (darkMode.onButton(x, y)) { 
			if (darkMode.pressed()) {
				Chart.toggleDarkMode(true);				
				darkMode.setPressed(false);
			}
		} else if (optimize.onButton(x, y)) { 		
			if (optimize.pressed()) {
				FileChooser fc = new FileChooser();
				fc.setTitle("Select MarketTick Files");
				fc.setInitialDirectory(new File("./"));
				List<File> files = fc.showOpenMultipleDialog(null);		
				if (files != null) {
					for (File f : files) {						
						Thread t = new Thread(new OptimizeTask(f, numJobs));
						t.start();						
					}
				}
			}
			optimize.setPressed(false);
		} else {
			int i = 0;
			Object[] dsbs = dsButtons.toArray();
			for (Object obj : dsbs) {
				if (obj == null) {
					i++;
					continue;					
				}
				DataSetButton dsb = (DataSetButton)obj;
				if (dsb.pressed()) {
					int index = (int)((y - MARGIN) / 58);
					if (index < 0) {
						index = 0;
					}
					Stage s = new Stage();
					ChartPane c = new ChartPane(s, 1280, 720, datasets.get(index), false, null, null);
					Scene scene = new Scene(c);
					scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
					s.setScene(scene);
					s.show();
					dsb.setPressed(false);
					break;
				} else if (dsb.closeButton().pressed()) {
					dsButtons.remove(i);
					for (int j = i + 1; j < dsButtons.size(); j++) {					
						DataSetButton d = dsButtons.get(j);
						if (d == null) {
							continue;
						}
						dsButtons.get(j).setY(d.y() - 58);				
					}
					for (LoadingDataSet l : loadingSets) {	
						if (l.addIndex().get() > i) {
							l.setAddIndex(l.addIndex().get() - 1);
						}
						l.setY(l.y() - 58);				
					}
					String name = datasets.get(i).name();
					Chart.closeAll(name, false);
					for (MarketReplayPane mrp : replays) {
						if (mrp.name().equals(name)) {
							mrp.endReplay();
						}
					}
					datasets.remove(i);
					break;
				} else if (dsb.mrButton().pressed() ) {
					int index = (int)((y - MARGIN) / 58);
					if (index < 0) {
						index = 0;
					}
					Stage s = new Stage();
					ChartPane c = new ChartPane(s, 1280, 720, datasets.get(index), false, null, null);
					Scene scene = new Scene(c);	
					scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
					s.setScene(scene);
					s.show();
					Stage s2 = new Stage();					
					MarketReplayPane mrp = new MarketReplayPane(c.getChart(), 0, s2);
					s2.setOnCloseRequest(ev -> {
						replays.remove(mrp);
						mrp.endReplay();
					});
					replays.add(mrp);
					s2.setResizable(false);		
					Scene scene2 = new Scene(mrp);
					s2.setScene(scene2);
					s2.show();
					dsb.mrButton().setPressed(false);
				}
				i++;
			}
		}
		if (reader instanceof OriginalTickFileReader) {
			marketTickReader.setPressed(false);
			marketTickOReader.setPressed(false);
			dukasNodeReader.setPressed(false);
		} else if (reader instanceof MarketTickFileReader) {
			marketTickOReader.setPressed(false);
			originalReader.setPressed(false);
			dukasNodeReader.setPressed(false);
		} else if (reader instanceof OptimizedMarketTickFileReader) {
			originalReader.setPressed(false);
			marketTickReader.setPressed(false);
			dukasNodeReader.setPressed(false);
		} else if (reader instanceof DukascopyNodeReader) {
			originalReader.setPressed(false);
			marketTickReader.setPressed(false);
			marketTickOReader.setPressed(false);
		}
		draw();
	}
	
	public void onMouseMoved(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		ButtonChecks.mouseButtonHoverCheck(loadData, x, y);
		ButtonChecks.mouseButtonHoverCheck(optimize, x, y);
		ButtonChecks.mouseButtonSwitchHoverCheck(marketTickReader, x, y);
		ButtonChecks.mouseButtonSwitchHoverCheck(marketTickOReader, x, y);
		ButtonChecks.mouseButtonSwitchHoverCheck(originalReader, x, y);
		ButtonChecks.mouseButtonSwitchHoverCheck(dukasNodeReader, x, y);
		ButtonChecks.mouseButtonHoverCheck(darkMode, x, y);
		for (DataSetButton dsb : dsButtons) {
			if (dsb == null) {
				continue;
			}
			CanvasButton close = dsb.closeButton();
			CanvasButton mr = dsb.mrButton();
			if (ButtonChecks.mouseButtonHoverCheck(close, x, y)) {
				dsb.setPressed(false);
				dsb.setHover(false);
				mr.setHover(false);
				mr.setPressed(false);
			} else if (ButtonChecks.mouseButtonHoverCheck(mr, x, y)) {
				close.setPressed(false);
				close.setHover(false);
				dsb.setHover(false);
				dsb.setPressed(false);
			} else {
				ButtonChecks.mouseButtonHoverCheck(dsb, x, y);
			}
		}		
		draw();
	}
	
	public void onMouseDragged(MouseEvent e) {
		onMouseMoved(e);
	}
	
	public void onMouseExited(MouseEvent e) {
		onMouseMoved(e);
	}
}