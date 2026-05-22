package com.github._7000toni.auto.dataset;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.github._7000toni.auto.Main;
import com.github._7000toni.auto.canvasnode.CanvasButton;
import com.github._7000toni.auto.canvasnode.DataSetButton;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartPane;
import com.github._7000toni.auto.dataset.reader.DukascopyNodeReader;
import com.github._7000toni.auto.dataset.reader.ITickDataFileReader;
import com.github._7000toni.auto.dataset.reader.MarketTickFileReader;
import com.github._7000toni.auto.dataset.reader.OptimizedMarketTickFileReader;
import com.github._7000toni.auto.dataset.reader.OriginalTickFileReader;
import com.github._7000toni.auto.marketreplay.MarketReplayPane;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

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
	private ITickDataFileReader reader;	
	private ArrayList<LoadingDataSet> loadingSets;
	private Tree<ICanvasNode> sceneGraph;
	private File file;
	
	public DataSetLoader(ArrayList<DataSet> datasets, ArrayList<DataSetButton> dsButtons, ArrayList<MarketReplayPane> replays, ITickDataFileReader reader, ArrayList<LoadingDataSet> loadingSets, Tree<ICanvasNode> sceneGraph) {
		this.datasets = datasets;
		this.dsButtons = dsButtons;
		this.replays = replays;
		this.reader = reader;
		this.loadingSets = loadingSets;
		this.sceneGraph = sceneGraph;
	}
	
	public DataSetLoader(File file, ArrayList<DataSet> datasets, ArrayList<DataSetButton> dsButtons, ArrayList<MarketReplayPane> replays, ITickDataFileReader reader, ArrayList<LoadingDataSet> loadingSets, Tree<ICanvasNode> sceneGraph) {
		this.file = file;
		this.datasets = datasets;
		this.dsButtons = dsButtons;
		this.replays = replays;
		this.reader = reader;
		this.loadingSets = loadingSets;
		this.sceneGraph = sceneGraph;
	}
	
	public void load() {
		if (file != null) {
			ArrayList<File> files = new ArrayList<File>();
			files.add(file);
			loadTask(files);
			return;
		}
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
				Menu.menu().varLock().lock();
				try {
					loadingSets.add(l);
					datasets.add(null);
					dsButtons.add(null);
				} finally {
					Menu.menu().varLock().unlock();
				}
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
		Menu.menu().varLock().lock();
		try {
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
		} finally {
			Menu.menu().varLock().unlock();
		}
		return 0;
	}
	
	private void startTask(String datum, LoadingDataSet l, File file) {
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {	
				ITickDataFileReader thisReader = setReader(datum);				
				DataSet ds = l.load(file, thisReader);
				Menu.menu().varLock().lock();
				try {
					loadingSets.remove(l);				
					if (ds == null) {
						abort(l);						
						return null;
					}			
					datasets.set(l.addIndex().get(), ds);
					DataSetButton dsb = new DataSetButton(Menu.menu().canvas().getGraphicsContext2D(), 510, 48, 120, l.y(), "Name: " + ds.name() + " Size: " + ds.tickData().size(), 2, 37);
					dsb.setVanGogh((x2, y2, gc) -> {
						gc.setFont(new Font(37));
						dsb.defaultDraw();		
					});
					dsButtons.set(l.addIndex().get(), dsb);	
					dsb.setDataSetIndex(l.addIndex().get());
					TNode<ICanvasNode> dsbNode = new TNode<ICanvasNode>(dsb, sceneGraph.root());
					TNode<ICanvasNode> mrNode = new TNode<ICanvasNode>(dsb.mrButton(), dsbNode);
					TNode<ICanvasNode> closeNode = new TNode<ICanvasNode>(dsb.closeButton(), dsbNode);
					setDSBEventHandler(dsb);
					setDSBCloseEventHandler(dsb.closeButton(), dsbNode);
					setDSBMREventHandler(dsb.mrButton(), dsb);
					sceneGraph.addNode(dsbNode);
					sceneGraph.addNode(mrNode);
					sceneGraph.addNode(closeNode);
				} finally {
					Menu.menu().varLock().unlock();
					Menu.menu().draw();
				}				
				return null;
			}
		};	
		new Thread(task).start();	
	}
	
	private ITickDataFileReader setReader(String datum) {
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
				//return new RandomTickDataGenerator();
				return omtfr;
			} else {
				return dnr;
			}
		}
		return reader;
	}
	
	private void abort(LoadingDataSet l) {
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
				l2.setY(l2.y() - 58);				
			}						
		}
		datasets.remove(l.addIndex().get());		
	}
	
	private void setDSBEventHandler(DataSetButton dsb) {
		dsb.setOnMouseClicked(e -> {
			Stage s = new Stage();
			if (Main.icon() != null) {
				s.getIcons().add(Main.icon());
			}
			s.setTitle(datasets.get(dsb.dataSetIndex()).name());
			ChartPane c = new ChartPane(s, 1280, 720, datasets.get(dsb.dataSetIndex()), false, null, null);
			Scene scene = new Scene(c);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();			
		});
	}
	
	private void setDSBCloseEventHandler(CanvasButton close, TNode<ICanvasNode> dsbNode) {
		close.setOnMouseClicked(e -> {
			Menu.menu().varLock().lock();
			try {
				sceneGraph.removeNode(dsbNode);
				dsButtons.remove(((DataSetButton)dsbNode.element()).dataSetIndex());			
				for (int j = ((DataSetButton)dsbNode.element()).dataSetIndex(); j < dsButtons.size(); j++) {					
					DataSetButton d = dsButtons.get(j);
					if (d == null) {
						continue;
					}
					d.setY(d.y() - 58);	
					d.setDataSetIndex(d.dataSetIndex() - 1);
				}
				for (LoadingDataSet l : loadingSets) {	
					if (l.addIndex().get() > ((DataSetButton)dsbNode.element()).dataSetIndex()) {
						l.setAddIndex(l.addIndex().get() - 1);
						l.setY(l.y() - 58);	
					}								
				}
				String name = datasets.get(((DataSetButton)dsbNode.element()).dataSetIndex()).name();
				Chart.closeAll(name, false);
				for (MarketReplayPane mrp : replays) {
					if (mrp.name().equals(name)) {
						mrp.endReplay();
					}
				}
				datasets.remove(((DataSetButton)dsbNode.element()).dataSetIndex());
			} finally {
				Menu.menu().varLock().unlock();
			}
		});	
	}
	
	private void setDSBMREventHandler(CanvasButton mr, DataSetButton dsb) {
		mr.setOnMouseClicked(e -> {
			int index = (int)((e.getY() - Menu.MARGIN) / 58);
			if (index < 0) {
				index = 0;
			}
			Stage s = new Stage();
			s.setTitle(datasets.get(dsb.dataSetIndex()).name());
			ChartPane c = new ChartPane(s, 1280, 720, datasets.get(index), false, null, null);
			Scene scene = new Scene(c);	
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
			Stage s2 = new Stage();	
			if (Main.icon() != null) {
				s.getIcons().add(Main.icon());
				s2.getIcons().add(Main.icon());
			}
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
