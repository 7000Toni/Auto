package com.github._7000toni.auto.canvasnode;

import java.util.ArrayList;

import com.github._7000toni.auto.chart.NodeManager;
import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.MiscellaneousSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class NodeIsland extends CanvasNode {	
	public static final int FONT_SIZE = 10;
	
	private int borderMargin = 3;
	private int nodeMargin = 3;
	private BooleanProperty draggable = new SimpleBooleanProperty(true);
	private boolean permanentlyLocked = false;
	private double maxLength;
	private boolean vertical;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private GraphicsContext gc;
	private String name;	
	private ArrayList<CanvasNode> nodes = new ArrayList<CanvasNode>();
	private boolean drawBorder = true;
	private NodeManager nodeMan;
	
	private double dragXOrigin = 0;
	private double dragYOrigin = 0;
	
	public NodeIsland(GraphicsContext gc, String name, double x, double y, double maxLength, boolean vertical, ArrayList<CanvasNode> nodes) {
		constructorStuff(gc, name, x, y, maxLength, vertical, null, Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, nodes);
		setPermanentlyLocked(true);
	}
	
	public NodeIsland(GraphicsContext gc, String name, double x, double y, double maxLength, boolean vertical, double minX, double maxX, double minY, double maxY, ArrayList<CanvasNode> nodes) {
		constructorStuff(gc, name, x, y, maxLength, vertical, null, minX, maxX, minY, maxY, nodes);
	}
	
	public NodeIsland(GraphicsContext gc, String name, double x, double y, double maxLength, boolean vertical, NodeManager nodeMan, ArrayList<CanvasNode> nodes) {
		constructorStuff(gc, name, x, y, maxLength, vertical, nodeMan, Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, nodes);
		setPermanentlyLocked(true);
	}
	
	public NodeIsland(GraphicsContext gc, String name, double x, double y, double maxLength, boolean vertical, NodeManager nodeMan, double minX, double maxX, double minY, double maxY, ArrayList<CanvasNode> nodes) {
		constructorStuff(gc, name, x, y, maxLength, vertical, nodeMan, minX, maxX, minY, maxY, nodes);
	}
	
	private void constructorStuff(GraphicsContext gc, String name, double x, double y, double maxLength, boolean vertical, NodeManager nodeMan, double minX, double maxX, double minY, double maxY, ArrayList<CanvasNode> nodes) {
		this.x.set(x);
		this.x.set(Math.max(minX, this.x.get()));
		this.x.set(Math.min(this.x.get(), maxX));
		this.y.set(y);
		this.y.set(Math.max(minY, this.y.get()));
		this.y.set(Math.min(this.y.get(), maxY));
		this.maxLength = maxLength;
		this.vertical = vertical;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.name = name;	
		this.gc = gc;
		
		this.nodeMan = new NodeManager(this);		
		if (nodeMan != null) {
			nodeMan.addNode(this.nodeMan);
		}
		
		resetNodePositions(x, y);
		if (nodes != null) {
			for (CanvasNode n : nodes) {
				addNode(n);
			}
		}
		
		setOnMousePressed(e -> {
			defaultOnMousePressed(e);
		});
		setOnMouseDragged(e -> {
			defaultOnMouseDragged(e);
		});
		
		draw();
	}
	
	public void defaultOnMousePressed(MouseEvent e) {
		dragXOrigin = e.getX();
		dragYOrigin = e.getY();
		if (e.getButton() == MouseButton.SECONDARY) {
			this.draggable.set(!this.draggable.get());
		}
	}
	
	public void defaultOnMouseDragged(MouseEvent e) {
		if (draggable.get() && !permanentlyLocked) {
			setX(this.x.get() + e.getX() - dragXOrigin);
			setY(this.y.get() + e.getY() - dragYOrigin);
			dragXOrigin = e.getX();
			dragYOrigin = e.getY();
		}
	}
	
	public ArrayList<CanvasNode> nodes() {
		return nodes;
	}	
	
	public NodeManager nodeMan() {
		return nodeMan;
	}
	
	public boolean addNode(CanvasNode node) {
		if (node.width() + width.get() > maxLength) {
			return false;
		}
		nodes.add(node);
		nodeMan.addNode(node);
		resetNodePositions(x.get(), y.get());
		if (x.get()+width.get() > maxX) {
			setX(maxX);
		}
		if (y.get()+height.get() > maxY) {
			setY(maxY);
		}
		return true;
	}	
	
	public void removeNode(CanvasNode node) {
		nodes.remove(node);
		nodeMan.removeNode(node);
		resetNodePositions(x.get(), y.get());
	}	
	
	@Override
	public boolean onNode(double x, double y) {
		for (CanvasNode c : nodes) {
			if (c.onNode(x, y)) {
				return true;
			}
		}
		return super.onNode(x, y);
	}
	
	private void resetNodePositions(double x, double y) {
		double off = borderMargin + (vertical&&name!=null?FONT_SIZE:0);
		double heightOrWidth = 0;		
		for (CanvasNode cn : nodes) {
			if (vertical) {
				cn.setX(x + borderMargin);
				cn.setY(y + off);
				off += cn.height() + nodeMargin;
			} else {
				cn.setX(x + off);
				cn.setY(y + (name!=null?borderMargin + FONT_SIZE:borderMargin));
				off += cn.width() + nodeMargin;
			}						
			heightOrWidth = Math.max(heightOrWidth, vertical?cn.width():cn.height());
		}
		if (!nodes.isEmpty()) {
			off -= nodeMargin;
		}
		double wid = 0;
		if (name != null) {
			Text t = new Text(name);
			t.setFont(Font.font(gc.getFont().getFamily(), FontWeight.EXTRA_BOLD, FONT_SIZE));
			wid = t.getLayoutBounds().getWidth() + borderMargin*2; 		
		}		
		if (vertical) {
			width.set(Math.max(heightOrWidth + borderMargin*2, wid));
			height.set(off + borderMargin);			
		} else {
			width.set(Math.max(off + borderMargin, wid));
			height.set(heightOrWidth + (name!=null?FONT_SIZE + nodeMargin:borderMargin) + (nodes.isEmpty()?0:borderMargin));
		}
		nodeMan.setWidth(width.get());
		nodeMan.setHeight(height.get());
		nodeMan.setX(x);
		nodeMan.setY(y);
	}
	
	public String name() {
		return this.name;
	}
	
	public ReadOnlyBooleanProperty draggable() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(draggable);
	}
	
	public void setDraggable(boolean draggable) {
		this.draggable.set(draggable);
	}
	
	public boolean permanentlyLocked() {
		return permanentlyLocked;
	}
	
	public void setPermanentlyLocked(boolean permanentlyLocked) {
		this.permanentlyLocked = permanentlyLocked;
		if (permanentlyLocked) {
			draggable.set(false);
		}
	}
	
	public int borderMargin() {
		return borderMargin;
	}
	
	public void setBorderMargin(int borderMargin) {
		this.borderMargin = borderMargin;
		resetNodePositions(x.get(), y.get());
	}
	
	public int nodeMargin() {
		return nodeMargin;
	}
	
	public void setNodeMargin(int nodeMargin) {
		this.nodeMargin = nodeMargin;
		resetNodePositions(x.get(), y.get());
	}
	
	public double maxLength() {
		return maxLength;
	}
	
	public void maxLength(double maxLength) {
		this.maxLength = maxLength;
	}
	
	public boolean vertical() {
		return vertical;
	}
	
	public void vertical(boolean vertical) {
		this.vertical = vertical;
	}
	
	public boolean drawBorder() {
		return drawBorder;
	}
	
	public void setDrawBorder(boolean drawBorder) {
		this.drawBorder = drawBorder;
	}
	
	public double minX() {
		return minX;
	}
	
	public void setMinX(double minX) {
		this.minX = minX;
		setX(x.get());
	}
	
	public double maxX() {
		return maxX;
	}
	
	public void setMaxX(double maxX) {
		this.maxX = maxX;
		setX(x.get());
	}
	
	public double minY() {
		return minY;
	}
	
	public void setMinY(double minY) {
		this.minY = minY;
		setY(y.get());
	}
	
	public double maxY() {
		return maxY;
	}
	
	public void setMaxY(double maxY) {
		this.maxY = maxY;
		setY(y.get());
	}
	
	@Override
	public void draw() {
		if (Platform.isFxApplicationThread()) {
			drawNode();
		} else {
			Platform.runLater(() -> {
				drawNode();
			});
		}		
	}
	
	private void drawNode() {
		Font oldFont = gc.getFont();
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.EXTRA_BOLD, FONT_SIZE));
		gc.setStroke(ColourSettings.colour(ColourIndex.TEXT_AND_STUFF));
		if (hover.get() && !permanentlyLocked) {
			gc.setStroke(Color.GRAY);
		}
		if (pressed.get() && !permanentlyLocked) {
			if (draggable.get()) {
				gc.setStroke(Color.DIMGRAY);
			} else {
				gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
			}
		}
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
		gc.fillRoundRect((int)x.get()+0.5, (int)y.get()+0.5, (int)width.get(), height.get(), MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		if (drawBorder) {
			gc.strokeRoundRect((int)x.get()+0.5, (int)y.get()+0.5, (int)width.get(), height.get(), MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		}
		if (name != null) {
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.TEXT_AND_STUFF));
			gc.fillText(name, x.get()+borderMargin, y.get()+FONT_SIZE);
		}
		gc.setFont(oldFont);
	}
	
	@Override
	public void setX(double x) {
		x = x>maxX-width.get()?maxX-width.get():x;
		x = x<minX?minX:x;
		
		resetNodePositions(x, y.get());
		
		this.x.set(x);
	}
	
	@Override
	public void setY(double y) {
		y = y>maxY-height.get()?maxY-height.get():y;
		y = y<minY?minY:y;
		
		resetNodePositions(x.get(), y);
		
		this.y.set(y);
	}
	
	@Override
	public void setWidth(double width) {}
	
	@Override
	public void setHeight(double height) {}
	
	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}
}
