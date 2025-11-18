import java.time.ZoneOffset;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MarketReplay {
	private ArrayList<Chart> charts;
	private DataSet data;
	private MarketReplayPane mrp;
	private IntegerProperty index = new SimpleIntegerProperty();	
	private BooleanProperty paused = new SimpleBooleanProperty(false);
	private BooleanProperty live = new SimpleBooleanProperty(true);
	private IntegerProperty speed = new SimpleIntegerProperty(1);
	private IntegerProperty tickDataSize = new SimpleIntegerProperty();
	private BooleanProperty run = new SimpleBooleanProperty(false);
	private IntegerProperty timeToNextTick = new SimpleIntegerProperty(); 
	
	private DoubleProperty slPrice = new SimpleDoubleProperty(-1);
	private DoubleProperty tpPrice = new SimpleDoubleProperty(-1);
	private Trade trade = null; 
	private IntegerProperty lastTick = new SimpleIntegerProperty(0);
	private BooleanProperty handled = new SimpleBooleanProperty(false);
	private ArrayList<Chart.PendingTrade> pendingTrades = new ArrayList<Chart.PendingTrade>();
	
	public MarketReplay(Chart chart, MarketReplayPane mrp, int index) {		
		this.charts = new ArrayList<Chart>();
		this.data = chart.data();
		this.mrp = mrp;
		this.tickDataSize.set(data.tickDataSize(false).get());
		data.setReplayTickDataSize(index);
		int ci = index;
		if (ci == tickDataSize.get()) {
			ci--;
		}
		data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex());		
		this.index.set(index);
		chart.enableReplayMode(this, mrp);
	}
	
	public void addChart(Chart chart) {
		this.charts.add(chart);
	}
	
	public void removeChart(Chart chart) {
		this.charts.remove(chart);
	}
	
	public ArrayList<Chart> charts() {
		return this.charts;
	}
	
	public void togglePause() {		
		paused.set(!paused.get());
	}
	
	public void toggleLive() {
		if (live.get()) {
			live.set(false);
		} else {
			live.set(true);
			for (Chart c : charts) {
				if (!c.mr().equals(this)) {
					continue;
				}
				c.setKeepStartIndex(false);
			}
		}
	}
	
	public ReadOnlyIntegerProperty index() {
		return IntegerProperty.readOnlyIntegerProperty(index);
	}
	
	public ReadOnlyIntegerProperty maxSize() {
		return IntegerProperty.readOnlyIntegerProperty(tickDataSize);
	}
	
	public ReadOnlyBooleanProperty handled() {
		return BooleanProperty.readOnlyBooleanProperty(handled);
	}
	
	public void setHandled(boolean handled) {
		this.handled.set(handled);
	}
	
	public DataSet data() {
		return this.data;
	}
	
	public ReadOnlyDoubleProperty slPrice() {
		return DoubleProperty.readOnlyDoubleProperty(slPrice);
	}
	
	public ReadOnlyDoubleProperty tpPrice() {
		return DoubleProperty.readOnlyDoubleProperty(tpPrice);
	}
	
	public void setSlPrice(double slPrice) {
		this.slPrice.set(slPrice);
	}
	
	public void setTpPrice(double tpPrice) {
		this.tpPrice.set(tpPrice);
	}
	
	public Trade trade() {
		return trade;
	}
	
	public ReadOnlyIntegerProperty lastTick() {
		return IntegerProperty.readOnlyIntegerProperty(lastTick);
	}
	
	public ArrayList<Chart.PendingTrade> pendingTrades() {
		return pendingTrades;
	}
	
	public void setPendingTrades(ArrayList<Chart.PendingTrade> pendingTrades) {
		this.pendingTrades = pendingTrades;
	}
	
	public void setTrade(Trade trade) {
		this.trade = trade;
	}
	
	public void setSpeed(int speed) {
		this.speed.set(speed);
	}
	
	public ReadOnlyBooleanProperty live() {
		return BooleanProperty.readOnlyBooleanProperty(live);
	}
	
	public void setIndex(int index, boolean increment) {
		if (increment) {
			if (this.index.get() + index > tickDataSize.get() - 1) {
				this.index.set(tickDataSize.get() - 1);
			} else if (this.index.get() + index < 0) {	
				this.index.set(0);
			} else {
				this.index.set(this.index.get() + index);
			}
		} else {
			if (index > tickDataSize.get() - 1) {
				this.index.set(tickDataSize.get() - 1);
			} else if (index < 0) {	
				this.index.set(0);
			} else {
				this.index.set(index);
			}
		}
		timeToNextTick.set(0);
		data.setReplayTickDataSize(this.index.get());
		int ci = this.index.get();
		if (ci == tickDataSize.get()) {
			ci--;							
		}		
		data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);
		for (Chart c : charts) {
			if (!c.mr().equals(this)) {
				continue;
			}
			c.tick(this);
			c.draw();
		}
	}
	
	private int timeToNextTick(int index) {
		if (index > tickDataSize.get() - 2) {
			index = tickDataSize.get() - 2;
		}
		return (int)(data.tickData().get(index + 1).dateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli() - data.tickData().get(index).dateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli())/speed.get();
	}
	
	public boolean paused() {
		return this.paused.get();
	}
	
	public void stop() {
		this.run.set(false);
	}
	
	public void run() {
		run.set(true);
		MarketReplay mr = this;
		new AnimationTimer() {
			long lastTick2 = 0;
			@Override
			public void handle(long now) {
				if (!run.get()) {
					this.stop();
				}
				if (lastTick2 == 0) {
					lastTick2 = now;
					timeToNextTick.set(timeToNextTick(index.get()));
					return;
				}
				long diff = (now - lastTick2) / HorizontalScrollBar.NANO_TO_MILLI;
				if (diff >= timeToNextTick.get()) {		
					if (mrp.hsb().dragged()) {
						index.set((int)(((mrp.hsb().x() - mrp.hsb().minPos()) / (mrp.hsb().maxPos() - mrp.hsb().sbWidth() - mrp.hsb().minPos())) * tickDataSize.get()));							
						data.setReplayTickDataSize(index.get());
						int ci = index.get();
						if (ci == tickDataSize.get()) {
							ci--;							
						}		
						data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);
					} 
					while (!paused.get() && index.get() < tickDataSize.get()) {
						index.set(index.get() + 1);
						diff -= timeToNextTick.get();
						timeToNextTick.set(timeToNextTick(index.get()));
						data.setReplayTickDataSize(index.get());
						int ci = index.get();
						if (ci == tickDataSize.get()) {
							ci--;							
						}
						data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);
						if (live.get()) {
							double newHSBPos = ((double)index.get() / tickDataSize.get()) * (mrp.hsb().maxPos() - mrp.hsb().sbWidth() - mrp.hsb().minPos());
							mrp.hsb().setPosition(newHSBPos, false);		
							for (Chart c : charts) {		
								if (!c.mr().equals(mr)) {
									continue;
								}
								c.setKeepStartIndex(false);
								c.hsb().setPosition(Integer.MAX_VALUE, false);
							}
						} else {
							double newHSBPos;
							for (Chart c : charts) {
								if (!c.mr().equals(mr)) {
									continue;
								}
								if (c.drawCandlesticks()) {
									newHSBPos = (c.width() - c.hsb().sbWidth() - Chart.PRICE_MARGIN) * ((double)c.startIndex() /(data.m1CandlesDataSize(c.replayMode()).get() - c.numCandlesticks() * Chart.END_MARGIN_COEF));
								} else {
									newHSBPos = (c.width() - c.hsb().sbWidth() - Chart.PRICE_MARGIN) * ((double)c.startIndex() /(data.tickDataSize(c.replayMode()).get() - c.numDataPoints() * Chart.END_MARGIN_COEF));
								}								
								c.setKeepStartIndex(true);
								c.hsb().setPosition(newHSBPos, false);
							}
							newHSBPos = ((double)index.get() / tickDataSize.get()) * (mrp.hsb().maxPos() - mrp.hsb().sbWidth() - mrp.hsb().minPos());
							mrp.hsb().setPosition(newHSBPos, false);											
						}												
						lastTick.set(index.get() - 1);
						for (Chart c : charts) {
							if (!c.mr().equals(mr)) {
								continue;
							}
							c.tick(mr);
						}
						handled.set(false);
						if (diff < timeToNextTick.get()) {
							break;
						}
					}		
					for (Chart c : charts) {
						if (!c.mr().equals(mr)) {
							continue;
						}
						c.draw();
					}
					mrp.draw();	
					lastTick2 = now;
				}				
			}
		}.start();
	}
}
