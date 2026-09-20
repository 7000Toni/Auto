package com.github._7000toni.auto.chart;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.NodeIsland;
import com.github._7000toni.auto.canvasnode.TextBox;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.marketreplay.MarketReplay;
import com.github._7000toni.auto.marketreplay.trade.PendingTrade;
import com.github._7000toni.auto.marketreplay.trade.PendingTradePair;
import com.github._7000toni.auto.marketreplay.trade.Trade;
import com.github._7000toni.auto.marketreplay.trade.TradeButtons;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ChartMarketReplayButtons {	
	private ChartNode chart;
	private CanvasButton buy;
	private CanvasButton sell;
	private TextBox txtVolume;
	private NodeIsland tradeSizeCalc;
	private TextBox txtRisk;
	private boolean tscVisible = false;
	private BooleanProperty measuring = new SimpleBooleanProperty(false);
	
	private TradeButtons tradeButs;
	private CanvasButton limitOrder;
	private CanvasButton stopOrder;
	private PendingButtonsNode pbn;
	
	public ChartMarketReplayButtons(ChartNode chart, MarketReplay mr, ChartButtonVanGoghs cbvg) {
		this.chart = chart;		
		init(mr, cbvg);
		addToSceneGraph();
		setMouseEvents();
		resetButtons();
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
		
		txtVolume = new TextBox(chart.chart().stage(), gc, 100, bh, initx + bw + mgn, inity, "1", TextBox.InputType.ABS_INT, true, false, false);
		txtVolume.setOnKeyTyped(e -> {txtVolKeyTypedEvent();});
		initTradeSizeCalc(cbvg);
		txtVolume.setOnMouseReleased(e -> {txtVolRightClickEvent(e);});
		
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
		
		tradeButs.setSetSL(new CanvasButton(gc, fontSize*2, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 + 60, 0, "SL", 6, fontSize/3));
		tradeButs.setSL().setVanGogh(cbvg.setSlVG(tradeButs.setSL()));
		
		tradeButs.setSetTP(new CanvasButton(gc, fontSize*2, fontSize*2, ChartNode.CHT_MARGIN + chartWidth / 2 + 70 + fontSize*2, 0, "TP", 6, fontSize/3));
		tradeButs.setTP().setVanGogh(cbvg.setTpVG(tradeButs.setTP()));
		
		limitOrder = new CanvasButton(gc, fontSize*2+2, fontSize, ChartNode.CHT_MARGIN + chartWidth - fontSize*2-2, 0, "LMT");
		limitOrder.setVanGogh(cbvg.pendingVG(limitOrder));
		stopOrder = new CanvasButton(gc, fontSize*2+2, fontSize, ChartNode.CHT_MARGIN + chartWidth - fontSize*4-6, 0, "STP");			
		stopOrder.setVanGogh(cbvg.pendingVG(stopOrder));
		pbn = new PendingButtonsNode();
	}
	
	private void txtVolKeyTypedEvent() {
		if (txtVolume.text().equals("") || txtVolume.text().equals("0")) {
			return;
		}
		int val = Integer.parseInt(txtVolume.text());
		if (val > 10000000) {
			txtVolume.setText("10000000");
		}
	}
	
	private void initTradeSizeCalc(ChartButtonVanGoghs cbvg) {
		tradeSizeCalc = new NodeIsland(chart.graphicsContext(), "TradeSizeCalc", txtVolume.x(), txtVolume.y() + txtVolume.height() + ChartNode.CHT_MARGIN, 400, true, chart.chart().sceneGraph(), chart.chartNode(), null);
		tradeSizeCalc.setPermanentlyLocked(true);
		NodeIsland risk = new NodeIsland(chart.graphicsContext(), null, 0, 0, 400, false, chart.chart().sceneGraph(), tradeSizeCalc.nodeIslandNode(), null);
		risk.setBorderMargin(0);
		risk.setPermanentlyLocked(true);
		risk.setDrawBorder(false);
		CanvasLabel lblRisk = new CanvasLabel(chart.graphicsContext(), 35, 20, txtVolume.x(), txtVolume.y(), "Risk: ");
		lblRisk.setVanGogh((x, y, gc) -> {
			lblRisk.simpleDefaultDraw(gc.getFont());
		});
		txtRisk = new TextBox(chart.chart().stage(), chart.graphicsContext(), 75, 20, 0, 0, null, TextBox.InputType.ABS_INT, false, true, false);		
		risk.addNode(lblRisk);
		risk.addNode(txtRisk);
		tradeSizeCalc.addNode(risk);
		CanvasButton measure = new CanvasButton(chart.graphicsContext(), lblRisk.width() + txtRisk.width() + risk.nodeMargin(), 20, 0, 0, "MEASURE");				
		measure.setVanGogh(cbvg.toggleVG(measure, measuring, "MEASURING...", "MEASURE"));
		measure.setOnMouseClicked(e -> {			
			if (measuring.get()) {
				measuring.set(false);
				chart.setMeasuringRisk(false);
			} else {
				measuring.set(true);
				chart.setMeasuringRisk(true);
				if (!chart.mr().paused().get()) {
					chart.mr().togglePause();
				}
			}
		});
		measure.disable();
		tradeSizeCalc.addNode(measure);
		txtRisk.setOnKeyTyped(e -> {
			if (txtRisk.text().equals("") || txtRisk.text().equals("0")) {
				measure.disable();
				return;
			}
			measure.enable();
			int val = Integer.parseInt(txtRisk.text());
			if (val > 99999999) {
				txtRisk.setText("99999999");
			}
		});
		chart.chart().sceneGraph().removeNode(tradeSizeCalc.nodeIslandNode());
	}
	
	private void txtVolRightClickEvent(MouseEvent e) {
		if (e.getButton() == MouseButton.SECONDARY) {
			if (!tscVisible) {
				chart.chart().sceneGraph().addNode(tradeSizeCalc.nodeIslandNode());
				tscVisible = true;
			}
		}
	}
	
	public void hideTradeSizeCalc() {
		if (tscVisible) {
			chart.chart().sceneGraph().removeNode(tradeSizeCalc.nodeIslandNode());
			tscVisible = false;
		}
	}
	
	public void measuringComplete(double measuredRisk) {
		measuring.set(false);
		chart.setMeasuringRisk(false);
		hideTradeSizeCalc();
		if (measuredRisk == 0) {
			return;
		}
		int risk = Integer.parseInt(txtRisk.text());
		int volume = (int)(risk/measuredRisk);
		if (volume == 0) {
			txtVolume.setText("1");
		} else if (volume <= 10000000) {
			txtVolume.setText(((Integer)volume).toString());
		} else {
			txtVolume.setText("10000000");
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
			if (txtVolume.text().equals("") || txtVolume.text().equals("0")) {
				txtVolume.setText("1");
				return;
			}
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
			if (txtVolume.text().equals("") || txtVolume.text().equals("0")) {
				txtVolume.setText("1");
				return;
			}
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
			if (txtVolume.text().equals("") || txtVolume.text().equals("0")) {
				txtVolume.setText("1");
				return;
			}
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
			if (txtVolume.text().equals("") || txtVolume.text().equals("0")) {
				txtVolume.setText("1");
				return;
			}
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
			sceneGraph.addNode(new TNode<ICanvasNode>(sell, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(txtVolume, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(buy, chart.chartNode()));
			sceneGraph.addNode(new TNode<ICanvasNode>(pbn, chart.chartNode()));
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
		chart.chartButtonVanGoghs().recalculateOffset();
		double offset = chart.chartButtonVanGoghs().offset();
		
		tradeButs.close().setX(offset);
		tradeButs.cancelTP().setX(offset);
		tradeButs.cancelSL().setX(offset);
		tradeButs.sl().setX(offset + fontSize*2 + 2);
		tradeButs.tp().setX(offset + fontSize*2 + 2);
		tradeButs.setSL().setX(offset + fontSize*2 + 162);
		tradeButs.setTP().setX(offset + fontSize*2 + 162 + fontSize*2 + 5);
		for (PendingTradePair p : pbn.tradePairs()) {
			p.pendingTradeButtons().order().setX(offset + fontSize*2 + 2);
			p.pendingTradeButtons().close().setX(offset);
			p.pendingTradeButtons().setSL().setX(offset + fontSize*2 + 112);
			p.pendingTradeButtons().setTP().setX(offset + fontSize*2 + 112 + fontSize*2 + 5);
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
	
	public boolean tscVisible() {
		return tscVisible;
	}
	
	public void setTSCVisible(boolean tscVisible) {
		this.tscVisible = tscVisible;
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
		pbn.draw();		
		tradeButs.sl().draw();
		tradeButs.cancelSL().draw();		
		tradeButs.tp().draw();
		tradeButs.cancelTP().draw();
		tradeButs.order().draw();
		tradeButs.close().draw();
		tradeButs.setSL().draw();
		tradeButs.setTP().draw();
		sell.draw();
		txtVolume.draw();
		buy.setX(ChartNode.CHT_MARGIN + txtVolume.x() + txtVolume.width());
		buy.draw();
		if (ChartNode.drawCrosshair().get()) {
			limitOrder.draw();
			stopOrder.draw();
		} else {
			limitOrder.disable();
			stopOrder.disable();
		}
		if (tscVisible) {
			tradeSizeCalc.draw();
		}
	}
}
