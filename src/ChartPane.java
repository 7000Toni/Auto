import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	private Chart chart;
	
	public ChartPane(Stage stage, double width, double height) {					
		try {
			//chart = new Chart("res/enqu.txt", 14209282, width, height, 0.25, 2, stage);
			chart = new Chart("res/mesu.txt", 24104933, width, height, 0.25, 2, stage);
			//chart = new Chart("res/ymu.txt", 1652574, width, height, 1, 0, stage);
			this.add(chart.getCanvas(), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Chart getChart() {
		return chart;
	}
}
