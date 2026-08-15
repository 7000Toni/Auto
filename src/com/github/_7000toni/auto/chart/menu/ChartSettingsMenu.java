package com.github._7000toni.auto.chart.menu;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.scrollbar.IScrollBarOwner;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.tabs.ColourSettingsTab;
import com.github._7000toni.auto.chart.menu.tabs.ImageSettingsTab;
import com.github._7000toni.auto.chart.menu.tabs.MiscellaneousSettingsTab;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.scene.canvas.GraphicsContext;

public class ChartSettingsMenu extends CanvasNode implements IScrollBarOwner {
	private Chart chart;
	private ChartMenu chartMenu;
	
	private CanvasButton previousSettings;
	private CanvasButton nextSettings;
	
	private ColourSettingsTab cst;
	private ImageSettingsTab ist;
	private MiscellaneousSettingsTab mst;
	
	private int settingsMenuIndex = 0;
	
	public ChartSettingsMenu(double x, double y, double width, double height, ChartMenu chartMenu, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		this.chartMenu = chartMenu;		
		this.cst = new ColourSettingsTab(x, y, width, height, chartMenu, gc, chart, cmbvg);
		this.ist = new ImageSettingsTab(x, y, width, height, chartMenu, gc, chart, cmbvg);	
		this.mst = new MiscellaneousSettingsTab(x, y, width, height, chartMenu, gc, chart, cmbvg);	
		initSettingsMenu();
	}
	
	private void initSettingsMenu() {
		previousSettings = new CanvasButton(gc, 142.5, 20, x + 5, y + 60, "PREVIOUS");
		previousSettings.setVanGogh((x2, y2, gc2) -> {
			previousSettings.defaultDraw(gc.getFont());
		});
		previousSettings.setOnMouseClicked(e -> {
			settingsMenuIndex = settingsMenuIndex==0?2:Math.abs((settingsMenuIndex - 1) % 2);
			chartMenu.setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});
		
		nextSettings = new CanvasButton(gc, 142.5, 20, x + 152.5, y + 60, "NEXT");	
		nextSettings.setVanGogh((x2, y2, gc2) -> {
			nextSettings.defaultDraw(gc.getFont());
		});
		nextSettings.setOnMouseClicked(e -> {
			settingsMenuIndex = Math.abs((settingsMenuIndex + 1) % 3); 
			chartMenu.setSettingsMenuSceneGraph(chart.sceneGraph(), chart.menuNode());
		});				
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
		chart.varLock().lock();
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
			chart.varLock().unlock();
		}
	}
	
	public Chart chart() {
		return chart;
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
		nextSettings.setX(x + 152.5);	
		
		cst.setX(x);
		ist.setX(x);
		mst.setX(x);
		this.x = x;
	}

	@Override
	public void setY(double y) {			
		previousSettings.setY(y + 60);
		nextSettings.setY(y + 60);	
		
		cst.setY(y);
		ist.setY(y);
		mst.setY(y);
		this.y = y;	
	}
}
