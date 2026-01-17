import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DataSetLoader {
	private ArrayList<DataSet> datasets;
	private ArrayList<DataSetButton> dsButtons;
	private ArrayList<MarketReplayPane> replays;
	private TickDataFileReader reader;	
	private ArrayList<LoadingDataSet> loadingSets;
	private Tree<CanvasNode> sceneGraph;
	
	public DataSetLoader(ArrayList<DataSet> datasets, ArrayList<DataSetButton> dsButtons, ArrayList<MarketReplayPane> replays, TickDataFileReader reader, ArrayList<LoadingDataSet> loadingSets, Tree<CanvasNode> sceneGraph) {
		this.datasets = datasets;
		this.dsButtons = dsButtons;
		this.replays = replays;
		this.reader = reader;
		this.loadingSets = loadingSets;
		this.sceneGraph = sceneGraph;
	}
	
	public void load() {
		File init = new File("C:\\Users\\Toni C\\Desktop\\TC'S\\The Projects\\Java\\Auto\\res");
		FileChooser fc = new FileChooser();
		if (init.exists()) {
			fc.setInitialDirectory(init);
		} else {
			fc.setInitialDirectory(new File("./"));
		}			
		List<File> files = fc.showOpenMultipleDialog(null);	
		if (files == null) {
			return;
		}
		loadTask(files);
	}
	
	private void loadTask(List<File> files) {
		for (File file : files) {	
			if (file == null) {
				break;
			}
			try (FileInputStream fis = new FileInputStream(file);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {								
				String signature = br.readLine();
				String datum = br.readLine();
				switch (preLoadChecks(signature)) {
					case 1:
						continue;
					case -1:
						return;
					default:
						break;
				}
				LoadingDataSet l = new LoadingDataSet(Menu.MARGIN + datasets.size() * 58, datasets.size(), signature);
				loadingSets.add(l);
				datasets.add(null);
				dsButtons.add(null);
				startTask(datum, l, file);			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
	}
	
	private int preLoadChecks(String signature) {
		if (!Signature.validFull(signature)) {
			System.err.println("file has invalid signature (regex: [0-9]+\s[A-Za-z0-9]+\s[0-9]*\\.[0-9]+\s[0-9]+)");
			return 1;
		}												
		for (DataSet d : datasets) {
			if (d == null) {
				continue;
			}
			if (signature.equals(d.signature())) {
				return 1;
			}
		}		
		for (LoadingDataSet l : loadingSets) {
			if (signature.equals(l.signature())) {
				return 1;
			}
		}
		if (datasets.size() >= 6) {
			return -1;
		}
		return 0;
	}
	
	private void startTask(String datum, LoadingDataSet l, File file) {
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {	
				TickDataFileReader thisReader = setReader(datum);				
				DataSet ds = l.load(file, thisReader);
				Menu.menu().varLock().lock();
				try {
					loadingSets.remove(l);
				} finally {
					Menu.menu().varLock().unlock();
				}
				if (ds == null) {
					abort(l);
					return null;
				}			
				datasets.set(l.addIndex().get(), ds);
				DataSetButton dsb = new DataSetButton(Menu.menu().canvas().getGraphicsContext2D(), 510, 48, 120, l.y(), "Name: " + ds.name() + " Size: " + ds.tickData().size(), 2, 37);
				dsb.setVanGogh((x2, y2, gc) -> {
					gc.setFont(new Font(37));
					dsb.defaultDrawButton();		
				});
				dsButtons.set(l.addIndex().get(), dsb);	
				dsb.setDataSetIndex(l.addIndex().get());
				setDSBEventHandler(dsb);
				setDSBCloseEventHandler(dsb.closeButton(), dsb);
				setDSBMREventHandler(dsb.mrButton());
				TNode<CanvasNode> dsbNode = new TNode<CanvasNode>(dsb, sceneGraph.root());
				Menu.menu().varLock().lock();
				try {
					sceneGraph.addNode(dsbNode);
					sceneGraph.addNode(new TNode<CanvasNode>(dsb.mrButton(), dsbNode));
					sceneGraph.addNode(new TNode<CanvasNode>(dsb.closeButton(), dsbNode));
				} finally {
					Menu.menu().varLock().unlock();
				}
				Menu.menu().draw();
				return null;
			}
		};	
		new Thread(task).start();	
	}
	
	private TickDataFileReader setReader(String datum) {
		if (reader == null) {
			MarketTickFileReader mtfr = new MarketTickFileReader();
			OriginalTickFileReader otfr = new OriginalTickFileReader();
			OptimizedMarketTickFileReader omtfr = new OptimizedMarketTickFileReader();
			DukascopyNodeReader dnr = new DukascopyNodeReader();
			if (mtfr.validDatum(datum)) {
				return mtfr;
			} else if (otfr.validDatum(datum)) {
				return otfr;
			} else if (omtfr.validDatum(datum)) {
				return omtfr;
			} else {
				return dnr;
			}
		}
		return reader;
	}
	
	private void abort(LoadingDataSet l) {
		Menu.menu().varLock().lock();
		try {
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
		} finally {
			Menu.menu().varLock().unlock();			
		}
		Menu.menu().draw();
	}
	
	private void setDSBEventHandler(DataSetButton dsb) {
		dsb.setOnMouseReleased(e -> {
			Stage s = new Stage();
			ChartPane c = new ChartPane(s, 1280, 720, datasets.get(dsb.dataSetIndex()), false, null, null);
			Scene scene = new Scene(c);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		});
	}
	
	private void setDSBCloseEventHandler(CanvasButton close, DataSetButton dsb) {
		close.setOnMouseReleased(e -> {
			Menu.menu().varLock().lock();
			try {
				dsButtons.remove(dsb.dataSetIndex());			
				for (int j = dsb.dataSetIndex(); j < dsButtons.size(); j++) {					
					DataSetButton d = dsButtons.get(j);
					if (d == null) {
						continue;
					}
					d.setY(d.y() - 58);	
					d.setDataSetIndex(d.dataSetIndex() - 1);
				}
				for (LoadingDataSet l : loadingSets) {	
					if (l.addIndex().get() > dsb.dataSetIndex()) {
						l.setAddIndex(l.addIndex().get() - 1);
					}
					l.setY(l.y() - 58);				
				}
				String name = datasets.get(dsb.dataSetIndex()).name();
				Chart.closeAll(name, false);
				for (MarketReplayPane mrp : replays) {
					if (mrp.name().equals(name)) {
						mrp.endReplay();
					}
				}
				datasets.remove(dsb.dataSetIndex());
			} finally {
				Menu.menu().varLock().unlock();
			}
		});	
	}
	
	private void setDSBMREventHandler(CanvasButton mr) {
		mr.setOnMouseReleased(e -> {
			int index = (int)((e.getY() - Menu.MARGIN) / 58);
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
				Menu.menu().varLock().lock();
				try {
					replays.remove(mrp);
				} finally {
					Menu.menu().varLock().unlock();
				}
				mrp.endReplay();
			});
			Menu.menu().varLock().lock();
			try {
				replays.add(mrp);
			} finally {
				Menu.menu().varLock().unlock();
			}
			s2.setResizable(false);		
			Scene scene2 = new Scene(mrp);
			s2.setScene(scene2);
			s2.show();	
		});
	}
}
