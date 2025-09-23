import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DataSet {
	private String name;
	private String signature;
	private int size;
	private double tickSize;
	private int numDecimalPts;
	private ArrayList<DataPair> tickData = new ArrayList<DataPair>();
	private ArrayList<Candlestick> m1Candles = new ArrayList<Candlestick>();
	private ArrayList<Double> lines = new ArrayList<Double>();
	
	class DataPair {
		private double price;
		private LocalDateTime dateTime;		
		
		public DataPair(double price, LocalDateTime dateTime) {
			this.price = price;
			this.dateTime = dateTime;
		}
		
		public double price() {
			return this.price;
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
		private LocalDateTime dateTime;
		
		public Candlestick(double open, double high, double low, double close, LocalDateTime dateTime) {
			this.open = open;
			this.high = high;
			this.low = low;
			this.close = close;
			this.dateTime = dateTime;
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
		
		public LocalDateTime dateTime() {
			return this.dateTime;
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
	
	public DataSet(File file) {
		readData(file);
	}
	
	public int size() {
		return this.size;
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
	
	private void readData(File file) {
		try (FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(" dd/MM/yyyy HH:mm:ss");
			String in;		
			String dateTime;
			String price;
			in = br.readLine();
			signature = in;
			size = Integer.parseInt(in.substring(0, in.indexOf(' ')));
			in = in.substring(in.indexOf(' ') + 1);
			name = in.substring(0, in.indexOf(' '));
			in = in.substring(in.indexOf(' ') + 1);
			tickSize = Double.parseDouble(in.substring(0, in.indexOf(' ')));
			in = in.substring(in.indexOf(' ') + 1);
			numDecimalPts = Integer.parseInt(in);
			in = br.readLine();
			price = in.substring(0, in.indexOf(' '));
			dateTime = in.substring(in.indexOf(' '));
			LocalDateTime ldt = LocalDateTime.parse(dateTime, dtf);
			double val = Double.parseDouble(price);
			tickData.add(new DataPair(val, ldt));								
			int progress = 0;			
			
			double open = val;
			double high = val;
			double low = val;
			double close = val;
			LocalDateTime ldtPrev = ldt;
			double prevPrice = val;
			
			boolean changed = true;
			int last = 0;
			while (true) {
				progress++;
				in = br.readLine();
				if (size != -1) {
					int percent = (int)((double)progress/size*100);
					if (last < percent) {
						changed = true;
					}
					last = percent;
					if (percent > 100 && changed) {
						System.out.println("99%");
						changed = false;
					} else if (changed) {
						System.out.println(percent + "%");
						changed = false;
					}
				}
				if (in == null) {
					break;
				}/*
				if (progress == 100000) {
					break;
				}*/
				price = in.substring(0, in.indexOf(' '));
				dateTime = in.substring(in.indexOf(' '));
				ldt = LocalDateTime.parse(dateTime, dtf);
				val = Double.parseDouble(price);
				tickData.add(new DataPair(val, ldt));
				
				int diff = getDiffInMinutes(ldtPrev, ldt);
				if (ldtPrev.getMinute() == ldt.getMinute()) {
					if (val > high) {
						high = val;
					} else if (val < low) {
						low = val;
					}
				} else {
					close = prevPrice;					
					m1Candles.add(new Candlestick(open, high, low, close, ldt.minusSeconds(ldt.getSecond()).minusMinutes(diff)));
					open = val;
					high = val;
					low = val;
					close = val;
					ldtPrev = ldt;
				}
				prevPrice = val;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int getDiffInMinutes(LocalDateTime ldt1, LocalDateTime ldt2) {
		long ldt1EpochSec = ldt1.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
		long ldt2EpochSec = ldt2.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
		return (int)((ldt2EpochSec - ldt1EpochSec) / 60.0); 
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
	
	public ArrayList<Double> lines() {
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
