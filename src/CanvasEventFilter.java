import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class CanvasEventFilter {
	private CanvasWindow cw;
	
	public CanvasEventFilter(CanvasWindow cw) {
		this.cw = cw;
	}
	
	public void canvasEventFilter(Event e) {
		TNode<CanvasNode> currentNode = null;
		MouseEvent me = null;
		ScrollEvent se = null;
		for (TNode<CanvasNode> t : cw.sceneGraph().postOrderArray()) {	
			CanvasNode cn = t.element();
			if (e instanceof MouseEvent) {
				me = (MouseEvent)e;
				if (!cn.onNode(me.getX(), me.getY())) {
					continue;
				}
				currentNode = t;
				if (!currentNode.equals(cw.lastNode())) {
					cn.onMouseEntered(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
					cn.onMouseDragged(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
					cn.onMouseExited(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
					cn.onMousePressed(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
					cn.onMouseReleased(me);
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
				}
				cw.setLastNode(currentNode);
			} else {
				cw.sceneGraph().root().element().onMouseExited(me);
			}
		}
		cw.draw();
	}
}
