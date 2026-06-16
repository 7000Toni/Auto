package com.github._7000toni.auto.chart;

import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.CanvasNumberChooser;
import com.github._7000toni.auto.marketreplay.MarketReplay;
import com.github._7000toni.auto.marketreplay.trade.PendingTrade;
import com.github._7000toni.auto.marketreplay.trade.PendingTradePair;
import com.github._7000toni.auto.marketreplay.trade.Trade;
import com.github._7000toni.auto.marketreplay.trade.TradeButtons;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class ChartMarketReplayButtons {
	private ChartNode chart;
	private CanvasButton buy;
	private CanvasButton sell;
	private CanvasNumberChooser volUnits;
	private CanvasNumberChooser volTens;
	
	private TradeButtons tradeButs;
	private ArrayList<PendingTradePair> penTrades;
	private CanvasButton limitOrder;
	private CanvasButton stopOrder;
	
	
	public ChartMarketReplayButtons(ChartNode chart, MarketReplay mr, ChartButtonVanGoghs cbvg) {
		this.chart = chart;
		init(mr, cbvg);
		addToSceneGraph();
		setMouseEvents();
	}
	
	private void init(MarketReplay mr, ChartButtonVanGoghs cbvg) {
		GraphicsContext gc = chart.graphicsContext();
		double fontSize = chart.fontSize();
		double chartWidth = chart.width();
		
		double bw = 40;
		double ncw = 20;
		double bh = 30;
		double mgn = 5;
		double initx = ChartNode.CHT_MARGIN + ChartNode.INFO_MARGIN;
		double inity = 30;		
		
		sell = new CanvasButton(gc, bw, bh, initx, inity, "SELL", 9, fontSize + 7);
		sell.setVanGogh(cbvg.sellVG(sell));
		buy = new CanvasButton(gc, bw, bh, initx + bw + mgn + ncw + mgn + ncw + mgn, inity, "BUY", 9, fontSize + 7);
		buy.setVanGogh(cbvg.buyVG(buy));
		
		double h = CanvasNumberChooser.getHeightForDesiredNumberHight(bh);
		double y = bh - CanvasNumberChooser.buttonHeight(h);
		volTens = new CanvasNumberChooser(gc, ncw, h, initx + bw + mgn, y);
		volUnits = new CanvasNumberChooser(gc, ncw, h, initx + bw + mgn + ncw + mgn, y);
		volUnits.setValue(1);
		setNumberChooserColours();
		
		tradeButs = new TradeButtons();
		
		tradeButs.setOrder(new CanvasButton(gc, 0, 0, ChartNode.CHT_MARGIN + chartWidth / 2 - 100 - fontSize*2, 0, "ORDER", 9, fontSize/3));
		tradeButs.order().setVanGogh(cbvg.orderVG(tradeButs.order(), mr.trade()));
		
		tradeButs.setClose(new CanvasButton(gc, fontSize*2, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3));
		tradeButs.close().setVanGogh(cbvg.closeVG(tradeButs.close(), mr.trade()));
		
		tradeButs.setCancelTP(new CanvasButton(gc, fontSize*2, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3));
		tradeButs.cancelTP().setVanGogh(cbvg.cancelTpVG(tradeButs.cancelTP()));
		
		tradeButs.setCancelSL(new CanvasButton(gc, fontSize*2, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2, 0, "X", 9, fontSize/3));
		tradeButs.cancelSL().setVanGogh(cbvg.cancelSlVG(tradeButs.cancelSL()));
		
		tradeButs.setSL(new CanvasButton(gc, 100, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 - 100, 0, "", 5, fontSize/3));		
		tradeButs.sl().setVanGogh(cbvg.slVG(tradeButs.sl()));
		
		tradeButs.setTP(new CanvasButton(gc, 100, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 - 100, 0, "", 5, fontSize/3));
		tradeButs.tp().setVanGogh(cbvg.tpVG(tradeButs.tp()));
		
		tradeButs.setSetSL(new CanvasButton(gc, fontSize*2, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 + 10, 0, "SL", 6, fontSize/3));
		tradeButs.setSL().setVanGogh(cbvg.setSlVG(tradeButs.setSL()));
		
		tradeButs.setSetTP(new CanvasButton(gc, fontSize*2, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 + 20 + fontSize*2, 0, "TP", 6, fontSize/3));
		tradeButs.setTP().setVanGogh(cbvg.setTpVG(tradeButs.setTP()));
		
		limitOrder = new CanvasButton(gc, fontSize*2+2, fontSize, ChartNode.CHT_MARGIN + chartWidth - fontSize*2-2, 0, "LMT");
		limitOrder.setVanGogh(cbvg.pendingVG(limitOrder));
		stopOrder = new CanvasButton(gc, fontSize*2+2, fontSize, ChartNode.CHT_MARGIN + chartWidth - fontSize*4-6, 0, "STP");			
		stopOrder.setVanGogh(cbvg.pendingVG(stopOrder));
		penTrades = new ArrayList<PendingTradePair>();
	}
	
	public void disablePendingOrderButtons() {
		chart.tradeButtons().limitOrder().setY(-10); 
		chart.tradeButtons().stopOrder().setY(-10);
		chart.tradeButtons().limitOrder().disable(); 
		chart.tradeButtons().stopOrder().disable();
	}
	
	public void enablePendingOrderButtons() {
		chart.tradeButtons().limitOrder().enable(); 
		chart.tradeButtons().stopOrder().enable();
	}
	
	private void setMouseEvents() {
		MarketReplay mr = chart.marketReplay();		
		
		sell.setOnMouseClicked(e -> {
			if (mr.trade().closed()) {
				mr.setTrade(new Trade(chart.data(), chart.data().tickDataSize(true).get() - 1, false, tradeVolume()));
			} else {
				if (mr.trade().buy()) {
					mr.scaleOut(tradeVolume(), chart.data().tickDataSize(true).get() - 1);
				} else {
					mr.scaleIn(tradeVolume(), chart.data().tickDataSize(true).get() - 1);
				}
			}
		});
		
		buy.setOnMouseClicked(e -> {
			if (mr.trade().closed()) {
				mr.setTrade(new Trade(chart.data(), chart.data().tickDataSize(true).get() - 1, true, tradeVolume()));
			} else {
				if (mr.trade().buy()) {
					mr.scaleIn(tradeVolume(), chart.data().tickDataSize(true).get() - 1);
				} else {
					mr.scaleOut(tradeVolume(), chart.data().tickDataSize(true).get() - 1);
				}
			}
		});
		
		limitOrder.setOnMouseClicked(e -> {
			double currentPrice = chart.tickData().get(chart.data().tickDataSize(true).get()).price();
			double crossHairPrice = chart.roundToNearestTick(chart.yCoordToPrice(e.getY()));
			boolean buy = true;			
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = false;
				}
				chart.marketReplay().addPendingTrade(new PendingTrade(true, buy, crossHairPrice, tradeVolume()));
			}	
		});
		
		stopOrder.setOnMouseClicked(e -> {			
			double currentPrice = chart.tickData().get(chart.data().tickDataSize(true).get()).price();
			double crossHairPrice = chart.roundToNearestTick(chart.yCoordToPrice(e.getY()));
			boolean buy = false;			
			if (crossHairPrice != currentPrice) {
				if (crossHairPrice > currentPrice) {
					buy = true;
				}
				chart.marketReplay().addPendingTrade(new PendingTrade(false, buy, crossHairPrice, tradeVolume()));
			}	
		});
		
		setTradeButMouseEvents();
	}
	
	private void setTradeButMouseEvents() {
		tradeButs.close().setOnMouseClicked(e -> {
			chart.marketReplay().closeTrade(chart.data().tickDataSize(true).get());
		});
		
		tradeButs.setSL().setOnMouseDragged(e -> {
			chart.marketReplay().setUnvalidatedSlPrice(chart.roundToNearestTick(chart.yCoordToPrice(e.getY())));
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
			chart.draw();
		});
		tradeButs.setSL().setOnMouseReleased(e -> {
			if (!chart.marketReplay().trade().closed()) {
				chart.marketReplay().validateSl();
			}
		});
		
		tradeButs.setTP().setOnMouseDragged(e -> {
			chart.marketReplay().setUnvalidatedTpPrice(chart.roundToNearestTick(chart.yCoordToPrice(e.getY())));
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
			chart.draw();
		});
		tradeButs.setTP().setOnMouseReleased(e -> {
			if (!chart.marketReplay().trade().closed()) {
				chart.marketReplay().validateTp();
			}
		});
		
		tradeButs.cancelSL().setOnMouseClicked(e -> {
			chart.marketReplay().cancelSl();
		});
		
		tradeButs.cancelTP().setOnMouseClicked(e -> {
			chart.marketReplay().cancelTp();
		});
		
		tradeButs.sl().setOnMouseDragged(e -> {
			chart.marketReplay().setUnvalidatedSlPrice(chart.roundToNearestTick(chart.yCoordToPrice(e.getY())));
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
			chart.draw();
		});
		tradeButs.sl().setOnMouseReleased(e -> {
			if (!chart.marketReplay().trade().closed()) {
				chart.marketReplay().validateSl();
			} else if (penTrades.size() == 1) {
				if (chart.marketReplay().unvalidatedSlPrice().get() > penTrades.get(0).pendingTrade().price() && penTrades.get(0).pendingTrade().buy() ||
						chart.marketReplay().unvalidatedSlPrice().get() < penTrades.get(0).pendingTrade().price() && !penTrades.get(0).pendingTrade().buy()) {
					chart.marketReplay().cancelSl();
				}
			}
		});
		
		tradeButs.tp().setOnMouseDragged(e -> {
			chart.marketReplay().setUnvalidatedTpPrice(chart.roundToNearestTick(chart.yCoordToPrice(e.getY())));
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
			chart.draw();
		});
		tradeButs.tp().setOnMouseReleased(e -> {
			if (!chart.marketReplay().trade().closed()) {
				chart.marketReplay().validateTp();
			} else if (penTrades.size() == 1) {
				if (chart.marketReplay().unvalidatedTpPrice().get() < penTrades.get(0).pendingTrade().price() && penTrades.get(0).pendingTrade().buy() ||
						chart.marketReplay().unvalidatedTpPrice().get() > penTrades.get(0).pendingTrade().price() && !penTrades.get(0).pendingTrade().buy()) {
					chart.marketReplay().cancelTp();
				}
			}
		});
		
		setCrossHairStuff();
	}
	
	private void setCrossHairStuff() {
		limitOrder.setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		limitOrder.setOnMouseExited(e -> {
			chart.onMouseExited(e);
		});
		
		stopOrder.setOnMouseMoved(e -> {			
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});		
		
		tradeButs.close().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.setSL().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.setTP().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.cancelSL().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.cancelTP().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.sl().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.tp().setOnMouseMoved(e -> {
			chart.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
	}
	
	private double tradeVolume() {
		CanvasNumberChooser[] c = {volTens, volUnits};
		return CanvasNumberChooser.number(c);
	}
	
	private void addToSceneGraph() {
		Tree<ICanvasNode> sceneGraph = chart.chart().sceneGraph();
		chart.chart().varLock().lock();
		try {
			sceneGraph.addNode(new TNode<ICanvasNode>(buy, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(sell, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(volTens, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(volUnits, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(limitOrder, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(stopOrder, chart.chartNode()));
			for (CanvasLabel b : tradeButs.buttons()) {
				sceneGraph.addNode(new TNode<ICanvasNode>(b, chart.chartNode()));
			}
		} finally {
			chart.chart().varLock().unlock();
		}
	}
	
	private void setNumberChooserColours() {
		volTens.resetColours();
		volUnits.resetColours();
	}
	
	public void resetButtons() {
		double fontSize = chart.fontSize();
		double chartWidth = chart.width();
		
		tradeButs.close().setX(ChartNode.CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2);
		tradeButs.cancelTP().setX(ChartNode.CHT_MARGIN + chartWidth / 2 - 102 -fontSize*2);
		tradeButs.cancelSL().setX(ChartNode.CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2);
		tradeButs.sl().setX(ChartNode.CHT_MARGIN + chartWidth / 2 - 100);
		tradeButs.tp().setX(ChartNode.CHT_MARGIN + chartWidth / 2 - 100);
		tradeButs.setSL().setX(ChartNode.CHT_MARGIN + chartWidth / 2 + 10);
		tradeButs.setTP().setX(ChartNode.CHT_MARGIN + chartWidth / 2 + 20 + fontSize*2);
		for (PendingTradePair p : penTrades) {
			p.pendingTradeButtons().order().setX(ChartNode.CHT_MARGIN + chartWidth / 2 - 100);
			p.pendingTradeButtons().close().setX(ChartNode.CHT_MARGIN + chartWidth / 2 - 102 - fontSize*2);
			p.pendingTradeButtons().setSL().setX(ChartNode.CHT_MARGIN + chartWidth / 2 + 10);
			p.pendingTradeButtons().setTP().setX(ChartNode.CHT_MARGIN + chartWidth / 2 + 20 + fontSize*2);
		}
		limitOrder.setX(ChartNode.CHT_MARGIN + chartWidth - fontSize*2-2);
		stopOrder.setX(ChartNode.CHT_MARGIN + chartWidth - fontSize*4-6);
	}
	
	public void enableButtons() {
		tradeButs.order().enable();
		tradeButs.close().enable();
		tradeButs.cancelTP().enable();
		tradeButs.cancelSL().enable();
		tradeButs.sl().enable();
		tradeButs.tp().enable();
		tradeButs.setSL().enable();
		tradeButs.setTP().enable();
	}
	
	public void disableButtons() {
		tradeButs.order().disable();
		tradeButs.close().disable();
		tradeButs.cancelTP().disable();
		tradeButs.cancelSL().disable();
		tradeButs.sl().disable();
		tradeButs.tp().disable();
		tradeButs.setSL().disable();
		tradeButs.setTP().disable();
	}
	
	public TradeButtons buttons() {
		return tradeButs;
	}
	
	public CanvasButton limitOrder() {
		return limitOrder;
	}
	
	public CanvasButton stopOrder() {
		return stopOrder;
	}
	
	public ArrayList<PendingTradePair> pendingTradePairs() {
		return penTrades;
	}		
	
	public void addPenTradePair(PendingTradePair ptp) {
		penTrades.add(ptp);
	}
	
	public void removePenTradePair(PendingTrade penTrade) {
		ArrayList<PendingTradePair> pt = new ArrayList<PendingTradePair>();
		for (PendingTradePair ptp : penTrades) {
			if (!ptp.pendingTrade().equals(penTrade)) {
				pt.add(ptp);
			} else {
				ptp.removeFromSceneGraph();
			}
		}
		penTrades = pt;
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
	
	public void draw() {		
		buy.draw();
		sell.draw();
		volTens.draw();
		volUnits.draw();
		tradeButs.sl().draw();
		tradeButs.cancelSL().draw();		
		tradeButs.tp().draw();
		tradeButs.cancelTP().draw();
		for (int i = 0; i < penTrades.size(); i++) {
			penTrades.get(penTrades.size()-i-1).drawOrder();
		}
		tradeButs.order().draw();
		tradeButs.close().draw();
		tradeButs.setSL().draw();
		tradeButs.setTP().draw();
		for (int i = 0; i < penTrades.size(); i++) {
			penTrades.get(penTrades.size()-i-1).drawSets();
		}
		limitOrder.draw();
		stopOrder.draw();
	}
}
