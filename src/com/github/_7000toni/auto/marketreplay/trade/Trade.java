package com.github._7000toni.auto.marketreplay.trade;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private Dataset data;
	private double entryPrice;
	private ArrayList<EntryPair> entryIndices = new ArrayList<EntryPair>();
	private int currentPriceIndex;
	private double sl = -1;
	private double tp = -1;
	private double exitPrice = -1;
	private LocalDateTime entryTime;
	private LocalDateTime exitTime = null;
	private boolean buy;
	private boolean closed = false;
	private boolean closedByRewind = false;
	private int volume;
	private double profit;
	private double lastProfit = 0;
	private boolean composite = false;
	private boolean partial = false;
	private double partialVol = -1;
	private static ArrayList<TradeHistoryPair> history = new ArrayList<TradeHistoryPair>();
	private static boolean lastTradeShort = false;
	private static boolean lastTradeLong = false;
	private static BooleanProperty shortReport = new SimpleBooleanProperty(true);
	private static double net = 0;	
	private boolean blank = false;
	
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
		blank = true;
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
		this.lastProfit = t.profit;
		this.composite = t.composite;
		this.partial = t.partial;
		this.partialVol = t.partialVol;	
		this.blank = t.blank;
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
	
	public void close(int currentPriceIndex) {
		close(currentPriceIndex, null);
	}
	
	public void close(int currentPriceIndex, MarketReplay mr) {
		partialVol = volume;
		this.currentPriceIndex = currentPriceIndex;
		this.exitPrice = data.tickData().get(currentPriceIndex).price();
		profit(volume);
		this.exitTime = data.tickData().get(currentPriceIndex).dateTime();
		this.closed = true;	
		net += profit;
		lastProfit = profit;
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
		blank = tradeState.blank();
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
	
	public double lastProfit() {
		return lastProfit;
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
	
	public void scaleOut(double vol, int currentPriceIndex) {
		scaleOut(vol, currentPriceIndex, null);
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
			lastProfit = p;
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
	
	public boolean blank() {
		return blank;
	}
	
	public static double net() {
		return net;
	}
	
	private void checkDir(String dir) {
		try {
			Path p = Paths.get(dir);
			Files.createDirectories(p);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeHistoryToFile() {
		if (blank) {
			return;
		}
		checkDir("./history");
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File("./history/" + data.name() + ".hst"), true), true)) {
			for (TradeHistoryPair t : history) {
				pw.append(t.history().buy() + "," + t.history().entryIndex() + "," + t.history().exitIndex() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeHistoryToFile(String name) {
		if (blank) {
			return;
		}
		checkDir("./history");
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File("./history/" + data.name() + ".hst"), true), true)) {
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
		if (blank) {
			return;
		}
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
	
	private String alternateToString() {	
		if (blank) {
			return null;
		}
		String ret = "Profit: " + profit(partialVol);
		ret += "\tRewind: " + closedByRewind;	
		ret += "\tNet: " + Round.round(net, 2);
		lastTradeShort = true;
		return ret;
	}
	
	private String originalToString() {		
		if (blank) {
			return null;
		}
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
