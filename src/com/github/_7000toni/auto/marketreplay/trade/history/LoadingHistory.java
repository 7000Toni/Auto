package com.github._7000toni.auto.marketreplay.trade.history;
import java.io.File;
import java.util.ArrayList;

import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.dataset.DataSet;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class LoadingHistory {
	private IntegerProperty progress = new SimpleIntegerProperty(0);
	private BooleanProperty complete = new SimpleBooleanProperty(false);
	private BooleanProperty started = new SimpleBooleanProperty(false);
	private Chart chart;
	private Task<Void> task = null;
	
	public LoadingHistory(Chart chart) {
		this.chart = chart;
	}
	
	public void loadApproxHistory(File history, ArrayList<DataSet.DataPair> data) {	
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {	
				started.set(true);
				chart.setHistory(TradeHistoryLoader.loadApproxHistory(history, data, progress, chart));
				complete.set(true);
				return null;
			}
		};	
		new Thread(task).start();	
	}
	
	public void loadMRHistory(File history) {	
		task = new Task<Void>() {
			@Override
			public Void call() {	
				started.set(true);
				chart.setHistory(TradeHistoryLoader.loadMRHistory(history));
				complete.set(true);
				return null;
			}
		};	
		new Thread(task).start();	
	}
	
	public void stop() {
		if (task != null) {
			task.cancel();
		}
	}
	
	public ReadOnlyIntegerProperty progress() {
		return ReadOnlyIntegerProperty.readOnlyIntegerProperty(progress);
	}
	
	public ReadOnlyBooleanProperty complete() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(complete);
	}
	
	public ReadOnlyBooleanProperty started() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(started);
	}
}
