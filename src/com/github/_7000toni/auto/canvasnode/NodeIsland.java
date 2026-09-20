package com.github._7000toni.auto.canvasnode;

import java.util.ArrayList;

import com.github._7000toni.auto.settings.ColourSettings;
import com.github._7000toni.auto.settings.MiscellaneousSettings;
import com.github._7000toni.auto.settings.ColourSettings.ColourIndex;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

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
	private TNode<ICanvasNode> ni;
	private ArrayList<TNode<ICanvasNode>> nodes = new ArrayList<TNode<ICanvasNode>>();
	private Tree<ICanvasNode> sceneGraph;
	private boolean drawBorder = true;
	
	private double dragXOrigin = 0;
	private double dragYOrigin = 0;
	
	public NodeIsland(GraphicsContext gc, String name, double x, double y, double maxLength, boolean vertical, Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> parent, ArrayList<ICanvasNode> nodes) {
		constructorStuff(gc, name, x, y, maxLength, vertical, sceneGraph, parent, Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, nodes);
		setPermanentlyLocked(true);
	}
	
	public NodeIsland(GraphicsContext gc, String name, double x, double y, double maxLength, boolean vertical, Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> parent, double minX, double maxX, double minY, double maxY, ArrayList<ICanvasNode> nodes) {
		constructorStuff(gc, name, x, y, maxLength, vertical, sceneGraph, parent, minX, maxX, minY, maxY, nodes);
	}
	
	private void constructorStuff(GraphicsContext gc, String name, double x, double y, double maxLength, boolean vertical, Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> parent, double minX, double maxX, double minY, double maxY, ArrayList<ICanvasNode> nodes) {
		this.x = x;
		this.x = Math.max(minX, this.x);
		this.x = Math.min(this.x, maxX);
		this.y = y;
		this.y = Math.max(minY, this.y);
		this.y = Math.min(this.y, maxY);
		this.maxLength = maxLength;
		this.vertical = vertical;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.name = name;	
		this.gc = gc;
		this.sceneGraph = sceneGraph;
		
		ni = new TNode<ICanvasNode>(this, parent);
		sceneGraph.addNode(ni);
		resetNodePositions(x, y);
		if (nodes != null) {
			for (ICanvasNode n : nodes) {
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
			setX(this.x + e.getX() - dragXOrigin);
			setY(this.y + e.getY() - dragYOrigin);
			dragXOrigin = e.getX();
			dragYOrigin = e.getY();
		}
	}
	
	public ArrayList<ICanvasNode> nodes() {
		ArrayList<ICanvasNode> n = new ArrayList<ICanvasNode>();
		for (TNode<ICanvasNode> cn : nodes) {
			n.add(cn.element());
		}
		return n;
	}	
	
	public boolean addNode(ICanvasNode node) {
		if (node.width() + width > maxLength) {
			return false;
		}
		TNode<ICanvasNode> tn = new TNode<ICanvasNode>(node, ni);
		this.nodes.add(tn);
		sceneGraph.addNode(tn);
		resetNodePositions(x, y);
		if (x+width > maxX) {
			setX(maxX);
		}
		if (y+height > maxY) {
			setY(maxY);
		}
		return true;
	}	
	
	public void removeNode(ICanvasNode node) {
		for (int i = 0; i < nodes.size(); i++) {
			TNode<ICanvasNode> cn = nodes.get(i);
			if (cn.element().equals(node)) {
				sceneGraph.removeNode(cn);
				nodes.remove(i);
				resetNodePositions(x, y);
				break;
			}
		}
	}	
	
	public TNode<ICanvasNode> nodeIslandNode() {
		return ni;
	}
	
	private void resetNodePositions(double x, double y) {
		double off = borderMargin + (vertical&&name!=null?FONT_SIZE:0);
		double heightOrWidth = 0;		
		for (TNode<ICanvasNode> tn : nodes) {
			ICanvasNode cn = tn.element();
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
			width = Math.max(heightOrWidth + borderMargin*2, wid);
			height = off + borderMargin;
		} else {
			width = Math.max(off + borderMargin, wid);
			height = heightOrWidth + (name!=null?FONT_SIZE + nodeMargin:borderMargin) + (nodes.isEmpty()?0:borderMargin);
		}
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
		resetNodePositions(x, y);
	}
	
	public int nodeMargin() {
		return nodeMargin;
	}
	
	public void setNodeMargin(int nodeMargin) {
		this.nodeMargin = nodeMargin;
		resetNodePositions(x, y);
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
		setX(x);
	}
	
	public double maxX() {
		return maxX;
	}
	
	public void setMaxX(double maxX) {
		this.maxX = maxX;
		setX(x);
	}
	
	public double minY() {
		return minY;
	}
	
	public void setMinY(double minY) {
		this.minY = minY;
		setY(y);
	}
	
	public double maxY() {
		return maxY;
	}
	
	public void setMaxY(double maxY) {
		this.maxY = maxY;
		setY(y);
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
		if (hover && !permanentlyLocked) {
			gc.setStroke(Color.GRAY);
		}
		if (pressed && !permanentlyLocked) {
			if (draggable.get()) {
				gc.setStroke(Color.DIMGRAY);
			} else {
				gc.setStroke(ColourSettings.colour(ColourSettings.ColourIndex.MISCELLANEOUS_2));
			}
		}
		gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND));
		gc.fillRoundRect((int)x+0.5, (int)y+0.5, (int)width, height, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		if (drawBorder) {
			gc.strokeRoundRect((int)x+0.5, (int)y+0.5, (int)width, height, MiscellaneousSettings.arcW(), MiscellaneousSettings.arcH());
		}
		if (name != null) {
			gc.setFill(ColourSettings.colour(ColourSettings.ColourIndex.TEXT_AND_STUFF));
			gc.fillText(name, x+borderMargin, y+FONT_SIZE);
		}
		gc.setFont(oldFont);
		for (TNode<ICanvasNode> tn : nodes) {
			tn.element().draw();
		}
	}
	
	@Override
	public void setX(double x) {
		x = x>maxX-width?maxX-width:x;
		x = x<minX?minX:x;
		
		resetNodePositions(x, y);
		
		this.x = x;
	}
	
	@Override
	public void setY(double y) {
		y = y>maxY-height?maxY-height:y;
		y = y<minY?minY:y;
		
		resetNodePositions(x, y);
		
		this.y = y;
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
