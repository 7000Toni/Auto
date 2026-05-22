import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;

public class ColourSettingsTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	private ChartMenu chartMenu;
	
	private CanvasLabel colourSettings;
	
	private ColourPicker colourPicker;
	private CanvasButton reset;
	private CanvasButton defaultColours;
	
	private CanvasButton save;
	private CanvasLabel saved;
	
	private ArrayList<CanvasButton> colourButtons;
	private boolean recentlySaved = false;
	
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
			colourSettings.alternateDraw(gc.getFont());
		});
		
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
	
	public void drawColourSettingsMenu() {
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
		saved.setX(x + 5);
		
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
		this.y = y;
		
		colourSettings.setY(y + 35);
		reset.setY(y + 480);
		save.setY(y + 505);
		saved.setY(y + 530);
		
		colourSettings.setY(y + 35);	
		
		colourPicker.setY(y + 85);
		for (int i = 0; i < ColourSettings.size(); i++) {
			colourButtons.get(i*2).setY(y + 255 + 25*i);
			colourButtons.get(i*2+1).setY(y + 255 + 25*i);
		}		
		defaultColours.setY(y + 480);
	}
}
