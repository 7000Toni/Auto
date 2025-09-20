import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class MenuPane extends GridPane {	
	private Menu menu;
	
	public MenuPane(double width, double height) {					
		menu = new Menu(width, height);
		this.add(menu.canvas(), 0, 0);
	}
}
