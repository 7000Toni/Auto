package com.github._7000toni.auto.marketreplay.trade;

import java.io.Serializable;

public class PendingTrade implements ITrade, Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean limit;
	private boolean buy;
	private double price;
	private int volume;
	
	public PendingTrade(boolean limit, boolean buy,	double price, int volume) {
		this.limit = limit;
		this.buy = buy;
		this.price = price;
		this.volume = volume;
	}
	
	public boolean limit() {
		return limit;
	}
	
	@Override
	public boolean buy() {
		return buy;
	}
	
	public double price() {
		return price;
	}
	
	public int volume() {
		return volume;
	}
	
	public void setLimit(boolean limit) {
		this.limit = limit;
	}
	
	public void setBuy(boolean buy) {
		this.buy = buy;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setVolume(int volume) {
		this.volume = volume;
	}
}
