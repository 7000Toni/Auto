import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartPane extends GridPane {
	private Chart chart;
	//private static DataSet ch = new DataSet(new File("res/enqu.txt"), "ENQU25", 27633688);
	//private static DataSet ch = new DataSet(new File("res/mesu.txt"), "MESU25", 28154855);
	//private static DataSet ch = new DataSet(new File("res/ymu.txt", "YMU25"), 2412479);
	
	public ChartPane(Stage stage, double width, double height, DataSet ch) {					
		try {
			chart = new Chart(width, height, 0.25, 2, stage, ch);
			this.add(chart.canvas(), 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Chart getChart() {
		return chart;
	}
}
