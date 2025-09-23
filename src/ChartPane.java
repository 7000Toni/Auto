import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	private Chart chart;
	//private static DataSet ch = new DataSet(new File("res/enqu.txt"));
	//private static DataSet ch = new DataSet(new File("res/mesu.txt"));
	//private static DataSet ch = new DataSet(new File("res/ymu.txt"));
	
	public ChartPane(Stage stage, double width, double height, DataSet ch) {					
		try {
			chart = new Chart(width, height, stage, ch);
			this.add(chart.canvas(), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Chart getChart() {
		return chart;
	}
}
