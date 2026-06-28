package com.github._7000toni.auto.dataset;

import java.util.ArrayList;

import com.github._7000toni.auto.dataset.Dataset.Candlestick;
import com.github._7000toni.auto.dataset.Dataset.DataPair;

public class Timeframe {
	private String name;
	private Dataset dataSet;
	private ArrayList<Candlestick> data = null;
	private boolean tickBased;
	private boolean staticTF;
	private int period;
	
	public Timeframe(Dataset dataSet, String name, boolean tickBased, boolean staticTF, int period) {
		this.dataSet = dataSet;
		this.name = name;
		this.tickBased = tickBased;
		this.staticTF = staticTF;
		this.period = period;
		if (tickBased) {
			calculateTickComposite();
		} else {
			calculateCandleComposite();
		}
	}
	
	public Timeframe(Dataset dataSet, boolean tickBased, boolean staticTF, int period) {
		this.dataSet = dataSet;
		this.tickBased = tickBased;
		this.period = period;
		if (tickBased) {
			name = "T" + period;
		} else {
			name = "M" + period;
		}
		if (tickBased) {
			calculateTickComposite();
		} else {
			calculateCandleComposite();
		}
	}
	
	private void calculateTickComposite() {
		this.data = new ArrayList<Candlestick>();
		float open = 0;
		float high = 0;
		float low = 0;
		ArrayList<DataPair> tickData = dataSet.tickData();
		
		for (int i = 0; i < tickData.size(); i++) {
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
		}	
	}
	
	private void calculateCandleComposite() {
		this.data = new ArrayList<Candlestick>();
		float open = 0;
		float high = 0;
		float low = 0;
		ArrayList<Candlestick> candles = dataSet.m1Candles();
		
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
		}	
	}
	
	public ArrayList<Candlestick> data() {
		return data;
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
}
