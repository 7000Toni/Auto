import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Main extends Application{
	public static void main(String[] args) {
		launch();
		System.exit(0);
	}
	
	@Override
	public void start(Stage stage) {
		double width = 1600;
		double height = 900;
		ChartPane chart = new ChartPane(stage, width, height);
		Scene scene = new Scene(chart, width, height);	
		scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> chart.getChart().hsb().keyPressed(e));
		stage.setScene(scene);
		stage.show();
	}
}
