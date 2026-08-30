package com.github._7000toni.auto.marketreplay.trade;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.marketreplay.MarketReplay;
import com.github._7000toni.auto.marketreplay.TradeState;
import com.github._7000toni.auto.marketreplay.trade.history.TradeHistory;
import com.github._7000toni.auto.miscellaneous.Round;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Trade implements ITrade {
	protected Dataset data;
	protected double entryPrice;
	protected ArrayList<EntryPair> entryIndices = new ArrayList<EntryPair>();
	protected int currentPriceIndex;
	protected double sl = -1;
	protected double tp = -1;
	protected double exitPrice = -1;
	protected LocalDateTime entryTime;
	protected LocalDateTime exitTime = null;
	protected boolean buy;
	protected boolean closed = false;
	protected boolean closedByRewind = false;
	protected int volume;
	protected double profit;
	protected boolean composite = false;
	protected boolean partial = false;
	protected double partialVol = -1;
	protected static ArrayList<TradeHistoryPair> history = new ArrayList<TradeHistoryPair>();
	protected static boolean lastTradeShort = false;
	protected static boolean lastTradeLong = false;
	protected static BooleanProperty shortReport = new SimpleBooleanProperty(true);
	protected static double net = 0;	
	
	private class TradeHistoryPair {		
		private TradeHistory history;
		private String name;
		
		public TradeHistoryPair(TradeHistory history, String name) {
			this.history = history;
			this.name = name;
		}
		
		public TradeHistory history() {
			return history;
		}
		
		public String name() {
			return name;
		}
	}
	
	public static class EntryPair implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private double volume;
		private int entryIndex;
		
		public EntryPair(double volume, int entryIndex) {
			this.volume = volume;
			this.entryIndex = entryIndex;
		}
		
		public double volume() {
			return volume;
		}
		
		public int entryIndex() {
			return entryIndex;
		}
		
		public void addVolume(double volume) {
			this.volume += volume;
		}
	}
	
	public Trade() {
		closed = true;
	}
	
	public Trade(Dataset data, int currentPriceIndex, double sl, double tp, boolean buy, int volume) {
		constructorStuff(data, currentPriceIndex, sl, tp, buy, volume);
	}
	
	public Trade(Dataset data, int currentPriceIndex, boolean buy, int volume) {
		constructorStuff(data, currentPriceIndex, -1, -1, buy, volume);
	}
	
	public void replaceTrade(Trade t) {
		this.data = t.data;
		this.entryPrice = t.entryPrice;
		this.entryIndices = t.entryIndices;
		this.currentPriceIndex = t.currentPriceIndex;
		this.buy = t.buy;
		this.sl = t.sl;
		this.tp = t.tp;		
		this.volume = t.volume;
		this.entryTime = t.entryTime;
		this.exitPrice = t.exitPrice;	
		this.exitTime = t.exitTime;
		this.closed = t.closed;
		this.closedByRewind = t.closedByRewind;
		this.profit = t.profit;
		this.composite = t.composite;
		this.partial = t.partial;
		this.partialVol = t.partialVol;	
	}
	
	private void constructorStuff(Dataset data, int currentPriceIndex, double sl, double tp, boolean buy, int volume) {
		this.data = data;
		this.entryPrice = data.tickData().get(currentPriceIndex).price();
		this.entryIndices.add(new EntryPair(volume, currentPriceIndex));		
		this.currentPriceIndex = currentPriceIndex;
		this.buy = buy;
		setSL(sl);
		setTP(tp);		
		this.volume = volume;
		this.entryTime = data.tickData().get(currentPriceIndex).dateTime();
		this.exitPrice = -1;
	}
	
	public void close(int currentPriceIndex, MarketReplay mr) {
		partialVol = volume;
		this.currentPriceIndex = currentPriceIndex;
		this.exitPrice = data.tickData().get(currentPriceIndex).price();
		profit(volume);
		this.exitTime = data.tickData().get(currentPriceIndex).dateTime();
		this.closed = true;	
		net += profit;
		if (mr != null) {
			mr.addProfit(profit);
		}
		for (EntryPair e : entryIndices) {
			history.add(new TradeHistoryPair(new TradeHistory(buy, e.entryIndex(), currentPriceIndex), data.name()));
		}
		entryIndices.removeAll(entryIndices);
	}
	
	public ArrayList<EntryPair> entryIndices() {
		return entryIndices;
	}
	
	public void cancelSL() {
		if (closed) {
			return;
		}
		sl = -1;		
	}
	
	public void cancelTP() {
		if (closed) {
			return;
		}
		tp = -1;		
	}

	public void loadState(TradeState tradeState, Dataset data) {
		this.data = data;
		entryPrice = tradeState.entryPrice();
		entryIndices = tradeState.entryIndices();
		currentPriceIndex = tradeState.currentPriceIndex();
		sl = tradeState.sl();
		tp = tradeState.tp();
		exitPrice = tradeState.exitPrice();
		entryTime = tradeState.entryTime();
		exitTime = tradeState.exitTime();
		buy = tradeState.buy();
		closed = tradeState.closed();
		closedByRewind = tradeState.closedByRewind();
		volume = tradeState.volume();
		composite = tradeState.composite();
		partial = tradeState.partial();
		partialVol = tradeState.partialVol();
		for (TradeHistory th : tradeState.history()) {
			TradeHistoryPair thp = new TradeHistoryPair(th, tradeState.mrName());
			history.add(thp);
		}
	}
	
	public static void addNetProfit(double netProfit) {
		Trade.net += netProfit;
	}
	
	public static void removeHistory(String name) {
		ArrayList<TradeHistoryPair> h = new ArrayList<TradeHistoryPair>();
		for (TradeHistoryPair thp : history) {
			if (!thp.name().equals(name)) {
				h.add(thp);
			}
		}
		history = h;
	}
	
	public static ArrayList<TradeHistory> history() {
		ArrayList<TradeHistory> history = new ArrayList<TradeHistory>();
		for (TradeHistoryPair thp : Trade.history) {
			history.add(new TradeHistory(thp.history().buy(), thp.history().entryIndex(), thp.history().exitIndex()));
		}
		return history;
	}

	public static ArrayList<TradeHistory> history(String name) {
		ArrayList<TradeHistory> history = new ArrayList<TradeHistory>();
		for (TradeHistoryPair thp : Trade.history) {
			if (thp.name().equals(name)) {
				history.add(new TradeHistory(thp.history().buy(), thp.history().entryIndex(), thp.history().exitIndex()));
			}
		}
		return history;
	}
	
	public double profit() {
		return profit(volume);
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
	
	public double profit(double volume) {
		if (closed) {
			return profit;
		}
		double diff = data.tickData().get(currentPriceIndex).price() - entryPrice;
		if (!buy) {
			diff = -diff;
		}
		profit = Round.round(diff * volume, 2);
		return profit;
	}
	
	public double hypotheticalProfit(double exitPrice) {
		double diff = exitPrice - entryPrice;
		if (!buy) {
			diff = -diff;
		}
		return Round.round(diff * volume, 2);
	}
	
	public static double hypotheticalProfit2(double entryPrice, double exitPrice, boolean buy, double volume) {
		double diff = exitPrice - entryPrice;
		if (!buy) {
			diff = -diff;
		}
		return Round.round(diff * volume, 2);
	}
	
	public void scaleIn(double vol, int currentPriceIndex) {	
		if (closed) {
			return;
		}
		if (buy) {
			entryPrice = data.tickData().get(currentPriceIndex).price() - (profit(volume) / (volume + vol));
		} else {
			entryPrice = data.tickData().get(currentPriceIndex).price() + (profit(volume) / (volume + vol));
		}
		entryIndices.add(new EntryPair(vol, currentPriceIndex));
		volume += vol;
		partialVol = volume;
		composite = true;
	}
	
	public void scaleOut(double vol, int currentPriceIndex, MarketReplay mr) {
		if (closed) {
			return;
		}
		if (volume - vol <= 0) {
			close(currentPriceIndex, mr);
		} else {
			volume -= vol;
			partial = true;
			partialVol = vol;
			exitPrice = data.tickData().get(currentPriceIndex).price();
			exitTime = data.tickData().get(currentPriceIndex).dateTime();
			double p = profit(vol);
			net += p;
			if (mr != null) {
				mr.addProfit(p);
			}
			while (vol > 0) {
				if (entryIndices.getLast().volume() > vol) {
					entryIndices.getLast().addVolume(-vol);
					history.add(new TradeHistoryPair(new TradeHistory(buy, entryIndices.getLast().entryIndex(), currentPriceIndex), data.name()));
					vol = 0;
				} else {
					vol -= entryIndices.getLast().volume();
					history.add(new TradeHistoryPair(new TradeHistory(buy, entryIndices.removeLast().entryIndex(), currentPriceIndex), data.name()));
				}
			}
		}		
	}
	
	public void updateTrade(int currentPriceIndex, MarketReplay mr) {
		if (closed) {
			return;
		}
		if (this.currentPriceIndex > currentPriceIndex) {			
			this.closed = true;
			this.closedByRewind = true;
		}
		for (int i = this.currentPriceIndex; i < currentPriceIndex + 1; i++) {			
			double price = data.tickData().get(i).price();
			if (buy) {
				if (price >= tp && tp != -1 || price <= sl && sl != -1) {	
					close(i, mr);
					break;
				}
			} else {
				if (price <= tp && tp != -1 || price >= sl && sl != -1) {
					close(i, mr);
					break;
				}
			}
		}
		this.currentPriceIndex = currentPriceIndex;
	}
	
	public void setSL(double sl) {
		if (closed) {
			return;
		}		
		if (buy) {
			if (sl >= data.tickData().get(currentPriceIndex).price()) {
				return;
			}
		} else {
			if (sl <= data.tickData().get(currentPriceIndex).price()) {
				return;
			}
		}
		this.sl = sl;		
	}
	
	public void setTP(double tp) {
		if (closed) {
			return;
		}
		if (buy) {
			if (tp <= data.tickData().get(currentPriceIndex).price()) {
				return;
			}
		} else {
			if (tp >= data.tickData().get(currentPriceIndex).price()) {
				return;
			}
		}
		this.tp = tp;
	}
	
	public double entryPrice() {
		return entryPrice;
	}
	
	public double exitPrice() {
		return exitPrice;
	}
	
	public double sl() {
		return sl;
	}
	
	public double tp() {
		return tp;
	}
	
	public LocalDateTime entryTime() {
		return entryTime;
	}
	
	public LocalDateTime exitTime() {
		return exitTime;
	}
	
	public static ReadOnlyBooleanProperty shortReport() {
		return shortReport;
	}
	
	public static void toggleShortReport() {
		shortReport.set(!shortReport.get());
	}
		
	@Override
	public boolean buy() {
		return buy;
	}
	
	public int volume() {
		return volume;
	}
	
	public boolean closed() {
		return closed;
	}
	
	public boolean closedByRewind() {
		return closedByRewind;
	}
	
	public static double net() {
		return net;
	}
	
	public void writeHistoryToFile() {
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File("./" + data.name() + ".hst"), true), true)) {
			for (TradeHistoryPair t : history) {
				pw.append(t.history().buy() + "," + t.history().entryIndex() + "," + t.history().exitIndex() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeHistoryToFile(String name) {
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File("./" + data.name() + ".hst"), true), true)) {
			for (TradeHistoryPair t : history) {
				if (t.name().equals(name)) {
					pw.append(t.history().buy() + "," + t.history().entryIndex() + "," + t.history().exitIndex() + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToFile(File file) {
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(file, true), true)) {
			pw.append(toString() + "\n");
			if (lastTradeLong) {
				lastTradeShort = false;
				lastTradeLong = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected String alternateToString() {		
		String ret = "Profit: " + profit(partialVol);
		ret += "\tRewind: " + closedByRewind;	
		ret += "\tNet: " + Round.round(net, 2);
		lastTradeShort = true;
		return ret;
	}
	
	protected String originalToString() {		
		String buyOrSell = "";
		if (lastTradeShort) {
			buyOrSell += '\n';
		}
		
		if (!buy) {
			buyOrSell += "Sold";
		} else {
			buyOrSell += "Bought";
		}
		
		String sls; 
		String tps;
		if (sl == -1) {
			sls = "none";
		} else {
			sls = ((Double)sl).toString();
		}
		
		if (tp == -1) {
			tps = "none";
		} else {
			tps = ((Double)tp).toString();
		}
		
		String ret = buyOrSell + " " + volume + " on " + data.name();
		if (composite) {
			ret += " (Composite)";
		}
		if (partial) {
			ret += " (" + partialVol + " Partial)";
		}
		ret += "\nEntry:\t" + entryPrice;
		if (exitPrice != -1) {
			ret += "\nExit:\t" + exitPrice;
		}
		ret += "\nSL:\t" + sls;
		ret += "\nTP:\t" + tps;
		ret += "\nEntry:\t" + entryTime.toString().replace('T', ' ');
		if (exitTime != null) {
			ret += "\nExit:\t" + exitTime.toString().replace('T', ' ');
		}
		ret += "\nChange:\t" + ((Double)(exitPrice - entryPrice)).toString();
		ret += "\nProfit:\t" + profit(partialVol);
		ret += "\nRewind:\t" + closedByRewind;
		ret += "\nNet: " + Round.round(net, 2) + '\n';
		lastTradeLong = true;
		return ret;
	}
	
	@Override
	public String toString() {
		if (shortReport.get()) {
			return alternateToString();
		} else {
			return originalToString();
		}
	}
}
