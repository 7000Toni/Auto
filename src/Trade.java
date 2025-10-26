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
	protected double exitPrice;
	protected LocalDateTime entryTime;
	protected LocalDateTime exitTime;
	protected boolean buy;
	protected boolean closed = false;
	protected double volume;
	protected double profit;
	
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
			return this.profit;
		}
		double diff = data.tickData().get(currentPriceIndex).price() - entryPrice;
		if (!buy) {
			diff = -diff;
		}
		profit = diff * volume;
		return this.profit;
	}
	
	public void updateTrade(int currentPriceIndex) {
		if (closed) {
			return;
		}
		for (int i = this.currentPriceIndex; i < currentPriceIndex + 1; i++) {
			double price = data.tickData().get(currentPriceIndex).price();
			if (buy) {
				if (price > tp || price < sl) {
					close(i);
					break;
				}
			} else {
				if (price < tp || price > sl) {
					close(i);
					break;
				}
			}
		}
	}
	
	public void setSL(double sl) {
		if (closed) {
			return;
		}
		this.sl = sl;		
	}
	
	public void setTP(double tp) {
		if (closed) {
			return;
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
}
