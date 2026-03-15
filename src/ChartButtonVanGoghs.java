import javafx.scene.paint.Color;

public class ChartButtonVanGoghs {
	private Chart c;
	
	public ChartButtonVanGoghs(Chart c) {
		this.c = c;
	}
	
	public IVanGogh menuButtonVG(CanvasButton menu) {
		return (x, y, gc) -> {
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
	
	public IVanGogh closeVG(CanvasButton close, ITrade trade) {
		return (x, y, gc) -> {
			Color textColour;
			Color boxColour;
			if (trade.buy()) {
				textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
			} else {
				textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
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
			c.drawTradeBox(x, y, close.width(), c.fontSize() * 2, close.textXOffset(), close.text(), textColour, boxColour);
		};
	}
	
	public IVanGogh cancelTpVG(CanvasButton cancelTP) {
		return (x, y, gc) -> {
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
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
			c.drawTradeBox(x, y, cancelTP.width(), c.fontSize() * 2, cancelTP.textXOffset(), cancelTP.text(), textColour, boxColour);
		};
	}
	
	public IVanGogh cancelSlVG(CanvasButton cancelSL) {
		return (x, y, gc) -> {
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
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
			c.drawTradeBox(x, y, cancelSL.width(), c.fontSize() * 2, cancelSL.textXOffset(), cancelSL.text(), textColour, boxColour);
		};
	}
	
	public IVanGogh slVG(CanvasButton sl) {
		return (x, y, gc) -> {
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
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
			c.drawTradeBox(x, y, sl.width(), 90, sl.textXOffset(), sl.text(), textColour, boxColour);
		};
	}
	
	public IVanGogh tpVG(CanvasButton tp) {
		return (x, y, gc) -> {
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
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
			c.drawTradeBox(x, y, tp.width(), 90, tp.textXOffset(), tp.text(), textColour, boxColour);
		};
	}
	
	public IVanGogh setSlVG(CanvasButton setSL) {
		return (x, y, gc) -> {
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
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
			c.drawTradeBox(x, y, setSL.width(), c.fontSize() * 2, setSL.textXOffset(), setSL.text(), textColour, boxColour);
		};
	}
	
	public IVanGogh setTpVG(CanvasButton setTP) {
		return (x, y, gc) -> {
			Color textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
			Color boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
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
			c.drawTradeBox(x, y, setTP.width(), c.fontSize() * 2, setTP.textXOffset(), setTP.text(), textColour, boxColour);
		};
	}
	
	public IVanGogh orderVG(CanvasButton order, PendingTrade trade) {
		return (x, y, gc) -> {
			Color textColour = Color.GRAY;
			Color boxColour = Color.GRAY;
			if (order.hover()) {
				boxColour = Color.DARKGRAY;
			}
			if (order.pressed()) {
				boxColour = Color.DIMGRAY;
			}
			if (!order.enabled()) {
				boxColour = Color.LIGHTGRAY;
			}
			if (trade.buy()) {
				textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index);
			} else {
				textColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
				boxColour = ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index);
			}
			c.drawTradeBox(x, y, order.width(), 90, order.textXOffset(), order.text(), textColour, boxColour);
		};
	}
	
	public IVanGogh pendingVG(CanvasButton btn) {		
		return (x, y, gc) -> {
			if (Chart.darkMode().get()) {
				gc.setFill(Color.WHITE);
			} else {
				gc.setFill(Color.BLACK);
			}
			gc.fillRect(x, y, btn.width(), btn.height());
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.BLACK);
			} else {
				gc.setStroke(Color.WHITE);
			}	
			if (btn.hover()) {
				if (c.yCoordToPrice(y + gc.getFont().getSize() / 3) > c.data().tickData().get(c.data().tickDataSize(c.replayMode()).get() - 1).price()) {
					if (btn.text().contains("STP")) {
						gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index));
					} else {
						gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index));
					}
				} else {
					if (btn.text().contains("STP")) {
						gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_2.index));
					} else {
						gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndices.MISCELLANEOUS_1.index));
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
}
