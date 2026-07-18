package com.github._7000toni.auto.chart;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.marketreplay.MarketReplay;

import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	private Chart chart;
	
	public ChartPane(Stage stage, double width, double height, Dataset ch, boolean replayMode, MarketReplay mr) {					
		try {
			chart = new Chart(width, height, stage, ch);
			if (replayMode) {
				chart.chartNode().enableReplayMode(mr);
			}
			this.add(chart.canvas(), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Chart getChart() {
		return chart;
	}
}
