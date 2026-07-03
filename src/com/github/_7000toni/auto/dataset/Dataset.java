package com.github._7000toni.auto.dataset;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import com.github._7000toni.auto.chart.drawing.Line;
import com.github._7000toni.auto.dataset.reader.ITickDataFileReader;
import com.github._7000toni.auto.dataset.timeframe.Timeframe;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.miscellaneous.Round;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Dataset {
	public static final String BASE_TF_NAME = "T1/M1";
	
	private String name;
	private String signature;
	private int size;
	private IntegerProperty replayTickDataSize = new SimpleIntegerProperty();
	private IntegerProperty replayM1CandlesDataSize = new SimpleIntegerProperty();
	private double tickSize;
	private int numDecimalPts;
	private ArrayList<DataPair> tickData = new ArrayList<DataPair>();
	private ArrayList<Candlestick> m1Candles = new ArrayList<Candlestick>();
	private ArrayList<Line> lines = new ArrayList<Line>();
	private ArrayList<Timeframe> timeframes = new ArrayList<Timeframe>();
	private long startEpochMinutes;
	private boolean failed = false;
	private int maxLength = 0;
	private final ReentrantLock varLock = new ReentrantLock();
	
	public static class DataPair {
		private float price;
		private int candleIndex;
		private long dateTime;		
		
		public DataPair(float price, LocalDateTime dateTime, int candleIndex) {
			this.price = price;
			this.candleIndex = candleIndex;
			this.dateTime = dateTime.toInstant(ZoneOffset.UTC).getEpochSecond()*1000000000 + dateTime.toInstant(ZoneOffset.UTC).getNano();
		}
		
		public float price() {
			return this.price;
		}
		
		public int candleIndex() {
			return this.candleIndex;
		}
		
		public LocalDateTime dateTime() {
			long seconds = dateTime / 1_000_000_000;
	        long nanos = dateTime % 1_000_000_000;
	        Instant i = Instant.ofEpochSecond(seconds, nanos);
	        LocalDateTime ldt = LocalDateTime.ofInstant(i, ZoneOffset.UTC);
			return ldt;
		}
	}
	
	public static class Candlestick {
		private float open;
		private float high;
		private float low;
		private float close;
		private int firstTickIndex;
		private long dateTime;
		
		public Candlestick(float open, float high, float low, float close, LocalDateTime dateTime, int firstTickIndex) {
			this.open = open;
			this.high = high;
			this.low = low;
			this.close = close;
			this.dateTime = dateTime.toInstant(ZoneOffset.UTC).getEpochSecond()*1000000000 + dateTime.toInstant(ZoneOffset.UTC).getNano();
			this.firstTickIndex = firstTickIndex;
		}
		
		public float open() {
			return this.open;
		}
		
		public float high() {
			return this.high;
		}
		
		public float low() {
			return this.low;
		}
		
		public float close() {
			return this.close;
		}
		
		public float price(char ohlc) {
			if (ohlc == 'o') {
				return open;
			} else if (ohlc == 'h') {
				return high;
			} else if (ohlc == 'l') {
				return low;
			} else {
				return close;
			}
		}
		
		public int firstTickIndex() {
			return this.firstTickIndex;
		}
		
		public LocalDateTime dateTime() {
			long seconds = dateTime / 1_000_000_000;
	        long nanos = dateTime % 1_000_000_000;
	        Instant i = Instant.ofEpochSecond(seconds, nanos);
	        LocalDateTime ldt = LocalDateTime.ofInstant(i, ZoneOffset.UTC);
			return ldt;
		}
		
		@Override
		public String toString() {
			String ret = "open:\t" + open;
			ret += "\nhigh:\t" + high;
			ret += "\nlow:\t" + low;
			ret += "\nclose:\t" + close;
			ret += "\nldt:\t" + dateTime;
			return ret;
		}
	}
	
	public static class ReadFileVars {
		public DateTimeFormatter dtf;
		public String in;		
		public String dateTime;
		public String price;
		public StringTokenizer tokens;
		public LocalDateTime ldt;
		public BufferedReader br;	
		public int firstTickIndex;
		public int candleIndex;
		public float val;
		public boolean add;
		public float open;
		public float high;
		public float low;
		public float close;
		public LocalDateTime ldtPrev;
		public float prevPrice;
		public int progress;
		public int trueProgress;
		public boolean changed = true;
		public int last = 0;
		public IntegerProperty percent = new SimpleIntegerProperty();
	}
	
	public Dataset(File file, ITickDataFileReader tdfr, IntegerProperty prog) {
		readData(file, tdfr, prog);
	}
	
	public Dataset(File file, ITickDataFileReader tdfr) {
		readData(file, tdfr, null);
	}
	
	public boolean failed() {
		return failed;
	}
	
	public ReentrantLock varLock() {
		return varLock;
	}
	
	public ReadOnlyIntegerProperty tickDataSize(boolean replayMode) {
		if (replayMode) {
			return IntegerProperty.readOnlyIntegerProperty(replayTickDataSize);
		} else {
			return IntegerProperty.readOnlyIntegerProperty(new SimpleIntegerProperty(tickData.size()));
		}
	}
	
	public Candlestick makeLastReplayCandlestick(int startIndex) {
		ReadFileVars rfv = new ReadFileVars();
		rfv.val = tickData().get(startIndex).price;
		rfv.ldt = tickData().get(startIndex).dateTime();
		rfv.firstTickIndex = startIndex;
		rfv.open = rfv.val;
		rfv.high = rfv.val;
		rfv.low = rfv.val;
		rfv.ldt = rfv.ldt.minusSeconds(rfv.ldt.getSecond()).minusNanos(rfv.ldt.getNano());
		Candlestick c = null;
		for (int i = startIndex + 1; i < replayTickDataSize.get(); i++) {			
			rfv.val = tickData().get(i).price;
			if (rfv.val > rfv.high) {
				rfv.high = rfv.val;
			} else if (rfv.val < rfv.low) {
				rfv.low = rfv.val;
			}
		}
		rfv.close = rfv.val;
		c = new Candlestick(rfv.open, rfv.high, rfv.low, rfv.close, rfv.ldt, rfv.firstTickIndex);
		return c;
	}
	
	public void setReplayTickDataSize(int replayTickDataSize) {
		if (replayTickDataSize > size) {
			this.replayTickDataSize.set(size);
		} else {
			this.replayTickDataSize.set(replayTickDataSize);
		}
	}
	
	public ReadOnlyIntegerProperty m1CandlesDataSize(boolean replayMode) {
		if (replayMode) {
			return IntegerProperty.readOnlyIntegerProperty(replayM1CandlesDataSize);
		} else {
			return IntegerProperty.readOnlyIntegerProperty(new SimpleIntegerProperty(m1Candles.size()));
		}
	}
	
	public void setReplayM1CandlesDataSize(int replayM1CandlesDataSize) {
		if (replayM1CandlesDataSize > m1Candles.size()) {
			this.replayM1CandlesDataSize.set(m1Candles.size());
		} else {
			this.replayM1CandlesDataSize.set(replayM1CandlesDataSize);
		}
	}
	
	public int maxLength() {
		return maxLength;
	}
	
	public double tickSize() {
		return this.tickSize;
	}
	
	public int numDecimalPts() {
		return this.numDecimalPts;
	}
	
	public String signature() {
		return this.signature;
	}
	
	public long startEpochMinutes() {
		return this.startEpochMinutes;
	}
	
	public ArrayList<Timeframe> timeframes() {
		return timeframes;
	}
	
	private void readSignature(ReadFileVars rfv) {
		try {
			rfv.in = rfv.br.readLine();
			signature = rfv.in;			
			size = Integer.parseInt(rfv.in.substring(0, rfv.in.indexOf(' ')));
			rfv.in = rfv.in.substring(rfv.in.indexOf(' ') + 1);
			name = rfv.in.substring(0, rfv.in.indexOf(' '));
			rfv.in = rfv.in.substring(rfv.in.indexOf(' ') + 1);
			tickSize = Double.parseDouble(rfv.in.substring(0, rfv.in.indexOf(' ')));
			rfv.in = rfv.in.substring(rfv.in.indexOf(' ') + 1);
			numDecimalPts = Integer.parseInt(rfv.in);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private void setInitCandlestickVars(ReadFileVars rfv) {
		rfv.open = rfv.val;
		rfv.high = rfv.val;
		rfv.low = rfv.val;
		rfv.close = rfv.val;
		rfv.firstTickIndex = 0;
		rfv.ldtPrev = rfv.ldt.minusSeconds(rfv.ldt.getSecond()).minusNanos(rfv.ldt.getNano());
		rfv.prevPrice = rfv.val;
		startEpochMinutes = rfv.ldtPrev.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
	}
	
	private void showPercentage(ReadFileVars rfv) {
		rfv.percent.set((int)((double)rfv.trueProgress/size*100));
		if (rfv.last < rfv.percent.get()) {
			rfv.changed = true;
		}
		rfv.last = (int)rfv.percent.get();
		if (rfv.changed) {
			//System.out.println(name + ": " + rfv.percent.get() + "%");
			Menu m = Menu.menu();
			if (m != null) {				
				m.draw();
			}
			rfv.changed = false;
		}
	}
	
	private void addCandlestick(ReadFileVars rfv) {
		rfv.close = rfv.prevPrice;	
		m1Candles.add(new Candlestick((float)Round.round(rfv.open, numDecimalPts), (float)Round.round(rfv.high, numDecimalPts), (float)Round.round(rfv.low, numDecimalPts), (float)Round.round(rfv.close, numDecimalPts), rfv.ldtPrev, rfv.firstTickIndex));
	}
	
	private void checkAddCandlestick(ReadFileVars rfv) {
		long ldtPrevEpochSec = rfv.ldtPrev.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
		long ldtEpochSec = rfv.ldt.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
		int diff = (int)((ldtEpochSec - ldtPrevEpochSec) / 60.0);
		if (diff == 0) {
			if (rfv.val > rfv.high) {
				rfv.high = rfv.val;
			} else if (rfv.val < rfv.low) {
				rfv.low = rfv.val;
			}
		} else {
			addCandlestick(rfv);
			rfv.firstTickIndex = rfv.progress - 1;
			rfv.open = rfv.val;
			rfv.high = rfv.val;
			rfv.low = rfv.val;
			rfv.close = rfv.val;
			rfv.ldtPrev = rfv.ldt.minusSeconds(rfv.ldt.getSecond()).minusNanos(rfv.ldt.getNano());
		}
		rfv.prevPrice = rfv.val;
	}
	
	private void checkLength(double val) {
		int length = ((Double)Round.round(val, numDecimalPts)).toString().length();
		if (length > maxLength) {
			maxLength = length; 
		}
	}
	
	private void readData(File file, ITickDataFileReader tdfr, IntegerProperty prog) {
		try (FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {			
			ReadFileVars rfv = new ReadFileVars();
			if (prog != null) {
				prog.bind(rfv.percent);
			}
			rfv.br = br;			
			readSignature(rfv);
			System.out.println("began loading: " + name);
			try {
				tdfr.readFirstTick(rfv);
			} catch (Exception e) {
				failed = true;
				e.printStackTrace();
				return;
			}
			if (!rfv.add) {
				return;
			}
			tickData.add(new DataPair(rfv.val, rfv.ldt, 0));	
			checkLength(rfv.val);
			setInitCandlestickVars(rfv);
			rfv.progress = 1;
			rfv.trueProgress = 1;
			rfv.changed = true;
			rfv.last = 0;
			for (int i = 1; i < size; i++) {
				rfv.progress++;
				rfv.trueProgress++;				
				showPercentage(rfv);
				try {
					tdfr.readNextTick(rfv);
				} catch (Exception e) {
					failed = true;
					e.printStackTrace();
					break;
				}
				if (rfv.in == null) {
					break;
				}
				if (!rfv.add) {
					rfv.progress--;
					continue;
				}										
				checkAddCandlestick(rfv);
				tickData.add(new DataPair((float)Round.round(rfv.val, numDecimalPts), rfv.ldt, m1Candles.size()));
				checkLength(rfv.val);
			}
			addCandlestick(rfv);
			System.out.println("finished loading: " + name);
			timeframes.add(new Timeframe(this, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean addTimeframe(Dataset dataSet, boolean tickBased, boolean staticTF, int period) {
		String name = Timeframe.determineName(tickBased, period);
		for (Timeframe tf : timeframes) {
			if (tf.name().equals(name)) {
				return false;
			}
		}
		timeframes.add(new Timeframe(dataSet, tickBased, staticTF, period));
		return true;
	}
	
	public Timeframe getTimeframe(String name) {
		for (Timeframe tf : timeframes) {
			if (tf.name().equals(name)) {
				return tf;
			}
		}
		return null;
	}
	
	public void removeTimeframe(String name) {
		for (int i = timeframes.size() - 1; i > -1; i--) {
			if (timeframes.get(i).name().equals(name)) {
				this.timeframes.remove(i);
			}
		}
	}
	
	public String name() {
		return this.name;
	}
	
	public ArrayList<DataPair> tickData() {
		return this.tickData;
	}
	
	public ArrayList<Candlestick> m1Candles() {
		return this.m1Candles;
	}
	
	public ArrayList<Line> lines() {
		return this.lines;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Dataset) {
			if (this.name.equals(((Dataset)obj).name())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
