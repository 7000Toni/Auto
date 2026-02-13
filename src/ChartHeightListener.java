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
		c.setChartHeight(c.height() - c.hsb().sbHeight() - Chart.CHT_MARGIN * 2); 
		if (c.chtDataMargin() > c.chartHeight() * Chart.CHT_DATA_MARGIN_COEF) {
			c.setChtDataMargin(c.chartHeight() * Chart.CHT_DATA_MARGIN_COEF);
		} else {
			if (!((Double)oldValue.doubleValue()).isNaN()) {
				double ratio = c.chtDataMargin() / (oldValue.doubleValue() - Chart.HEIGHT_EXTRA);
				c.setChtDataMargin((newValue.doubleValue() - Chart.HEIGHT_EXTRA) * ratio);
			}
		}
		c.hsb().setY(c.height() - Chart.HSB_HEIGHT);
		c.setMRPY(c.height() - Chart.HSB_HEIGHT - Chart.CHT_MARGIN - 105 - c.fontSize());
		c.chartMenu().setHeight(c.chartHeight());
		c.btnMenu().setY(Chart.CHT_MARGIN + c.chartHeight() + 1);
		c.draw();
	}		
}
