import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ChartMenuButtonVanGoghs {
	
	public IVanGogh menuButtonVG(CanvasButton cb, double fontSize) {
		return (x, y, gc) -> {
			double oldFontSize = gc.getFont().getSize();
			gc.setFont(new Font(fontSize));
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
				gc.setFill(Color.BLACK);
			}
			if (cb.on) {
				gc.setStroke(ColourSettings.colours().get(ColourSettings.ColourIndices.MISCELLANEOUS_2.index));
				gc.setFill(ColourSettings.colours().get(ColourSettings.ColourIndices.MISCELLANEOUS_2.index));
			} 
			if (cb.hover) {
				gc.setStroke(ColourSettings.colours().get(ColourSettings.ColourIndices.MISCELLANEOUS_2.index));
				gc.setFill(ColourSettings.colours().get(ColourSettings.ColourIndices.MISCELLANEOUS_2.index));
			} 
			if (cb.pressed) {				
				gc.setStroke(ColourSettings.colours().get(ColourSettings.ColourIndices.MISCELLANEOUS_1.index));
				gc.setFill(ColourSettings.colours().get(ColourSettings.ColourIndices.MISCELLANEOUS_1.index));
			}	
			gc.strokeRect(x, y, cb.width(), cb.height());
			gc.strokeText(cb.text(), x + cb.textXOffset(), y + cb.textYOffset());
			gc.setFont(new Font(oldFontSize));
		};
	}
	
	public IVanGogh toggleVG(CanvasButton cb, ReadOnlyBooleanProperty condition, String text1, String text2, double fontSize1, double fontSize2, double xoff1, double xoff2, double yoff1, double yoff2) {
		return (x, y, gc) -> {
			double oldFontSize = gc.getFont().getSize();
			double fontSize;
			if (condition.get()) {
				cb.setText(text1);	
				cb.setTextXOffset(xoff1);
				cb.setTextYOffset(yoff1);
				fontSize = fontSize1;
			} else {
				cb.setText(text2);
				cb.setTextXOffset(xoff2);
				cb.setTextYOffset(yoff2);
				fontSize = fontSize2;
			}
			gc.setFont(new Font(fontSize));
			cb.alternateDraw(gc.getFont());
			gc.setFont(new Font(oldFontSize));
		};
	}	
	
	public IVanGogh colourPreviewVG(CanvasButton cb, int index) {
		return (x, y, gc) -> {
			if (Chart.darkMode().get()) {
				gc.setStroke(Color.WHITE);
			} else {
				gc.setStroke(Color.BLACK);
			}
			gc.strokeRect(x, y, cb.width(), cb.height());
			gc.setFill(ColourSettings.colours().get(index));
			gc.fillRect(x+1, y+1, cb.width()-2, cb.height()-2);
		};
	}	
}
