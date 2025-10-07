import java.io.File;

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
	public void start(Stage stage) {/*
		MenuPane mp = new MenuPane(640, 360);
		Scene scene2 = new Scene(mp, 640, 360);			
		stage.setScene(scene2);
		stage.setResizable(false);
		stage.setOnCloseRequest(e -> {
			System.exit(0);
		});
		stage.show();*/
		/*Stage s = new Stage();
		ChartPane c = new ChartPane(s, 1280, 720, new DataSet(new File("res/20240624_Optimized.csv"), new OptimizedMarketTickFileReader()));
		Scene scene = new Scene(c);	
		scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
		s.setScene(scene);
		s.show();*/
		Stage s2 = new Stage();
		s2.setResizable(false);
		MarketReplayPane mrp = new MarketReplayPane(null, 0);
		Scene scene2 = new Scene(mrp);	
		//scene2.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
		s2.setScene(scene2);
		s2.show();
	}
}
