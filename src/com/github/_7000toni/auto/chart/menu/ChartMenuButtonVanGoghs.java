package com.github._7000toni.auto.chart.menu;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.marketreplay.trade.history.LoadingHistory;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;
import com.github._7000toni.auto.settings.ImageSettings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ChartMenuButtonVanGoghs {
	
	public IVanGogh menuButtonVG(CanvasButton cb, double fontSize) {
		return (x, y, gc) -> {
			double oldFontSize = gc.getFont().getSize();
			gc.setFont(new Font(fontSize));
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.BLACK);
				gc.setFill(Color.WHITE);
			} else {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.BLACK);
			}
			if (cb.on()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
			} 
			if (cb.hover()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
			} 
			if (cb.pressed()) {				
				gc.setStroke(Color.BLACK);
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
			}	
			gc.fillRoundRect(x, y, cb.width(), cb.height(), CanvasButton.ARC_W, CanvasButton.ARC_H);
			cb.calculateOffsets(gc.getFont());
			gc.strokeText(cb.text(), x + cb.textXOffset(), y + cb.textYOffset());
			gc.setFont(new Font(oldFontSize));
		};
	}
	
	public IVanGogh imgSettingsToggleVG(CanvasButton cb, String text1, String text2) {
		return (x, y, gc) -> {
			BooleanProperty condition;
			if (cb.text().contains("DRAW")) {
				condition = ImageSettings.draw();
			} else {
				condition = ImageSettings.stretch();
			}
			if (condition.get()) {
				cb.setText(text1);	
			} else {
				cb.setText(text2);
			}
			cb.alternateDraw(gc.getFont());
		};
	}
	
	public IVanGogh toggleVG(CanvasButton cb, ReadOnlyBooleanProperty condition, String text1, String text2) {
		return (x, y, gc) -> {
			if (condition.get()) {
				cb.setText(text1);	
			} else {
				cb.setText(text2);
			}
			cb.alternateDraw(gc.getFont());
		};
	}	
	
	public IVanGogh colourPreviewVG(CanvasButton cb, int index) {
		return (x, y, gc) -> {			
			gc.setFill(ColourSettings.colour(index));
			gc.fillRoundRect(x, y, cb.width(), cb.height(), CanvasButton.ARC_W, CanvasButton.ARC_H);
			if (Chart.darkMode().get() && ColourSettings.colour(index).equals(ColourSettings.colour(ColourIndex.CHART_BACKGROUND))) {
				gc.setStroke(Color.WHITE);
				gc.strokeRoundRect(x + 0.5, y + 0.5, cb.width() - 1, cb.height() - 1, CanvasButton.ARC_W, CanvasButton.ARC_H);
			} else if (!Chart.darkMode().get() && ColourSettings.colour(index).equals(ColourSettings.colour(ColourIndex.CHART_BACKGROUND))) {
				gc.setStroke(Color.BLACK);
				gc.strokeRoundRect(x + 0.5, y + 0.5, cb.width() - 1, cb.height() - 1, CanvasButton.ARC_W, CanvasButton.ARC_H);
			}
		};
	}	
	
	public IVanGogh initHstDraw(CanvasButton cb, Chart c) {
		return (x, y, gc) -> {
			LoadingHistory lh = c.chartNode().loadingHistory();
			if (lh != null && lh.started().get() && !lh.complete().get()) {
				gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				gc.fillRect(x+1, y+1, (cb.width()-2)*(lh.progress().get()/100.0), cb.height()-2);
			}
			cb.alternateDraw(gc.getFont());
		};
	}	
}
