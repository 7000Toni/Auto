import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{
	public static void main(String[] args) {
		launch();
		System.exit(0);
	}
	
	@Override
	public void start(Stage stage) {
		ChartPane chart = new ChartPane();
		Scene scene = new Scene(chart, 1600, 900);
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
	}
}
