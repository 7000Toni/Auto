package com.github._7000toni.auto.chart.menu.tabs;

import java.io.File;
import java.util.List;

import com.github._7000toni.auto.canvasnode.CanvasLabel;
import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.canvasnode.TextBox;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.menu.ChartMenuButtonVanGoghs;
import com.github._7000toni.auto.dataset.OptimizeTask;
import com.github._7000toni.auto.menu.SettingsPane;
import com.github._7000toni.auto.miscellaneous.RandomFunctions;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.MiscellaneousSettings;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

public class MiscellaneousTab extends CanvasNode {
	private Chart chart;
	private SettingsPane sp;
	
	private CanvasLabel miscellaneousFunctions;
	private TextBox txtContract;
	private CanvasButton databendoOptimizer;
	private CanvasButton mergeFiles;
	private CanvasButton optimize;
	private String oldText = "";
	
	private IntegerProperty doProg = new SimpleIntegerProperty(0);
	private IntegerProperty mfProg = new SimpleIntegerProperty(-2);
	private IntegerProperty numJobs = new SimpleIntegerProperty();
	
	private IVanGogh optimizeVG = (x, y, gc) -> {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.BOLD, optimize.height() - 8));
		if (numJobs.get() > 0) {
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_1));
		} else {
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.TEXT_AND_STUFF));
		}		
		if (optimize.hover()) {
			gc.setFill(Color.GRAY);
		}
		if (optimize.pressed()) {
			gc.setFill(Color.DIMGRAY);
		}
		if (!optimize.enabled()) {
			gc.setFill(Color.LIGHTGRAY);
		}
		gc.fillRoundRect(x, y, optimize.width(), optimize.height(), MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		optimize.setColoursText();
		optimize.calculateOffsets(gc.getFont());
		gc.fillText(optimize.text(), x + optimize.textXOffset(), y + optimize.textYOffset());		
		gc.setFont(oldFont);
	};
	
	public MiscellaneousTab(double x, double y, double width, double height, GraphicsContext gc, SettingsPane sp, ChartMenuButtonVanGoghs cmbvg) {
		constructorStuff(x, y, width, height, gc, null, sp, cmbvg);
	}
	
	public MiscellaneousTab(double x, double y, double width, double height, GraphicsContext gc, Chart chart, ChartMenuButtonVanGoghs cmbvg) {
		constructorStuff(x, y, width, height, gc, chart, null, cmbvg);
	}
	
	private void constructorStuff(double x, double y, double width, double height, GraphicsContext gc, Chart chart, SettingsPane sp, ChartMenuButtonVanGoghs cmbvg) {
		this.x.set(x);
		this.y.set(y);
		this.width.set(width);
		this.height.set(height);
		this.gc = gc;
		this.chart = chart;
		this.sp = sp;
		
		initMiscellaneousMenu(cmbvg);
	}
	
	private void initMiscellaneousMenu(ChartMenuButtonVanGoghs cmbvg) {
		miscellaneousFunctions = new CanvasLabel(gc, 290, 20, x.get() + 5, y.get() + 35, "MISCELLANEOUS FUNCTIONS");
		miscellaneousFunctions.setVanGogh((x2, y2, gc2) -> {
			miscellaneousFunctions.defaultDraw(gc.getFont());
		});	
		
		optimize = new CanvasButton(gc, 290, 20, x.get() + 5, y.get() + 85, "OPTIMIZE");
		optimize.setVanGogh(optimizeVG);
		optimize.setOnMouseClicked(e -> {
			File init = new File(MiscellaneousSettings.dataPath());
			FileChooser fc = new FileChooser();
			if (init.exists()) {
				fc.setInitialDirectory(init);
			} else {
				fc.setInitialDirectory(new File("./"));
			}	
			fc.setTitle("Select MarketTick Files");
			List<File> files = fc.showOpenMultipleDialog(null);		
			if (files != null) {
				for (File f : files) {						
					Thread t = new Thread(new OptimizeTask(f, numJobs));
					t.start();
				}
			}
		});
		
		if (chart != null) {
			txtContract = new TextBox(chart.stage(), gc, 100, 20, x.get() + 5, y.get() + 110, "", TextBox.InputType.ANY, false, true, false);
		} else {
			txtContract = new TextBox(sp.stage(), gc, 100, 20, x.get() + 5, y.get() + 110, "", TextBox.InputType.ANY, false, true, false);
		}
		setTextEvents();
				
		databendoOptimizer = new CanvasButton(gc, 185, 20, x.get() + 110, y.get() + 110, "DATABENDO OPTIMIZER");
		databendoOptimizer.setVanGogh(cmbvg.databendoOptimizerVG(databendoOptimizer, doProg));
		databendoOptimizer.setOnMouseClicked(e -> {
			String contract = null;
			if (!txtContract.text().equals("")) {
				contract = txtContract.text();
			}
			RandomFunctions.databendoOptimizer(contract, doProg);
		});
				
		mergeFiles = new CanvasButton(gc, 290, 20, x.get() + 5, y.get() + 135, "MERGE FILES");
		mergeFiles.setVanGogh(cmbvg.mergeFilesVG(mergeFiles, mfProg));
		mergeFiles.setOnMouseClicked(e -> {
			RandomFunctions.mergeFiles(mfProg);
		});
	}
	
	private void setTextEvents() {
		txtContract.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				String contract = null;
				if (!txtContract.text().equals("")) {
					contract = txtContract.text();
				}
				RandomFunctions.databendoOptimizer(contract, doProg);
			}
		});
		
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
		optimize.draw();
		txtContract.draw();
		databendoOptimizer.draw();
		mergeFiles.draw();
	}
	
	public void setMiscellaneousFunctionsSceneGraph(Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> menuNode) {
		sceneGraph.addNode(new TNode<ICanvasNode>(miscellaneousFunctions, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(optimize, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(txtContract, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(databendoOptimizer, menuNode));
		sceneGraph.addNode(new TNode<ICanvasNode>(mergeFiles, menuNode));
		if (sp != null) {
			sp.stage().setHeight(Chart.HEIGHT_EXTRA + 160);
		}
	}
	
	public Chart chart() {
		return chart;
	}

	public SettingsPane settingsPane() {
		return sp;
	}
	
	@Override
	public void draw() {		
		drawMiscellaneousFunctionsMenu();
	}

	@Override
	public void setX(double x) {	
		miscellaneousFunctions.setX(x + 5);
		optimize.setX(x + 5);
		txtContract.setX(x + 5);
		databendoOptimizer.setX(x + 110);
		mergeFiles.setX(x + 5);
		
		this.x.set(x);
	}

	@Override
	public void setY(double y) {
		miscellaneousFunctions.setY(y + 35);
		optimize.setX(y + 85);
		txtContract.setY(y + 110);
		databendoOptimizer.setY(y + 110);
		mergeFiles.setY(y + 135);
		
		this.y.set(y);
	}
}
