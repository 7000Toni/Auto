import javafx.scene.paint.Color;

public class Settings {
	private static Color upCandleStickFill;
	private static Color downCandleStickFill;
	
	public static Color upCandleStickFill() {
		return upCandleStickFill;
	}
	
	public static Color downCandleStickFill() {
		return downCandleStickFill;
	}
	
	public static void setUpCandleStickFill(Color upCandleStickFill) {
		Settings.upCandleStickFill = upCandleStickFill;
	}
	
	public static void setDownCandleStickFill(Color downCandleStickFill) {
		Settings.downCandleStickFill = downCandleStickFill;
	}
}
