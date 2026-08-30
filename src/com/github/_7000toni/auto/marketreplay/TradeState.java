package com.github._7000toni.auto.marketreplay;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.github._7000toni.auto.marketreplay.trade.Trade;
import com.github._7000toni.auto.marketreplay.trade.Trade.EntryPair;
import com.github._7000toni.auto.marketreplay.trade.history.TradeHistory;

public class TradeState implements Serializable {
	private static final long serialVersionUID = 1L;

	private String mrName;
	private double entryPrice;
	private ArrayList<EntryPair> entryIndices;
	private int currentPriceIndex;
	private double sl;
	private double tp;
	private double exitPrice;
	private LocalDateTime entryTime;
	private LocalDateTime exitTime;
	private boolean buy;
	private boolean closed;
	private boolean closedByRewind;
	private int volume;
	private boolean composite;
	private boolean partial;
	private double partialVol;
	private ArrayList<TradeHistory> history = new ArrayList<TradeHistory>();
	
	public TradeState(Trade trade, String mrName, int index) {
		this.mrName = mrName;
		entryPrice = trade.entryPrice();
		entryIndices = trade.entryIndices();
		currentPriceIndex = index;
		sl = trade.sl();
		tp = trade.tp();
		exitPrice = trade.exitPrice();
		entryTime = trade.entryTime();
		exitTime = trade.exitTime();
		buy = trade.buy();
		closed = trade.closed();
		closedByRewind = trade.closedByRewind();
		volume = trade.volume();
		composite = trade.composite();
		partial = trade.partial();
		partialVol = trade.partialVol();
		history = Trade.history(mrName);
	}
	
	public String mrName() {
		return mrName;
	}
	
	public double entryPrice() {
		return entryPrice;
	}
	
	public ArrayList<EntryPair> entryIndices() {
		return entryIndices;
	}
	
	public int currentPriceIndex() {
		return currentPriceIndex;
	}
	
	public double sl() {
		return sl;
	}
	
	public double tp() {
		return tp;
	}
	
	public double exitPrice() {
		return exitPrice;
	}
	
	public LocalDateTime entryTime() {
		return entryTime;
	}
	
	public LocalDateTime exitTime() {
		return exitTime;
	}
	
	public boolean buy() {
		return buy;
	}
	
	public boolean closed() {
		return closed;
	}
	
	public boolean closedByRewind() {
		return closedByRewind;
	}
	
	public int volume() {
		return volume;
	}
	
	public boolean composite() {
		return composite;
	}
	
	public boolean partial() {
		return partial;
	}
	
	public double partialVol() {
		return partialVol;
	}
	
	public ArrayList<TradeHistory> history() {
		return history;
	}
}
