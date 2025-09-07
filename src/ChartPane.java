import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	private Chart chart;
	//private static DataSet nq = new DataSet("res/enqu.txt", "ENQU25", 14209282);
	private static DataSet es = new DataSet("res/mesu.txt", "MESU25", 24104933);
	//private static DataSet ym = new DataSet("res/ymu.txt", "YMU25", 1652574);
	
	public ChartPane(Stage stage, double width, double height) {					
		try {
			
			//chart = new Chart("res/enqu.txt", 14209282, width, height, 0.25, 2, stage, "ENQU25");
			chart = new Chart(width, height, 0.25, 2, stage, "MESU25", es);
			//chart = new Chart("res/ymu.txt", 1652574, width, height, 1, 0, stage, "YMU25");
			this.add(chart.getCanvas(), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Chart getChart() {
		return chart;
	}
}
