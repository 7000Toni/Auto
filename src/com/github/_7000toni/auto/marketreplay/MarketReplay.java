package com.github._7000toni.auto.marketreplay;
import java.io.File;
import java.time.ZoneOffset;
import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalScrollBar;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.marketreplay.trade.PendingTrade;
import com.github._7000toni.auto.marketreplay.trade.PendingTradePair;
import com.github._7000toni.auto.marketreplay.trade.Trade;

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
	private ArrayList<ChartNode> charts;
	private String name;
	private Dataset data;
	private MarketReplayNode mrNode;
	private IntegerProperty index = new SimpleIntegerProperty();	
	private BooleanProperty paused = new SimpleBooleanProperty(false);
	private BooleanProperty live = new SimpleBooleanProperty(true);
	private IntegerProperty speed = new SimpleIntegerProperty(1);
	private IntegerProperty tickDataSize = new SimpleIntegerProperty();
	private BooleanProperty run = new SimpleBooleanProperty(false);
	private IntegerProperty timeToNextTick = new SimpleIntegerProperty(); 
	
	private DoubleProperty slPrice = new SimpleDoubleProperty(-1);
	private DoubleProperty tpPrice = new SimpleDoubleProperty(-1);
	private DoubleProperty unvalidatedSlPrice = new SimpleDoubleProperty(-1);
	private DoubleProperty unvalidatedTpPrice = new SimpleDoubleProperty(-1);
	private Trade trade = new Trade(); 
	private IntegerProperty lastTick = new SimpleIntegerProperty(0);
	private long lastTickTime = 0;
	private ArrayList<PendingTrade> pendingTrades = new ArrayList<PendingTrade>();
	private static BooleanProperty writeToFile = new SimpleBooleanProperty(true);
	
	public MarketReplay(Chart chart, MarketReplayNode mrNode, int index) {		
		this.charts = new ArrayList<ChartNode>();
		this.name = chart.chartNode().name();
		this.data = chart.chartNode().data();
		this.mrNode = mrNode;
		this.tickDataSize.set(data.tickDataSize(false).get());
		if (index < 0) {
			index = 0;
		}
		data.setReplayTickDataSize(index + 1);		
		int ci = index;
		if (ci >= tickDataSize.get()) {
			ci = tickDataSize.get() - 1;
		}
		data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);		
		this.index.set(index);
		chart.chartNode().enableReplayMode(this);
	}
	
	public static ReadOnlyBooleanProperty writeToFile() {
		return writeToFile;
	}
	
	public static void toggleWriteToFile() {
		writeToFile.set(!writeToFile.get());
	}
	
	public void addChart(ChartNode chart) {
		charts.add(chart);
	}
	
	public void removeChart(ChartNode chart) {
		charts.remove(chart);
	}
	
	public ArrayList<ChartNode> charts() {
		return charts;
	}
	
	public void togglePause() {		
		paused.set(!paused.get());
	}
	
	public void toggleLive() {
		if (live.get()) {
			live.set(false);
		} else {
			live.set(true);
			for (ChartNode c : charts) {
				if (!c.mr().equals(this)) {
					continue;
				}
				c.setKeepStartIndex(false);
			}
		}
	}
	
	public ReadOnlyIntegerProperty tickDataSize() {
		return IntegerProperty.readOnlyIntegerProperty(tickDataSize);
	}
	
	public ReadOnlyIntegerProperty index() {
		return IntegerProperty.readOnlyIntegerProperty(index);
	}
	
	public ReadOnlyIntegerProperty maxSize() {
		return IntegerProperty.readOnlyIntegerProperty(tickDataSize);
	}
	
	public Dataset data() {
		return this.data;
	}
	
	public ReadOnlyDoubleProperty slPrice() {
		return DoubleProperty.readOnlyDoubleProperty(slPrice);
	}
	
	public ReadOnlyDoubleProperty tpPrice() {
		return DoubleProperty.readOnlyDoubleProperty(tpPrice);
	}
	
	public ReadOnlyDoubleProperty unvalidatedSlPrice() {		
		return DoubleProperty.readOnlyDoubleProperty(unvalidatedSlPrice);
	}
	
	public ReadOnlyDoubleProperty unvalidatedTpPrice() {
		return DoubleProperty.readOnlyDoubleProperty(unvalidatedTpPrice);
	}
	
	public void cancelSl() {
		slPrice.set(-1);
		unvalidatedSlPrice.set(-1);
	}
	
	public void cancelTp() {
		tpPrice.set(-1);
		unvalidatedTpPrice.set(-1);
	}
	
	public void setSlPrice(double slPrice) {
		validateSl(slPrice);
	}
	
	public void setTpPrice(double tpPrice) {
		validateTp(tpPrice);
	}
	
	public void setUnvalidatedSlPrice(double slPrice) {
		unvalidatedSlPrice.set(slPrice);
	}
	
	public void setUnvalidatedTpPrice(double tpPrice) {
		unvalidatedTpPrice.set(tpPrice);
	}
	
	public boolean validateSl(double sl) {
		if (sl < data.tickData().get(data.tickDataSize(true).get() - 1).price() && trade.buy() ||
				sl > data.tickData().get(data.tickDataSize(true).get() - 1).price() && !trade.buy()) {
			slPrice.set(sl);
			unvalidatedSlPrice.set(slPrice.get());
			trade.setSL(sl);
			return true;
		}
		unvalidatedSlPrice.set(slPrice.get());
		return false;
	}
	
	public boolean validateTp(double tp) {
		if (tp < data.tickData().get(data.tickDataSize(true).get() - 1).price() && !trade.buy() ||
				tp > data.tickData().get(data.tickDataSize(true).get() - 1).price() && trade.buy()) {
			tpPrice.set(tp);
			unvalidatedTpPrice.set(tpPrice.get());
			trade.setTP(tp);
			return true;
		}
		unvalidatedTpPrice.set(tpPrice.get());
		return false;
	}
	
	public boolean validateSl() {
		return validateSl(unvalidatedSlPrice.get());
	}
	
	public boolean validateTp() {
		return validateTp(unvalidatedTpPrice.get());
	}
	
	public Trade trade() {
		return trade;
	}
	
	public ReadOnlyIntegerProperty lastTick() {
		return IntegerProperty.readOnlyIntegerProperty(lastTick);
	}
	
	public ArrayList<PendingTrade> pendingTrades() {
		return pendingTrades;
	}
	
	public void addPendingTrade(PendingTrade pendingTrade) {
		this.pendingTrades.add(pendingTrade);
		for (ChartNode c : charts) {
			c.tradeButtons().addPenTradePair(new PendingTradePair(pendingTrade, c.chart().chartNode()));
		}
	}
	
	public void removePendingTrade(PendingTrade pendingTrade) {
		this.pendingTrades.remove(pendingTrade);
		if (pendingTrades.size() == 0 && trade.closed()) {
			cancelSl();
			cancelTp();
		}
		for (ChartNode c : charts) {
			c.tradeButtons().removePenTradePair(pendingTrade);
		}
	}
	
	public void setTrade(Trade trade) {
		this.trade.replaceTrade(trade);
		if (!trade.closed()) {
			validateSl(unvalidatedSlPrice.get());
			validateTp(unvalidatedTpPrice.get());
			for (ChartNode c : charts) {
				c.tradeButtons().enableButtons();
			}
		} else {
			cancelSl();
			cancelTp();
		}
	}
	
	public void closeTrade(int currentPriceIndex) {
		trade.close(currentPriceIndex);
		closedTradeProc();
	}
	
	public void scaleIn(double volume, int index) {
		trade.scaleIn(volume, index);
	}
	
	public void scaleOut(double volume, int index) {
		trade.scaleOut(volume, index);
		closedTradeProc();
		if (trade.closed()) {
			for (ChartNode c : charts) {
				c.tradeButtons().disableButtons();
			}
		}
	}
	
	public void setSpeed(int speed) {
		this.speed.set(speed);
		lastTickTime = System.nanoTime();
		timeToNextTick.set(timeToNextTick(index.get()));
	}
	
	public ReadOnlyBooleanProperty live() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(live);
	}
	
	public void setIndex(int index, boolean increment) {
		if (increment) {
			if (index == 0) {
				return;
			}
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
		if (ci >= tickDataSize.get()) {
			ci = tickDataSize.get() - 1;
		}	
		data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);
		tick();
		
		if (!charts.isEmpty()) {
			charts.get(0).draw();
		}
		mrNode.updateHSBPos();
		mrNode.draw();
	}
	
	private int timeToNextTick(int index) {
		if (index > tickDataSize.get() - 2) {
			index = tickDataSize.get() - 2;
		}
		return (int)(data.tickData().get(index + 1).dateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli() - data.tickData().get(index).dateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli())/speed.get();
	}
	
	public ReadOnlyBooleanProperty paused() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(paused);
	}
	
	public void stop() {
		this.run.set(false);
		Trade.removeHistory(name);
	}
	
	private void executePendingOrder(PendingTrade p, int i) {
		boolean nt = false;
		if (trade.closed()) {
			setTrade(new Trade(data, i, slPrice.get(), tpPrice.get(), p.buy(), p.volume()));	
			for (ChartNode c : charts) {
				c.tradeButtons().enableButtons();
				c.tradeButtons().removePenTradePair(p);
			}
			nt = true;
		} else {			
			if (trade.buy() && p.buy() || !trade.buy() && !p.buy()) {
				scaleIn(p.volume(), i);
			} else {
				scaleOut(p.volume(), i);
			}
			nt = false;
		} 		
		if (nt) {
			trade().updateTrade(data.tickDataSize(true).get() - 1);
			if (trade().closed()) {
				closedTradeProc();
				for (ChartNode c : charts) {
					c.tradeButtons().disableButtons();
					c.tradeButtons().removePenTradePair(p);
				}
			}
		}
	}
	
	private void checkPendingOrders() {						
		for (int i = lastTick.get(); i < data.tickDataSize(true).get(); i++) {
			double currentPrice = data.tickData().get(i).price();
			int j = 0;
			Object[] pt = pendingTrades.toArray();
			for (Object obj : pt) {
				PendingTrade p = (PendingTrade) obj;
				boolean changed = false;
				if (p.buy()) {
					if (currentPrice >= p.price() && !p.limit()) {
						executePendingOrder(p, i);
						pendingTrades.remove(j);
						changed = true;
						j--;
					} else if (currentPrice <= p.price() && p.limit()) {
						executePendingOrder(p, i);
						pendingTrades.remove(j);
						changed = true;
						j--;
					}
				} else {
					if (currentPrice <= p.price() && !p.limit()) {
						executePendingOrder(p, i);
						pendingTrades.remove(j);
						changed = true;
						j--;
					} else if (currentPrice >= p.price() && p.limit()) {
						executePendingOrder(p, i);
						pendingTrades.remove(j);
						changed = true;
						j--;
					}
				}
				if (changed) {
					for (ChartNode c : charts) {
						c.tradeButtons().removePenTradePair(p);
					}	
				}
				j++;
			}
		}
	}
	
	public void closedTradeProc() {
		if (trade.closed()) {
			cancelSl();
			cancelTp();
		}
		if (writeToFile.get()) {
			trade.writeToFile(new File("./trades.txt"));
		}
	}
	
	public void tick() {		
		if (!trade().closed()) {
			trade().updateTrade(data.tickDataSize(true).get() - 1);			
			if (trade().closed()) {
				closedTradeProc();
				if (!charts.isEmpty()) {
					charts.get(0).draw();
				}
			}
		}	
		checkPendingOrders();			
	}
	
	public void run() {
		run.set(true);
		new AnimationTimer() {			
			@Override
			public void handle(long now) {
				if (!run.get()) {
					this.stop();
				}
				if (lastTickTime == 0) {
					lastTickTime = now;
					timeToNextTick.set(timeToNextTick(index.get()));
					return;
				}
				long diff = (now - lastTickTime) / HorizontalScrollBar.NANO_TO_MILLI;
				if (diff >= timeToNextTick.get()) {	
					while (!paused.get() && index.get() < tickDataSize.get()) {
						index.set(index.get() + 1);
						diff -= timeToNextTick.get();
						timeToNextTick.set(timeToNextTick(index.get()));
						data.setReplayTickDataSize(index.get() + 1);
						int ci = index.get();
						if (ci >= tickDataSize.get()) {
							ci = tickDataSize.get() - 1;
						}
						data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);
						if (live.get()) {
							double newHSBPos = ((double)index.get() / tickDataSize.get()) * (mrNode.hsb().maxPos() - mrNode.hsb().sbWidth() - mrNode.hsb().minPos());
							mrNode.hsb().setPosition(newHSBPos, false);		
							mrNode.updateHSBPos();
							for (ChartNode c : charts) {
								c.setKeepStartIndex(false);
								c.chart().hsb().setPosition(Integer.MAX_VALUE, false);
							}
						} else {
							double newHSBPos;
							for (ChartNode c : charts) {
								if (c.drawCandlesticks().get()) {
									newHSBPos = (ChartNode.CHT_MARGIN + c.width() - Chart.HSB_WIDTH) * ((double)c.startIndex() /(data.m1CandlesDataSize(c.replayMode()).get() - c.numCandlesticks() * ChartNode.END_MARGIN_COEF));
								} else {
									newHSBPos = (ChartNode.CHT_MARGIN + c.width() - Chart.HSB_WIDTH) * ((double)c.startIndex() /(data.tickDataSize(c.replayMode()).get() - c.numDataPoints() * ChartNode.END_MARGIN_COEF));
								}								
								c.setKeepStartIndex(true);
								c.chart().hsb().setPosition(newHSBPos, false);
							}
							newHSBPos = ((double)index.get() / tickDataSize.get()) * (mrNode.hsb().maxPos() - mrNode.hsb().sbWidth() - mrNode.hsb().minPos());
							mrNode.hsb().setPosition(newHSBPos, false);
							mrNode.updateHSBPos();
						}												
						lastTick.set(index.get() - 1);
						tick();
						if (diff < timeToNextTick.get()) {
							break;
						}
					}											
					
					if (!charts.isEmpty()) {
						charts.get(0).draw();
					}
					mrNode.draw();
					lastTickTime = now;
				}				
			}
		}.start();
	}
}
