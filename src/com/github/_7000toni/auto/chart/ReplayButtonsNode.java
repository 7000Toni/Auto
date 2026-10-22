package com.github._7000toni.auto.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.canvasnode.ICanvasNode;
import com.github._7000toni.auto.canvasnode.NodeIsland;
import com.github._7000toni.auto.canvasnode.TextBox;
import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.marketreplay.MarketReplayNode;
import com.github._7000toni.auto.marketreplay.trade.TradeButtons;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class ReplayButtonsNode extends CanvasNode {
	private LinkedList<List<ICanvasNode>> nodes = new LinkedList<List<ICanvasNode>>();
	private ICanvasNode onNode = null;
	private	ICanvasNode lastNode = null;
	private List<ICanvasNode> onNodeList;
	private ICanvasNode focused = null;
	
	private ChartNode cn;
	private CanvasButton sell;
	private CanvasButton buy;
	private TextBox txtVolume;
	private MarketReplayNode mrn;
	private NodeIsland tfi;
	private boolean mrnOnTop = true;
	
	public ReplayButtonsNode(ChartNode cn, TradeButtons tradeButs, CanvasButton sell, CanvasButton buy, TextBox txtVolume, PendingButtonsNode pbn) {
		this.cn = cn;
		this.sell = sell;
		this.buy = buy;
		this.txtVolume = txtVolume;
		this.mrn = cn.mrn();
		this.tfi = cn.timeframeIsland();
		nodes.addFirst(new ArrayList<ICanvasNode>(List.of(tradeButs.cancelSL(), tradeButs.sl())));
		nodes.addFirst(new ArrayList<ICanvasNode>(List.of(tradeButs.cancelTP(), tradeButs.tp())));
		nodes.addFirst(new ArrayList<ICanvasNode>(List.of(tradeButs.close(), tradeButs.order(), tradeButs.setSL(), tradeButs.setTP())));
		nodes.addFirst(new ArrayList<ICanvasNode>(List.of(pbn)));
	}
	
	private void drawMRNandTFI() {
		if (mrnOnTop) {
			if (cn.drawTFI().get()) {
				tfi.nodeMan().draw();
			}
			if (cn.drawMRN().get()) {
				mrn.nodeMan().draw();
			}			
		} else {			
			if (cn.drawMRN().get()) {
				mrn.nodeMan().draw();
			}	
			if (cn.drawTFI().get()) {
				tfi.nodeMan().draw();
			}
		}	
	}
	
	@Override
	public void draw() {
		Iterator<List<ICanvasNode>> i = nodes.descendingIterator();		
        while (i.hasNext()) {
        	List<ICanvasNode> n = i.next();
        	for (ICanvasNode cn : n) {
        		cn.draw();
        	}
        }		
		sell.draw();
		txtVolume.draw();
		buy.setX(ChartNode.CHT_MARGIN + txtVolume.x() + txtVolume.width());
		buy.draw();
		drawMRNandTFI();
	}	
	
	private boolean mrnAndTFIOnNode(double x, double y) {
		if (mrnOnTop) {
			if (mrn.enabled() && cn.drawMRN().get() && mrn.onNode(x, y)) {
				mrn.nodeMan().onNode(x, y);
				onNode = mrn.nodeMan().onNode();
				return true;
			}
			if (tfi.enabled() && cn.drawTFI().get() && tfi.onNode(x, y)) {
				tfi.nodeMan().onNode(x, y);
				onNode = tfi.nodeMan().onNode();
				return true;
			}
		} else {
			if (tfi.enabled() && cn.drawTFI().get() && tfi.onNode(x, y)) {
				tfi.nodeMan().onNode(x, y);
				onNode = tfi.nodeMan().onNode();
				return true;
			}
			if (mrn.enabled() && cn.drawMRN().get() && mrn.onNode(x, y)) {
				mrn.nodeMan().onNode(x, y);
				onNode = mrn.nodeMan().onNode();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onNode(double x, double y) {
		lastNode = onNode;
		if (mrnAndTFIOnNode(x, y)) {
			return true;
		}
		if (sell.enabled() && sell.onNode(x, y)) {
			onNode = sell;
			return true;
		}
		if (txtVolume.enabled() && txtVolume.onNode(x, y)) {
			onNode = txtVolume;
			return true;
		}
		if (buy.enabled() && buy.onNode(x, y)) {
			onNode = buy;
			return true;
		}
		Iterator<List<ICanvasNode>> i = nodes.iterator();
		while (i.hasNext()) {
			List<ICanvasNode> n = i.next();
			for (ICanvasNode cn : n) {
				if (cn.enabled() && cn.onNode(x, y)) {
					onNode = cn;
					onNodeList = n;
					return true;
				}
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
	
	private void checkMRNOnTop() {
		if (onNode == tfi) {
			mrnOnTop = false;
		} else if (onNode == mrn) {
			mrnOnTop = true;
		}
	}
	
	@Override
	public void onMousePressed(MouseEvent e) {
		if (focused == null) {
			onNode.setFocused(true);
			focused = onNode;
		} else if (focused != null && !focused.equals(onNode)) {
			focused.setFocused(false);
			onNode.setFocused(true);
			focused = onNode;
		}	
		onNode.onMousePressed(e);
		if (onNodeList != null) {
			nodes.remove(onNodeList);
			nodes.add(0, onNodeList);
		}		
		checkMRNOnTop();
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
	
	@Override
	public void onKeyPressed(KeyEvent e) {
		focused.onKeyPressed(e);
	}
	
	@Override
	public void onKeyReleased(KeyEvent e) {
		focused.onKeyReleased(e);
	}
	
	@Override
	public void onKeyTyped(KeyEvent e) {
		focused.onKeyTyped(e);
	}
	
	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.focused.setFocused(false);
			this.focused = null;
		}
	}
}
