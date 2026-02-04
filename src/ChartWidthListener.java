import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ChartWidthListener implements ChangeListener<Number> {
	Chart c;	
	
	public ChartWidthListener(Chart c) {
		this.c = c;
	}
	
	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {			
		double newHSBPos = (c.hsb().x() / (c.width() - c.hsb().sbWidth() - Chart.PRICE_MARGIN)) * (newValue.doubleValue() - Chart.WIDTH_EXTRA - c.hsb().sbWidth() - Chart.PRICE_MARGIN);	
		c.setWidth(newValue.doubleValue() - Chart.WIDTH_EXTRA);
		c.canvas().setWidth(c.width());
		c.setChartWidth(c.width() - Chart.PRICE_MARGIN - Chart.CHT_MARGIN);
		c.setCandleStickVars(c.numCandlesticks());
		c.hsb().setMaxPos(c.width() - Chart.PRICE_MARGIN);
		c.hsb().setPosition(newHSBPos, false);
		c.setMRPX(Chart.CHT_MARGIN + 5);			
		
		if (c.replayMode()) {
			c.tradeButs().close().setX(Chart.CHT_MARGIN + c.chartWidth() / 2 - 102 - c.fontSize()*2);
			c.tradeButs().cancelTP().setX(Chart.CHT_MARGIN + c.chartWidth() / 2 - 102 - c.fontSize()*2);
			c.tradeButs().cancelSL().setX(Chart.CHT_MARGIN + c.chartWidth() / 2 - 102 - c.fontSize()*2);
			c.tradeButs().sl().setX(Chart.CHT_MARGIN + c.chartWidth() / 2 - 100);
			c.tradeButs().tp().setX(Chart.CHT_MARGIN + c.chartWidth() / 2 - 100);
			c.tradeButs().setSL().setX(Chart.CHT_MARGIN + c.chartWidth() / 2 + 10);
			c.tradeButs().setTP().setX(Chart.CHT_MARGIN + c.chartWidth() / 2 + 20 + c.fontSize()*2);
			if (c.penTrade() != null) {
				c.pendingTrades().add(c.penTrade());
			}
			for (PendingTrade p : c.pendingTrades()) {
				p.pTradeButs().order.setX(Chart.CHT_MARGIN + c.chartWidth() / 2 - 100);
				p.pTradeButs().close.setX(Chart.CHT_MARGIN + c.chartWidth() / 2 - 102 - c.fontSize()*2);
				p.pTradeButs().setSL.setX(Chart.CHT_MARGIN + c.chartWidth() / 2 + 10);
				p.pTradeButs().setTP.setX(Chart.CHT_MARGIN + c.chartWidth() / 2 + 20 + c.fontSize()*2);
			}
			if (c.penTrade() != null) {
				c.pendingTrades().remove(c.penTrade());
			}
			c.limitOrder().setX(c.width() - Chart.PRICE_MARGIN - c.fontSize()*2-2);
			c.stopOrder().setX(c.width() - Chart.PRICE_MARGIN - c.fontSize()*4-4);
		}			
		c.draw();
	}		
}
