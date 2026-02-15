import javafx.scene.paint.Color;

public class ColourCalculator {

	public static Color colour(double x, double minPos, double maxPos) {
		int r = 0;
		int g = 0;
		int b = 0;
		double perc = (x - minPos) / (maxPos - minPos);
		if (perc > 2.0/3) {
			perc -= 2.0/3;
			if (perc > (1.0/3)/2) {
				perc -= (1.0/3)/2;
				r = 255;
				b = 255 - (int)(255*(perc/((1.0/3)/2)));
			} else {
				b = 255;
				r = (int)(255*(perc/((1.0/3)/2)));
			}			
		} else if (perc > 1.0/3) {
			perc -= 1.0/3;
			if (perc > (1.0/3)/2) {
				perc -= (1.0/3)/2;
				b = 255;
				g = 255 - (int)(255*(perc/((1.0/3)/2)));
			} else {
				g = 255;
				b = (int)(255*(perc/((1.0/3)/2)));
			}
		} else {
			if (perc > (1.0/3)/2) {
				perc -= (1.0/3)/2;
				g = 255;
				r = 255 - (int)(255*(perc/((1.0/3)/2)));
			} else {
				r = 255;
				g = (int)(255*(perc/((1.0/3)/2)));
			}
		}
		return Color.web("rgb(" + r + "," + g + "," +  b + ")");
	}
}
