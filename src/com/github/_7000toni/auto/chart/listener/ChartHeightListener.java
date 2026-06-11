package com.github._7000toni.auto.chart.listener;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartNode;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ChartHeightListener implements ChangeListener<Number> {
	public Chart c; 
	
	public ChartHeightListener(Chart c) {
		this.c = c;
	}
	
	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		c.setHeight(newValue.doubleValue() - Chart.HEIGHT_EXTRA);
		c.canvas().setHeight(c.height());
		c.chartNode().setHeight(c.height() - c.hsb().sbHeight() - ChartNode.CHT_MARGIN * 2); 
		if (c.chartNode().chtDataMargin() > c.chartNode().height() * ChartNode.CHT_DATA_MARGIN_COEF) {
			c.chartNode().setChtDataMargin(c.chartNode().height() * ChartNode.CHT_DATA_MARGIN_COEF);
		} else {
			if (!((Double)oldValue.doubleValue()).isNaN()) {
				double ratio = c.chartNode().chtDataMargin() / (oldValue.doubleValue() - Chart.HEIGHT_EXTRA);
				c.chartNode().setChtDataMargin((newValue.doubleValue() - Chart.HEIGHT_EXTRA) * ratio);
			}
		}
		c.hsb().setY(c.height() - Chart.HSB_HEIGHT);
		c.chartNode().setMRPY(c.height() - Chart.HSB_HEIGHT - ChartNode.CHT_MARGIN - 105 - c.chartNode().fontSize());
		c.chartMenu().setHeight(c.chartNode().height());
		c.btnMenu().setY(ChartNode.CHT_MARGIN + c.chartNode().height() + 1);
		c.draw();
	}		
}
