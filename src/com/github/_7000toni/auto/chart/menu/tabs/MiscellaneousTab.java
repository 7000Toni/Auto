package com.github._7000toni.auto.chart.menu.tabs;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.TextBox;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.miscellaneous.RandomFunctions;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;

public class MiscellaneousTab extends CanvasNode {
	private Chart chart;
	
	private CanvasLabel miscellaneousFunctions;
	private TextBox txtContract;
	private CanvasButton databendoOptimizer;
	private CanvasButton mergeFiles;
	private String oldText = "";
	
	public MiscellaneousTab(double x, double y, double width, double height, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		this.chart = chart;
		
		initMiscellaneousMenu(cmbvg);
	}
	
	private void initMiscellaneousMenu(ChartMenuButtonVanGoghs cmbvg) {
		miscellaneousFunctions = new CanvasLabel(gc, 290, 20, x + 5, y + 35, "MISCELLANEOUS FUNCTIONS");
		miscellaneousFunctions.setVanGogh((x2, y2, gc2) -> {
			miscellaneousFunctions.defaultDraw(gc.getFont());
		});	
		
		txtContract = new TextBox(chart.stage(), gc, 100, 20, x + 5, y + 85, "", TextBox.InputType.ANY, false, true, false);
		setTextEvents();
		
		IntegerProperty doProg = new SimpleIntegerProperty(0);
		databendoOptimizer = new CanvasButton(gc, 185, 20, x + 110, y + 85, "DATABENDO OPTIMIZER");
		databendoOptimizer.setVanGogh(cmbvg.databendoOptimizerVG(databendoOptimizer, doProg));
		databendoOptimizer.setOnMouseClicked(e -> {
			String contract = null;
			if (!txtContract.text().equals("")) {
				contract = txtContract.text();
			}
			RandomFunctions.databendoOptimizer(contract, doProg, chart.chartNode());
		});
		
		IntegerProperty mfProg = new SimpleIntegerProperty(-2);
		mergeFiles = new CanvasButton(gc, 290, 20, x + 5, y + 110, "MERGE FILES");
		mergeFiles.setVanGogh(cmbvg.mergeFilesVG(mergeFiles, mfProg));
		mergeFiles.setOnMouseClicked(e -> {
			RandomFunctions.mergeFiles(mfProg, chart.chartNode());
		});
	}
	
	private void setTextEvents() {
		txtContract.setOnKeyTyped(e -> {
			if (txtContract.text().length() > 7) {
				txtContract.setText(oldText);
			} else {
				oldText = txtContract.text();
			}
		});
	}
	
	private void drawMiscellaneousFunctionsMenu() {
		miscellaneousFunctions.draw();
		txtContract.draw();
		databendoOptimizer.draw();
		mergeFiles.draw();
	}
	
	public void setMiscellaneousFunctionsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(miscellaneousFunctions, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(txtContract, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(databendoOptimizer, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(mergeFiles, menuNode));
	}
	
	public Chart chart() {
		return chart;
	}

	@Override
	public void draw() {		
		drawMiscellaneousFunctionsMenu();
	}

	@Override
	public void setX(double x) {	
		miscellaneousFunctions.setX(x + 5);
		txtContract.setX(x + 5);
		databendoOptimizer.setX(x + 110);
		mergeFiles.setX(x + 5);
		
		this.x = x;
	}

	@Override
	public void setY(double y) {
		miscellaneousFunctions.setY(y + 35);
		txtContract.setY(y + 85);
		databendoOptimizer.setY(y + 85);
		mergeFiles.setY(y + 110);
		
		this.y = y;
	}
}
