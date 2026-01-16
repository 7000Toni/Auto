
public class ButtonChecks {
	
	public static boolean mouseButtonHoverCheck(CanvasButton button, double x, double y) {
		if (!button.enabled()) {
			return false;
		}
		if (button.onButton(x, y)) {			
			if (!button.pressed()) {
				button.setHover(true);				
			}
			return true;
		} else {			
			button.setPressed(false);
			button.setHover(false);
			return false;
		}
	}
	
	public static boolean mouseButtonSwitchHoverCheck(CanvasButton button, double x, double y) {
		if (!button.enabled()) {
			return false;
		}
		if (button.onButton(x, y)) {
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
