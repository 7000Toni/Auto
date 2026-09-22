package com.github._7000toni.auto.chart;

import java.util.Iterator;
import java.util.LinkedList;

import com.github._7000toni.auto.canvasnode.CanvasNode;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class NodeManager extends CanvasNode {
	private LinkedList<CanvasNode> nodes = new LinkedList<CanvasNode>();
	private CanvasNode parent = null;
	private CanvasNode onNode;
	private	CanvasNode lastNode;
	private CanvasNode focused = null;
	
	public NodeManager() {}
	
	public NodeManager(CanvasNode parent) {
		this.parent = parent;
	}
	
	public void addNode(CanvasNode node) {
		nodes.addFirst(node);
	}
	
	public void removeNode(CanvasNode node) {
		nodes.remove(node);
	}
		
	public CanvasNode parent() {
		return parent;
	}
	
	public void setParent(CanvasNode parent) {
		this.parent = parent;
	}
	
	public LinkedList<CanvasNode> nodes() {
		return nodes;
	}
	
	@Override
	public void draw() {
		if (parent != null) {
			parent.draw();
		}
		Iterator<CanvasNode> i = nodes.descendingIterator();		
        while (i.hasNext()) {
        	i.next().draw();
        }
	}	
	
	@Override
	public boolean onNode(double x, double y) {
		Iterator<CanvasNode> i = nodes.iterator();
		lastNode = onNode;
		while (i.hasNext()) {
			CanvasNode n = i.next();
			if (n.onNode(x, y)) {
				onNode = n;				
				return true;
			}
		}				
		if (parent != null) {
			if (parent.onNode(x, y)) {
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
