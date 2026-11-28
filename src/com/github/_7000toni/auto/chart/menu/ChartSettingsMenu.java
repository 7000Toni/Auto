package com.github._7000toni.auto.chart.menu;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.tabs.ColourSettingsTab;
import com.github._7000toni.auto.chart.menu.tabs.ImageSettingsTab;
import com.github._7000toni.auto.chart.menu.tabs.MiscellaneousSettingsTab;
import com.github._7000toni.auto.menu.SettingsPane;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class ChartSettingsMenu extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private SettingsPane sp;
	private ChartMenu chartMenu;
	
	private CanvasButton previousSettings;
	private CanvasButton nextSettings;
	
	private ColourSettingsTab cst;
	private ImageSettingsTab ist;
	private MiscellaneousSettingsTab mst;
	
	private int settingsMenuIndex = 0;
	
	public ChartSettingsMenu(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, SettingsPane sp, ChartMenuButtonVanGoghs cmbvg) {
		constructorStuff(x, y, width, height, chartMenu, gc, null, sp, cmbvg);
	}
	
	public ChartSettingsMenu(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		constructorStuff(x, y, width, height, chartMenu, gc, chart, null, cmbvg);
	}
	
	private void constructorStuff(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, SettingsPane sp, ChartMenuButtonVanGoghs cmbvg) {
		this.x.set(x);
		this.y.set(y);
		this.width.set(width);
		this.height.set(height);
		this.gc = gc;
		this.chart = chart;
		this.sp = sp;
		this.chartMenu = chartMenu;		
		if (chart != null) {
			this.cst = new ColourSettingsTab(x, y, width, height, chartMenu, gc, chart, cmbvg);
			this.ist = new ImageSettingsTab(x, y, width, height, chartMenu, gc, chart, cmbvg);	
			this.mst = new MiscellaneousSettingsTab(x, y, width, height, chartMenu, gc, chart, cmbvg);
		} else {
			this.cst = new ColourSettingsTab(x, y, width, height, chartMenu, gc, sp, cmbvg);
			this.ist = new ImageSettingsTab(x, y, width, height, chartMenu, gc, sp, cmbvg);	
			this.mst = new MiscellaneousSettingsTab(x, y, width, height, chartMenu, gc, sp, cmbvg);
		}
		initSettingsMenu();
	}
	
	private void initSettingsMenu() {
		previousSettings = new CanvasButton(gc, 142, 20, x.get() + 5, y.get() + 60, "PREVIOUS");
		previousSettings.setVanGogh((x2, y2, gc2) -> {
			previousSettings.defaultDraw(gc.getFont());
		});
		previousSettings.setOnMouseClicked(e -> {
			settingsMenuIndex = settingsMenuIndex==0?2:(settingsMenuIndex - 1);
			setSceneGraph();
		});
		
		nextSettings = new CanvasButton(gc, 142, 20, x.get() + 153, y.get() + 60, "NEXT");	
		nextSettings.setVanGogh((x2, y2, gc2) -> {
			nextSettings.defaultDraw(gc.getFont());
		});
		nextSettings.setOnMouseClicked(e -> {
			settingsMenuIndex = Math.abs((settingsMenuIndex + 1) % 3);
			setSceneGraph();
		});				
	}
	
	private void setSceneGraph() {
		if (chart != null) {
			chartMenu.setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		} else {
			chartMenu.setSettingsMenuSceneGraph(sp.sceneGraph(), sp.menuNode());
		}
	}
	
	private void drawSettingsMenu() {
		previousSettings.draw();
		nextSettings.draw();
		if (settingsMenuIndex == 0) {
			cst.draw();
		} else if (settingsMenuIndex == 1) {
			ist.draw();
		} else {
			mst.draw();
		}
	}
	
	public void setSettingsMenuSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		lock();
		try {
			sceneGraph.addNode(new TNode<ICanvasNode>(previousSettings, menuNode));
			sceneGraph.addNode(new TNode<ICanvasNode>(nextSettings, menuNode));
			if (settingsMenuIndex == 0) {
				cst.setColourSettingsSceneGraph(sceneGraph, menuNode);
			} else if (settingsMenuIndex == 1) {
				ist.setImageSettingsSceneGraph(sceneGraph, menuNode);
			} else {
				mst.setMiscellaneousSettingsSceneGraph(sceneGraph, menuNode);
			}
		} finally {
			unlock();
		}
	}
	
	private void lock() {
		if (chart != null) {
			chart.varLock().lock();
		} else {
			sp.varLock().lock();
		}
	}
	
	private void unlock() {
		if (chart != null) {
			chart.varLock().unlock();
		} else {
			sp.varLock().unlock();
		}
	}
	
	public Chart chart() {
		return chart;
	}	
	
	public SettingsPane settingsPane() {
		return sp;
	}
	
	public ColourSettingsTab colourSettingsTab() {
		return cst;
	}
	
	public ImageSettingsTab imageSettingsTab() {
		return ist;
	}

	public MiscellaneousSettingsTab miscellaneousSettingsTab() {
		return mst;
	}
	
	@Override
	public void draw() {
		drawSettingsMenu();	
	}

	@Override
	public void setX(double x) {previousSettings.setX(x + 5);
		nextSettings.setX(x + 153);	
		
		cst.setX(x);
		ist.setX(x);
		mst.setX(x);
		this.x.set(x);
	}

	@Override
	public void setY(double y) {			
		previousSettings.setY(y + 60);
		nextSettings.setY(y + 60);	
		
		cst.setY(y);
		ist.setY(y);
		mst.setY(y);
		this.y.set(y);	
	}
}
