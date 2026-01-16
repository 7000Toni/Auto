import java.util.ArrayList;

public class Tree<T> {
	private TNode<T> root;
	private ArrayList<TNode<T>> postOrderArray = new ArrayList<TNode<T>>(); 
	
	public Tree() {}
	
	public Tree(T element) {
		addNode(new TNode<T>(element, null));
	}
	
	public Tree(TNode<T> root) {
		addNode(root);
	}
	
	public TNode<T> root() {
		return root;
	}
	
	public int size() {
		if (root == null) {
			return 0;
		}
		return root.size();
	}
	
	public void addNode(TNode<T> node) {
		if (node.parent() == null) {
			root = node;
		} else {
			node.parent().addChild(node);
		}
		makePostOrderTraversalArray(root);
	}
	
	public void removeNode(TNode<T> node) {
		if (node.parent() == null) {
			root = null;
		} else {
			node.parent().removeChild(node);
		}
		makePostOrderTraversalArray(root);
	}
	
	public void setNode(TNode<T> oldNode, TNode<T> newNode, boolean keepChildren) {
		oldNode.parent().setChild(oldNode, newNode, keepChildren);
		makePostOrderTraversalArray(root);
	}
	
	public void makePostOrderTraversalArray(TNode<T> startNode) {
		if (startNode.equals(root)) {
			postOrderArray = new ArrayList<TNode<T>>();
		}
		for (TNode<T> t : startNode.children()) {
			makePostOrderTraversalArray(t);
		}
		postOrderArray.add(startNode);
	}
	
	public ArrayList<TNode<T>> postOrderArray() {
		return postOrderArray;
	}
}
