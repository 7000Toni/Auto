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
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Menu {
	private final double MARGIN = 10;
	
	private Canvas canvas;
	private GraphicsContext gc;
	private CanvasButton loadData;
	private double width;
	private double height;
	private ArrayList<DataSet> datasets = new ArrayList<DataSet>();
	private ArrayList<DataSetButton> dsButtons = new ArrayList<DataSetButton>();
	
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
		canvas.setOnMousePressed(e -> onMousePressed(e));
		canvas.setOnMouseReleased(e -> onMouseReleased(e));
		canvas.setOnMouseMoved(e -> onMouseMoved(e));
		canvas.setOnMouseDragged(e -> onMouseDragged(e));
		canvas.setOnMouseExited(e -> onMouseExited(e));
		
		datasets.add(new DataSet(new File("res/mesu.txt")));
		DataSet ds = datasets.get(datasets.size() - 1);
		DataSetButton dsb = new DataSetButton(gc, 510, 48, 120, MARGIN + dsButtons.size() * 58, "Name: " + ds.name() + " Size: " + ds.tickData().size(), 2, 37, null);
		dsb.setVanGogh((x, y, gc) -> {
			gc.setFont(new Font(37));
			dsb.defaultDrawButton();		
		});
		dsButtons.add(dsb);
		
		drawMenu();
	}	
	
	public Canvas canvas() {
		return this.canvas;
	}
	
	public void drawMenu() {	
		canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
		loadData.drawButton();
		for (DataSetButton dsb : dsButtons) {
			dsb.drawButton();
		}
	}
	
	public void onMousePressed(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		if (loadData.onButton(x, y)) {
			loadData.setClicked(true);
		} else {
			for (DataSetButton dsb : dsButtons) {
				CanvasButton close = dsb.closeButton();
				if (close.onButton(x, y)) {
					close.setClicked(true);
					break;
				} else if (dsb.onButton(x, y)) {
					dsb.setClicked(true);
					break;
				}
			}
		}
		drawMenu();
	}
	
	public void onMouseReleased(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		if (loadData.onButton(x, y)) {
			FileChooser fc = new FileChooser();
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
						datasets.add(new DataSet(file));
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
			loadData.setClicked(false);
		} else {
			int i = 0;
			Object[] dsbs = dsButtons.toArray();
			for (Object obj : dsbs) {
				DataSetButton dsb = (DataSetButton)obj;
				if (dsb.clicked()) {
					int index = (int)((y - MARGIN) / 58);
					if (index < 0) {
						index = 0;
					}
					Stage s = new Stage();
					ChartPane c = new ChartPane(s, width, height, datasets.get(index));
					Scene scene = new Scene(c);
					scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
					s.setScene(scene);
					s.show();
					dsb.setClicked(false);
					break;
				} else if (dsb.closeButton().clicked()) {
					dsButtons.remove(i);
					for (int j = i; j < dsButtons.size(); j++) {					
						dsButtons.get(j).setY(dsButtons.get(j).y() - 58);				
					}
					Chart.closeAll(datasets.get(i).name());
					datasets.remove(i);
					i--;
					dsb.closeButton().setClicked(false);
					break;
				}
				i++;
			}
		}
		drawMenu();
	}
	
	private boolean mouseButtonHoverCheck(CanvasButton button, double x, double y) {
		if (button.onButton(x, y)) {
			if (!button.clicked()) {
				button.setHover(true);				
			}
			return true;
		} else {
			button.setClicked(false);
			button.setHover(false);
			return false;
		}
	}
	
	public void onMouseMoved(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		mouseButtonHoverCheck(loadData, x, y);
		for (DataSetButton dsb : dsButtons) {
			CanvasButton close = dsb.closeButton();
			if (mouseButtonHoverCheck(close, x, y)) {
				dsb.setClicked(false);
				dsb.setHover(false);
			} else {
				mouseButtonHoverCheck(dsb, x, y);
			}
		}		
		drawMenu();
	}
	
	public void onMouseDragged(MouseEvent e) {
		onMouseMoved(e);
	}
	
	public void onMouseExited(MouseEvent e) {
		onMouseMoved(e);
	}
}