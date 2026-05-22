package com.github._7000toni.auto.dataset.reader;
import java.io.IOException;

import com.github._7000toni.auto.dataset.DataSet;

public interface ITickDataFileReader {
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException, Exception;
	public void readFirstTick(DataSet.ReadFileVars rfv) throws IOException, Exception;
	public boolean validDatum(String datum);
}
