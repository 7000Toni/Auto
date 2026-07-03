package com.github._7000toni.auto.dataset.timeframe;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;

import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.Dataset.Candlestick;
import com.github._7000toni.auto.dataset.Dataset.DataPair;

public class Timeframe {
	private String name;
	private Dataset dataSet;
	private ArrayList<Candlestick> data = null;
	private ArrayList<DataPair> tickData = null;
	private boolean base = false;
	private boolean tickBased;
	private boolean staticTF;
	private int period = -1;
	
	public Timeframe(Dataset dataSet, boolean base) {
		this.dataSet = dataSet;
		this.name = Dataset.BASE_TF_NAME;
		this.base = true;
		this.data = dataSet.m1Candles();
		this.tickData = dataSet.tickData();
	}
	
	public Timeframe(Dataset dataSet, boolean tickBased, boolean staticTF, int period) {
		constructorStuff(dataSet, tickBased, staticTF, period);
	}
	
	private void constructorStuff(Dataset dataSet, boolean tickBased, boolean staticTF, int period) {
		this.dataSet = dataSet;
		this.tickBased = tickBased;
		this.period = period;
		name = determineName(tickBased, period);
		if (tickBased) {
			calculateTickComposite();
		} else {
			calculateCandleComposite();
		}
	}
	
	public static String determineName(boolean tickBased, int period) {
		String name = "";
		if (tickBased) {
			name = "T" + period;
		} else {
			int M = period/43800;
			int w = period/10080;
			int d = period/1440;
			int h = period/60;
			int m = period%60;
			int od = d;
			d = d - 7*w;			
			h = h - 24*od;
			if (M > 0) {
				name += w==0&&d==0&&h==0&&m==0?"MONTHLY":"M"+M+"+";
			}
			if (w > 0) {
				name += M==0&&d==0&&h==0&&m==0?"WEEKLY":"W"+w+"+";
			}
			if (d > 0) {
				name += M==0&&w==0&&h==0&&m==0?"DAILY":"D"+d+"+";
			}
			if (h > 0) {
				name += M==0&&w==0&&d==0&&m==0?"HOURLY":"H"+h+"+";
			}
			if (m > 0) {
				name += "M"+m;
			}
			if (name.endsWith("+")) {
				name = name.substring(0, name.length() - 1);
			}
		}
		return name.trim();
	}
	
	private void calculateTickComposite() {
		this.data = new ArrayList<Candlestick>();
		float open = 0;
		float high = 0;
		float low = 0;
		ArrayList<DataPair> tickData = dataSet.tickData();
		int size = tickData.size();
		for (int i = 0; i < size; i++) {
			float val = tickData.get(i).price();
			if (i % period == 0) {
				open = val;
				high = val;
				low = val;
			}			
			if (val > high) {
				high = val;
			} else if (val < low) {
				low = val;
			}
			boolean complete = (i+1)%period == 0;
			if (complete || i == tickData.size() - 1) {
				int firstTickIndex = i-(i%period);
				this.data.add(new Candlestick(open, high, low, val, tickData.get(firstTickIndex).dateTime(), firstTickIndex));
			}	
		}	
	}
	
	private void calculateCandleComposite() {
		this.data = new ArrayList<Candlestick>();		
		ArrayList<Candlestick> candles = dataSet.m1Candles();
		if (candles.isEmpty()) {
			return;
		}
		int firstCandleIndex = 0;
		float open = candles.get(0).open();
		float high = candles.get(0).high();
		float low = candles.get(0).low();
		float close = candles.get(0).close();
		LocalDateTime last = candles.get(0).dateTime();
		for (int i = 1; i < candles.size(); i++) {
			LocalDateTime l = candles.get(i).dateTime();
			boolean add = checkAdd(last, l);
			if (add || i == candles.size() - 1) {
				this.data.add(new Candlestick(open, high, low, close, candles.get(firstCandleIndex).dateTime(), candles.get(firstCandleIndex).firstTickIndex()));
				open = candles.get(i).open();
				high = candles.get(i).high();
				low = candles.get(i).low();
				close = candles.get(i).close();
				firstCandleIndex = i;
				last = l;
			}	
			Candlestick c = candles.get(i);
			if (c.high() > high) {
				high = c.high();
			}
			if (c.low() < low) {
				low = c.low();
			}			
			close = c.close();
		}	
	}
	
	private boolean checkAdd(LocalDateTime last, LocalDateTime current) {
		if (period == 43800) {
			return Duration.between(last, current).toDays() > 31 || current.getMonth() != last.getMonth();
		} else if (period == 10080) {
			return Duration.between(last, current).toDays() > 7 || current.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) != last.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
		} else if (period == 1440) {
			return Duration.between(last, current).toHours() > 24 || current.getDayOfWeek() != last.getDayOfWeek();
		} else {
			return Duration.between(last, current).toMinutes() > period || current.getMinute() % period == 0;
		}
	}
	
	public ArrayList<Candlestick> data() {
		return data;
	}
	
	public ArrayList<DataPair> tickData() {
		return tickData;
	}
	
	public boolean base() {
		return base;
	}
	
	public String name() {
		return name;
	}
	
	public boolean tickBased() {
		return tickBased;
	}
	
	public boolean staticTF() {
		return staticTF;
	}
	
	private int size(boolean replayMode) {
		int size;
		if (tickBased) {
			size = (int)(dataSet.tickDataSize(replayMode).get() / (double)period);
			if (dataSet.tickDataSize(replayMode).get() % period != 0) {
				return size + 1;
			}	
			return size;
		} else {
			if (replayMode) {
				ArrayList<Candlestick> m1Candles = dataSet.m1Candles();
				LocalDateTime currDate = m1Candles.get(dataSet.m1CandlesDataSize(true).get() - 1).dateTime();
				int startIndex = (int)(dataSet.m1CandlesDataSize(true).get()/(double)period);
				size = startIndex + 1;
				if (data.get(size - 1).dateTime().isEqual(currDate)) {
					return size;
				}
				boolean add = data.get(startIndex).dateTime().isBefore(currDate);				
				while(true) {					
					if (size == 1 && !add || size == data.size() && add || data.get(size - 1).dateTime().isEqual(currDate) || data.get(size - 1).dateTime().isBefore(currDate) && !add || data.get(size - 1).dateTime().isAfter(currDate) && add) {
						return size;
					} else {
						size += add?1:-1;
					}
				}				
			} else {
				return data.size();
			}			
		}			
	}
	
	public int size(boolean replayMode, boolean tick) {
		if (!base) {
			return size(replayMode);
		} else {
			if (tick) {
				return dataSet.tickDataSize(replayMode).get();
			} else {
				return dataSet.m1CandlesDataSize(replayMode).get();
			}
		}
	}
}
