package com.github._7000toni.auto.chart;
import com.github._7000toni.auto.dataset.DataSet;
import com.github._7000toni.auto.marketreplay.MarketReplay;
import com.github._7000toni.auto.marketreplay.MarketReplayPane;

import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	private Chart chart;
	
	public ChartPane(Stage stage, double width, double height, DataSet ch, boolean replayMode, MarketReplay mr, MarketReplayPane mrp) {					
		try {
			chart = new Chart(width, height, stage, ch);
			if (replayMode) {
				chart.enableReplayMode(mr, mrp);
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
