import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ChartWidthListener implements ChangeListener<Number> {
	Chart c;	
	
	public ChartWidthListener(Chart c) {
		this.c = c;
	}
	
	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {	
		double oldCW = c.chartWidth();
		c.setWidth(newValue.doubleValue() - Chart.WIDTH_EXTRA);
		c.canvas().setWidth(c.width());
		c.setChartWidth(c.width() - Chart.PRICE_MARGIN - Chart.CHT_MARGIN - (c.menuHidden()?0:1 * 300));
		c.setCandleStickVars(c.numCandlesticks());
		c.hsb().setMaxPos(Chart.CHT_MARGIN +  c.chartWidth());
		double newHSBPos = (c.hsb().x() / (Chart.CHT_MARGIN + oldCW - c.hsb().sbWidth())) * (Chart.CHT_MARGIN + c.chartWidth() - c.hsb().sbWidth());
		c.hsb().setPosition(newHSBPos, false);
		c.setMRPX(Chart.CHT_MARGIN + 5);			
		c.chartMenu().setX(Chart.CHT_MARGIN + c.chartWidth() + Chart.PRICE_MARGIN);
		c.btnMenu().setX(Chart.CHT_MARGIN + c.chartWidth() + 1);
		c.chartTypeShortcut().setX(Chart.CHT_MARGIN + c.chartWidth() - 15);

		c.resetTradeButtons();
		
		c.draw();
	}		
}
