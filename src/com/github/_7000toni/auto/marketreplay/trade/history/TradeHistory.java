package com.github._7000toni.auto.marketreplay.trade.history;

import java.io.Serializable;

public class TradeHistory implements ITradeHistory, Serializable {	
	private static final long serialVersionUID = 2L;
	
	private boolean buy;
	private int entryIndex;
	private int exitIndex;
	private double volume;
	private double sl;
	private double tp;
	
	public TradeHistory(boolean buy, int entryIndex, int exitIndex, double volume, double sl, double tp) {
		this.buy = buy;
		this.entryIndex = entryIndex;
		this.exitIndex = exitIndex;
		this.volume = volume;
		this.sl = sl;
		this.tp = tp;
	}
	
	public boolean buy() {
		return buy;
	}
	
	public int entryIndex() { 
		return entryIndex;
	}
	
	public int exitIndex() { 
		return exitIndex;
	}
	
	public double volume() {
		return volume;
	}
	
	public double sl() {
		return sl;
	}
	
	public double tp() {
		return tp;
	}
}
