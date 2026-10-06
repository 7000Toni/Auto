package com.github._7000toni.auto.marketreplay;

public class MRStateProfitPair {
	private String signature;
	private double profit;
	
	public MRStateProfitPair(String signature, double profit) {
		this.signature = signature;
		this.profit = profit;
	}
	
	public String signature() {
		return signature;
	}
	
	public double profit() {
		return profit;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public void setProfit(double profit) {
		this.profit = profit;
	}
	
	public void addProfit(double profit) {
		this.profit += profit;
	}
}
