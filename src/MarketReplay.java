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
			int i = 0;
			@Override
			public void handle(long now) {
				if (lastTick == 0) {
					lastTick = now;
					return;
				}
				long diff = (now - lastTick) / ScrollBar.NANO_TO_MILLI;/*
				System.out.println("+" + diff);
				if (i == 10) {
				System.exit(0);
				}
				i++;*/
				System.out.println(diff + "\n" + timeToNextTick + "\n");
				if (diff >= timeToNextTick) {					
					while (true) {
						index++;
						diff -= timeToNextTick;
						timeToNextTick = timeToNextTick(index);
						data.setReplayTickDataSize(index);
						chart.hsb().setPosition(Integer.MAX_VALUE, false);
						chart.drawChart();
						System.out.println(data.tickData().get(index).price());
						if (diff < timeToNextTick) {
							System.out.println("out");
							break;
						}
					}
					lastTick = now;
				}				
			}
		}.start();
	}
}
