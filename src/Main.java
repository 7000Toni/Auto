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
		MenuPane mp = new MenuPane(640, 360);
		Scene scene2 = new Scene(mp, 640, 360);			
		stage.setScene(scene2);
		stage.setResizable(false);
		stage.setOnCloseRequest(e -> {
			System.exit(0);
		});
		stage.show();
	}
}
