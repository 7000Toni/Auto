import java.io.File;

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
