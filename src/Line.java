
public class Line {
	private double price;
	private boolean highlighted;
	
	public Line(double price, boolean highlighted) {
		this.price = price;
		this.highlighted = highlighted;
	}
	
	public Line(double price) {
		this.price = price;
		this.highlighted = false;
	}
	
	public double price() {
		return this.price;
	}
	
	public boolean highlighted() {
		return this.highlighted;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}
}
