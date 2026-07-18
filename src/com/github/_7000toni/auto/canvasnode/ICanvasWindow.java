package com.github._7000toni.auto.canvasnode;
import java.util.concurrent.locks.ReentrantLock;

import com.github._7000toni.auto.tree.TNode;
import com.github._7000toni.auto.tree.Tree;

public interface ICanvasWindow {
	public void draw();
	public Tree<ICanvasNode> sceneGraph();
	public ReentrantLock varLock();
	public boolean onWindow(double x, double y);
	public boolean dragging();
	public TNode<ICanvasNode> lastNode();
	public void setLastNode(TNode<ICanvasNode> lastNode);
	public ICanvasNode lastFocused();
	public void setLastFocused(ICanvasNode lastFocused);
	public void setDragging(boolean dragging);
	public CanvasEventFilter canvasEventFilter();
}
