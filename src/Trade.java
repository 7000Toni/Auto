import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Trade implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected DataSet data;
	protected double entryPrice;
	protected int currentPriceIndex;
	protected double sl;
	protected double tp;
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
	
	public Trade(DataSet data, int currentPriceIndex, double sl, double tp, boolean buy, double volume) {
		constructorStuff(data, currentPriceIndex, sl, tp, buy, volume);
	}
	
	public Trade(DataSet data, int currentPriceIndex, boolean buy, double volume) {
		constructorStuff(data, currentPriceIndex, -1, -1, buy, volume);
	}
	
	private void constructorStuff(DataSet data, int currentPriceIndex, double sl, double tp, boolean buy, double volume) {
		this.data = data;
		this.entryPrice = data.tickData().get(currentPriceIndex).price();
		this.sl = sl;
		this.tp = tp;
		this.buy = buy;
		this.volume = volume;
		this.entryTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Z"));
		this.exitTime = null;
		this.exitPrice = -1;
		this.currentPriceIndex = currentPriceIndex;
	}
	
	public void close(int currentPriceIndex) {
		if (partial) {
			partialVol = volume;
		}
		this.currentPriceIndex = currentPriceIndex;
		this.exitPrice = data.tickData().get(currentPriceIndex).price();
		profit();
		this.exitTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Z"));
		this.closed = true;
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
	
	public double profit() {
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
	
	public void scaleIn(double vol, int currentPriceIndex) {	
		if (closed) {
			return;
		}
		if (buy) {
			entryPrice = data.tickData().get(currentPriceIndex).price() - (profit() / (volume + vol));
		} else {
			entryPrice = data.tickData().get(currentPriceIndex).price() + (profit() / (volume + vol));
		}
		volume += vol;
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
			exitTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Z"));
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
				System.out.println("ran");
				return;
			}
		} else {
			if (sl <= data.tickData().get(currentPriceIndex).price()) {
				System.out.println("ran2");
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
	
	public void writeToFile(File file) {
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(file, true), true)) {
			pw.append(toString() + "\n\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		String buyOrSell = "Bought";
		if (!buy) {
			buyOrSell = "Sold";
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
		ret += "\nEntry:\t" + entryTime.toString().replace('T', ' ') + " UTC+0";
		if (exitTime != null) {
			ret += "\nExit:\t" + exitTime.toString().replace('T', ' ') + " UTC+0";
		}
		ret += "\nChange:\t" + ((Double)(exitPrice - entryPrice)).toString();
		ret += "\nProfit:\t" + profit();
		return ret;
	}
}
