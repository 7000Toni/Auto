import java.util.ArrayList;
import java.util.Arrays;

import javafx.scene.paint.Color;

public class ColourSettings {
	private static ArrayList<Color> colours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK));
	
	public enum ColourIndices {
		UP_CANDLESTICK_FILL(0),		
		UP_CANDLESTICK_STROKE(1),
		DOWN_CANDLESTICK_FILL(2),
		DOWN_CANDLESTICK_STROKE(3),
		LIGHT_MODE_LINE(4),
		DARK_MODE_LINE(5),
		LIGHT_MODE_MENU_BACKGROUND(6),
		DARK_MODE_MENU_BACKGROUND(7),
		LIGHT_MODE_CHART_BACKGROUND(8),
		DARK_MODE_CHART_BACKGROUND(9);
		
		public final int index;
		
		private ColourIndices(int index) {
			this.index = index;
		}
	}
	
	public static ArrayList<Color> colours() {
		return colours;
	}
	
	public static void defaultColours() {
		colours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK));
	}
	
	public static String string() {
		String s = "";
		for (Color c : colours) {
			s += c.toString() + '\n';
		}
		return s;
	}
}
