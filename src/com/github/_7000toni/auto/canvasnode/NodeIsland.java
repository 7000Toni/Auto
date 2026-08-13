package com.github._7000toni.auto.canvasnode;

import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class NodeIsland extends CanvasNode {
	public static final int MARGIN = 2;
	public static final int FONT_SIZE = 10;
	
	private BooleanProperty draggable = new SimpleBooleanProperty(true);
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private GraphicsContext gc;
	private String name;	
	private TNode<ICanvasNode> ni;
	private ArrayList<TNode<ICanvasNode>> nodes = new ArrayList<TNode<ICanvasNode>>();
	private Tree<ICanvasNode> sceneGraph;
	
	private CanvasButton close;
	
	private double dragXOrigin = 0;
	private double dragYOrigin = 0;
	
	public NodeIsland(GraphicsContext gc, String name, double x, double y, double width, double height, Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> parent, boolean draggable, double minX, double maxX, double minY, double maxY, ArrayList<ICanvasNode> nodes) {
		constructorStuff(gc, name, x, y, width, height, sceneGraph, parent, draggable, minX, maxX, minY, maxY, nodes);
	}
	
	private void constructorStuff(GraphicsContext gc, String name, double x, double y, double width, double height, Tree<ICanvasNode> sceneGraph, TNode<ICanvasNode> parent, boolean draggable, double minX, double maxX, double minY, double maxY, ArrayList<ICanvasNode> nodes) {
		this.x = x;
		this.x = Math.max(minX, this.x);
		this.x = Math.min(this.x, maxX);
		this.y = y;
		this.y = Math.max(minY, this.y);
		this.y = Math.min(this.y, maxY);
		this.width = width;
		this.height = height;
		this.draggable.set(draggable);
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.name = name;	
		this.gc = gc;
		this.sceneGraph = sceneGraph;
		
		close = new CanvasButton(gc, 40, 20, x+349, y+10, null, 0, 0);
		close.setVanGogh((x2, y2, gc2) -> {
			close.defaultDraw(gc.getFont());
		});
		
		ni = new TNode<ICanvasNode>(this, parent);
		sceneGraph.addNode(ni);
		sceneGraph.addNode(new TNode<ICanvasNode>(close, ni));
		for (ICanvasNode n : nodes) {
			TNode<ICanvasNode> tn = new TNode<ICanvasNode>(n, ni);
			this.nodes.add(tn);
			sceneGraph.addNode(tn);
		}
		resetNodePositions();
		
		setOnMousePressed(e -> {
			dragXOrigin = e.getX();
			dragYOrigin = e.getY();
			if (e.getButton() == MouseButton.SECONDARY) {
				this.draggable.set(!this.draggable.get());
			}
		});
		setOnMouseDragged(e -> {
			if (this.draggable.get()) {
				setX(this.x + e.getX() - dragXOrigin);
				setY(this.y + e.getY() - dragYOrigin);
				dragXOrigin = e.getX();
				dragYOrigin = e.getY();
			}
		});
		
		draw();
	}
	
	public ArrayList<ICanvasNode> nodes() {
		ArrayList<ICanvasNode> n = new ArrayList<ICanvasNode>();
		for (TNode<ICanvasNode> cn : nodes) {
			n.add(cn.element());
		}
		return n;
	}	
	
	public void addNode(ICanvasNode node) {
		TNode<ICanvasNode> tn = new TNode<ICanvasNode>(node, ni);
		this.nodes.add(tn);
		sceneGraph.addNode(tn);
		resetNodePositions();
	}	
	
	public void removeNode(ICanvasNode node) {
		for (int i = 0; i < nodes.size(); i++) {
			TNode<ICanvasNode> cn = nodes.get(i);
			if (cn.element().equals(node)) {
				sceneGraph.removeNode(cn);
				nodes.remove(i);
				resetNodePositions();
				break;
			}
		}
	}	
	
	public TNode<ICanvasNode> nodeIslandNode() {
		return ni;
	}
	
	private void resetNodePositions() {		
		
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
		gc.setFont(Font.font(oldFont.getFamily(), FontWeight.NORMAL, FONT_SIZE));
		
		gc.setFont(oldFont);
	}
	
	@Override
	public void setX(double x) {
		x = x>maxX?maxX:x;
		x = x<minX?minX:x;
		
		resetNodePositions();
		
		this.x = x;
	}
	
	@Override
	public void setY(double y) {
		y = y>maxY?maxY:y;
		y = y<minY?minY:y;
		
		for (TNode<ICanvasNode> tn : nodes) {
			tn.element().setY(y + MARGIN*2 + FONT_SIZE);
		}
		
		this.y = y;
	}
	
	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}
}
