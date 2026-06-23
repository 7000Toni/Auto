package com.github._7000toni.auto;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import com.github._7000toni.auto.menu.MenuPane;
import com.github._7000toni.auto.miscellaneous.DualPrintStream;
import com.github._7000toni.auto.settings.Settings;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
	private static Image icon = new Image(Main.class.getResourceAsStream("/icon.jpg"));	
	
	public static void main(String[] args) {
		setOutputFile();
		launch(args);
		System.exit(0);
	}
	
	public static Image icon() {
		return icon;
	}
	
	private static void setOutputFile() {
		try {
			File out = new File("./out.txt");
			File err = new File("./err.txt");
			boolean outAppend = true;
			boolean errAppend = true;
			if (out.length() > 104857600) {
				outAppend = false;
			}
			if (err.length() > 104857600) {
				errAppend = false;
			}
			DualPrintStream ps1 = new DualPrintStream(System.out, new PrintStream(new FileOutputStream(out, outAppend)));
			DualPrintStream ps2 = new DualPrintStream(System.err, new PrintStream(new FileOutputStream(err, errAppend)));
            System.setOut(ps1);
            System.setErr(ps2);
            LocalDateTime ldt = LocalDateTime.now();
            System.out.println("launched on " + ldt);
            System.err.println("launched on " + ldt);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
