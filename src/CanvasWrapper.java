import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class CanvasWrapper implements CanvasNode {
	private Canvas canvas;	
	private Tree<CanvasNode> sceneGraph;
	private EventHandler<MouseEvent> onMouseMoved = e -> {
		for (TNode<CanvasNode> tn : sceneGraph.postOrderArray()) {			
			if (tn.element() instanceof CanvasButton) {				
				ButtonChecks.mouseButtonHoverCheck((CanvasButton)tn.element(), e.getX(), e.getY());
			}
			if (tn.element() instanceof CanvasNumberChooser) {
				ButtonChecks.mouseNumberChooserUpHoverCheck((CanvasNumberChooser)tn.element(), e.getX(), e.getY());
				ButtonChecks.mouseNumberChooserDownHoverCheck((CanvasNumberChooser)tn.element(), e.getX(), e.getY());
			}				
		}
	};
	
	public CanvasWrapper(Canvas canvas, Tree<CanvasNode> sceneGraph) {
		this.canvas = canvas;
		this.sceneGraph = sceneGraph;
	}
	
	public Canvas canvas() {
		return canvas;
	}
	
	public void onMouseDragged(MouseEvent e) {}
	public void onMouseEntered(MouseEvent e) {}
	public void onMouseExited(MouseEvent e) {}
	public void onMousePressed(MouseEvent e) {}
	public void onMouseReleased(MouseEvent e) {}
	
	public void onMouseMoved(MouseEvent e) {
		onMouseMoved.handle(e);
	}
	
	public void onScroll(ScrollEvent e) {}
	
	public void setOnMouseDragged(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMouseEntered(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMouseExited(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMousePressed(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMouseReleased(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMouseMoved(EventHandler<? super MouseEvent> e) {}
	
	public void setOnScroll(EventHandler<? super ScrollEvent> e) {}
	
	public boolean onNode(double x, double y) {
		return true;
	}	
	
	public void draw() {}
	
	public GraphicsContext graphicsContext() {
		return canvas.getGraphicsContext2D();
	}
	
	public void setGraphicsContext(GraphicsContext gc) {}	
	public void setX(double x) {}
	public void setY(double y) {}
	
	public double x() {
		return 0;
	}
	
	public double y() {
		return 0;
	}
}
