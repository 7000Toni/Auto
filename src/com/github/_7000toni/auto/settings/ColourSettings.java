package com.github._7000toni.auto.settings;
import java.util.ArrayList;
import java.util.Arrays;

import com.github._7000toni.auto.chart.Chart;

import javafx.scene.paint.Color;

public class ColourSettings {
	private static ArrayList<Color> colours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.BLACK,
																					Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE, Color.WHITE));
	public static final int SIZE = 10;
	
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
		TEXT_AND_STUFF(9);
		
		public final int index;
		
		private ColourIndex(int index) {
			this.index = index;
		}
	}
	
	public static int index(ColourIndex colour) {
		if (Chart.darkMode().get()) {
			return colour.index + 10;
		} else {
			return colour.index;
		}
	}
	
	public static void setColour(ColourIndex cindex, Color colour) {
		int index = Chart.darkMode().get()?cindex.index:cindex.index+10;
		colours.set(index, colour);
	}
	
	public static ArrayList<Color> colours() {
		return colours;
	}
	
	public static void setDefaultColours() {
		colours = new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.BLACK,
													Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE, Color.WHITE));
	}
	
	public static ArrayList<Color> defaultColours() {
		return new ArrayList<Color>(Arrays.asList(Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.BLACK, Color.WHITE, Color.WHITE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.BLACK,
												Color.CORNFLOWERBLUE, Color.CORNFLOWERBLUE, Color.ORANGE, Color.ORANGE, Color.WHITE, Color.BLACK, Color.BLACK, Color.CORNFLOWERBLUE, Color.ORANGE, Color.WHITE));
	}
	
	public static Color colour(ColourIndex cindex) {
		if (Chart.darkMode().get()) {
			return colours.get(cindex.index + 10);
		} else {
			return colours.get(cindex.index);
		}
	}
	
	public static Color colour(int index) {
		if (Chart.darkMode().get()) {
			return colours.get(index + 10);
		} else {
			return colours.get(index);
		}
	}
	
	public static String string() {
		String s = "";
		for (int i = 0; i < colours.size(); i++) {
			Color c = colours.get(i);
			s += c.toString();
			if (i != colours.size() - 1) {
				s += "\n";
			}
		}
		return s;
	}
}
