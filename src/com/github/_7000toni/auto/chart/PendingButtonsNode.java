package com.github._7000toni.auto.chart;

import java.util.Iterator;
import java.util.LinkedList;

import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.marketreplay.trade.PendingTrade;
import com.github._7000toni.auto.marketreplay.trade.PendingTradePair;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class PendingButtonsNode extends CanvasNode {
	private LinkedList<PendingTradePair> tradePairs = new LinkedList<PendingTradePair>();
	private CanvasButton onNode;
	private	CanvasButton lastNode;
	private PendingTradePair onNodePair;
	
	public void addPair(PendingTradePair ptp) {
		tradePairs.addFirst(ptp);
	}
	
	public void removePair(PendingTradePair ptp) {
		tradePairs.remove(ptp);
	}
	
	public void removePair(PendingTrade p) {
		Iterator<PendingTradePair> i = tradePairs.iterator();		
        while (i.hasNext()) {
        	PendingTradePair ptp = i.next();
        	if (p.equals(ptp.pendingTrade())) {
        		i.remove();
        	}
        }
	}
		
	public LinkedList<PendingTradePair> tradePairs() {
		return tradePairs;
	}
	
	@Override
	public void draw() {
		Iterator<PendingTradePair> i = tradePairs.descendingIterator();		
        while (i.hasNext()) {
        	i.next().draw();
        }
	}	
	
	@Override
	public boolean onNode(double x, double y) {
		Iterator<PendingTradePair> i = tradePairs.iterator();
		lastNode = onNode;
		while (i.hasNext()) {
			PendingTradePair n = i.next();
			if (n.pendingTradeButtons().order().onNode(x, y)) {
				onNode = n.pendingTradeButtons().order();	
				onNodePair = n;
				return true;
			}
			if (n.pendingTradeButtons().close().onNode(x, y)) {
				onNode = n.pendingTradeButtons().close();
				onNodePair = n;
				return true;
			}
			if (n.pendingTradeButtons().setSL().onNode(x, y) && n.pendingTradeButtons().setSL().enabled()) {
				onNode = n.pendingTradeButtons().setSL();
				onNodePair = n;
				return true;
			}
			if (n.pendingTradeButtons().setTP().onNode(x, y) && n.pendingTradeButtons().setTP().enabled()) {
				onNode = n.pendingTradeButtons().setTP();
				onNodePair = n;
				return true;
			}
		}		
		return false;
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
		onNode.onMouseDragged(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onNode != lastNode && lastNode != null) {
			lastNode.onMouseExited(e);
		}
		onNode.onMouseEntered(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		onNode.onMouseExited(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		onNode.onMousePressed(e);
		tradePairs.remove(onNodePair);
		tradePairs.add(0, onNodePair);
	}
	
	@Override
	public void onMouseClicked(MouseEvent e) {
		onNode.onMouseClicked(e);
	}
	
	@Override
	public void onMouseReleased(MouseEvent e) {		
		onNode.onMouseReleased(e);		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		if (onNode != lastNode && lastNode != null) {
			lastNode.onMouseExited(e);
		}
		onNode.onMouseMoved(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		onNode.onScroll(e);
	}
}
