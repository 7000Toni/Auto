import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public class CanvasWrapper extends CanvasNode {
	private Canvas canvas;	
	private Tree<ICanvasNode> sceneGraph;
	protected EventHandler<MouseEvent> onMouseExited = e -> {
		for (TNode<ICanvasNode> tn : sceneGraph.postOrderArray()) {			
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
	
	public CanvasWrapper(Canvas canvas, Tree<ICanvasNode> sceneGraph) {
		this.canvas = canvas;
		this.sceneGraph = sceneGraph;
	}
	
	public Canvas canvas() {
		return canvas;
	}
	
	@Override
	public boolean onNode(double x, double y) {
		return true;
	}	
	
	@Override
	public void draw() {}
	
	
	@Override
	public GraphicsContext graphicsContext() {
		return canvas.getGraphicsContext2D();
	}
}
