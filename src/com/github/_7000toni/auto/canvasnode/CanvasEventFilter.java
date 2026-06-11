package com.github._7000toni.auto.canvasnode;
import com.github._7000toni.auto.tree.TNode;

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class CanvasEventFilter {
	private ICanvasWindow cw;
	
	public CanvasEventFilter(ICanvasWindow cw) {
		this.cw = cw;
	}
	
	public void canvasEventFilter(Event e) {
		TNode<ICanvasNode> currentNode = null;
		MouseEvent me = null;
		ScrollEvent se = null;
		cw.varLock().lock();
		try {
			for (TNode<ICanvasNode> t : cw.sceneGraph().postOrderArray()) {	
				ICanvasNode cn = t.element();
				if (e instanceof MouseEvent) {
					me = (MouseEvent)e;			
					if (!cn.onNode(me.getX(), me.getY()) && !cw.dragging() || !cn.enabled()) {
						continue;
					}
					if (!cw.dragging()) {
						currentNode = t;
					} else {
						currentNode = cw.lastNode();
						cn = currentNode.element();
					}
					if (!currentNode.equals(cw.lastNode()) && cw.lastNode() == null) {
						cn.onMouseEntered(me);
					} else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {	
						cn.onMouseDragged(me);						
						cw.setDragging(true);
					} else if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
						cn.onMousePressed(me);
					} else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
						cn.onMouseReleased(me);
						cw.setDragging(false);
					} else if (e.getEventType() == MouseEvent.MOUSE_CLICKED) { 
						cn.onMouseClicked(me);
					} else if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
						cn.onMouseMoved(me);
					}
					break;
				} else if (e instanceof ScrollEvent) {
					se = (ScrollEvent)e;
					if (!cn.onNode(se.getX(), se.getY())) {
						continue;
					}
					currentNode = t;
					if (e.getEventType() == ScrollEvent.SCROLL) {
						cn.onScroll(se);
					}
					break;
				}
			}		
			if (me != null) {
				if (currentNode != null && !currentNode.equals(cw.lastNode())) {
					if (cw.lastNode() != null) {
						cw.lastNode().element().onMouseExited(me);
						currentNode.element().onMouseEntered(me);
						cw.setLastNode(null);
					}
					cw.setLastNode(currentNode);
				} else if (!cw.onWindow(me.getX(), me.getY()) && !cw.dragging()) {
					cw.sceneGraph().root().element().onMouseExited(me);
					cw.setLastNode(null);
				}
			}
		} finally {
			cw.varLock().unlock();
		}
		cw.draw();
	}
}
