import java.util.concurrent.locks.ReentrantLock;

public interface CanvasWindow {
	public void draw();
	public Tree<CanvasNode> sceneGraph();
	public TNode<CanvasNode> lastNode();
	public void setLastNode(TNode<CanvasNode> lastNode);
	public ReentrantLock varLock();
}
