package com.github._7000toni.auto.chart;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.marketreplay.MarketReplay;
import com.github._7000toni.auto.marketreplay.trade.ITrade;
import com.github._7000toni.auto.marketreplay.trade.PendingTrade;
import com.github._7000toni.auto.marketreplay.trade.Trade;
import com.github._7000toni.auto.marketreplay.trade.TradeButtons;
import com.github._7000toni.auto.settings.ColourSettings;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ChartButtonVanGoghs {
	private ChartNode c;
	
	public ChartButtonVanGoghs(ChartNode c) {
		this.c = c;
	}
	
	public IVanGogh menuButtonVG(CanvasButton menu) {
		return (x, y, gc) -> {
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}			
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
			if (menu.hover()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.GRAY);
				gc.fillRect(x, y, menu.width(), menu.height());
			}
			if (menu.pressed()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.DIMGRAY);
				gc.fillRect(x, y, menu.width(), menu.height());
			}
			if (!menu.enabled()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.LIGHTGRAY);
				gc.fillRect(x, y, menu.width(), menu.height());
			}
			gc.strokeText(menu.text(), menu.x() + menu.textXOffset(), menu.y() + menu.textYOffset());
		};
	}
	
	public IVanGogh buyVG(CanvasButton buy) {  
		return (x, y, gc) -> {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.DODGERBLUE);
			if (buy.hover()) {
				gc.setFill(Color.STEELBLUE);
			}
			if (buy.pressed()) {
				gc.setFill(Color.DARKBLUE);
			}
			if (!buy.enabled()) {
				gc.setFill(Color.LIGHTGRAY);
			}
			gc.fillRect(x, y, buy.width(), buy.height());
			gc.strokeText(buy.text(), x + buy.textXOffset(), y + buy.textYOffset());
		};
	}
	
	public IVanGogh sellVG(CanvasButton sell) { 
		return (x, y, gc) -> {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.ORANGERED);
			if (sell.hover()) {
				gc.setFill(Color.INDIANRED);
			}
			if (sell.pressed()) {
				gc.setFill(Color.DARKRED);
			}
			if (!sell.enabled()) {
				gc.setFill(Color.LIGHTGRAY);
			}
			gc.fillRect(x, y, sell.width(), sell.height());
			gc.strokeText(sell.text(), x + sell.textXOffset(), y + sell.textYOffset());
		};
	}
	
	private boolean preDrawClose() {
		double fontSize = c.fontSize();
		MarketReplay mr = c.marketReplay();
		TradeButtons tradeButs = c.tradeButtons().buttons();
		
		double entryY = c.priceToYCoord(c.roundToNearestTick(mr.trade().entryPrice()));
		
		if (c.onChart(ChartNode.CHT_MARGIN + 1, entryY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, entryY - fontSize - 3, false) && !c.marketReplay().trade().closed()) {
			tradeButs.close().setY(entryY - fontSize);
			return true;
		}
		return false;
	}
	
	public IVanGogh closeVG(CanvasButton close, ITrade trade) {
		return (x, y, gc) -> {			
			boolean draw = preDrawClose();
			if (!draw) {
				return;
			}
			Color textColour;
			Color boxColour;
			if (trade.buy()) {
				textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			} else {
				textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			}
			if (close.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (close.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!close.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(close.x(), close.y(), close.width(), c.fontSize() * 2, close.textXOffset(), close.text(), textColour, boxColour);
		};
	}
	
	private boolean preDrawPenClose(CanvasButton close, PendingTrade trade) {
		double fontSize = c.fontSize();
		
		double entryY = c.priceToYCoord(c.roundToNearestTick(trade.price()));
		if (c.onChart(ChartNode.CHT_MARGIN + 1, entryY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, entryY - fontSize - 3, false)) {
			close.enable();
			close.setY(entryY - fontSize);
			return true;
		}
		close.disable();
		return false;
	}
	
	public IVanGogh penCloseVG(CanvasButton close, PendingTrade trade) {
		return (x, y, gc) -> {			
			boolean draw = preDrawPenClose(close, trade);
			if (!draw) {
				return;
			}
			Color textColour;
			Color boxColour;
			if (trade.buy()) {
				textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			} else {
				textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			}
			if (close.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (close.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!close.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(close.x(), close.y(), close.width(), c.fontSize() * 2, close.textXOffset(), close.text(), textColour, boxColour);
		};
	}
	
	private boolean preDrawCancelTP() {
		double fontSize = c.fontSize();
		MarketReplay mr = c.marketReplay();
		TradeButtons tradeButs = c.tradeButtons().buttons(); 
		
		double tpY = c.priceToYCoord(mr.unvalidatedTpPrice().get());	
		if (mr.trade().closed()) {	
			if (mr.pendingTrades().size() == 0) {
				tradeButs.cancelTP().disable();
				return false;
			}
			if (c.onChart(ChartNode.CHT_MARGIN + 1, tpY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, tpY - fontSize - 3, false)) {	
				tradeButs.cancelTP().enable();
				tradeButs.cancelTP().setY(tpY - fontSize);	
				return true;
			} else {
				tradeButs.cancelTP().disable();
				return false;
			}
		} else {
			if (c.onChart(ChartNode.CHT_MARGIN + 1, tpY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, tpY - fontSize - 3, false)) {
				tradeButs.cancelTP().enable();
				tradeButs.cancelTP().setY(tpY - fontSize);
				return true;
			}
			tradeButs.cancelTP().disable();
			return false;
		}
	}
	
	public IVanGogh cancelTpVG(CanvasButton cancelTP) {
		return (x, y, gc) -> {
			boolean draw = preDrawCancelTP();
			if (!draw) {
				return;
			}
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			if (cancelTP.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (cancelTP.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!cancelTP.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(cancelTP.x(), cancelTP.y(), cancelTP.width(), c.fontSize() * 2, cancelTP.textXOffset(), cancelTP.text(), textColour, boxColour);
		};
	}	
	
	private boolean preDrawCancelSL() {
		double fontSize = c.fontSize();
		MarketReplay mr = c.marketReplay();
		TradeButtons tradeButs = c.tradeButtons().buttons(); 
		
		double slY = c.priceToYCoord(mr.unvalidatedSlPrice().get());	
		if (mr.trade().closed()) {
			if (mr.pendingTrades().size() == 0) {
				tradeButs.cancelSL().disable();
				return false;
			}
			if (c.onChart(ChartNode.CHT_MARGIN + 1, slY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, slY - fontSize - 3, false)) {	
				tradeButs.cancelSL().enable();
				tradeButs.cancelSL().setY(slY - fontSize);
				return true;
			} else {
				tradeButs.cancelSL().disable();
				return false;
			}
		} else {
			if (c.onChart(ChartNode.CHT_MARGIN + 1, slY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, slY - fontSize - 3, false)) {
				tradeButs.cancelSL().enable();
				tradeButs.cancelSL().setY(slY - fontSize);
				return true;
			}
			tradeButs.cancelSL().disable();
			return false;
		}
	}
	
	public IVanGogh cancelSlVG(CanvasButton cancelSL) {
		return (x, y, gc) -> {
			boolean draw = preDrawCancelSL();
			if (!draw) {
				return;
			}			
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			if (cancelSL.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (cancelSL.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!cancelSL.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(cancelSL.x(), cancelSL.y(), cancelSL.width(), c.fontSize() * 2, cancelSL.textXOffset(), cancelSL.text(), textColour, boxColour);
		};
	}
	
	private boolean preDrawSL() {
		double fontSize = c.fontSize();
		GraphicsContext gc = c.graphicsContext();
		double chartWidth = c.width();
		MarketReplay mr = c.marketReplay();
		TradeButtons tradeButs = c.tradeButtons().buttons(); 
		
		double x1 = ChartNode.CHT_MARGIN + chartWidth / 2;
		double x2 = ChartNode.CHT_MARGIN + chartWidth;
		double slY = c.priceToYCoord(mr.unvalidatedSlPrice().get());	
		if (mr.unvalidatedSlPrice().get() == -1) {
			tradeButs.sl().disable();
			return false;
		} else if (mr.trade().closed()) {		
			if (mr.pendingTrades().size() == 0) {
				tradeButs.sl().disable();
				return false;
			} else if (c.onChart(ChartNode.CHT_MARGIN + 1, slY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, slY - fontSize - 3, false)) {				
				tradeButs.sl().enable();
				tradeButs.sl().setY(slY - fontSize);
				gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				gc.strokeLine(x1, slY, x2, slY);
				drawPriceBox(slY, mr.unvalidatedSlPrice().get(), Color.WHITE, ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				return true;
			} else {
				tradeButs.sl().disable();
				return false;
			}
		} else {
			if (c.onChart(ChartNode.CHT_MARGIN + 1, slY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, slY - fontSize - 3, false)) {
				tradeButs.sl().enable();
				gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				gc.strokeLine(x1, slY, x2, slY);
				drawPriceBox(slY, mr.unvalidatedSlPrice().get(), Color.WHITE, ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
				tradeButs.sl().setY(slY - fontSize);
				return true;
			}
			tradeButs.sl().disable();
			return false;
		}
	}
	
	public IVanGogh slVG(CanvasButton sl) {
		return (x, y, gc) -> {
			boolean draw = preDrawSL();
			if (!draw) {
				return;
			}
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			if (sl.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (sl.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!sl.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			if (c.marketReplay().trade().closed()) {
				if (c.marketReplay().pendingTrades().size() == 1) {
					PendingTrade p = c.marketReplay().pendingTrades().get(0);
					c.tradeButtons().buttons().sl().setText(p.volume() + "  $" + Trade.hypotheticalProfit2(p.price(), c.marketReplay().unvalidatedSlPrice().get(), p.buy(), p.volume()));
				} else {
					c.tradeButtons().buttons().sl().setText("SL");
				}
			} else {
				c.tradeButtons().buttons().sl().setText(c.marketReplay().trade().volume() + "  $" + c.marketReplay().trade().hypotheticalProfit(c.marketReplay().unvalidatedSlPrice().get()));
			}	
			drawTradeBox(sl.x(), sl.y(), sl.width(), 90, sl.textXOffset(), sl.text(), textColour, boxColour);
		};
	}
	
	private boolean preDrawTP() {
		double fontSize = c.fontSize();
		GraphicsContext gc = c.graphicsContext();
		double chartWidth = c.width();
		MarketReplay mr = c.marketReplay();
		TradeButtons tradeButs = c.tradeButtons().buttons(); 
		
		double x1 = ChartNode.CHT_MARGIN + chartWidth / 2;
		double x2 = ChartNode.CHT_MARGIN + chartWidth;
		double tpY = c.priceToYCoord(mr.unvalidatedTpPrice().get());
		if (mr.unvalidatedTpPrice().get() == -1) {
			tradeButs.tp().disable();
			return false;
		} else if (mr.trade().closed()) {		
			if (mr.pendingTrades().size() == 0) {
				tradeButs.tp().disable();
				return false;
			} else if (c.onChart(ChartNode.CHT_MARGIN + 1, tpY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, tpY - fontSize - 3, false)) {				
				tradeButs.tp().enable();
				tradeButs.tp().setY(tpY - fontSize);
				gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
				gc.strokeLine(x1, tpY, x2, tpY);
				drawPriceBox(tpY, mr.unvalidatedTpPrice().get(), Color.WHITE, ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
				return true;
			} else {
				tradeButs.tp().disable();
				return false;
			}
		} else {			
			if (c.onChart(ChartNode.CHT_MARGIN + 1, tpY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, tpY - fontSize - 3, false)) {
				tradeButs.tp().enable();
				gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
				gc.strokeLine(x1, tpY, x2, tpY);
				drawPriceBox(tpY, mr.unvalidatedTpPrice().get(), Color.WHITE, ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
				tradeButs.tp().setY(tpY - fontSize);
				return true;
			}
			tradeButs.tp().disable();
			return false;			
		}
	}
	
	public IVanGogh tpVG(CanvasButton tp) {
		return (x, y, gc) -> {
			boolean draw = preDrawTP();
			if (!draw) {
				return;
			}
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			if (tp.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (tp.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!tp.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			if (c.marketReplay().trade().closed()) {
				if (c.marketReplay().pendingTrades().size() == 1) {
					PendingTrade p = c.marketReplay().pendingTrades().get(0);
					c.tradeButtons().buttons().tp().setText(p.volume() + "  $" + Trade.hypotheticalProfit2(p.price(), c.marketReplay().unvalidatedTpPrice().get(), p.buy(), p.volume()));
				} else {
					c.tradeButtons().buttons().tp().setText("TP");
				}
			} else {
				c.tradeButtons().buttons().tp().setText(c.marketReplay().trade().volume() + "  $" + c.marketReplay().trade().hypotheticalProfit(c.marketReplay().unvalidatedTpPrice().get()));
			}				
			drawTradeBox(tp.x(), tp.y(), tp.width(), 90, tp.textXOffset(), tp.text(), textColour, boxColour);
		};
	}
	
	public boolean preDrawSetSL() {
		double fontSize = c.fontSize();
		TradeButtons tradeButs = c.tradeButtons().buttons(); 
		double entryY = c.priceToYCoord(c.roundToNearestTick(c.marketReplay().trade().entryPrice()));
		
		if (c.onChart(ChartNode.CHT_MARGIN + 1, entryY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, entryY - fontSize - 3, false) && !c.marketReplay().trade().closed()) {
			tradeButs.setSL().setY(entryY - fontSize);
			return true;
		}
		return false;
	}
	
	public IVanGogh setSlVG(CanvasButton setSL) {
		return (x, y, gc) -> {
			boolean draw = preDrawSetSL();
			if (!draw) {
				return;
			}
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			if (setSL.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (setSL.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!setSL.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(setSL.x(), setSL.y(), setSL.width(), c.fontSize() * 2, setSL.textXOffset(), setSL.text(), textColour, boxColour);
		};
	}	
	
	public boolean preDrawSetTP() {
		double fontSize = c.fontSize();
		TradeButtons tradeButs = c.tradeButtons().buttons(); 
		double entryY = c.priceToYCoord(c.roundToNearestTick(c.marketReplay().trade().entryPrice()));
		
		if (c.onChart(ChartNode.CHT_MARGIN + 1, entryY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, entryY - fontSize - 3, false) && !c.marketReplay().trade().closed()) {
			tradeButs.setTP().setY(entryY - fontSize);			
			return true;
		}
		return false;
	}
	
	public IVanGogh setTpVG(CanvasButton setTP) {
		return (x, y, gc) -> {
			boolean draw = preDrawSetTP();
			if (!draw) {
				return;
			}
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			if (setTP.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (setTP.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!setTP.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(setTP.x(), setTP.y(), setTP.width(), c.fontSize() * 2, setTP.textXOffset(), setTP.text(), textColour, boxColour);
		};
	}
	
	public boolean preDrawPenSetSL(CanvasButton setSL, PendingTrade trade) {
		double fontSize = c.fontSize();
		double entryY = c.priceToYCoord(c.roundToNearestTick(trade.price()));
		
		if (c.onChart(ChartNode.CHT_MARGIN + 1, entryY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, entryY - fontSize - 3, false) && c.marketReplay().trade().closed()) {
			setSL.enable();
			setSL.setY(entryY - fontSize);
			return true;
		}
		setSL.disable();
		return false;
	}
	
	public IVanGogh penSetSlVG(CanvasButton setSL, PendingTrade trade) {
		return (x, y, gc) -> {
			boolean draw = preDrawPenSetSL(setSL, trade);
			if (!draw) {
				return;
			}
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			if (setSL.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (setSL.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!setSL.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(setSL.x(), setSL.y(), setSL.width(), c.fontSize() * 2, setSL.textXOffset(), setSL.text(), textColour, boxColour);
		};
	}	
	
	public boolean preDrawPenSetTP(CanvasButton setTP, PendingTrade trade) {
		double fontSize = c.fontSize();
		double entryY = c.priceToYCoord(c.roundToNearestTick(trade.price()));
		
		if (c.onChart(ChartNode.CHT_MARGIN + 1, entryY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, entryY - fontSize - 3, false) && c.marketReplay().trade().closed()) {
			setTP.enable();
			setTP.setY(entryY - fontSize);			
			return true;
		}
		setTP.disable();
		return false;
	}
	
	public IVanGogh penSetTpVG(CanvasButton setTP, PendingTrade trade) {
		return (x, y, gc) -> {
			boolean draw = preDrawPenSetTP(setTP, trade);
			if (!draw) {
				return;
			}
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			if (setTP.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (setTP.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!setTP.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			drawTradeBox(setTP.x(), setTP.y(), setTP.width(), c.fontSize() * 2, setTP.textXOffset(), setTP.text(), textColour, boxColour);
		};
	}
	
	private boolean preDrawOrder() {
		if (c.marketReplay().trade().closed()) {
			return false;
		}
		
		double fontSize = c.fontSize();
		GraphicsContext gc = c.graphicsContext();
		double chartWidth = c.width();
		MarketReplay mr = c.marketReplay();
		
		double x1 = ChartNode.CHT_MARGIN + chartWidth / 2;
		double x2 = ChartNode.CHT_MARGIN + chartWidth;
		double entryY = c.priceToYCoord(c.roundToNearestTick(mr.trade().entryPrice()));
		
		if (c.onChart(ChartNode.CHT_MARGIN + 1, entryY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, entryY - fontSize - 3, false)) {
			Color boxColour;
			if (mr.trade().buy()) {
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
				gc.setStroke(boxColour);
			} else {
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
				gc.setStroke(boxColour);
			}
			gc.strokeLine(x1, entryY, x2, entryY);
			drawPriceBox(entryY, mr.trade().entryPrice(), Color.WHITE, boxColour);
			return true;
		}
		return false;
	}
	
	public IVanGogh orderVG(CanvasButton order, Trade trade) {
		return (x, y, gc) -> {			
			boolean draw = preDrawOrder();
			if (!draw) {
				return;
			}
			Color textColour = Color.GRAY;
			Color boxColour = Color.GRAY;			
			if (!order.enabled()) {
				boxColour = Color.LIGHTGRAY;
			}
			if (trade.buy()) {
				textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			} else {
				textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			}
			double x1 = ChartNode.CHT_MARGIN + c.width() / 2;
			double entryY = c.priceToYCoord(c.roundToNearestTick(c.marketReplay().trade().entryPrice()));
			order.setX(x1 - 100);
			order.setY(entryY - c.fontSize());
			drawTradeBox(x1 - 100, entryY - c.fontSize(), 100, 90, 5, ((Double)(c.marketReplay().trade().volume())).toString() + "  $" + ((Double)(c.marketReplay().trade().profit())).toString(), textColour, boxColour);
		};
	}
	
	public boolean preDrawPenOrder(CanvasButton order, PendingTrade trade) {
		double fontSize = c.fontSize();
		GraphicsContext gc = c.graphicsContext();
		double chartWidth = c.width();
		
		double x1 = ChartNode.CHT_MARGIN + chartWidth / 2;
		double x2 = ChartNode.CHT_MARGIN + chartWidth;
		double entryY = c.priceToYCoord(c.roundToNearestTick(trade.price()));
		
		if (c.onChart(ChartNode.CHT_MARGIN + 1, entryY + fontSize + 3, false) && c.onChart(ChartNode.CHT_MARGIN + 1, entryY - fontSize - 3, false)) {
			Color boxColour;
			if (trade.buy()) {
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
				gc.setStroke(boxColour);
			} else {
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
				gc.setStroke(boxColour);
			}
			gc.strokeLine(x1, entryY, x2, entryY);
			drawPriceBox(entryY, trade.price(), Color.WHITE, boxColour);
			order.enable();
			return true;
		}
		order.disable();
		return false;
	}
	
	public IVanGogh penOrderVG(CanvasButton order, PendingTrade trade) {
		return (x, y, gc) -> {
			boolean draw = preDrawPenOrder(order, trade);
			if (!draw) {
				return;
			}
			Color textColour = Color.GRAY;
			Color boxColour = Color.GRAY;
			
			if (trade.buy()) {
				textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1);
			} else {
				textColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2);
			}
			
			if (order.hover()) {
				textColour = Color.GRAY;
				boxColour = Color.GRAY;
			}
			if (order.pressed()) {
				textColour = Color.DIMGRAY;
				boxColour = Color.DIMGRAY;
			}
			if (!order.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			} 
			
			double x1 = ChartNode.CHT_MARGIN + c.width() / 2;
			double entryY = c.priceToYCoord(c.roundToNearestTick(trade.price()));
			String text = trade.volume() + "  ";
			if (trade.limit()) {
				text += "LIMIT";
			} else {
				text += "STOP";
			}
			order.setX(x1 - 100);
			order.setY(entryY - c.fontSize());
			drawTradeBox(x1 - 100, entryY - c.fontSize(), 100, 90, 5, text, textColour, boxColour);
		};
	}
	
	public IVanGogh pendingVG(CanvasButton btn) {		
		return (x, y, gc) -> {
			if (!c.onChart(x, y, false) || y < 25) {
				btn.disable(); 
				return;
			}
			btn.calculateOffsets(gc.getFont());
			if (Chart.darkMode().get()) {
				gc.setFill(Color.WHITE);
			} else {
				gc.setFill(Color.BLACK);
			}
			gc.fillRect(x, y, btn.width()+2, btn.height());
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.BLACK);
			} else {
				gc.setStroke(Color.WHITE);
			}	
			if (btn.hover()) {
				if (c.yCoordToPrice(y + gc.getFont().getSize() / 3) > c.data().tickData().get(c.data().tickDataSize(c.replayMode()).get() - 1).price()) {
					if (btn.text().contains("STP")) {
						gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
					} else {
						gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
					}
				} else {
					if (btn.text().contains("STP")) {
						gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
					} else {
						gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
					}					
				}	
			}
			if (btn.pressed()) {
				gc.setStroke(Color.DIMGRAY);
			}
			if (!btn.enabled()) {
				gc.setStroke(Color.LIGHTGRAY);
			}
			gc.strokeText(btn.text(), x + btn.textXOffset(), y + btn.textYOffset());
		};
	}
	
	public void drawPriceBox(double yPos, double price, Color textColour, Color boxColour) {
		double fontSize = c.fontSize();
		GraphicsContext gc = c.graphicsContext();
		
		gc.setStroke(textColour);
		gc.setFill(boxColour);
		gc.fillRect(c.width() + ChartNode.CHT_MARGIN, yPos - fontSize/2, c.chart().priceMargin().width(), fontSize);
		gc.strokeText(((Double)(c.roundToNearestTick(price))).toString(), c.width() + ChartNode.CHT_MARGIN + PriceMargin.PRICE_DASH_MARGIN, yPos + fontSize/3, c.chart().priceMargin().width() - PriceMargin.PRICE_DASH_SIZE - PriceMargin.PRICE_DASH_MARGIN);
	}
	
	public void drawTradeBox(double xPos, double yPos, double width, double textMaxWidth, double textMargin, String text, Color textColour, Color boxColour) {
		double fontSize = c.fontSize();
		GraphicsContext gc = c.graphicsContext();
		
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
		gc.fillRect(xPos, yPos, width, fontSize * 2);
		gc.setStroke(boxColour);
		gc.strokeRect(xPos, yPos, width, fontSize * 2);
		gc.setStroke(textColour);	
		gc.strokeText(text, xPos + textMargin, yPos + 4*fontSize/3, textMaxWidth);
	}
}
