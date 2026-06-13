package com.github._7000toni.auto.canvasnode;

import com.github._7000toni.auto.canvasnode.button.CanvasButton;
import com.github._7000toni.auto.canvasnode.button.CanvasNumberChooser;

public class NodeChecks {
	
	public static boolean mouseNodeHoverCheck(CanvasNode node, double x, double y) {
		if (!node.enabled()) {
			return false;
		}
		if (node.onNode(x, y)) {			
			if (!node.pressed()) {
				node.setHover(true);				
			}
			return true;
		} else {			
			node.setPressed(false);
			node.setHover(false);
			return false;
		}
	}
	
	public static boolean mouseButtonSwitchHoverCheck(CanvasButton button, double x, double y) {
		if (!button.enabled()) {
			return false;
		}
		if (button.onNode(x, y)) {
			button.setHover(true);		
			return true;
		} else {
			button.setHover(false);
			return false;
		}
	}
	
	public static boolean mouseNumberChooserUpHoverCheck(CanvasNumberChooser cnc, double x, double y) {
		if (!cnc.enabled()) {
			return false;
		}
		if (cnc.onUp(x, y)) {
			if (!cnc.upPressed()) {
				cnc.setUpHover(true);				
			}
			return true;
		} else {
			cnc.setUpPressed(false);
			cnc.setUpHover(false);
			return false;
		}		
	}
	
	public static boolean mouseNumberChooserDownHoverCheck(CanvasNumberChooser cnc, double x, double y) {
		if (!cnc.enabled()) {
			return false;
		}
		if (cnc.onDown(x, y)) {
			if (!cnc.downPressed()) {
				cnc.setDownHover(true);				
			}
			return true;
		} else {
			cnc.setDownPressed(false);
			cnc.setDownHover(false);
			return false;
		}
	}
}
