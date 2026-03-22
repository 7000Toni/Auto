import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
	private static Image icon = new Image(Main.class.getResourceAsStream("/icon.jpg"));
	
	public static void main(String[] args) {
		launch();
		System.exit(0);
	}
	
	public static Image icon() {
		return icon;
	}
	
	@Override
	public void start(Stage stage) {
		Settings.loadSettings();
		MenuPane mp = new MenuPane(640, 360);
		Scene scene = new Scene(mp, 640, 360);
		if (icon != null) {
			stage.getIcons().add(icon);
		}
		stage.setTitle("Auto");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(e -> {
			System.exit(0);
		});
		stage.show();
	}
}
