import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	private Chart chart;
	
	public ChartPane(Stage stage, double width, double height, DataSet ch, boolean replayMode, MarketReplay mr) {					
		try {
			chart = new Chart(width, height, stage, ch);
			if (replayMode) {
				chart.enableReplayMode(mr);
			}
			this.add(chart.canvas(), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Chart getChart() {
		return chart;
	}
}
