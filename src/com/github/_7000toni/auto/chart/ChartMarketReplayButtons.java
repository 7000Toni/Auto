package com.github._7000toni.auto.chart;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.TextBox;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
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
	private TextBox txtVolume;
	
	private TradeButtons tradeButs;
	private CanvasButton limitOrder;
	private CanvasButton stopOrder;
	private PendingButtonsNode pbn;
	
	
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
		double bh = 25;
		double mgn = 5;
		double initx = ChartNode.CHT_MARGIN + ChartNode.INFO_MARGIN;
		double inity = 30;		
		
		sell = new CanvasButton(gc, bw, bh, initx, inity, "SELL", 9, fontSize + 7);
		sell.setVanGogh(cbvg.sellVG(sell));	
		
		txtVolume = new TextBox(chart.chart().stage(), gc, 100, bh, initx + bw + mgn, inity, "1", TextBox.InputType.INT, true);
		txtVolume.setOnKeyPressed(e -> {txtVolKeyPressedEvent();});
		txtVolume.setOnKeyTyped(e -> {txtVolKeyTypedEvent();});
		buy = new CanvasButton(gc, bw, bh, txtVolume.width() + ChartNode.CHT_MARGIN, inity, "BUY", 9, fontSize + 7);
		buy.setVanGogh(cbvg.buyVG(buy));
		
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
		pbn = new PendingButtonsNode();
	}
	
	private void txtVolKeyPressedEvent() {
		if (txtVolume.text().equals("") || tradeVolume() == 0) {
			txtVolume.setText("1");
			txtVolume.setCursorPos(1);
		}
	}
	
	private void txtVolKeyTypedEvent() {
		int val = Integer.parseInt(txtVolume.text());
		if (val > 10000000) {
			txtVolume.setText("10000000");
			txtVolume.setCursorPos(8);
		}
	}
	
	public void disablePendingOrderButtons() {		
		limitOrder.disable(); 
		stopOrder.disable();
	}
	
	public void enablePendingOrderButtons() {
		limitOrder.enable(); 
		stopOrder.enable();
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
		sell.setOnMouseMoved(e -> {
			chart.setFocusedChart(false);
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
		buy.setOnMouseMoved(e -> {
			chart.setFocusedChart(false);
		});
		
		limitOrder.setOnMouseClicked(e -> {
			double currentPrice = chart.data().tickData().get(chart.data().tickDataSize(true).get() - 1).price();
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
			double currentPrice = chart.data().tickData().get(chart.data().tickDataSize(true).get() - 1).price();
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
			chart.marketReplay().closeTrade(chart.data().tickDataSize(true).get() - 1);
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
			} else if (pbn.tradePairs().size() == 1) {
				if (chart.marketReplay().unvalidatedSlPrice().get() > pbn.tradePairs().getFirst().pendingTrade().price() && pbn.tradePairs().getFirst().pendingTrade().buy() ||
						chart.marketReplay().unvalidatedSlPrice().get() < pbn.tradePairs().getFirst().pendingTrade().price() && !pbn.tradePairs().getFirst().pendingTrade().buy()) {
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
			} else if (pbn.tradePairs().size() == 1) {				
				if (chart.marketReplay().unvalidatedTpPrice().get() < pbn.tradePairs().getFirst().pendingTrade().price() && pbn.tradePairs().getFirst().pendingTrade().buy() ||
						chart.marketReplay().unvalidatedTpPrice().get() > pbn.tradePairs().getFirst().pendingTrade().price() && !pbn.tradePairs().getFirst().pendingTrade().buy()) {
					chart.marketReplay().cancelTp();
				}
			}
		});
		
		setCrossHairStuff();
	}
	
	private void setCrossHairStuff() {
		limitOrder.setOnMouseMoved(e -> {
			setCrossHairVars(e.getX(), e.getY());
		});
		limitOrder.setOnMouseExited(e -> {
			if (e.getX() > chart.width() + ChartNode.CHT_MARGIN) {
				chart.onMouseExited(e);
			}
		});
		
		stopOrder.setOnMouseMoved(e -> {	
			setCrossHairVars(e.getX(), e.getY());
		});	
		
		tradeButs.close().setOnMouseMoved(e -> {
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.setSL().setOnMouseMoved(e -> {
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.setTP().setOnMouseMoved(e -> {
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.cancelSL().setOnMouseMoved(e -> {
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.cancelTP().setOnMouseMoved(e -> {
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.sl().setOnMouseMoved(e -> {
			setCrossHairVars(e.getX(), e.getY());
		});
		
		tradeButs.tp().setOnMouseMoved(e -> {
			setCrossHairVars(e.getX(), e.getY());
		});
	}
	
	private int tradeVolume() {
		return Integer.parseInt(txtVolume.text());
	}
	
	private void addToSceneGraph() {
		Tree<ICanvasNode> sceneGraph = chart.chart().sceneGraph();
		chart.chart().varLock().lock();
		try {
			sceneGraph.addNode(new TNode<ICanvasNode>(pbn, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(sell, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(txtVolume, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(buy, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(limitOrder, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(stopOrder, chart.chartNode()));
			for (CanvasLabel b : tradeButs.buttons()) {
				sceneGraph.addNode(new TNode<ICanvasNode>(b, chart.chartNode()));
			}
		} finally {
			chart.chart().varLock().unlock();
		}
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
		for (PendingTradePair p : pbn.tradePairs()) {
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
	
	public PendingButtonsNode pendingButtonsNode() {
		return pbn;
	}
	
	public void addPenTradePair(PendingTradePair ptp) {
		pbn.addPair(ptp);
	}
	
	public void removePenTradePair(PendingTrade p) {
		pbn.removePair(p);
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
		sell.draw();
		txtVolume.draw();
		buy.setX(ChartNode.CHT_MARGIN + txtVolume.x() + txtVolume.width());
		buy.draw();
		/*volTens.draw();
		volUnits.draw();*/	
		pbn.draw();		
		tradeButs.sl().draw();
		tradeButs.cancelSL().draw();		
		tradeButs.tp().draw();
		tradeButs.cancelTP().draw();
		tradeButs.order().draw();
		tradeButs.close().draw();
		tradeButs.setSL().draw();
		tradeButs.setTP().draw();
		limitOrder.draw();
		stopOrder.draw();
	}
}
