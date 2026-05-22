package com.github._7000toni.auto.dataset;
import java.io.File;

import com.github._7000toni.auto.dataset.reader.MarketTickFileOptimizer;

import javafx.beans.property.IntegerProperty;
import javafx.concurrent.Task;

public class OptimizeTask extends Task<Void> {
	private File file;
	private IntegerProperty numJobs;
	
	public OptimizeTask(File file, IntegerProperty numJobs) {
		this.file = file;
		this.numJobs = numJobs;
	}
	
	@Override
	protected Void call() throws Exception {
		MarketTickFileOptimizer.optimize(file, true, numJobs);
		return null;
	}	
}
