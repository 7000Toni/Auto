import javafx.scene.paint.Color;

public class ChartButtonVanGoghs {
	private Chart c;
	
	public ChartButtonVanGoghs(Chart c) {
		this.c = c;
	}
	
	public VanGogh menuButtonVG(CanvasButton menu) {
		return (x, y, gc) -> {
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(ColourSettings.colours().get(ColourSettings.ColourIndices.DARK_MODE_CHART_BACKGROUND.index));
			} else {
				gc.setStroke(Color.BLACK);
				gc.setFill(ColourSettings.colours().get(ColourSettings.ColourIndices.LIGHT_MODE_CHART_BACKGROUND.index));				
			}		
			if (menu.hover()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.GRAY);
			}
			if (menu.pressed()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.DIMGRAY);
			}
			if (!menu.enabled()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.LIGHTGRAY);
			}
			gc.fillRect(x, y, menu.width(), menu.height());
			gc.strokeText(menu.text(), menu.x() + menu.textXOffset(), menu.y() + menu.textYOffset());
		};
	}
	
	public VanGogh buyVG(CanvasButton buy) {  
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
	
	public VanGogh sellVG(CanvasButton sell) { 
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
	
	public VanGogh closeVG(CanvasButton close) {
		return (x, y, gc) -> {
			Color textColour = Color.RED;
			Color boxColour = Color.RED;
			if (close.hover()) {
				textColour = Color.DARKRED;
				boxColour = Color.DARKRED;
			}
			if (close.pressed()) {
				textColour = Color.MAROON;
				boxColour = Color.MAROON;
			}
			if (!close.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			c.drawTradeBox(x, y, close.width(), c.fontSize() * 2, close.textXOffset(), close.text(), textColour, boxColour);
		};
	}
	
	public VanGogh cancelTpVG(CanvasButton cancelTP) {
		return (x, y, gc) -> {
			Color textColour = Color.CORNFLOWERBLUE;
			Color boxColour = Color.CORNFLOWERBLUE;
			if (cancelTP.hover()) {
				textColour = Color.STEELBLUE;
				boxColour = Color.STEELBLUE;
			}
			if (cancelTP.pressed()) {
				textColour = Color.NAVY;
				boxColour = Color.NAVY;
			}
			if (!cancelTP.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			c.drawTradeBox(x, y, cancelTP.width(), c.fontSize() * 2, cancelTP.textXOffset(), cancelTP.text(), textColour, boxColour);
		};
	}
	
	public VanGogh cancelSlVG(CanvasButton cancelSL) {
		return (x, y, gc) -> {
			Color textColour = Color.ORANGE;
			Color boxColour = Color.ORANGE;
			if (cancelSL.hover()) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (cancelSL.pressed()) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (!cancelSL.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			c.drawTradeBox(x, y, cancelSL.width(), c.fontSize() * 2, cancelSL.textXOffset(), cancelSL.text(), textColour, boxColour);
		};
	}
	
	public VanGogh slVG(CanvasButton sl) {
		return (x, y, gc) -> {
			Color textColour = Color.ORANGE;
			Color boxColour = Color.ORANGE;
			if (sl.hover()) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (sl.pressed()) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (!sl.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			c.drawTradeBox(x, y, sl.width(), 90, sl.textXOffset(), sl.text(), textColour, boxColour);
		};
	}
	
	public VanGogh tpVG(CanvasButton tp) {
		return (x, y, gc) -> {
			Color textColour = Color.CORNFLOWERBLUE;
			Color boxColour = Color.CORNFLOWERBLUE;
			if (tp.hover()) {
				textColour = Color.STEELBLUE;
				boxColour = Color.STEELBLUE;
			}
			if (tp.pressed()) {
				textColour = Color.NAVY;
				boxColour = Color.NAVY;
			}
			if (!tp.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			c.drawTradeBox(x, y, tp.width(), 90, tp.textXOffset(), tp.text(), textColour, boxColour);
		};
	}
	
	public VanGogh setSlVG(CanvasButton setSL) {
		return (x, y, gc) -> {
			Color textColour = Color.ORANGE;
			Color boxColour = Color.ORANGE;
			if (setSL.hover()) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (setSL.pressed()) {
				textColour = Color.DARKORANGE;
				boxColour = Color.DARKORANGE;
			}
			if (!setSL.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			c.drawTradeBox(x, y, setSL.width(), c.fontSize() * 2, setSL.textXOffset(), setSL.text(), textColour, boxColour);
		};
	}
	
	public VanGogh setTpVG(CanvasButton setTP) {
		return (x, y, gc) -> {
			Color textColour = Color.CORNFLOWERBLUE;
			Color boxColour = Color.CORNFLOWERBLUE;
			if (setTP.hover()) {
				textColour = Color.STEELBLUE;
				boxColour = Color.STEELBLUE;
			}
			if (setTP.pressed()) {
				textColour = Color.NAVY;
				boxColour = Color.NAVY;
			}
			if (!setTP.enabled()) {
				textColour = Color.LIGHTGRAY;
				boxColour = Color.LIGHTGRAY;
			}
			c.drawTradeBox(x, y, setTP.width(), c.fontSize() * 2, setTP.textXOffset(), setTP.text(), textColour, boxColour);
		};
	}
	
	public VanGogh orderVG(CanvasButton order, PendingTrade trade) {
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
				textColour = Color.FORESTGREEN;
			} else {
				textColour = Color.RED;
			}
			c.drawTradeBox(x, y, order.width(), 90, order.textXOffset(), order.text(), textColour, boxColour);
		};
	}
	
	public VanGogh pendingVG(CanvasButton btn) {		
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
				gc.setStroke(Color.ORANGE);
			}
			if (btn.pressed()) {
				gc.setStroke(Color.DARKORANGE);
			}
			if (!btn.enabled()) {
				gc.setStroke(Color.LIGHTGRAY);
			}
			gc.strokeText(btn.text(), x + btn.textXOffset(), y + btn.textYOffset());
		};
	}
}
