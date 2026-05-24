package com.github._7000toni.auto.marketreplay;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.settings.ColourSettings;

import javafx.beans.property.BooleanProperty;
import javafx.scene.paint.Color;

public class MarketReplayPaneVanGoghs {
	public IVanGogh newChartVG(CanvasButton btn) {
		return (x, y, gc) -> {
			if (Chart.darkMode().get()) {			
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}			
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
			if (btn.hover()) {
				gc.setFill(Color.GRAY);
				gc.setStroke(Color.GRAY);
			}
			if (btn.pressed()) {
				gc.setFill(Color.DIMGRAY);
			}
			if (!btn.enabled()) {
				gc.setStroke(Color.LIGHTGRAY);
				gc.setFill(Color.LIGHTGRAY);
			}
			gc.strokeRect(x, y, 40, 20);
			gc.fillRect(x + 1, y + 1, 38, 18);
		};
	}
	
	public IVanGogh pausePlayVG(CanvasButton btn, BooleanProperty bPlay) {
		return (x, y, gc) -> {
			if (Chart.darkMode().get()) {
				gc.setFill(Color.WHITE);
				gc.setStroke(Color.WHITE);
			} else {
				gc.setFill(Color.BLACK);
				gc.setStroke(Color.BLACK);
			}	
			if (btn.hover()) {
				gc.setFill(Color.GRAY);
				gc.setStroke(Color.GRAY);
			}
			if (btn.pressed()) {
				gc.setStroke(Color.DIMGRAY);
				gc.setFill(Color.DIMGRAY);
			}
			if (!btn.enabled()) {
				gc.setStroke(Color.LIGHTGRAY);
				gc.setFill(Color.LIGHTGRAY);
			}
			if (bPlay.get()) {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				gc.strokeRect(x, y, 40, 40);	
				gc.fillRect(x + 10, y + 10, 8, 20);
				gc.fillRect(x + 22, y + 10, 8, 20);
			} else {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
				gc.strokeRect(x, y, 40, 40);	
				double[] xa = {x + 12, x + 32, x + 12, x + 12};
				double[] ya = {y + 10, y + 20, y + 30, y + 10};
				gc.fillPolygon(xa, ya, 4);
			}
		};
	}
	
	public IVanGogh backVG(CanvasButton btn) {
		return (x, y, gc) -> {
			if (Chart.darkMode().get()) {
				gc.setFill(Color.WHITE);
				gc.setStroke(Color.WHITE);
			} else {
				gc.setFill(Color.BLACK);
				gc.setStroke(Color.BLACK);
			}
			if (btn.hover()) {
				gc.setFill(Color.GRAY);
				gc.setStroke(Color.GRAY);
			}
			if (btn.pressed()) {
				gc.setStroke(Color.DIMGRAY);
				gc.setFill(Color.DIMGRAY);
			}
			if (!btn.enabled()) {
				gc.setStroke(Color.LIGHTGRAY);
				gc.setFill(Color.LIGHTGRAY);
			}
			gc.strokeRect(x, y, 40, 40);	
			double[] xa = {x + 5, x + 20, x + 20, x + 35, x + 35, x + 20, x + 20, x + 5};
			double[] ya = {y + 20, y + 8, y + 15, y + 15, y + 25, y + 25, y + 32, y + 20};
			gc.fillPolygon(xa, ya, 8);
		};
	}
	
	public IVanGogh forwardVG(CanvasButton btn) {
		return (x, y, gc) -> {		
			if (Chart.darkMode().get()) {
				gc.setFill(Color.WHITE);
				gc.setStroke(Color.WHITE);
			} else {
				gc.setFill(Color.BLACK);
				gc.setStroke(Color.BLACK);
			}
			if (btn.hover()) {
				gc.setFill(Color.GRAY);
				gc.setStroke(Color.GRAY);
			}
			if (btn.pressed()) {
				gc.setStroke(Color.DIMGRAY);
				gc.setFill(Color.DIMGRAY);
			}
			if (!btn.enabled()) {
				gc.setStroke(Color.LIGHTGRAY);
				gc.setFill(Color.LIGHTGRAY);
			}
			gc.strokeRect(x, y, 40, 40);	
			double[] xa = {x + 5, x + 20, x + 20, x + 35, x + 20, x + 20, x + 5, x + 5};
			double[] ya = {y + 15, y + 15, y + 8, y + 20, y + 32, y + 25, y + 25, y + 15};
			gc.fillPolygon(xa, ya, 8);
		};
	}
	
	public IVanGogh liveVG(CanvasButton btn, BooleanProperty bLive) {
		return (x, y, gc) -> {
			if (Chart.darkMode().get()) {
				gc.setFill(Color.WHITE);
				gc.setStroke(Color.WHITE);
			} else {
				gc.setFill(Color.BLACK);
				gc.setStroke(Color.BLACK);
			}
			if (btn.hover()) {
				gc.setFill(Color.GRAY);
				gc.setStroke(Color.GRAY);
			}
			if (btn.pressed()) {
				gc.setStroke(Color.DIMGRAY);
				gc.setFill(Color.DIMGRAY);
			}
			if (!btn.enabled()) {
				gc.setStroke(Color.LIGHTGRAY);
				gc.setFill(Color.LIGHTGRAY);
			}
			if (bLive.get()) {
				gc.setFill(Color.RED);
				gc.strokeRect(x, y, 40, 40);	
				gc.fillOval(x + 15, y + 15, 10, 10);
			} else {
				gc.setFill(Color.GRAY);
				gc.strokeRect(x, y, 40, 40);	
				gc.fillOval(x + 15, y + 15, 10, 10);
			}
		};
	}
}
