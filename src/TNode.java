import java.util.ArrayList;

public class TNode<T> {
	private T element;
	private ArrayList<TNode<T>> children = new ArrayList<TNode<T>>();
	private TNode<T> parent;
	private int size = 1;
	
	public TNode(T element, TNode<T> parent) {
		this.element = element;
		this.parent = parent;
	}
	
	public T element() {
		return element;
	}
	
	public ArrayList<TNode<T>> children() {
		return children;
	}
	
	public void setChildren(ArrayList<TNode<T>> children) {		
		this.children = children;
	}
	
	public TNode<T> parent() {
		return parent;
	}
	
	public boolean isRoot() {
		return parent.element() == null;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	public void addChild(TNode<T> child) {
		children.add(child);
		calcSize(this);
	}
	
	public void setChild(TNode<T> oldChild, TNode<T> newChild, boolean keepChildren) {
		children.set(children.indexOf(oldChild), newChild);
		if (keepChildren) {
			setNewParent(oldChild, newChild);
		}
		calcSize(this);
	}
	
	public void setChild(int index, TNode<T> child, boolean keepChildren) {
		children.set(index, child);
		if (keepChildren) {
			setNewParent(children.get(index), child);
		}
		calcSize(this);
	}
	
	private void setNewParent(TNode<T> oldParent, TNode<T> newParent) {
		newParent.setChildren(oldParent.children());
		for (TNode<T> t : oldParent.children()) {
			t.setParent(newParent);
		}
	}
	
	public void removeChild(TNode<T> child) {
		children.remove(child);
		calcSize(this);
	}
	
	public void removeChild(int index) {
		children.remove(index);
		calcSize(this);
	}
	
	public void setParent(TNode<T> parent) {
		this.parent = parent;
	}
	
	public int size() {
		return calcSize(this);
	}
	
	private int calcSize(TNode<T> node) {
		node.size = 1;
		for (int i = 0; i < node.children().size(); i++) {
			node.size += calcSize(node.children().get(i));
		}
		return node.size;
	}
}
