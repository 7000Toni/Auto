package com.github._7000toni.auto.dataset.timeframe;

import java.util.ArrayList;

import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.dataset.Dataset.Candlestick;
import com.github._7000toni.auto.dataset.Dataset.DataPair;

import javafx.beans.property.IntegerProperty;

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
	
	public Timeframe(Dataset dataSet, boolean tickBased, boolean staticTF, int period, IntegerProperty prog) {
		constructorStuff(dataSet, tickBased, staticTF, period, prog);
	}
	
	public Timeframe(Dataset dataSet, boolean tickBased, boolean staticTF, int period) {
		constructorStuff(dataSet, tickBased, staticTF, period, null);
	}
	
	private void constructorStuff(Dataset dataSet, boolean tickBased, boolean staticTF, int period, IntegerProperty prog) {
		this.dataSet = dataSet;
		this.tickBased = tickBased;
		this.period = period;
		name = determineName(tickBased, period);
		if (tickBased) {
			calculateTickComposite(prog);
		} else {
			calculateCandleComposite(prog);
		}
	}
	
	public static String determineName(boolean tickBased, int period) {
		String name = "";
		if (tickBased) {
			name = "T" + period;
		} else {
			int w = period/10080;
			int d = period/1440;
			int h = period/60;
			int m = period%60;
			int od = d;
			d = d - 7*w;			
			h = h - 24*od;
			if (w > 0) {
				name += d==0&&h==0&&m==0?"WEEKLY":"W"+w+"+";
			}
			if (d > 0) {
				name += w==0&&h==0&&m==0?"DAILY":"D"+d+"+";
			}
			if (h > 0) {
				name += w==0&&d==0&&m==0?"HOURLY":"H"+h+"+";
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
	
	private void calculateTickComposite(IntegerProperty prog) {
		this.data = new ArrayList<Candlestick>();
		float open = 0;
		float high = 0;
		float low = 0;
		ArrayList<DataPair> tickData = dataSet.tickData();
		int size = tickData.size();
		int last = 0;
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
				this.data.add(new Candlestick(open, high, low, val, tickData.get(firstTickIndex).dateTime(), complete, firstTickIndex));
			}		
			if (prog != null) {
				prog.set((int)(i*100/(size-1.0)));
				if (last < prog.get()) {
					last = prog.get();
					Chart.drawCharts(null);
				}				
			}
		}	
	}
	
	private void calculateCandleComposite(IntegerProperty prog) {
		this.data = new ArrayList<Candlestick>();
		float open = 0;
		float high = 0;
		float low = 0;
		ArrayList<Candlestick> candles = dataSet.m1Candles();
		int size = candles.size();
		int last = 0;
		for (int i = 0; i < candles.size(); i++) {
			if (i % period == 0) {
				open = candles.get(i).open();
				high = candles.get(i).high();
				low = candles.get(i).low();
			}
			Candlestick c = candles.get(i);
			if (c.high() > high) {
				high = c.high();
			}
			if (c.low() < low) {
				low = c.low();
			}
			boolean complete = (i+1)%period == 0;
			if (complete || i == candles.size() - 1) {
				int firstCandleIndex = i-(i%period);
				this.data.add(new Candlestick(open, high, low, c.close(), candles.get(firstCandleIndex).dateTime(), complete, firstCandleIndex));
			}	
			if (prog != null) {
				prog.set((int)(i*100/(size-1.0)));
				if (last < prog.get()) {
					last = prog.get();
					Chart.drawCharts(null);
				}				
			}
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
		} else {
			size = (int)(dataSet.m1CandlesDataSize(replayMode).get() / (double)period);
			if (dataSet.m1CandlesDataSize(replayMode).get() % period != 0) {
				return size + 1;
			}			
		}	
		return size;
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
