package com.github._7000toni.auto.chart.listener;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartNode;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ChartWidthListener implements ChangeListener<Number> {
	Chart c;	
	
	public ChartWidthListener(Chart c) {
		this.c = c;
	}
	
	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {	
		double oldCW = c.chartNode().width();
		c.setWidth(newValue.doubleValue() - Chart.WIDTH_EXTRA);
		c.canvas().setWidth(c.width());
		c.chartNode().setWidth(c.width() - c.priceMargin().width() - ChartNode.CHT_MARGIN - (c.menuHidden()?0:300));
		c.chartNode().setCandleStickVars(c.chartNode().numCandlesticks());
		c.hsb().setMaxPos(ChartNode.CHT_MARGIN +  c.chartNode().width());
		double newHSBPos = (c.hsb().x() / (ChartNode.CHT_MARGIN + oldCW - c.hsb().sbWidth())) * (ChartNode.CHT_MARGIN + c.chartNode().width() - c.hsb().sbWidth());
		c.hsb().setPosition(newHSBPos, false);
		c.chartNode().updateMRNXVars();			
		c.chartMenu().setX(ChartNode.CHT_MARGIN + c.chartNode().width() + c.priceMargin().width());
		c.btnMenu().setX(ChartNode.CHT_MARGIN + c.chartNode().width() + 1);
		c.chartNode().chartShortcut().setX(ChartNode.CHT_MARGIN + c.chartNode().width() - 15);

		if (c.chartNode().replayMode()) {
			c.chartNode().tradeButtons().resetButtons();
		}
		
		c.draw();
	}		
}
