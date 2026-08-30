package com.github._7000toni.auto.marketreplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.github._7000toni.auto.marketreplay.trade.PendingTrade;

public class MarketReplayState implements Serializable {
	private static final long serialVersionUID = 1L;

	private String signature;
	private String name;
	private int index;	
	private double slPrice;
	private double tpPrice;
	private double unvalidatedSlPrice;
	private double unvalidatedTpPrice;	
	private ArrayList<PendingTrade> pendingTrades;
	private double net;
	private TradeState trade; 
	private boolean valid = true;
	
	public MarketReplayState(File f) {
		load(f);
	}
	
	public MarketReplayState(MarketReplay mr) {
		signature = mr.data().signature();
		name = mr.data().name();
		index = mr.index().get();
		slPrice = mr.slPrice().get();
		tpPrice = mr.tpPrice().get();
		unvalidatedSlPrice = mr.unvalidatedSlPrice().get();
		unvalidatedTpPrice = mr.unvalidatedTpPrice().get();
		pendingTrades = mr.pendingTrades();
		net = mr.netProfit();
		trade = new TradeState(mr.trade(), name, index);
	}
	
	public void saveToFile() {
		try (FileOutputStream fos = new FileOutputStream(new File("./" + name + ".state"));
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load(File file) {
		if (!file.exists()) {
			return;
		}
		try (FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream ois = new ObjectInputStream(bis)) {
			MarketReplayState mrp = (MarketReplayState)ois.readObject();
			signature = mrp.signature;
			name = mrp.name;
			index = mrp.index;
			slPrice = mrp.slPrice;
			tpPrice = mrp.tpPrice;
			unvalidatedSlPrice = mrp.unvalidatedSlPrice;
			unvalidatedTpPrice = mrp.unvalidatedTpPrice;
			pendingTrades = mrp.pendingTrades;
			net = mrp.net;
			trade = mrp.trade;
		} catch (IOException | ClassNotFoundException e) {
			valid = false;
			e.printStackTrace();
		}
	}	
	
	public String signature() {
		return signature;
	}
	
	public String name() {
		return name;
	}
	
	public int index() {
		return index;
	}
	
	public double slPrice() {
		return slPrice;
	}
	
	public double tpPrice() {
		return tpPrice;
	}
	
	public double unvalidatedSlPrice() {
		return unvalidatedSlPrice;
	}
	
	public double unvalidatedTpPrice() {
		return unvalidatedTpPrice;
	}
	
	public ArrayList<PendingTrade> pendingTrades() {
		return pendingTrades;
	}
	
	public double netProfit() {
		return net;
	}
	
	public TradeState tradeState() {
		return trade;
	}
	
	public boolean valid() {
		return valid;
	}
}
