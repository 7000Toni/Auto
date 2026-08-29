package com.github._7000toni.auto.chart.menu.tabs;
import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.DirectoryChooser;

import java.io.File;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.HorizontalScrollBar;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenu;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.chart.menu.ChartSettingsMenu;
import com.github._7000toni.auto.marketreplay.MarketReplayNode;
import com.github._7000toni.auto.menu.Menu;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.ImageSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;
import com.github._7000toni.auto.settings.MiscellaneousSettings;
import com.github._7000toni.auto.settings.Settings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

public class MiscellaneousSettingsTab extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenuButtonVanGoghs cmbvg;
	private ChartMenu chartMenu;
	
	private CanvasLabel miscellaneousSettings;
	private CanvasButton reset;
	private CanvasButton defaultMiscellaneousSettings;
	private CanvasButton save;
	private BooleanProperty recentlySaved = new SimpleBooleanProperty(false);
	
	private CanvasLabel arcW;
	private CanvasLabel arcH;
	private CanvasButton setInitFileDir;
	private HorizontalScrollBar arcWSB;
	private HorizontalScrollBar arcHSB;	
	private CanvasLabel tradeButtonOffset;
	private HorizontalScrollBar tboSB;
	
	public MiscellaneousSettingsTab(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.chartMenu = chartMenu;
		this.gc = gc;
		this.chart = chart;
		this.cmbvg = cmbvg;
		
		initMiscellaneousSettingsMenu();
	}
	
	private void initMiscellaneousSettingsMenu() {
		miscellaneousSettings = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "MISCELLANEOUS SETTINGS");
		miscellaneousSettings.setVanGogh((x2, y2, gc2) -> {
			miscellaneousSettings.defaultDraw(gc.getFont());
		});
				
		setInitFileDir = new CanvasButton(gc, 290, 20, x + 5, y + 85, "SET INIT LOAD FILE DIR");
		setInitFileDir.setVanGogh((x2, y2, gc2) -> {
			setInitFileDir.defaultDraw(gc.getFont());
		});
		setInitFileDir.setOnMouseClicked(e -> {
			DirectoryChooser dc = new DirectoryChooser();
			dc.setInitialDirectory(new File("./"));
	        dc.setTitle("Select New File Directory");

			File dir = dc.showDialog(null);
			if (dir != null) {
				MiscellaneousSettings.setInitFileDir(dir.getAbsolutePath());
			}
		});		
		
		arcW = new CanvasLabel(gc, 290, 20, x + 5, y + 110, "ARC WIDTH");
		arcW.setVanGogh((x2, y2, gc2) -> {
			arcW.defaultDraw(gc.getFont());
		});
		arcWSB = new HorizontalScrollBar(this, x + 5, x + 295, 20, 10, y + 135);
		arcWSB.setX(x + 5 + (MiscellaneousSettings.arcW() / 20) * 270);
		arcWSB.percentage().addListener((observable, oldValue, newValue) -> {
			MiscellaneousSettings.setArcW(20 * newValue.doubleValue());
		});
		
		arcH = new CanvasLabel(gc, 290, 20, x + 5, y + 155, "ARC HEIGHT");
		arcH.setVanGogh((x2, y2, gc2) -> {
			arcH.defaultDraw(gc.getFont());
		});
		arcHSB = new HorizontalScrollBar(this, x + 5, x + 295, 20, 10, y + 180);
		arcHSB.setX(x + 5 + (MiscellaneousSettings.arcH() / 20) * 270);
		arcHSB.percentage().addListener((observable, oldValue, newValue) -> {
			MiscellaneousSettings.setArcH(20 * newValue.doubleValue());
		});
		
		tradeButtonOffset = new CanvasLabel(gc, 290, 20, x + 5, y + 200, "TRADE BUTTON OFFSET");
		tradeButtonOffset.setVanGogh((x2, y2, gc2) -> {
			tradeButtonOffset.defaultDraw(gc.getFont());
		});
		tboSB = new HorizontalScrollBar(this, x + 5, x + 295, 20, 10, y + 225);
		tboSB.setX(x + 5 + MiscellaneousSettings.tradeButtonOffset() * 270);
		tboSB.percentage().addListener((observable, oldValue, newValue) -> {
			MiscellaneousSettings.setTradeButtonOffset(newValue.doubleValue());
			for (Chart c : Chart.charts()) {
				if (c.chartNode().replayMode()) {
					c.chartNode().tradeButtons().resetButtons();
				}
			}
		});
		
		reset = new CanvasButton(gc, 142.5, 20, x + 5, y + 245, "RESET");
		reset.setVanGogh((x2, y2, gc2) -> {
			reset.defaultDraw(gc.getFont());
		});
		reset.setOnMouseClicked(e -> {
			Settings.loadSettings();
			chartMenu.chartSettingsMenu().imageSettingsTab().bsb().setX(x + ((ImageSettings.brightness() + 1) / 2) * 270);
			arcWSB.setX(x + 5 + (MiscellaneousSettings.arcW() / 20) * 270);
			arcHSB.setX(x + 5 + (MiscellaneousSettings.arcH() / 20) * 270);
			tboSB.setX(x + 5 + MiscellaneousSettings.tradeButtonOffset() * 270);
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayNode.drawReplayNodes();
		});
		
		defaultMiscellaneousSettings = new CanvasButton(gc, 142.5, 20, x + 152.5, y + 245, "DEFAULT");
		defaultMiscellaneousSettings.setVanGogh((x2, y2, gc2) -> {
			defaultMiscellaneousSettings.defaultDraw(gc.getFont());
		});
		defaultMiscellaneousSettings.setOnMouseClicked(e -> {
			MiscellaneousSettings.setDefaultSettings();
			arcWSB.setX(x + 5 + (MiscellaneousSettings.arcW() / 20) * 270);
			arcHSB.setX(x + 5 + (MiscellaneousSettings.arcH() / 20) * 270);
			tboSB.setX(x + 5 + MiscellaneousSettings.tradeButtonOffset() * 270);
			Menu.menu().draw();
			Chart.drawCharts(null);
			MarketReplayNode.drawReplayNodes();
		});
		
		save = new CanvasButton(gc, 290, 20, x + 5, y + 270, "SAVE");
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
	
	public HorizontalScrollBar arcWSB() {
		return arcWSB;
	}
	
	public HorizontalScrollBar arcHSB() {
		return arcHSB;
	}
	
	public HorizontalScrollBar tboSB() {
		return tboSB;
	}
	
	private void drawHSBRects() {
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		gc.strokeRoundRect(x + 5.5, arcWSB.y() + 0.5, 290, 9, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		gc.strokeRoundRect(x + 5.5, arcHSB.y() + 0.5, 290, 9, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		gc.strokeRoundRect(x + 5.5, tboSB.y() + 0.5, 290, 9, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
	}
	
	private void drawMiscellaneousSettingsMenu() {
		drawHSBRects();
		miscellaneousSettings.draw();
		setInitFileDir.draw();
		arcW.draw();
		arcWSB.draw();
		arcH.draw();
		arcHSB.draw();
		tradeButtonOffset.draw();
		tboSB.draw();
		reset.draw();
		defaultMiscellaneousSettings.draw();
		save.draw();
	}
	
	public void setMiscellaneousSettingsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(miscellaneousSettings, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(setInitFileDir, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(arcW, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(arcWSB, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(arcH, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(arcHSB, menuNode));	
		sceneGraph.addNode(new TNode<ICanvasNode>(tradeButtonOffset, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(tboSB, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(reset, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(defaultMiscellaneousSettings, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(save, menuNode));
	}
	
	public Chart chart() {
		return chart;
	}

	@Override
	public void draw() {
		Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (callerClass != ChartSettingsMenu.class) {
			chart.draw();
		} else {
			drawMiscellaneousSettingsMenu();
		}
	}

	@Override
	public void setX(double x) {
		miscellaneousSettings.setX(x + 5);				
		setInitFileDir.setX(x + 5);
		
		arcW.setX(x + 5);
		double hsbOffset = arcWSB.x() - this.x;
		arcWSB.setMinPos(x + 5);
		arcWSB.setMaxPos(x + 295);
		arcWSB.setX(hsbOffset + x);
		
		arcH.setX(x + 5);
		double hsbOffset2 = arcHSB.x() - this.x;
		arcHSB.setMinPos(x + 5);
		arcHSB.setMaxPos(x + 295);
		arcHSB.setX(hsbOffset2 + x);
		
		tradeButtonOffset.setX(x + 5);
		double hsbOffset3 = tboSB.x() - this.x;
		tboSB.setMinPos(x + 5);
		tboSB.setMaxPos(x + 295);
		tboSB.setX(hsbOffset3 + x);
		
		reset.setX(x + 5);
		defaultMiscellaneousSettings.setX(x + 152.5);
		save.setX(x + 5);
		
		this.x = x;
	}

	@Override
	public void setY(double y) {				
		miscellaneousSettings.setY(y + 35);		
		
		setInitFileDir.setY(y + 85);
		arcW.setY(y + 110);
		arcWSB.setY(y + 135);
		arcH.setY(y + 155);
		arcHSB.setY(y + 180);
		tradeButtonOffset.setY(y + 200);
		tboSB.setY(y + 225);
		
		reset.setY(y + 245);
		defaultMiscellaneousSettings.setY(y + 245);
		save.setY(y + 270);
		
		this.y = y;
	}
}
