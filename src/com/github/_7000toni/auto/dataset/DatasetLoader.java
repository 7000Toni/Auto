package com.github._7000toni.auto.dataset;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.github._7000toni.auto.Main;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.DatasetButton;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartPane;
import com.github._7000toni.auto.dataset.reader.DukascopyNodeReader;
import com.github._7000toni.auto.dataset.reader.ITickDataFileReader;
import com.github._7000toni.auto.dataset.reader.MarketTickFileReader;
import com.github._7000toni.auto.dataset.reader.OptimizedMarketTickFileReader;
import com.github._7000toni.auto.dataset.reader.OriginalTickFileReader;
import com.github._7000toni.auto.marketreplay.MarketReplayNode;
import com.github._7000toni.auto.marketreplay.MarketReplayPane;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.settings.MiscellaneousSettings;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DatasetLoader {
	private ArrayList<Dataset> datasets;
	private ArrayList<DatasetButton> dsButtons;
	private ArrayList<MarketReplayNode> replays;
	private ITickDataFileReader reader;	
	private ArrayList<LoadingDataset> loadingSets;
	private File file;
	
	public DatasetLoader(ArrayList<Dataset> datasets, ArrayList<DatasetButton> dsButtons, ArrayList<MarketReplayNode> replays, ITickDataFileReader reader, ArrayList<LoadingDataset> loadingSets) {
		this.datasets = datasets;
		this.dsButtons = dsButtons;
		this.replays = replays;
		this.reader = reader;
		this.loadingSets = loadingSets;
	}
	
	public DatasetLoader(File file, ArrayList<Dataset> datasets, ArrayList<DatasetButton> dsButtons, ArrayList<MarketReplayNode> replays, ITickDataFileReader reader, ArrayList<LoadingDataset> loadingSets) {
		this.file = file;
		this.datasets = datasets;
		this.dsButtons = dsButtons;
		this.replays = replays;
		this.reader = reader;
		this.loadingSets = loadingSets;
	}
	
	public void load() {
		if (file != null) {
			ArrayList<File> files = new ArrayList<File>();
			files.add(file);
			loadTask(files);
			return;
		}
		File init = new File(MiscellaneousSettings.initFileDir());
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
				LoadingDataset l = new LoadingDataset(Menu.MARGIN + datasets.size() * 58, datasets.size(), signature);
				Menu.menu().varLock().lock();
				try {					
					loadingSets.add(l);
					if (!l.validFullSignature()) {
						LoadingDataset.incrementLoadingSets();
					}
					datasets.add(null);
					dsButtons.add(null);
					Menu.menu().recalculateVSBPos();
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
		if (!Signature.validFull(signature) && !Signature.validPartial(signature)) {
			System.err.println("file has invalid signature (regex: [0-9]+\s[A-Za-z0-9]+\s[0-9]*\\.[0-9]+\s[0-9]+ or [A-Za-z0-9]+\s[0-9]*\\.[0-9]+\s[0-9]+)");
			return 1;
		}
		Menu.menu().varLock().lock();
		try {
			for (Dataset d : datasets) {
				if (d == null) {
					continue;
				}
				if (signature.equals(d.signature())) {
					return 1;
				}
			}		
			for (LoadingDataset l : loadingSets) {
				if (signature.equals(l.signature())) {
					return 1;
				}
			}
		} finally {
			Menu.menu().varLock().unlock();
		}
		return 0;
	}
	
	private void startTask(String datum, LoadingDataset l, File file) {
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {	
				ITickDataFileReader thisReader = setReader(datum);				
				Dataset ds = l.load(file, thisReader);				
				Menu.menu().varLock().lock();
				try {
					if (!l.validFullSignature()) {
						LoadingDataset.decrementLoadingSets();						
					}		
					loadingSets.remove(l);						
					if (ds == null) {
						abort(l);						
						return null;
					}			
					datasets.set(l.addIndex().get(), ds);
					DatasetButton dsb = new DatasetButton(Menu.menu().canvas().getGraphicsContext2D(), 510, 48, 120, l.y(), "Name: " + ds.name() + " Size: " + ds.tickData().size(), 2, 37);
					dsb.setVanGogh((x2, y2, gc) -> {
						Font oldFont = gc.getFont();
						gc.setFont(Font.font(oldFont.getName(), FontWeight.findByName(oldFont.getStyle()), 37));
						dsb.defaultDraw();	
						gc.setFont(oldFont);
					});
					dsButtons.set(l.addIndex().get(), dsb);	
					dsb.setDatasetIndex(l.addIndex().get());
					setDSBEventHandler(dsb);
					setDSBCloseEventHandler(dsb.closeButton(), dsb);
					setDSBMREventHandler(dsb.mrButton(), dsb);
					Menu.menu().resetSceneGraph();				
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
	
	private void abort(LoadingDataset l) {
		dsButtons.remove(l.addIndex().get());
		for (int j = l.addIndex().get() + 1; j < dsButtons.size(); j++) {		
			DatasetButton dsb = dsButtons.get(j);
			if (dsb == null) {
				continue;
			}
			dsButtons.get(j).setY(dsb.y() - 58);				
		}
		for (LoadingDataset l2 : loadingSets) {
			if (l2.addIndex().get() > l.addIndex().get()) {
				l2.setAddIndex(l2.addIndex().get() - 1);
				l2.setY(l2.y() - 58);				
			}						
		}
		datasets.remove(l.addIndex().get());		
		Menu.menu().adjustDatasetPositions();
		Menu.menu().recalculateVSBPos();
	}
	
	private void setDSBEventHandler(DatasetButton dsb) {
		dsb.setOnScroll(e -> {	
			Menu.menu().onScroll(e);
		});
		dsb.setOnMouseClicked(e -> {			
			Stage s = new Stage();
			if (Main.icon() != null) {
				s.getIcons().add(Main.icon());
			}
			s.setTitle(datasets.get(dsb.datasetIndex()).name());
			ChartPane c = new ChartPane(s, 1280, 720, datasets.get(dsb.datasetIndex()), false, null);
			Scene scene = new Scene(c);
			scene.addEventFilter(KeyEvent.ANY, ev -> c.getChart().canvasEventFilter().canvasEventFilter(ev));
			s.setScene(scene);
			s.show();			
		});
	}
	
	private void setDSBCloseEventHandler(CanvasButton close, DatasetButton dsb) {
		close.setOnScroll(e -> {	
			Menu.menu().onScroll(e);
		});
		close.setOnMouseClicked(e -> {
			Menu.menu().varLock().lock();
			try {				
				dsButtons.remove(dsb.datasetIndex());
				Menu.menu().resetSceneGraph();
				for (int j = dsb.datasetIndex(); j < dsButtons.size(); j++) {					
					DatasetButton d = dsButtons.get(j);
					if (d == null) {
						continue;
					}
					d.setY(d.y() - 58);	
					d.setDatasetIndex(d.datasetIndex() - 1);
				}
				for (LoadingDataset l : loadingSets) {	
					if (l.addIndex().get() > dsb.datasetIndex()) {
						l.setAddIndex(l.addIndex().get() - 1);
						l.setY(l.y() - 58);	
					}								
				}				
				String name = datasets.get(dsb.datasetIndex()).name();
				Chart.closeAll(name, false);
				MarketReplayNode mrn = null;
				for (MarketReplayNode n : replays) {
					if (n.name().equals(name)) {
						n.endReplay();
						mrn = n;
					}
				}
				if (mrn != null) {
					replays.remove(mrn);
				}
				int index = dsb.datasetIndex();
				datasets.remove(index);
				Menu.menu().recalculateVSBPos();			
			} finally {
				Menu.menu().varLock().unlock();
			}
		});	
	}
	
	private void setDSBMREventHandler(CanvasButton mr, DatasetButton dsb) {
		mr.setOnScroll(e -> {	
			Menu.menu().onScroll(e);
		});
		mr.setOnMouseClicked(e -> {
			for (MarketReplayNode mrn : replays) {
				if (mrn.name().equals(datasets.get(dsb.datasetIndex()).name())) {
					return;
				}
			}		
			Stage s = new Stage();
			s.setTitle(datasets.get(dsb.datasetIndex()).name());
			ChartPane c = new ChartPane(s, 1280, 720, datasets.get(dsb.datasetIndex()), false, null);
			Scene scene = new Scene(c);	
			scene.addEventFilter(KeyEvent.ANY, ev -> c.getChart().canvasEventFilter().canvasEventFilter(ev));
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
					replays.remove(mrp.mrNode());
				} finally {
					Menu.menu().varLock().unlock();
				}
				mrp.mrNode().endReplay();
			});			
			Menu.menu().varLock().lock();
			try {
				replays.add(mrp.mrNode());
			} finally {
				Menu.menu().varLock().unlock();
			}
			s2.setResizable(false);		
			Scene scene2 = new Scene(mrp);
			scene2.addEventFilter(KeyEvent.ANY, ev -> mrp.canvasEventFilter().canvasEventFilter(ev));
			s2.setScene(scene2);
			s2.show();	
		});
	}
}
