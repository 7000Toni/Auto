import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class CanvasWrapper implements CanvasNode {
	private Canvas canvas;	
	private Tree<CanvasNode> sceneGraph;
	private EventHandler<MouseEvent> onMouseExited = e -> {
		for (TNode<CanvasNode> tn : sceneGraph.postOrderArray()) {			
			if (tn.element() instanceof CanvasButton) {				
				((CanvasButton)tn.element()).setHover(false);
				((CanvasButton)tn.element()).setPressed(false);
			}
			if (tn.element() instanceof CanvasNumberChooser) {
				((CanvasNumberChooser)tn.element()).setDownHover(false);
				((CanvasNumberChooser)tn.element()).setDownPressed(false);
				((CanvasNumberChooser)tn.element()).setUpHover(false);
				((CanvasNumberChooser)tn.element()).setUpPressed(false);
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
	
	public void onMouseExited(MouseEvent e) {
		onMouseExited.handle(e);
	}
	
	public void onMousePressed(MouseEvent e) {}
	public void onMouseClicked(MouseEvent e) {}
	public void onMouseReleased(MouseEvent e) {}
	public void onMouseMoved(MouseEvent e) {}
	
	public void onScroll(ScrollEvent e) {}
	
	public void setOnMouseDragged(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMouseEntered(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMouseExited(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMousePressed(EventHandler<? super MouseEvent> e) {}
	
	public void setOnMouseClicked(EventHandler<? super MouseEvent> e) {}
	
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
