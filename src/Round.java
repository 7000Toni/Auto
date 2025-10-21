
public class Round {
	public static double round(double val, int decimalPlaces) {
		if (decimalPlaces < 1) {
			return val;
		}
		int val2 = (int)(val * Math.pow(10, decimalPlaces + 1));
		int val3 = (int)(val * Math.pow(10, decimalPlaces));
		int val4 = val3 * 10;
		int val5 = val2 - val4;
		if (val5 >= 5) {
			val3++;
		}
		return val3 / Math.pow(10, decimalPlaces);
	}
}
