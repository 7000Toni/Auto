import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ChartSettingsMenu extends CanvasNode implements IScrollBarOwner {
	private ChartMenu chartMenu;
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel colourSettings;
	private CanvasLabel imageSettings;
	
	private CanvasButton previousSettings;
	private CanvasButton nextSettings;
	
	private ColourPicker colourPicker;
	private CanvasButton reset;
	private CanvasButton defaultColours;
	
	private CanvasButton save;
	private CanvasLabel saved;
	
	private CanvasLabel brightness;
	private BrightnessScrollBar bsb;
	private CanvasButton drawImg;
	private CanvasButton stretch;
	private CanvasButton clearImage;
	private CanvasButton setImage;
	private CanvasLabel noImage;
	
	private ArrayList<CanvasButton> colourButtons;
	private int settingsMenuIndex = 0;
	private boolean recentlySaved = false;
	
	public ChartSettingsMenu(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.chartMenu = chartMenu;
		this.gc = gc;
		this.chart = chart;
		this.cmbvg = cmbvg;
		
		initSettingsMenu();
	}
	
	private void setSharedButtonVars() {
		reset.setX(x + 5);
		save.setX(x + 5);
		saved.setX(x + 5);
		if (settingsMenuIndex == 0) {
			reset.setWidth(142.5);
			reset.setY(y + 480);
			save.setY(y + 505);
			saved.setY(y + 530);
		} else if (settingsMenuIndex == 1) {
			reset.setWidth(290);
			reset.setY(230);
			save.setY(y + 255);
			saved.setY(y + 280);
		}
	}
	
	private void initSettingsMenu() {
		colourSettings = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "COLOUR SETTINGS");
		colourSettings.setVanGogh((x2, y2, gc2) -> {
			colourSettings.alternateDraw(gc.getFont());
		});
		
		imageSettings = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "IMAGE SETTINGS");
		imageSettings.setVanGogh((x2, y2, gc2) -> {
			imageSettings.alternateDraw(gc.getFont());
		});
		
		previousSettings = new CanvasButton(gc, 142.5, 20, x + 5, y + 60, "PREVIOUS");
		previousSettings.setVanGogh((x2, y2, gc2) -> {
			previousSettings.alternateDraw(gc.getFont());
		});
		previousSettings.setOnMouseClicked(e -> {
			settingsMenuIndex = Math.abs((settingsMenuIndex - 1) % 2); 
			setSharedButtonVars();
			setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});
		
		nextSettings = new CanvasButton(gc, 142.5, 20, x + 152.5, y + 60, "NEXT");	
		nextSettings.setVanGogh((x2, y2, gc2) -> {
			nextSettings.alternateDraw(gc.getFont());
		});
		nextSettings.setOnMouseClicked(e -> {
			settingsMenuIndex = Math.abs((settingsMenuIndex + 1) % 2); 
			setSharedButtonVars();
			setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});		
		
		initColoursSettingsMenu();
		initImageSettingsMenu();
	}
	
	private void initColoursSettingsMenu() {
		colourPicker = new ColourPicker(x + 5, y + 85, 290, 165, gc, chartMenu);
		
		colourButtons = new ArrayList<CanvasButton>();
		initColourButtons();
		
		reset = new CanvasButton(gc, 142.5, 20, x + 5, y + 480, "RESET");
		reset.setVanGogh((x2, y2, gc2) -> {
			reset.alternateDraw(gc.getFont());
		});
		reset.setOnMouseClicked(e -> {
			Settings.loadSettings();
			bsb.setX(bsb.minPos() + ((ImageSettings.brightness() + 1) / 2) * 289);
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		defaultColours = new CanvasButton(gc, 142.5, 20, x + 152.5, y + 480, "DEFAULT");
		defaultColours.setVanGogh((x2, y2, gc2) -> {
			defaultColours.alternateDraw(gc.getFont());
		});
		defaultColours.setOnMouseClicked(e -> {
			ColourSettings.setDefaultColours();
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		save = new CanvasButton(gc, 290, 20, x + 5, y + 505, "SAVE");
		save.setVanGogh((x2, y2, gc2) -> {
			save.alternateDraw(gc.getFont());
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
		
		saved = new CanvasLabel(gc, 290, 20, x + 5, y + 530, "SAVED"); 
		saved.setVanGogh((x2, y2, gc2) -> {
			saved.alternateDraw(gc.getFont());
		});
	}
	
	private void initImageSettingsMenu() {
		brightness = new CanvasLabel(gc, 290, 20, x + 5, y + 85, "BRIGHTNESS");
		brightness.setVanGogh((x2, y2, gc2) -> {
			brightness.alternateDraw(gc.getFont());
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
			clearImage.alternateDraw(gc.getFont());
		});
		clearImage.setOnMouseClicked(e -> {
			ImageSettings.clearImage();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		setImage = new CanvasButton(gc, 290, 20, x + 5, y + 205, "SET IMAGE");
		setImage.setVanGogh((x2, y2, gc2) -> {
			setImage.alternateDraw(gc.getFont());
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
			fc.setTitle("Select MarketTick Files");
			File file = fc.showOpenDialog(null);		
			if (file != null) {
				ImageSettings.setImage(file);
			}
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
		
		noImage = new CanvasLabel(gc, 290, 20, x + 5, y + 305, "NO IMAGE SELECTED");
		noImage.setVanGogh((x2, y2, gc2) -> {
			noImage.alternateDraw(gc.getFont());
		});
	}
	
	private void drawSettingsMenu() {
		previousSettings.draw();
		nextSettings.draw();
		if (settingsMenuIndex == 0) {
			drawColourSettingsMenu();
		} else if (settingsMenuIndex == 1) {
			drawImageSettingsMenu();
		}
	}
	
	private void drawColourSettingsMenu() {
		colourSettings.draw();
		colourPicker.draw();
		for (CanvasButton c : colourButtons) {
			c.draw();
		}	
		reset.draw();
		defaultColours.draw();
		save.draw();
		if (recentlySaved) {
			saved.draw();
		}
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
	
	private void initColourButtons() {
		for (int i = 0; i < ColourSettings.size(); i++) {
			CanvasButton javaisannoying = new CanvasButton(gc, 265, 20, x + 5, y + 255 + 25*i, ChartMenu.ColourButtonIndices.values()[i*2].text);
			colourButtons.add(javaisannoying);
			javaisannoying.setVanGogh((x2, y2, gc2) -> {
				javaisannoying.alternateDraw(gc2.getFont());
			});
			setMouseEvent(javaisannoying, i);
			colourButtons.add(new CanvasButton(gc, 20, 20, x + 275, y + 255 + 25*i, null));
			colourButtons.get(i*2+1).setVanGogh(cmbvg.colourPreviewVG(colourButtons.get(i*2+1), i));
		}
	}
	
	private void setMouseEvent(CanvasButton cb, int index) {
		cb.setOnMouseClicked(e -> {
			if (Chart.darkMode().get()) {
				ColourSettings.colours().set(index + 9, colourPicker.finalColour());
			} else {
				ColourSettings.colours().set(index, colourPicker.finalColour());
			}			
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayPane.drawReplayPanes();
		});
	}
	
	public void setSettingsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		chart.varLock().lock();
		try {
			sceneGraph.addNode(new TNode<ICanvasNode>(previousSettings, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(nextSettings, menuNode));
			if (settingsMenuIndex == 0) {
				setColourSettingsSceneGraph(sceneGraph, menuNode);
			} else if (settingsMenuIndex == 1) {
				setImageSettingsSceneGraph(sceneGraph, menuNode);
			}
		} finally {
			chart.varLock().unlock();
		}
	}
	
	private void setColourSettingsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
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
	
	private void setImageSettingsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
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
	
	public ColourPicker colourPicker() {
		return colourPicker;
	}

	@Override
	public void draw() {
		drawSettingsMenu();	
	}

	@Override
	public void setX(double x) {
		colourSettings.setX(x + 5);
		imageSettings.setX(x + 5);		
		
		previousSettings.setX(x + 5);
		nextSettings.setX(x + 152.5);		
		
		colourPicker.setX(x + 5);
		int i = 0;
		while (i < colourButtons.size()) {
			colourButtons.get(i).setX(x + 5);
			colourButtons.get(i + 1).setX(x + 275);
			i += 2;
		}
		defaultColours.setX(x + 152.5);		
		
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
		setSharedButtonVars();
	}

	@Override
	public void setY(double y) {
		this.y = y;
		
		colourSettings.setY(y + 35);
		imageSettings.setY(y + 35);
		
		previousSettings.setY(y + 60);
		nextSettings.setY(y + 60);		
		
		colourPicker.setY(y + 85);
		for (int i = 0; i < ColourSettings.size(); i++) {
			colourButtons.get(i*2).setY(y + 255 + 25*i);
			colourButtons.get(i*2+1).setY(y + 255 + 25*i);
		}		
		defaultColours.setY(y + 480);
		
		brightness.setY(y + 85);
		bsb.setY(y + 105);
		drawImg.setY(y + 130);
		stretch.setY(y + 155);
		setImage.setY(y + 180);
		clearImage.setY(y + 205);
		noImage.setY(y + 305);
		setSharedButtonVars();	
	}
}
