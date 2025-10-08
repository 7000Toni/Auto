import java.time.ZoneOffset;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;

public class MarketReplay {
	private ArrayList<Chart> charts;
	private DataSet data;
	private MarketReplayPane mrp;
	private int index;	
	private boolean pause = false;
	private boolean live = true;
	private int speed = 1;
	private int tickDataSize;
	private boolean run = false;
	
	public MarketReplay(Chart chart, MarketReplayPane mrp, int index) {		
		this.charts = new ArrayList<Chart>();
		this.data = chart.data();
		this.mrp = mrp;
		this.tickDataSize = data.tickDataSize(false);
		data.setReplayTickDataSize(index);
		int ci = index;
		if (ci == tickDataSize) {
			ci--;
		}
		data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex());		
		this.index = index;
		chart.enableReplayMode(this);
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
		if (pause) {
			pause = false;
		} else {
			pause = true;
		}
	}
	
	public void toggleLive() {
		if (live) {
			live = false;
		} else {
			live = true;
			for (Chart c : charts) {
				c.setKeepStartIndex(false);
			}
		}
	}
	
	public int index() {
		return this.index;
	}
	
	public int maxSize() {
		return this.tickDataSize;
	}
	
	public DataSet data() {
		return this.data;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public void setIndex(int index, boolean increment) {
		if (increment) {
			if (this.index + index > tickDataSize - 1) {
				this.index = tickDataSize - 1;
			} else if (this.index + index < 0) {	
				this.index = 0;
			} else {
				this.index += index;
			}
		} else {
			if (index > tickDataSize - 1) {
				this.index = tickDataSize - 1;
			} else if (index < 0) {	
				this.index = 0;
			} else {
				this.index = index;
			}
		}
		data.setReplayTickDataSize(this.index);
		int ci = this.index;
		if (ci == tickDataSize) {
			ci--;							
		}		
		data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);
	}
	
	private int timeToNextTick(int index) {
		if (index > tickDataSize - 2) {
			index = tickDataSize - 2;
		}
		return (int)(data.tickData().get(index + 1).dateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli() - data.tickData().get(index).dateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli())/speed;
	}
	
	public void stop() {
		this.run = false;
	}
	
	public void run() {
		run = true;
		new AnimationTimer() {
			long lastTick = 0;
			int timeToNextTick = timeToNextTick(index);
			@Override
			public void handle(long now) {
				if (!run) {
					this.stop();
				}
				if (lastTick == 0) {
					lastTick = now;
					return;
				}
				long diff = (now - lastTick) / HorizontalScrollBar.NANO_TO_MILLI;
				if (diff >= timeToNextTick) {		
					if (mrp.hsb().dragged()) {
						index = (int)((mrp.hsb().xPos() / (mrp.hsb().maxPos() - mrp.hsb().sbWidth())) * tickDataSize);						
						data.setReplayTickDataSize(index);
						int ci = index;
						if (ci == tickDataSize) {
							ci--;							
						}		
						data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);
					} 
					while (!pause && index < tickDataSize) {
						index++;
						diff -= timeToNextTick;
						timeToNextTick = timeToNextTick(index);
						data.setReplayTickDataSize(index);
						int ci = index;
						if (ci == tickDataSize) {
							ci--;							
						}
						data.setReplayM1CandlesDataSize(data.tickData().get(ci).candleIndex() + 1);
						if (live) {
							double newHSBPos = ((double)index / tickDataSize) * (mrp.hsb().maxPos() - mrp.hsb().sbWidth() - mrp.hsb().minPos());
							mrp.hsb().setPosition(newHSBPos, false);		
							for (Chart c : charts) {		
								c.hsb().setPosition(Integer.MAX_VALUE, false);
							}
						} else {
							double newHSBPos;
							for (Chart c : charts) {
								newHSBPos = (c.width() - c.hsb().sbWidth() - Chart.PRICE_MARGIN) * ((double)c.startIndex() /(data.tickDataSize(c.replayMode()) - c.numDataPoints() * Chart.END_MARGIN_COEF));
								c.setKeepStartIndex(true);
								c.hsb().setPosition(newHSBPos, false);
							}
							newHSBPos = ((double)index / tickDataSize) * (mrp.hsb().maxPos() - mrp.hsb().sbWidth() - mrp.hsb().minPos());
							mrp.hsb().setPosition(newHSBPos, false);											
						}						
						if (diff < timeToNextTick) {
							break;
						}
					}
					mrp.draw();
					for (Chart c : charts) {
						c.draw();
					}
					lastTick = now;
				}				
			}
		}.start();
	}
}
