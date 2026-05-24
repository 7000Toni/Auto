package com.github._7000toni.auto.marketreplay.trade;

public class PendingTrade implements ITrade {
	private boolean limit;
	private boolean buy;
	private double price;
	private double volume;
	
	public PendingTrade(boolean limit, boolean buy,	double price, double volume) {
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
	
	public double volume() {
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
	
	public void setVolume(double volume) {
		this.volume = volume;
	}
}
