package com.github._7000toni.auto.marketreplay.trade;

import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.chart.CrossHair;

public class PendingTradePair {
	private PendingTrade penTrade;
	private PendingTradeButtons penTradeButs;
	private ChartNode chart;	
	
	public PendingTradePair(PendingTrade penTrade, ChartNode chart) {
		this.penTrade = penTrade;
		this.chart = chart;
		
		penTradeButs = new TradeButtons();
		String text = "";
		if (penTrade.buy()) {
			text += "BUY ";
		} else {
			text += "SELL ";
		}
		if (penTrade.limit()) {
			text += "LIMIT";
		} else {
			text += "STOP";
		}
		penTradeButs.setOrder(new CanvasButton(chart.graphicsContext(), 100, chart.fontSize()*2, ChartNode.CHT_MARGIN + chart.width() / 2 - 100 - chart.fontSize()*2, 0, text, 6, chart.fontSize()/3));
		penTradeButs.order().setVanGogh(chart.chartButtonVanGoghs().penOrderVG(penTradeButs.order(), penTrade));
		
		penTradeButs.setClose(new CanvasButton(chart.graphicsContext(), chart.fontSize()*2, chart.fontSize()*2, ChartNode.CHT_MARGIN + chart.width() / 2 - 102 - chart.fontSize()*2, 0, "X", 9, chart.fontSize()/3));
		penTradeButs.close().setVanGogh(chart.chartButtonVanGoghs().penCloseVG(penTradeButs.close(), penTrade));		
		
		penTradeButs.setSetSL(new CanvasButton(chart.graphicsContext(), chart.fontSize()*2, chart.fontSize()*2, ChartNode.CHT_MARGIN + chart.width() / 2 + 10, 0, "SL", 6, chart.fontSize()/3));
		penTradeButs.setSL().setVanGogh(chart.chartButtonVanGoghs().penSetSlVG(penTradeButs.setSL(), penTrade));		
		
		penTradeButs.setSetTP(new CanvasButton(chart.graphicsContext(), chart.fontSize()*2, chart.fontSize()*2, ChartNode.CHT_MARGIN + chart.width() / 2 + 20 + chart.fontSize()*2, 0, "TP", 6, chart.fontSize()/3));
		penTradeButs.setTP().setVanGogh(chart.chartButtonVanGoghs().penSetTpVG(penTradeButs.setTP(), penTrade));
		
		setEvents();
	}
	
	private void checkTP() {
		if (chart.tradeButtons().pendingButtonsNode().tradePairs().size() == 1 && chart.marketReplay().unvalidatedTpPrice().get() != -1 && chart.marketReplay().trade().closed()) {	
			PendingTrade penTrade = chart.tradeButtons().pendingButtonsNode().tradePairs().get(0).pendingTrade();
			if (chart.marketReplay().unvalidatedTpPrice().get() < penTrade.price() && penTrade.buy() ||
					chart.marketReplay().unvalidatedTpPrice().get() > penTrade.price() && !penTrade.buy()) {
				chart.marketReplay().cancelTp();
			}
		}
	}
	
	private void checkSL() {		
		if (chart.tradeButtons().pendingButtonsNode().tradePairs().size() == 1 && chart.marketReplay().unvalidatedSlPrice().get() != -1 && chart.marketReplay().trade().closed()) {
			PendingTrade penTrade = chart.tradeButtons().pendingButtonsNode().tradePairs().get(0).pendingTrade();
			if (chart.marketReplay().unvalidatedSlPrice().get() > penTrade.price() && penTrade.buy() ||
					chart.marketReplay().unvalidatedSlPrice().get() < penTrade.price() && !penTrade.buy()) {
				chart.marketReplay().cancelSl();
			}
		}
	}
	
	private void setEvents() {
		penTradeButs.order().setOnMouseDragged(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
			double price = chart.roundToNearestTick(chart.yCoordToPrice(e.getY()));
			if (price != chart.data().tickData().get(chart.data().tickDataSize(true).get() - 1).price()) {
				penTrade.setPrice(price);
				if (price > chart.data().tickData().get(chart.data().tickDataSize(true).get() - 1).price()) {
					if (penTrade.limit()) {
						penTrade.setBuy(false);
					} else {
						penTrade.setBuy(true);
					}
				} else {
					if (penTrade.limit()) {
						penTrade.setBuy(true);
					} else {
						penTrade.setBuy(false);
					}
				}
			}
			CrossHair.setX(e.getX());
			CrossHair.setY(e.getY());
			CrossHair.setPrice(chart.yCoordToPrice(e.getY()));			
			chart.draw();
		});
		penTradeButs.order().setOnMouseReleased(e -> {
			checkTP();
			checkSL();
		});
		
		penTradeButs.close().setOnMouseClicked(e -> {
			chart.marketReplay().removePendingTrade(penTrade);
			checkTP();
			checkSL();
		});
		
		penTradeButs.setSL().setOnMouseDragged(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
			chart.marketReplay().setUnvalidatedSlPrice(chart.roundToNearestTick(chart.yCoordToPrice(e.getY())));
			CrossHair.setX(e.getX());
			CrossHair.setY(e.getY());
			CrossHair.setPrice(chart.yCoordToPrice(e.getY()));
			chart.draw();
		});
		penTradeButs.setSL().setOnMouseReleased(e -> {
			if (chart.marketReplay().unvalidatedSlPrice().get() > penTrade.price() && penTrade.buy() ||
					chart.marketReplay().unvalidatedSlPrice().get() < penTrade.price() && !penTrade.buy()) {
				chart.marketReplay().cancelSl();
			}
		});
		
		penTradeButs.setTP().setOnMouseDragged(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
			chart.marketReplay().setUnvalidatedTpPrice(chart.roundToNearestTick(chart.yCoordToPrice(e.getY())));
			CrossHair.setX(e.getX());
			CrossHair.setY(e.getY());
			CrossHair.setPrice(chart.yCoordToPrice(e.getY()));
			chart.draw();
		});
		penTradeButs.setTP().setOnMouseReleased(e -> {
			if (chart.marketReplay().unvalidatedTpPrice().get() < penTrade.price() && penTrade.buy() ||
					chart.marketReplay().unvalidatedTpPrice().get() > penTrade.price() && !penTrade.buy()) {
				chart.marketReplay().cancelTp();
			}
		});
		
		setCrossHairStuff();
	}
	
	private void setCrossHairStuff() {
		penTradeButs.order().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		penTradeButs.close().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		penTradeButs.setSL().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		penTradeButs.setTP().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
	}
	
	public void draw() {
		penTradeButs.order().draw();
		penTradeButs.close().draw();
		penTradeButs.setSL().draw();
		penTradeButs.setTP().draw();
	}
	
	public void drawOrder() {
		penTradeButs.order().draw();
		penTradeButs.close().draw();
	}
	
	public void drawSets() {
		penTradeButs.setSL().draw();
		penTradeButs.setTP().draw();
	}
	
	public PendingTrade pendingTrade() {
		return penTrade;
	}
	
	public PendingTradeButtons pendingTradeButtons() {
		return penTradeButs;
	}
	
	private void setCrossHairVars(double x, double y) {
		if (!chart.onChart(x, y)) {
			chart.setFocusedChart(false);
		} else {
			chart.setFocusedChart(true);
			CrossHair.setX(x);
			CrossHair.setY(y);
			CrossHair.setPrice(chart.yCoordToPrice(y));
		}
	}
}
