import java.util.ArrayList;
import java.util.Arrays;

import javafx.scene.paint.Color;

public class ColourSettings {
	private static ArrayList<Color> lightColours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE));
	private static ArrayList<Color> darkColours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE));
	
	public enum ColourIndices {
		UP_CANDLESTICK_FILL(0),		
		UP_CANDLESTICK_STROKE(1),
		DOWN_CANDLESTICK_FILL(2),
		DOWN_CANDLESTICK_STROKE(3),
		LINE_CHART(4),
		MENU_BACKGROUND(5),
		CHART_BACKGROUND(6),
		MISCELLANEOUS_1(7),
		MISCELLANEOUS_2(8);
		
		public final int index;
		
		private ColourIndices(int index) {
			this.index = index;
		}
	}
	
	public static ArrayList<Color> lightColours() {
		return lightColours;
	}
	
	public static ArrayList<Color> darkColours() {
		return darkColours;
	}
	
	public static void setDefaultColours() {
		lightColours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE));
		darkColours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE));
	}
	
	public static void setDefaultLightColours() {
		lightColours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE));
	}
	
	public static void setDefaultDarkColours() {
		darkColours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE));
	}
	
	public static ArrayList<Color> defaultLightColours() {
		return new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE));
	}
	
	public static ArrayList<Color> defaultDarkColours() {
		return new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE));
	}
	
	public static Color colour(int index) {
		if (Chart.darkMode().get()) {
			return darkColours.get(index);
		} else {
			return lightColours.get(index);
		}
	}
	
	public static String lightString() {
		String s = "";
		for (Color c : lightColours) {
			s += c.toString() + '\n';
		}
		return s;
	}
	
	public static String darkString() {
		String s = "";
		for (Color c : darkColours) {
			s += c.toString() + '\n';
		}
		return s;
	}
}
