import java.util.ArrayList;

public class PendingTradeButtons {
	protected CanvasButton order;
	protected CanvasButton close;
	protected CanvasButton setSL;
	protected CanvasButton setTP;
	
	public ArrayList<CanvasButton> buttons() {
		ArrayList<CanvasButton> b = new ArrayList<CanvasButton>();
		b.add(order);
		b.add(close);
		b.add(setSL);
		b.add(setTP);
		return b;
	}
	
	public CanvasButton order() {
		return order;
	}
	
	public CanvasButton close() {
		return close;
	}
	
	public CanvasButton setSL() {
		return setSL;
	}
	
	public CanvasButton setTP() {
		return setTP;
	}
	
	public void setOrder(CanvasButton order) {
		this.order = order;
	}
	
	public void setClose(CanvasButton close) {
		this.close = close;
	}
	
	public void setSetSL(CanvasButton setSL) {
		this.setSL = setSL;
	}
	
	public void setSetTP(CanvasButton setTP) {
		this.setTP = setTP;
	}
}
