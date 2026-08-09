package com.github._7000toni.auto.menu;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class MenuPane extends GridPane {	
	private Menu menu;
	
	public MenuPane(double width, double height, Stage stage) {					
		menu = new Menu(width, height, stage);
		this.add(menu.canvas(), 0, 0);
	}
	
	public Menu menu() {
		return this.menu;
	}
}
