package com.github._7000toni.auto.settings;
import java.util.ArrayList;
import java.util.Arrays;

import com.github._7000toni.auto.chart.Chart;

import javafx.scene.paint.Color;

public class ColourSettings {
	private static ArrayList<Color> colours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.BLACK, Color.SLATEBLUE, Color.CORNFLOWERBLUE, Color.ORANGE,
																					Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE, Color.WHITE, Color.SLATEBLUE, Color.CORNFLOWERBLUE, Color.ORANGE));
	public static final int SIZE = 13;
	
	public enum ColourIndex {
		UP_CANDLESTICK_FILL(0),		
		UP_CANDLESTICK_STROKE(1),
		DOWN_CANDLESTICK_FILL(2),
		DOWN_CANDLESTICK_STROKE(3),
		LINE_CHART(4),
		MENU_BACKGROUND(5),
		CHART_BACKGROUND(6),
		MISCELLANEOUS_1(7),
		MISCELLANEOUS_2(8),
		TEXT_AND_STUFF(9),
		CURRENT_PRICE_LINE(10),
		BUY_HISTORY(11),
		SELL_HISTORY(12);
		
		public final int index;
		
		private ColourIndex(int index) {
			this.index = index;
		}
	}
	
	public static int index(ColourIndex colour) {
		if (Chart.darkMode().get()) {
			return colour.index + SIZE;
		} else {
			return colour.index;
		}
	}
	
	public static void setColour(ColourIndex cindex, Color colour) {
		int index = Chart.darkMode().get()?cindex.index+SIZE:cindex.index;
		colours.set(index, colour);
	}
	
	public static ArrayList<Color> colours() {
		return colours;
	}
	
	public static void setDefaultColours() {
		colours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.BLACK, Color.SLATEBLUE, Color.CORNFLOWERBLUE, Color.ORANGE,
													Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE, Color.WHITE, Color.SLATEBLUE, Color.CORNFLOWERBLUE, Color.ORANGE));
	}
	
	public static ArrayList<Color> defaultColours() {
		return new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.BLACK, Color.SLATEBLUE, Color.CORNFLOWERBLUE, Color.ORANGE,
												Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE, Color.WHITE, Color.SLATEBLUE, Color.CORNFLOWERBLUE, Color.ORANGE));
	}
	
	public static Color colour(ColourIndex cindex) {
		if (Chart.darkMode().get()) {
			return colours.get(cindex.index + SIZE);
		} else {
			return colours.get(cindex.index);
		}
	}
	
	public static Color colour(int index) {
		if (Chart.darkMode().get()) {
			return colours.get(index + SIZE);
		} else {
			return colours.get(index);
		}
	}
	
	private static String string(ArrayList<Color> colours, int offset) {
		String s = "";
		for (int i = offset; i < SIZE + offset; i++) {
			s += ColourIndex.values()[i - offset].name();
			s += "=" + colours.get(i) + "\n";
		}
		return s;
	}
	
	public static String lightModeString() {
		return string(colours, 0);
	}
	
	public static String darkModeString() {
		return string(colours, SIZE);
	}
	
	public static String defaultLightModeString() {
		return string(defaultColours(), 0);
	}
	
	public static String defaultDarkModeString() {
		return string(defaultColours(), SIZE);
	}
}
