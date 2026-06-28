package com.github._7000toni.auto.dataset.reader;
import java.io.IOException;

import com.github._7000toni.auto.dataset.Dataset;

public interface ITickDataFileReader {
	public void readNextTick(Dataset.ReadFileVars rfv) throws IOException, Exception;
	public void readFirstTick(Dataset.ReadFileVars rfv) throws IOException, Exception;
	public boolean validDatum(String datum);
}
