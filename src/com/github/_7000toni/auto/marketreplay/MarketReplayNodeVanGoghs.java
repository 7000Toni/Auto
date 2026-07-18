package com.github._7000toni.auto.marketreplay;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.settings.ColourSettings;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.paint.Color;

public class MarketReplayNodeVanGoghs {
	public IVanGogh newChartVG(CanvasButton btn) {
		return (x, y, gc) -> {
			btn.setColoursRect();
			gc.fillRoundRect(x, y, 40, 20, CanvasButton.ARC_W, CanvasButton.ARC_H);
		};
	}
	
	public IVanGogh pausePlayVG(CanvasButton btn, ReadOnlyBooleanProperty paused) {
		return (x, y, gc) -> {
			btn.setColoursRect();
			gc.fillRoundRect(x, y, 40, 40, CanvasButton.ARC_W, CanvasButton.ARC_H);
			if (!paused.get()) {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				gc.fillRect(x + 10, y + 10, 8, 20);
				gc.fillRect(x + 22, y + 10, 8, 20);
			} else {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
				double[] xa = {x + 12, x + 32, x + 12, x + 12};
				double[] ya = {y + 10, y + 20, y + 30, y + 10};
				gc.fillPolygon(xa, ya, 4);
			}
		};
	}
	
	public IVanGogh backVG(CanvasButton btn) {
		return (x, y, gc) -> {
			btn.setColoursRect();
			gc.fillRoundRect(x, y, 40, 40, CanvasButton.ARC_W, CanvasButton.ARC_H);	
			btn.setColoursText();
			double[] xa = {x + 5, x + 20, x + 20, x + 35, x + 35, x + 20, x + 20, x + 5};
			double[] ya = {y + 20, y + 8, y + 15, y + 15, y + 25, y + 25, y + 32, y + 20};
			gc.fillPolygon(xa, ya, 8);
		};
	}
	
	public IVanGogh forwardVG(CanvasButton btn) {
		return (x, y, gc) -> {		
			btn.setColoursRect();
			gc.fillRoundRect(x, y, 40, 40, CanvasButton.ARC_W, CanvasButton.ARC_H);	
			btn.setColoursText();
			double[] xa = {x + 5, x + 20, x + 20, x + 35, x + 20, x + 20, x + 5, x + 5};
			double[] ya = {y + 15, y + 15, y + 8, y + 20, y + 32, y + 25, y + 25, y + 15};
			gc.fillPolygon(xa, ya, 8);
		};
	}
	
	public IVanGogh liveVG(CanvasButton btn, ReadOnlyBooleanProperty bLive) {
		return (x, y, gc) -> {
			btn.setColoursRect();
			gc.fillRoundRect(x, y, 40, 40, CanvasButton.ARC_W, CanvasButton.ARC_H);	
			if (bLive.get()) {
				gc.setFill(Color.RED);	
				gc.fillOval(x + 15, y + 15, 10, 10);
			} else {
				gc.setFill(Color.DIMGRAY);	
				gc.fillOval(x + 15, y + 15, 10, 10);
			}
		};
	}
}
