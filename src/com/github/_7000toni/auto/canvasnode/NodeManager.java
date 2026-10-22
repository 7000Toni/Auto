package com.github._7000toni.auto.canvasnode;

import java.util.Iterator;
import java.util.LinkedList;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class NodeManager extends CanvasNode {
	private LinkedList<ICanvasNode> nodes = new LinkedList<ICanvasNode>();
	private ICanvasNode parent = null;
	private ICanvasNode onNode;
	private	ICanvasNode lastNode;
	private ICanvasNode focused = null;
	private boolean drawParent = true;
	private boolean drawChildren = true;
	
	public NodeManager() {}
	
	public NodeManager(ICanvasNode parent) {
		this.parent = parent;
	}
	
	public void addNode(ICanvasNode node) {
		nodes.addFirst(node);
	}
	
	public void removeNode(ICanvasNode node) {
		nodes.remove(node);
	}
		
	public ICanvasNode parent() {
		return parent;
	}
	
	public void setParent(ICanvasNode parent) {
		this.parent = parent;
	}
	
	public boolean drawParent() {
		return drawParent;
	}
	
	public void setDrawParent(boolean drawParent) {
		this.drawParent = drawParent;
	}
	
	public boolean drawChildren() {
		return drawChildren;
	}
	
	public void setDrawChildren(boolean drawChildren) {
		this.drawChildren = drawChildren;
	}
	
	public LinkedList<ICanvasNode> nodes() {
		return nodes;
	}
	
	public ICanvasNode onNode() {
		return onNode;
	}
	
	@Override
	public void draw() {
		if (parent != null && drawParent) {
			parent.draw();
		}
		if (drawChildren) {
			Iterator<ICanvasNode> i = nodes.descendingIterator();		
	        while (i.hasNext()) {
	        	i.next().draw();
	        }
		}
	}	
	
	@Override
	public boolean onNode(double x, double y) {
		Iterator<ICanvasNode> i = nodes.iterator();
		lastNode = onNode;
		while (i.hasNext()) {			
			ICanvasNode n = i.next();
			if (n.enabled() && n.onNode(x, y)) {
				onNode = n;				
				return true;
			}
		}				
		if (parent != null) {
			if (parent.enabled() && parent.onNode(x, y)) {
				onNode = parent;				
				return true;
			}
		}			
		return false;
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
		onNode.onMouseDragged(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onNode != lastNode && lastNode != null) {
			lastNode.onMouseExited(e);
		}
		onNode.onMouseEntered(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		onNode.onMouseExited(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		if (focused == null) {
			onNode.setFocused(true);
			focused = onNode;
		} else if (focused != null && !focused.equals(onNode)) {
			focused.setFocused(false);
			onNode.setFocused(true);
			focused = onNode;
		}	
		onNode.onMousePressed(e);
		if (onNode != parent) {
			nodes.remove(onNode);
			nodes.add(0, onNode);
		}
	}
	
	@Override
	public void onMouseClicked(MouseEvent e) {
		onNode.onMouseClicked(e);
	}
	
	@Override
	public void onMouseReleased(MouseEvent e) {		
		onNode.onMouseReleased(e);		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		if (onNode != lastNode && lastNode != null) {
			lastNode.onMouseExited(e);
		}
		onNode.onMouseMoved(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		onNode.onScroll(e);
	}
	
	@Override
	public void onKeyPressed(KeyEvent e) {
		focused.onKeyPressed(e);
	}
	
	@Override
	public void onKeyReleased(KeyEvent e) {
		focused.onKeyReleased(e);
	}
	
	@Override
	public void onKeyTyped(KeyEvent e) {
		focused.onKeyTyped(e);
	}
	
	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.focused.setFocused(false);
			this.focused = null;
		}
	}
}
