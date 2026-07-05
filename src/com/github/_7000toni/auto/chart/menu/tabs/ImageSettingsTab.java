package com.github._7000toni.auto.chart.menu.tabs;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.BrightnessScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.marketreplay.MarketReplayPane;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ImageFunctions;
import com.github._7000toni.auto.settings.ImageSettings;
import com.github._7000toni.auto.settings.Settings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

public class ImageSettingsTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel imageSettings;
	private CanvasButton reset;
	private CanvasButton save;
	private CanvasLabel saved;
	private boolean recentlySaved = false;
	
	private CanvasLabel brightness;
	private BrightnessScrollBar bsb;
	private CanvasButton drawImg;
	private CanvasButton stretch;
	private CanvasButton clearImage;
	private CanvasButton setImage;
	private CanvasLabel noImage;	
	
	public ImageSettingsTab(double x, double y, double width, double height, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		this.cmbvg = cmbvg;
		
		initImageSettingsMenu();
	}
	
	private void initImageSettingsMenu() {
		imageSettings = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "IMAGE SETTINGS");
		imageSettings.setVanGogh((x2, y2, gc2) -> {
			imageSettings.defaultDraw(gc.getFont());
		});
		
		reset = new CanvasButton(gc, 290, 20, x + 5, y + 230, "RESET");
		reset.setVanGogh((x2, y2, gc2) -> {
			reset.defaultDraw(gc.getFont());
		});
		reset.setOnMouseClicked(e -> {
			Settings.loadSettings();
			bsb.setX(bsb.minPos() + ((ImageSettings.brightness() + 1) / 2) * 289);
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		save = new CanvasButton(gc, 290, 20, x + 5, y + 255, "SAVE");
		save.setVanGogh((x2, y2, gc2) -> {
			save.defaultDraw(gc.getFont());
		});
		save.setOnMouseClicked(e -> {
			Settings.saveSettings();
			recentlySaved = true;
			new AnimationTimer() {
				private long init = 0;
				
				@Override
				public void handle(long now) {
					if (init == 0) {
						init = now;
					}
					if ((now - init) / HorizontalScrollBar.NANO_TO_MILLI > 1500) {
						recentlySaved = false;
						chart.draw();
						this.stop();
					}
				}
			}.start();
		});
		
		saved = new CanvasLabel(gc, 290, 20, x + 5, y + 280, "SAVED"); 
		saved.setVanGogh((x2, y2, gc2) -> {
			saved.defaultDraw(gc.getFont());
		});
		
		brightness = new CanvasLabel(gc, 290, 20, x + 5, y + 85, "BRIGHTNESS");
		brightness.setVanGogh((x2, y2, gc2) -> {
			brightness.defaultDraw(gc.getFont());
		});
		
		bsb = new BrightnessScrollBar(this, x + ((ImageSettings.brightness() + 1) / 2) * 289, x + 299, 15, 15, y + 105);
		
		drawImg = new CanvasButton(gc, 290, 20, x + 5, y + 130, "DRAW IMAGE");
		drawImg.setVanGogh(cmbvg.imgSettingsToggleVG(drawImg, "DON'T DRAW", "DRAW IMAGE"));
		drawImg.setOnMouseClicked(e -> {
			ImageSettings.setDraw(!ImageSettings.draw().get());
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});		
		
		stretch = new CanvasButton(gc, 290, 20, x + 5, y + 155, "STRETCH IMAGE");
		stretch.setVanGogh(cmbvg.imgSettingsToggleVG(stretch, "DON'T STRETCH", "STRETCH IMAGE"));		
		stretch.setOnMouseClicked(e -> {
			ImageSettings.setStretch(!ImageSettings.stretch().get());
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		clearImage = new CanvasButton(gc, 290, 20, x + 5, y + 180, "CLEAR IMAGE");
		clearImage.setVanGogh((x2, y2, gc2) -> {
			clearImage.defaultDraw(gc.getFont());
		});
		clearImage.setOnMouseClicked(e -> {
			ImageSettings.clearImage();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		setImage = new CanvasButton(gc, 290, 20, x + 5, y + 205, "SET IMAGE");
		setImage.setVanGogh((x2, y2, gc2) -> {
			setImage.defaultDraw(gc.getFont());
		});
		setImage.setOnMouseClicked(e -> {
			String userHome = System.getProperty("user.home");
	        Path pics = Paths.get(userHome, "Pictures");
	        File picsDir = pics.toFile();
			FileChooser fc = new FileChooser();
			if (picsDir.exists()) {
				fc.setInitialDirectory(picsDir);
			} else {
				fc.setInitialDirectory(new File("./"));
			}	
			fc.setTitle("Select Image");
			File file = fc.showOpenDialog(null);		
			if (file != null) {
				ImageSettings.setImage(file);
			}
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		noImage = new CanvasLabel(gc, 290, 20, x + 5, y + 305, "NO IMAGE SELECTED");
		noImage.setVanGogh((x2, y2, gc2) -> {
			noImage.defaultDraw(gc.getFont());
		});
	}
	
	private void drawImageSettingsMenu() {
		imageSettings.draw();
		brightness.draw();	
		bsb.draw();
		drawImg.draw();
		stretch.draw();
		clearImage.draw();
		setImage.draw();
		reset.draw();
		save.draw();
		if (ImageSettings.image() == null) {
			noImage.draw();
		} else {
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
			gc.fillRect(x + 5, y + 305, 290, 165);
			ImageFunctions.drawImage(gc, ImageSettings.image(), x + 5, y + 305, 290, 165);
		}
		if (recentlySaved) {
			saved.draw();
		}
	}
	
	public void setImageSettingsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(imageSettings, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(bsb, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(drawImg, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(stretch, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(clearImage, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(setImage, menuNode));		
		sceneGraph.addNode(new TNode<ICanvasNode>(reset, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(save, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(noImage, menuNode));
	}
	
	public Chart chart() {
		return chart;
	}

	@Override
	public void draw() {
		drawImageSettingsMenu();	
	}

	@Override
	public void setX(double x) {
		reset.setX(x + 5);
		save.setX(x + 5);
		saved.setX(x + 5);
		
		imageSettings.setX(x + 5);
		brightness.setX(x + 5);
		
		double hsbOffset = bsb.x() - this.x;
		bsb.setMinPos(x);
		bsb.setMaxPos(x + 299);
		bsb.setX(hsbOffset + x);
		
		
		drawImg.setX(x + 5);
		stretch.setX(x + 5);
		setImage.setX(x + 5);
		clearImage.setX(x + 5);
		noImage.setX(x + 5);
		this.x = x;
	}

	@Override
	public void setY(double y) {				
		imageSettings.setY(y + 35);
		reset.setY(y + 230);
		save.setY(y + 255);
		saved.setY(y + 280);
		
		brightness.setY(y + 85);
		bsb.setY(y + 105);
		drawImg.setY(y + 130);
		stretch.setY(y + 155);
		setImage.setY(y + 180);
		clearImage.setY(y + 205);
		noImage.setY(y + 305);
		this.y = y;
	}
}
