import javafx.scene.layout.GridPane;

public class ChartPane extends GridPane {
	private Chart chart;
	
	public ChartPane() {					
		try {
			chart = new Chart("res/enqu.txt", 14209282, 1600, 900, 0.25, 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.add(chart.getCanvas(), 0, 0);
	}
	
	public Chart getChart() {
		return chart;
	}
}
