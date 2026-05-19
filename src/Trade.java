import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Trade implements ITrade {
	protected DataSet data;
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
	protected double volume;
	protected double profit;
	protected boolean composite = false;
	protected boolean partial = false;
	protected double partialVol = -1;
	protected static ArrayList<TradeHistoryPair> history = new ArrayList<TradeHistoryPair>();
	protected static boolean lastTradeShort = false;
	protected static boolean lastTradeLong = false;
	protected static BooleanProperty shortReport = new SimpleBooleanProperty(true);
	protected static double net = 0;	
	protected static boolean init = false;
	
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
	
	private class EntryPair {
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
	
	public Trade(DataSet data, int currentPriceIndex, double sl, double tp, boolean buy, double volume) {
		constructorStuff(data, currentPriceIndex, sl, tp, buy, volume);
	}
	
	public Trade(DataSet data, int currentPriceIndex, boolean buy, double volume) {
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
	
	private void constructorStuff(DataSet data, int currentPriceIndex, double sl, double tp, boolean buy, double volume) {
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
		partialVol = volume;
		this.currentPriceIndex = currentPriceIndex;
		this.exitPrice = data.tickData().get(currentPriceIndex).price();
		profit(volume);
		this.exitTime = data.tickData().get(currentPriceIndex).dateTime();
		this.closed = true;
		net += profit;
		if (init) {
			for (EntryPair e : entryIndices) {
				history.add(new TradeHistoryPair(new TradeHistory(buy, e.entryIndex(), currentPriceIndex), data.name()));
			}
		} else {
			init = true;
		}
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
		if (closed) {
			return;
		}
		if (volume - vol <= 0) {
			close(currentPriceIndex);
		} else {
			volume -= vol;
			partial = true;
			partialVol = vol;
			exitPrice = data.tickData().get(currentPriceIndex).price();
			exitTime = data.tickData().get(currentPriceIndex).dateTime();
			net += profit(vol);
			if (init) {				
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
			} else {
				init = true;
			}
		}		
	}
	
	public void updateTrade(int currentPriceIndex) {
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
					close(i);
					break;
				}
			} else {
				if (price <= tp && tp != -1 || price >= sl && sl != -1) {
					close(i);
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
	
	public double volume() {
		return volume;
	}
	
	public boolean closed() {
		return closed;
	}
	
	public boolean closedByRewind() {
		return closedByRewind;
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
		buyOrSell += "Bought";
		if (!buy) {
			buyOrSell += "Sold";
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
