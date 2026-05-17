import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ChartMenu extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private boolean replayMode = false;
	private ChartMenuButtonVanGoghs cmbvg;
	
	private CanvasLabel general;
	private CanvasLabel timeFrames;
	private CanvasLabel marketReplay;
	
	private CanvasButton chartFunctions;
	private CanvasButton chartSettings;
	private CanvasButton previous;
	private CanvasButton next;
	private CanvasLabel colourSettings;
	private CanvasLabel imageSettings;
	
	private CanvasButton newChart;
	private CanvasButton chartType;
	private CanvasButton darkMode;
	private CanvasButton chartTypeShortcut;	
	private CanvasButton initHst;
	private CanvasButton toggleHst;
	
	private CanvasButton saveHst;
	private CanvasButton replayShortcut;
	private CanvasButton toggleShortReport;
	private CanvasButton saveMRHst;
	
	private boolean functions;
	
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
	
	private boolean recentlySaved = false;
	private BooleanProperty mrRecentlySaved = new SimpleBooleanProperty(false);
	private ArrayList<CanvasButton> colourButtons;
	private int settingsMenuIndex = 0;
	
	public enum ColourButtonIndices {
		UP_CANDLESTICK_FILL(0, "UP CANDLESTICK FILL"),
		UCF_PREVIEW(0, null),
		UP_CANDLESTICK_STROKE(1, "UP CANDLESTICK STROKE"),
		UCS_PREVIEW(1, null),
		DOWN_CANDLESTICK_FILL(2, "DOWN CANDLESTICK FILL"),	
		DCF_PREVIEW(2, null),
		DOWN_CANDLESTICK_STROKE(3, "DOWN CANDLESTICK STROKE"),
		DCS_PREVIEW(3, null),
		LINE_CHART(4, "LINE CHART"),
		LC_PREVIEW(4, null),
		MENU_BACKGROUND(5, "MENU BACKGROUND"),
		MB_PREVIEW(5, null),
		CHART_BACKGROUND(6, "CHART BACKGROUND"),
		CB_PREVIEW(6, null),
		MISCELLANEOUS_1(7, "MISCELLANEOUS 1"),
		MISC1_PREIVEW(7, null),
		MISCELLANEOUS_2(8, "MISCELLANEOUS 2"),
		MISC2_PREIVEW(8, null);
		
		public final int index;
		public final String text;
		
		private ColourButtonIndices(int index, String text) {
			this.index = index;
			this.text = text;
		}
	}
	
	public ChartMenu(double x, double y, double width, double height, GraphicsContext gc, Chart chart) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		cmbvg = new ChartMenuButtonVanGoghs();
		
		initFunctionsMenu();
		initSettingsMenu();		
		setReplayMode(chart.replayMode());
	}
	
	private void initFunctionsMenu() {
		general = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "GENERAL");
		general.setVanGogh((x2, y2, gc2) -> {
			general.alternateDraw(gc.getFont());
		});		
		
		chartFunctions = new CanvasButton(gc, 142, 20, x + 5, y + 5, "FUNCTIONS");
		chartFunctions.setVanGogh(cmbvg.menuButtonVG(chartFunctions, gc.getFont().getSize()));
		chartFunctions.setOnMouseClicked(e -> {
			chartFunctions.setOn(true);
			chartSettings.setOn(false);
			functions = true;
			setFunctionsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});				
		
		chartSettings = new CanvasButton(gc, 142, 20, x + 153, y + 5, "SETTINGS");
		chartSettings.setVanGogh(cmbvg.menuButtonVG(chartSettings, gc.getFont().getSize()));
		chartSettings.setOnMouseClicked(e -> {
			chartFunctions.setOn(false);
			chartSettings.setOn(true);
			functions = false;
			setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});
		
		newChart = new CanvasButton(gc, 290, 20, x + 5, y + 60, "NEW CHART");
		newChart.setVanGogh((x2, y2, gc2) -> {
			newChart.alternateDraw(gc.getFont());
		});
		newChart.setOnMouseClicked(e -> {
			Stage s = new Stage();
			if (Main.icon() != null) {
				s.getIcons().add(Main.icon());
			}
			s.setTitle(chart.name());
			ChartPane cpane = new ChartPane(s, chart.width(), chart.height(), chart.data(), replayMode, chart.mr(), chart.mrp());			
			Scene scene = new Scene(cpane);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> cpane.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		});
		
		chartType = new CanvasButton(gc, 290, 20, x + 5, y + 85, "CANDLESTICK CHART");
		chartType.setVanGogh(cmbvg.toggleVG(chartType, chart.drawCandlesticks(), "LINE CHART", "CANDLESTICK CHART"));
		chartType.setOnMouseClicked(e -> {
			chart.toggleChartType();
		});
		
		darkMode = new CanvasButton(gc, 290, 20, x + 5, y + 110, "DARK MODE");
		darkMode.setVanGogh(cmbvg.toggleVG(darkMode, Chart.darkMode(), "LIGHT MODE", "DARK MODE"));
		darkMode.setOnMouseClicked(e -> {
			Chart.toggleDarkMode();
		});
		
		chartTypeShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 135, "CHART TYPE SHORTCUT");
		chartTypeShortcut.setVanGogh((x2, y2, gc2) -> {
			chartTypeShortcut.alternateDraw(gc.getFont());
		});
		chartTypeShortcut.setOnMouseClicked(e -> {
			chart.toggleChartTypeShortcut();
		});
		
		initHst = new CanvasButton(gc, 290, 20, x + 5, y + 160, "LOAD HISTORY");
		initHst.setVanGogh((x2, y2, gc2) -> {
			initHst.alternateDraw(gc.getFont());
		});
		initHst.setOnMouseClicked(e -> {
			chart.initHst();
		});;
		
		toggleHst = new CanvasButton(gc, 290, 20, x + 5, y + 185, "SHOW TRADE HISTORY");
		toggleHst.setVanGogh(cmbvg.toggleVG(toggleHst, chart.plotHst(), "HIDE TRADE HISTORY", "SHOW TRADE HISTORY"));
		toggleHst.setOnMouseClicked(e -> {
			chart.toggleHst();
		});;
		
		marketReplay = new CanvasLabel(gc, 290, 20, x + 5, y + 210, "MARKET REPLAY");
		marketReplay.setVanGogh((x2, y2, gc2) -> {
			marketReplay.alternateDraw(gc.getFont());
		});
		
		replayShortcut = new CanvasButton(gc, 290, 20, x + 5, y + 235, "REPLAY SHORTCUT");
		replayShortcut.setVanGogh((x2, y2, gc2) -> {
			replayShortcut.alternateDraw(gc.getFont());
		});
		replayShortcut.setOnMouseClicked(e -> {
			chart.toggleMRPShortcut();
		});;		
		
		saveHst = new CanvasButton(gc, 290, 20, x + 5, y + 260, "DON'T SAVE TRADE HISTORY");
		saveHst.setVanGogh(cmbvg.toggleVG(saveHst, MarketReplay.writeToFile(), "DON'T SAVE TRADE HISTORY", "SAVE TRADE HISTORY"));
		saveHst.setOnMouseClicked(e -> {
			MarketReplay.toggleWriteToFile();
		});;
		
		toggleShortReport = new CanvasButton(gc, 290, 20, x + 5, y + 285, "WRITE LONG REPORT");
		toggleShortReport.setVanGogh(cmbvg.toggleVG(toggleShortReport, Trade.shortReport(), "WRITE LONG REPORT", "WRITE SHORT REPORT"));
		toggleShortReport.setOnMouseClicked(e -> {
			Trade.toggleShortReport();
		});;
		
		saveMRHst = new CanvasButton(gc, 290, 20, x + 5, y + 310, "SAVE LOADABLE HISTORY");
		saveMRHst.setVanGogh(cmbvg.toggleVG(saveMRHst, mrRecentlySaved, "SAVED", "SAVE LOADABLE HISTORY"));
		saveMRHst.setOnMouseClicked(e -> {
			chart.marketReplay().trade().writeHistoryToFile();
			mrRecentlySaved.set(true);
			new AnimationTimer() {
				private long init = 0;				
				@Override
				public void handle(long now) {
					if (init == 0) {
						init = now;
					}
					if ((now - init) / HorizontalScrollBar.NANO_TO_MILLI > 1500) {
						mrRecentlySaved.set(false);
						chart.draw();
						this.stop();
					}
				}
			}.start();
		});;
		
		timeFrames = new CanvasLabel(gc, 290, 20, x + 5, y + 335, "TIME FRAMES");
		timeFrames.setVanGogh((x2, y2, gc2) -> {
			timeFrames.alternateDraw(gc.getFont());
		});
				
		chartFunctions.setOn(true);
		functions = true;
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
		
		previous = new CanvasButton(gc, 142.5, 20, x + 5, y + 60, "PREVIOUS");
		previous.setVanGogh((x2, y2, gc2) -> {
			previous.alternateDraw(gc.getFont());
		});
		previous.setOnMouseClicked(e -> {
			settingsMenuIndex = Math.abs((settingsMenuIndex - 1) % 2); 
			setSharedButtonVars();
			setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});
		
		next = new CanvasButton(gc, 142.5, 20, x + 152.5, y + 60, "NEXT");	
		next.setVanGogh((x2, y2, gc2) -> {
			next.alternateDraw(gc.getFont());
		});
		next.setOnMouseClicked(e -> {
			settingsMenuIndex = Math.abs((settingsMenuIndex + 1) % 2); 
			setSharedButtonVars();
			setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});
		
		initColoursSettingsMenu();
		initImageSettingsMenu();
	}
	
	private void initColoursSettingsMenu() {
		colourPicker = new ColourPicker(x + 5, y + 85, 290, 165, gc, this);
		
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
		previous.draw();
		next.draw();
		if (settingsMenuIndex == 0) {
			colourSettings.draw();
			drawColourSettingsMenu();
		} else {
			imageSettings.draw();
			drawImageSettingsMenu();
		}
	}
	
	private void drawColourSettingsMenu() {
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
	
	private void drawFunctionsMenu() {
		general.draw();
		newChart.draw();
		chartType.draw();
		darkMode.draw();
		chartTypeShortcut.draw();		
		initHst.draw();
		toggleHst.draw();
		marketReplay.draw();
		replayShortcut.draw();
		saveHst.draw();
		toggleShortReport.draw();
		saveMRHst.draw();
		//timeFrames.draw();
	}
	
	private void initColourButtons() {
		for (int i = 0; i < ColourSettings.size(); i++) {
			CanvasButton javaisannoying = new CanvasButton(gc, 265, 20, x + 5, y + 255 + 25*i, ColourButtonIndices.values()[i*2].text);
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
	
	public void setFunctionsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		chart.varLock().lock();
		try {
			menuNode.removeAllChildren();
			sceneGraph.addNode(new TNode<ICanvasNode>(chartFunctions, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(chartSettings, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(general, menuNode)); 
			sceneGraph.addNode(new TNode<ICanvasNode>(newChart, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(chartType, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(darkMode, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(chartTypeShortcut, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(initHst, menuNode)); 
			sceneGraph.addNode(new TNode<ICanvasNode>(toggleHst, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(marketReplay, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(replayShortcut, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(saveHst, menuNode)); 
			sceneGraph.addNode(new TNode<ICanvasNode>(toggleShortReport, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(saveMRHst, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(timeFrames, menuNode)); 
		} finally {
			chart.varLock().unlock();
		}
	}
	
	public void setSettingsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		chart.varLock().lock();
		try {
			menuNode.removeAllChildren();
			sceneGraph.addNode(new TNode<ICanvasNode>(chartFunctions, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(chartSettings, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(previous, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(next, menuNode));
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
	
	public CanvasButton chartFunctions() {
		return chartFunctions;
	}
	
	public CanvasButton chartSettings() {
		return chartSettings;
	}
	
	public CanvasButton newChart() {
		return newChart;
	}
	
	public CanvasButton chartType() {
		return chartType;
	}
	
	public CanvasButton darkMode() {
		return darkMode;
	}
	
	public CanvasButton chartTypeShortcut() {
		return chartTypeShortcut;
	}
	
	public CanvasButton replayShortcut() {
		return replayShortcut;
	}
	
	public ColourPicker colourPicker() {
		return colourPicker;
	}
	
	public void setReplayMode(boolean replayMode) {
		this.replayMode = replayMode;
		if (this.replayMode) {
			replayShortcut.enable();
			saveHst.enable();
			toggleShortReport.enable();
			saveMRHst.enable();
			
		} else {
			replayShortcut.disable();
			saveHst.disable();
			toggleShortReport.disable();
			saveMRHst.disable();
		}
	}

	@Override
	public void draw() {
		chartFunctions.draw();
		chartSettings.draw();
		if (functions) {		
			drawFunctionsMenu();
		} else {
			drawSettingsMenu();					
		}
	}

	@Override
	public void setX(double x) {
		chartFunctions.setX(x + 5);
		chartSettings.setX(x + 152.5);
		
		colourSettings.setX(x + 5);
		imageSettings.setX(x + 5);
		previous.setX(x + 5);
		next.setX(x + 152.5);		
		
		general.setX(x + 5);
		newChart.setX(x + 5);
		chartType.setX(x + 5);
		darkMode.setX(x + 5);
		chartTypeShortcut.setX(x + 5);
		initHst.setX(x + 5);
		toggleHst.setX(x + 5);
		marketReplay.setX(x + 5);
		replayShortcut.setX(x + 5);
		saveHst.setX(x + 5);
		toggleShortReport.setX(x + 5);
		saveMRHst.setX(x + 5);
		timeFrames.setX(x + 5);
		
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
		chartFunctions.setY(y + 5);
		chartSettings.setY(y + 5);
		
		colourSettings.setY(y + 35);
		imageSettings.setY(y + 35);
		previous.setY(y + 60);
		next.setY(y + 60);		
		
		general.setY(y + 35);
		newChart.setY(y + 60);
		chartType.setY(y + 85);
		darkMode.setY(y + 110);
		chartTypeShortcut.setY(y + 135);		
		initHst.setY(y + 185);
		toggleHst.setY(y + 210);
		marketReplay.setY(y + 210);
		replayShortcut.setY(y + 235);
		saveHst.setY(y + 260);
		toggleShortReport.setY(y + 285);
		saveMRHst.setY(y + 310);
		timeFrames.setY(y + 335);
		
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
