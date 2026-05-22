package com.github._7000toni.auto.marketreplay.trade;

import com.github._7000toni.auto.canvasnode.CanvasButton;
import com.github._7000toni.auto.chart.Chart;

public class PendingTrade implements ITrade {
	private boolean limit;
	private boolean buy;
	private double price;
	private double volume;
	private PendingTradeButtons pTradeButs;
	
	public PendingTrade(boolean limit, boolean buy,	double price, double volume, Chart c) {
		this.limit = limit;
		this.buy = buy;
		this.price = price;
		this.volume = volume;
		this.pTradeButs = new PendingTradeButtons();
		this.pTradeButs.order = new CanvasButton(c.graphicsContext(), 100, c.fontSize()*2, Chart.CHT_MARGIN + c.chartWidth() / 2 - 100, 0, "", 5, c.fontSize()/3);
		this.pTradeButs.close = new CanvasButton(c.graphicsContext(), c.fontSize()*2, c.fontSize()*2, Chart.CHT_MARGIN + c.chartWidth() / 2 - 102 - c.fontSize()*2, 0, "X", 9, c.fontSize()/3);			
		this.pTradeButs.setSL = new CanvasButton(c.graphicsContext(), c.fontSize()*2, c.fontSize()*2, Chart.CHT_MARGIN + c.chartWidth() / 2 + 10, 0, "SL", 6, c.fontSize()/3);
		this.pTradeButs.setTP = new CanvasButton(c.graphicsContext(), c.fontSize()*2, c.fontSize()*2, Chart.CHT_MARGIN + c.chartWidth() / 2 + 20 + c.fontSize()*2, 0, "TP", 6, c.fontSize()/3);
		this.pTradeButs.order.setVanGogh(c.chartButtonVanGoghs().orderVG(pTradeButs.order, this));
		this.pTradeButs.close.setVanGogh(c.chartButtonVanGoghs().closeVG(pTradeButs.close, this));			
		this.pTradeButs.setSL.setVanGogh(c.chartButtonVanGoghs().setSlVG(pTradeButs.setSL));
		this.pTradeButs.setTP.setVanGogh(c.chartButtonVanGoghs().setTpVG(pTradeButs.setTP));
		String text = "STOP";
		if (limit) {
			text = "LIMIT";
		}
		this.pTradeButs.order.setText(((Double)(volume)).toString() + "  " + text);
	}
	
	public boolean limit() {
		return limit;
	}
	
	@Override
	public boolean buy() {
		return buy;
	}
	
	public double price() {
		return price;
	}
	
	public double volume() {
		return volume;
	}
	
	public void setLimit(boolean limit) {
		this.limit = limit;
	}
	
	public void setBuy(boolean buy) {
		this.buy = buy;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setVolume(double volume) {
		this.volume = volume;
	}
	
	public PendingTradeButtons pTradeButs() {
		return pTradeButs;
	}
}
