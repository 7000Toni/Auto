import java.util.concurrent.locks.ReentrantLock;

public interface ICanvasWindow {
	public void draw();
	public Tree<ICanvasNode> sceneGraph();
	public TNode<ICanvasNode> lastNode();
	public void setLastNode(TNode<ICanvasNode> lastNode);
	public ReentrantLock varLock();
	public boolean onWindow(double x, double y);
	public boolean dragging();
	public void setDragging(boolean dragging);
}
