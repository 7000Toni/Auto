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

public class DataSet {
	private String name;
	private String signature;
	private int size;
	private int replayTickDataSize;
	private int replayM1CandlesDataSize;
	private double tickSize;
	private int numDecimalPts;
	private ArrayList<DataPair> tickData = new ArrayList<DataPair>();
	private ArrayList<Candlestick> m1Candles = new ArrayList<Candlestick>();
	private ArrayList<Line> lines = new ArrayList<Line>();
	private long startEpochMinutes;
	
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
		boolean changed = true;
		int last = 0;
	}
	
	public DataSet(File file, TickDataFileReader tdfr) {
		readData(file, tdfr);
	}
	
	public int tickDataSize(boolean replayMode) {
		if (replayMode) {
			return this.replayTickDataSize;
		} else {
			return this.tickData.size();
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
		for (int i = startIndex + 1; i < replayTickDataSize; i++) {			
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
		this.replayTickDataSize = replayTickDataSize;
	}
	
	public int m1CandlesDataSize(boolean replayMode) {
		if (replayMode) {
			return this.replayM1CandlesDataSize;
		} else {
			return this.m1Candles.size();
		}
	}
	
	public void setReplayM1CandlesDataSize(int replayM1CandlesDataSize) {
		this.replayM1CandlesDataSize = replayM1CandlesDataSize;
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
		int percent = (int)((double)rfv.progress/size*100);
		if (rfv.last < percent) {
			rfv.changed = true;
		}
		rfv.last = percent;
		if (rfv.changed) {
			System.out.println(percent + "%");
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
	
	private void readData(File file, TickDataFileReader tdfr) {
		try (FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			ReadFileVars rfv = new ReadFileVars();
			rfv.br = br;
			readSignature(rfv);
			tdfr.readFirstTick(rfv);			
			if (!rfv.add) {
				return;
			}
			tickData.add(new DataPair(rfv.val, rfv.ldt, 0));												
			setInitCandlestickVars(rfv);
			rfv.progress = 1;
			rfv.changed = true;
			rfv.last = 0;
			for (int i = 1; i < size; i++) {
				rfv.progress++;
				showPercentage(rfv);
				tdfr.readNextTick(rfv);		
				if (rfv.in == null) {
					break;
				}
				if (!rfv.add) {
					continue;
				}										
				checkAddCandlestick(rfv);
				tickData.add(new DataPair(rfv.val, rfv.ldt, m1Candles.size()));
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
