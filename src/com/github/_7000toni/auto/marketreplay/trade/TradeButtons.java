package com.github._7000toni.auto.marketreplay.trade;
import java.util.ArrayList;

import com.github._7000toni.auto.canvasnode.CanvasButton;

public class TradeButtons extends PendingTradeButtons {
	private CanvasButton cancelTP;
	private CanvasButton cancelSL;
	private CanvasButton sl;
	private CanvasButton tp;
	
	@Override
	public ArrayList<CanvasButton> buttons() {
		ArrayList<CanvasButton> b = new ArrayList<CanvasButton>();
		b.add(close);
		b.add(cancelTP);
		b.add(cancelSL);
		b.add(sl);
		b.add(tp);
		b.add(setSL);
		b.add(setTP);
		return b;
	}
	
	public CanvasButton tp() {
		return tp;
	}
	
	public CanvasButton sl() {
		return sl;
	}
	
	public CanvasButton cancelTP() {
		return cancelTP;
	}
	
	public CanvasButton cancelSL() {
		return cancelSL;
	}
	
	public void setCancelTP(CanvasButton cancelTP) {
		this.cancelTP = cancelTP;
	}
	
	public void setCancelSL(CanvasButton cancelSL) {
		this.cancelSL = cancelSL;
	}
	
	public void setSL(CanvasButton sl) {
		this.sl = sl;
	}
	
	public void setTP(CanvasButton tp) {
		this.tp = tp;
	}
}