import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	private Chart chart;
	
	public ChartPane(Stage stage, double width, double height) {					
		try {
			chart = new Chart("res/enqu.txt", 14209282, width, height, 0.25, 2, stage);
			this.add(chart.getCanvas(), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Chart getChart() {
		return chart;
	}
}
