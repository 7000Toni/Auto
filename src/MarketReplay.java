import java.time.ZoneOffset;

import javafx.animation.AnimationTimer;

public class MarketReplay {
	private Chart chart;
	private DataSet data;
	private int index;	
	private int tickDataSize;
	private int m1DataSize;
	
	public MarketReplay(Chart chart, int index) {
		this.chart = chart;
		this.data = chart.data();
		chart.setReplayMode(true);
		data.setReplayTickDataSize(index);
		tickDataSize = data.tickDataSize(false);
		m1DataSize = data.m1CandlesDataSize(false);
		this.index = index;
	}
	
	private int timeToNextTick(int index) {
		return (int)(data.tickData().get(index + 1).dateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli() - data.tickData().get(index).dateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli())/100;
	}
	
	public void run() {
		new AnimationTimer() {
			long lastTick = 0;
			int timeToNextTick = timeToNextTick(index);
			@Override
			public void handle(long now) {
				if (lastTick == 0) {
					lastTick = now;
					return;
				}
				long diff = (now - lastTick) / HorizontalChartScrollBar.NANO_TO_MILLI;
				if (diff >= timeToNextTick) {					
					while (true) {
						index++;
						diff -= timeToNextTick;
						timeToNextTick = timeToNextTick(index);
						data.setReplayTickDataSize(index);
						chart.hsb().setPosition(Integer.MAX_VALUE, false);
						chart.drawChart();
						if (diff < timeToNextTick) {
							break;
						}
					}
					lastTick = now;
				}				
			}
		}.start();
	}
}
