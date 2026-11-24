package com.github._7000toni.auto.chart;
import com.github._7000toni.auto.dataset.Dataset;
import com.github._7000toni.auto.marketreplay.MarketReplay;

import javafx.geometry.Rectangle2D;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	public static final double SCREEN_MARGIN = 50;
	private Chart chart;
	
	public ChartPane(Stage stage, double width, double height, Dataset ch, boolean replayMode, MarketReplay mr) {					
		try {
			Rectangle2D r = Screen.getPrimary().getBounds();
			double w = r.getWidth();
			double h = r.getHeight();
			width = (width + SCREEN_MARGIN > w)?w - SCREEN_MARGIN:width;
			height = (height + SCREEN_MARGIN > h)?h - SCREEN_MARGIN:height;
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
