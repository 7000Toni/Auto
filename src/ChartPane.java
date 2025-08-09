import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.GridPane;

public class ChartPane extends GridPane {
	private ScrollPane sp;
	
	public ChartPane() {					
		Chart chart = new Chart("res/enqu.txt", 14209282, 1600, 900, 0.25);		
		sp = new ScrollPane(chart.getCanvas());		
		sp.setVbarPolicy(ScrollBarPolicy.NEVER);
		this.add(chart.getCanvas(), 0, 0);
	}
}
