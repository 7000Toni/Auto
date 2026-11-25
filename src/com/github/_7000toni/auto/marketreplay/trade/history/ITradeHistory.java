package com.github._7000toni.auto.marketreplay.trade.history;

public interface ITradeHistory {
	public boolean buy();
	public int entryIndex();
	public int exitIndex();
	public double volume();
	public double sl();
	public double tp();
}
