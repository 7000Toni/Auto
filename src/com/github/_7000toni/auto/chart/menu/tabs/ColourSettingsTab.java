package com.github._7000toni.auto.chart.menu.tabs;
import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ColourPicker;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.BrightnessScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenu;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.marketreplay.MarketReplayNode;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ImageSettings;
import com.github._7000toni.auto.settings.Settings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

public class ColourSettingsTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	private ChartMenu chartMenu;
	
	private CanvasLabel colourSettings;
	
	private ColourPicker colourPicker;
	private CanvasButton reset;
	private CanvasButton defaultColours;
	
	private CanvasButton save;
	
	private ArrayList<CanvasButton> colourButtons;
	private BooleanProperty recentlySaved = new SimpleBooleanProperty(false);
	
	public ColourSettingsTab(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg, BrightnessScrollBar bsb) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.chartMenu = chartMenu;
		this.gc = gc;
		this.chart = chart;
		this.cmbvg = cmbvg;
		
		initColourSettingsMenu(bsb);
	}
	
	private void initColourSettingsMenu(BrightnessScrollBar bsb) {
		colourSettings = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "COLOUR SETTINGS");
		colourSettings.setVanGogh((x2, y2, gc2) -> {
			colourSettings.defaultDraw(gc.getFont());
		});
		
		colourPicker = new ColourPicker(x + 5, y + 85, 290, 165, gc, chartMenu);
		
		colourButtons = new ArrayList<CanvasButton>();
		initColourButtons();
		
		reset = new CanvasButton(gc, 142.5, 20, x + 5, y + 480, "RESET");
		reset.setVanGogh((x2, y2, gc2) -> {
			reset.defaultDraw(gc.getFont());
		});
		reset.setOnMouseClicked(e -> {
			Settings.loadSettings();
			bsb.setX(bsb.minPos() + ((ImageSettings.brightness() + 1) / 2) * 289);
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayNode.drawReplayNodes();
		});
		
		defaultColours = new CanvasButton(gc, 142.5, 20, x + 152.5, y + 480, "DEFAULT");
		defaultColours.setVanGogh((x2, y2, gc2) -> {
			defaultColours.defaultDraw(gc.getFont());
		});
		defaultColours.setOnMouseClicked(e -> {
			ColourSettings.setDefaultColours();
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayNode.drawReplayNodes();
		});
		
		save = new CanvasButton(gc, 290, 20, x + 5, y + 505, "SAVE");
		save.setVanGogh(cmbvg.toggleVG(save, recentlySaved, "SAVED", "SAVE"));
		save.setOnMouseClicked(e -> {
			Settings.saveSettings();
			recentlySaved.set(true);
			new AnimationTimer() {
				private long init = 0;
				
				@Override
				public void handle(long now) {
					if (init == 0) {
						init = now;
					}
					if ((now - init) / HorizontalScrollBar.NANO_TO_MILLI > 1500) {
						recentlySaved.set(false);
						chart.draw();
						this.stop();
					}
				}
			}.start();
		});
	}
	
	public void drawColourSettingsMenu() {
		colourSettings.draw();
		colourPicker.draw();
		for (CanvasButton c : colourButtons) {
			c.draw();
		}	
		reset.draw();
		defaultColours.draw();
		save.draw();
	}
	
	private void initColourButtons() {
		for (int i = 0; i < ColourSettings.SIZE; i++) {
			CanvasButton javaisannoying = new CanvasButton(gc, 265, 20, x + 5, y + 255 + 25*i, ChartMenu.ColourButtonIndices.values()[i*2].text);
			colourButtons.add(javaisannoying);
			javaisannoying.setVanGogh((x2, y2, gc2) -> {
				javaisannoying.defaultDraw(gc2.getFont());
			});			
			CanvasButton colPrev = new CanvasButton(gc, 20, 20, x + 275, y + 255 + 25*i, null);
			setMouseEvent(javaisannoying, colPrev, i);
			colourButtons.add(colPrev);
			colourButtons.get(i*2+1).setVanGogh(cmbvg.colourPreviewVG(colourButtons.get(i*2+1), i));
		}
	}
	
	private void setMouseEvent(CanvasButton cb, CanvasButton cbPrev, int index) {
		cb.setOnMouseClicked(e -> {
			if (Chart.darkMode().get()) {
				ColourSettings.colours().set(index + 9, colourPicker.finalColour());
			} else {
				ColourSettings.colours().set(index, colourPicker.finalColour());
			}			
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayNode.drawReplayNodes();
		});
		cbPrev.setOnMouseClicked(e -> {
			colourPicker.setFinalColour(ColourSettings.colour(index));
		});
	}
	
	public void setColourSettingsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(colourSettings, menuNode));
		TNode<ICanvasNode> colourPickerNode = new TNode<ICanvasNode>(colourPicker, menuNode);
		sceneGraph.addNode(colourPickerNode);
		sceneGraph.addNode(new TNode<ICanvasNode>(colourPicker.hsb(), colourPickerNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(colourPicker.usb(), colourPickerNode));
		for (CanvasButton c : colourButtons) {
			sceneGraph.addNode(new TNode<ICanvasNode>(c, menuNode));
		}
		sceneGraph.addNode(new TNode<ICanvasNode>(reset, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(defaultColours, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(save, menuNode));
	}
	
	public Chart chart() {
		return chart;
	}
	
	public ColourPicker colourPicker() {
		return colourPicker;
	}

	@Override
	public void draw() {
		drawColourSettingsMenu();	
	}

	@Override
	public void setX(double x) {
		reset.setX(x + 5);
		save.setX(x + 5);
		
		colourSettings.setX(x + 5);	
		colourPicker.setX(x + 5);
		int i = 0;
		while (i < colourButtons.size()) {
			colourButtons.get(i).setX(x + 5);
			colourButtons.get(i + 1).setX(x + 275);
			i += 2;
		}
		defaultColours.setX(x + 152.5);		
		
		this.x = x;
	}

	@Override
	public void setY(double y) {				
		colourSettings.setY(y + 35);
		reset.setY(y + 480);
		save.setY(y + 505);
		
		colourSettings.setY(y + 35);	
		
		colourPicker.setY(y + 85);
		for (int i = 0; i < ColourSettings.SIZE; i++) {
			colourButtons.get(i*2).setY(y + 255 + 25*i);
			colourButtons.get(i*2+1).setY(y + 255 + 25*i);
		}		
		defaultColours.setY(y + 480);
		
		this.y = y;
	}
}
