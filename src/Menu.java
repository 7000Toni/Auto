import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
	private double width;
	private double height;
	private ArrayList<DataSet> datasets = new ArrayList<DataSet>();
	private ArrayList<DataSetButton> dsButtons = new ArrayList<DataSetButton>();
	private ArrayList<MarketReplayPane> replays = new ArrayList<MarketReplayPane>();
	private TickDataFileReader reader;
	
	private boolean openChartOnStart = false;
	
	public Menu(double width, double height) {
		this.canvas = new Canvas(width, height);
		this.gc = canvas.getGraphicsContext2D();
		this.width = width;
		this.height = height;
		this.loadData = new CanvasButton(gc, 100, 48, MARGIN, MARGIN, "LOAD", 2, 37, null);
		this.loadData.setVanGogh((x, y, gc) -> {
			gc.setFont(new Font(37));
			loadData.defaultDrawButton();			
		});
		this.optimize = new CanvasButton(gc, 100, 48, MARGIN, MARGIN + 58, "OPTIMIZE", 2, 32, null);
		this.optimize.setVanGogh((x, y, gc) -> {
			gc.setFont(new Font(22));
			optimize.defaultDrawButton();			
		});
		this.marketTickReader = new CanvasButton(gc, 100, 32, MARGIN, MARGIN + 58*4, "MT READER", 2, 23, null);
		this.marketTickReader.setVanGogh(readerVG(marketTickReader, 18));
		this.marketTickOReader = new CanvasButton(gc, 100, 32, MARGIN, MARGIN + 58*4 + 37, "MTO READER", 2, 22, null);
		this.marketTickOReader.setVanGogh(readerVG(marketTickOReader, 16));
		this.originalReader = new CanvasButton(gc, 100, 32, MARGIN, MARGIN + 58*4 + 74, "OG READER", 2, 23, null);
		this.originalReader.setVanGogh(readerVG(originalReader, 18));
		marketTickOReader.setPressed(true);
		reader = new OptimizedMarketTickFileReader();
		canvas.setOnMousePressed(e -> onMousePressed(e));
		canvas.setOnMouseReleased(e -> onMouseReleased(e));
		canvas.setOnMouseMoved(e -> onMouseMoved(e));
		canvas.setOnMouseDragged(e -> onMouseDragged(e));
		canvas.setOnMouseExited(e -> onMouseExited(e));
		
		if (openChartOnStart) {
			datasets.add(new DataSet(new File("res/20240624_Optimized.csv"), new OptimizedMarketTickFileReader()));
			DataSet ds = datasets.get(datasets.size() - 1);
			DataSetButton dsb = new DataSetButton(gc, 510, 48, 120, MARGIN + dsButtons.size() * 58, "Name: " + ds.name() + " Size: " + ds.tickData().size(), 2, 37, null);		
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
	}	
	
	private ButtonVanGogh readerVG(CanvasButton cb, int fontSize) {
		return (x, y, gc) -> {
			gc.setFont(new Font(fontSize));
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
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
	
	private void draw() {	
		if (datasets.size() < 6) {
			loadData.enable();
		} else {
			loadData.disable();
		}
		canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
		loadData.draw();
		optimize.draw();
		marketTickReader.draw();
		marketTickOReader.draw();
		originalReader.draw();
		for (DataSetButton dsb : dsButtons) {
			dsb.draw();
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
			reader = new MarketTickFileReader();
		} else if (marketTickOReader.onButton(x, y)) {
			marketTickOReader.setPressed(true);
			reader = new OptimizedMarketTickFileReader();
		} else if (originalReader.onButton(x, y)) {
			originalReader.setPressed(true);
			reader = new OriginalTickFileReader();
		} else {
			for (DataSetButton dsb : dsButtons) {
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
						for (DataSet d : datasets) {
							if (in.equals(d.signature())) {
								add = false;
								break;
							}
						}				
						if (add) {
							datasets.add(new DataSet(file, reader));
							DataSet ds = datasets.get(datasets.size() - 1);
							DataSetButton dsb = new DataSetButton(gc, 510, 48, 120, MARGIN + dsButtons.size() * 58, "Name: " + ds.name() + " Size: " + ds.tickData().size(), 2, 37, null);
							dsb.setVanGogh((x2, y2, gc) -> {
								gc.setFont(new Font(37));
								dsb.defaultDrawButton();		
							});
							dsButtons.add(dsb);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}				
				}
			}
			loadData.setPressed(false);
		} else if (marketTickReader.onButton(x, y)) {
			if (marketTickReader.pressed()) {
				marketTickOReader.setPressed(false);
				originalReader.setPressed(false);
			}			
		} else if (marketTickOReader.onButton(x, y)) {
			if (marketTickOReader.pressed()) {
				marketTickReader.setPressed(false);
				originalReader.setPressed(false);
			}
		} else if (originalReader.onButton(x, y)) {
			if (originalReader.pressed()) {
				marketTickReader.setPressed(false);
				marketTickOReader.setPressed(false);
			}
		} else if (optimize.onButton(x, y)) { 
			if (optimize.pressed()) {
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("./"));
				File file = fc.showOpenDialog(null);		
				if (file != null) {
					MarketTickFileOptimizer.optimize(file, true);			
				}
			}
			optimize.setPressed(false);
		} else {
			int i = 0;
			Object[] dsbs = dsButtons.toArray();
			for (Object obj : dsbs) {
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
					for (int j = i; j < dsButtons.size(); j++) {					
						dsButtons.get(j).setY(dsButtons.get(j).y() - 58);				
					}
					String name = datasets.get(i).name();
					Chart.closeAll(name, false);
					for (MarketReplayPane mrp : replays) {
						if (mrp.name().equals(name)) {
							mrp.endReplay();
						}
					}
					datasets.remove(i);
					i--;
					dsb.closeButton().setPressed(false);
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
		for (DataSetButton dsb : dsButtons) {
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