import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class DataSet {
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
	private long startEpochMinutes;
	private boolean failed = false;
	private int maxLength = 0;
	
	class DataPair {
		private double price;
		private int candleIndex;
		private LocalDateTime dateTime;		
		
		public DataPair(double price, LocalDateTime dateTime, int candleIndex) {
			this.price = price;
			this.candleIndex = candleIndex;
			this.dateTime = dateTime;
		}
		
		public double price() {
			return this.price;
		}
		
		public int candleIndex() {
			return this.candleIndex;
		}
		
		public LocalDateTime dateTime() {
			return this.dateTime;
		}
	}
	
	class Candlestick {
		private double open;
		private double high;
		private double low;
		private double close;
		private int firstTickIndex;
		private LocalDateTime dateTime;
		private boolean complete;
		
		public Candlestick(double open, double high, double low, double close, LocalDateTime dateTime, boolean complete, int firstTickIndex) {
			this.open = open;
			this.high = high;
			this.low = low;
			this.close = close;
			this.dateTime = dateTime;
			this.complete = complete;
			this.firstTickIndex = firstTickIndex;
		}
		
		public double open() {
			return this.open;
		}
		
		public double high() {
			return this.high;
		}
		
		public double low() {
			return this.low;
		}
		
		public double close() {
			return this.close;
		}
		
		public int firstTickIndex() {
			return this.firstTickIndex;
		}
		
		public LocalDateTime dateTime() {
			return this.dateTime;
		}
		
		public boolean complete() {
			return this.complete;
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
	
	class ReadFileVars {
		DateTimeFormatter dtf;
		String in;		
		String dateTime;
		String price;
		StringTokenizer tokens;
		LocalDateTime ldt;
		BufferedReader br;	
		int firstTickIndex;
		int candleIndex;
		double val;
		boolean add;
		double open;
		double high;
		double low;
		double close;
		LocalDateTime ldtPrev;
		double prevPrice;
		int progress;
		int trueProgress;
		boolean changed = true;
		int last = 0;
		IntegerProperty percent = new SimpleIntegerProperty();
	}
	
	public DataSet(File file, TickDataFileReader tdfr, IntegerProperty prog) {
		readData(file, tdfr, prog);
	}
	
	public DataSet(File file, TickDataFileReader tdfr) {
		readData(file, tdfr, null);
	}
	
	public boolean failed() {
		return failed;
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
		rfv.ldt = tickData().get(startIndex).dateTime;
		rfv.firstTickIndex = startIndex;
		rfv.open = rfv.val;
		rfv.high = rfv.val;
		rfv.low = rfv.val;
		rfv.ldt = rfv.ldt.minusSeconds(rfv.ldt.getSecond()).minusNanos(rfv.ldt.getNano());
		long ldtPrevEpochSec = rfv.ldt.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
		Candlestick c = null;
		for (int i = startIndex + 1; i < replayTickDataSize.get(); i++) {			
			long ldtEpochSec = tickData.get(i).dateTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
			int diff = (int)((ldtEpochSec - ldtPrevEpochSec) / 60.0);
			if (diff == 0) {
				rfv.val = tickData().get(i).price;
				if (rfv.val > rfv.high) {
					rfv.high = rfv.val;
				} else if (rfv.val < rfv.low) {
					rfv.low = rfv.val;
				}
			} else {
				break;
			}
		}
		rfv.close = rfv.val;
		c = new Candlestick(rfv.open, rfv.high, rfv.low, rfv.close, rfv.ldt, false, rfv.firstTickIndex);
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
			System.out.println(name + ": " + rfv.percent.get() + "%");
			Menu m = Menu.menu();
			if (m != null) {
				m.draw();
			}
			rfv.changed = false;
		}
	}
	
	private void addCandlestick(ReadFileVars rfv, boolean complete) {
		rfv.close = rfv.prevPrice;	
		m1Candles.add(new Candlestick(rfv.open, rfv.high, rfv.low, rfv.close, rfv.ldtPrev, complete, rfv.firstTickIndex));
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
			addCandlestick(rfv, true);
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
		int length = ((Double)val).toString().length();
		if (length > maxLength) {
			maxLength = length; 
		}
	}
	
	private void readData(File file, TickDataFileReader tdfr, IntegerProperty prog) {
		try (FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			ReadFileVars rfv = new ReadFileVars();
			if (prog != null) {
				prog.bind(rfv.percent);
			}
			rfv.br = br;
			readSignature(rfv);
			try {
				tdfr.readFirstTick(rfv);
			} catch (Exception e) {
				failed = true;
				e.printStackTrace();
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
				}
				if (rfv.in == null) {
					break;
				}
				if (!rfv.add) {
					rfv.progress--;
					continue;
				}										
				checkAddCandlestick(rfv);
				tickData.add(new DataPair(rfv.val, rfv.ldt, m1Candles.size()));
				checkLength(rfv.val);
			}
			addCandlestick(rfv, false);
		} catch (IOException e) {
			e.printStackTrace();
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
		if (obj instanceof DataSet) {
			if (this.name.equals(((DataSet)obj).name())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
