import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Trade implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double entryPrice;
	private double sl;
	private double tp;
	private double exitPrice;
	private LocalDateTime entryTime;
	private LocalDateTime exitTime;
	private boolean buy;
	private double volume;
	private double profit;
	
	public Trade(double entryPrice, double sl, double tp, boolean buy, double volume) {
		constructorStuff(entryPrice, sl, tp, buy, volume);
	}
	
	public Trade(double entryPrice, boolean buy, double volume) {
		constructorStuff(entryPrice, -1, -1, buy, volume);
	}
	
	private void constructorStuff(double entryPrice, double sl, double tp, boolean buy, double volume) {
		this.entryPrice = entryPrice;
		this.sl = sl;
		this.tp = tp;
		this.buy = buy;
		this.volume = volume;
		this.entryTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Z"));
		this.exitTime = null;
		this.exitPrice = -1;
	}
	
	public void close(double exitPrice) {
		this.exitPrice = exitPrice;
		this.exitTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Z"));
	}
	
	public void cancelSL() {
		sl = -1;
	}
	
	public void cancelTP() {
		tp = -1;
	}
	
	public void setSL(double sl) {
		this.sl = sl;
	}
	
	public void setTP(double tp) {
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
