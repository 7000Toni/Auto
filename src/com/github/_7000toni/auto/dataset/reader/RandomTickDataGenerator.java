package com.github._7000toni.auto.dataset.reader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Random;

import com.github._7000toni.auto.dataset.Dataset;

public class RandomTickDataGenerator implements ITickDataFileReader {
	private static int size;
	
	@Override
	public void readNextTick(Dataset.ReadFileVars rfv) throws IOException, Exception {
		Random ran = new Random();
		rfv.add = false;
		if (size > -1) {
			rfv.in = "read";
		} else {
			rfv.in = null;
		}
		if (rfv.in != null) {
			size--;
			rfv.ldt = Instant.ofEpochMilli(System.currentTimeMillis() - (size - 1000000) * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime();
			if (ran.nextBoolean()) {
				rfv.val = rfv.prevPrice + 0.25f;
			} else {
				rfv.val = rfv.prevPrice - 0.25f;
			}
			rfv.add = true;
		}
	}
	
	@Override
	public void readFirstTick(Dataset.ReadFileVars rfv) throws IOException, Exception {
		size = 1000000;
		while (!rfv.add) {
			readNextTick(rfv);						
		}
		size--;
	}
	
	@Override
	public boolean validDatum(String datum) {
		return true;
	}
}
