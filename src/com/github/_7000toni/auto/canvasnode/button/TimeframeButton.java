package com.github._7000toni.auto.canvasnode.button;
import com.github._7000toni.auto.canvasnode.IVanGogh;
import com.github._7000toni.auto.chart.Chart;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TimeframeButton extends CanvasButton {
	private CanvasButton remove;
	private String timeframeName;
	
	public TimeframeButton(GraphicsContext gc, double width, double height, double x, double y, String text, String name) {
		super(gc, width, height, x, y, text);
		timeframeName = name;
		IVanGogh drawCross = (x2, y2, gc2) -> {
			if (Chart.darkMode().get()) {
				gc2.setFill(Color.BLACK);
			} else {
				gc2.setFill(Color.WHITE);
			}	
			if (remove.hover()) {
				gc2.setFill(Color.GRAY);
			}
			if (remove.pressed()) {
				gc2.setFill(Color.RED);
			}			
			gc2.fillRoundRect(x2, y2, remove.width(), remove.width(), ARC_W, ARC_H);	
			setColoursShape(remove, gc2);
			double mgn = remove.textXOffset();
			double v = (remove.width() - mgn*2) / 3;
			double v2 = (remove.width() - mgn*2) / 4;
			double valx = x2 + mgn + v;
			double val2x = x2 + mgn + v*2;
			double val3x = x2 + mgn + v*3;
			double val4x = x2 + mgn + v*1.5;
			double valy = y2 + mgn + v2;
			double val2y = y2 + mgn + v2*3;
			double val3y = y2 + mgn + v2*4;
			double val4y = y2 + mgn + v2*2;
			double[] x3 = {x2 + mgn, valx, val4x, val2x, val3x, val2x, val3x, val2x, val4x, valx, x2 + mgn, valx, x2 + mgn};
			double[] y3 = {y2 + mgn, y2 + mgn, valy, y2 + mgn, y2 + mgn, val4y, val3y, val3y, val2y, val3y, val3y, val4y, y2 + mgn};
			gc2.fillPolygon(x3, y3, 13);
		};
		remove = new CanvasButton(gc, 16, 16, x + width - 18, y + 2, null, 2, 2);	
		remove.setVanGogh(drawCross);
	}
	
	private void setColoursShape(CanvasButton cb, GraphicsContext gc) {
		if (Chart.darkMode().get()) {
			gc.setFill(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
		}		
		if (cb.hover()) {
			gc.setFill(Color.WHITE);
		}
		if (cb.pressed()) {
			gc.setFill(Color.BLACK);
		}
		if (!cb.enabled()) {
			gc.setFill(Color.LIGHTGRAY);
		}
	}
	
	public void setTimeframeName(String name) {
		timeframeName = name;
	}
	
	public String timeframeName() {
		return timeframeName;
	}
	
	@Override
	public void defaultDraw() {		
		calculateOffsets(gc.getFont());
		setColoursRect();
		gc.fillRoundRect(x, y, width, height, ARC_W, ARC_H);
		setColoursText();
		gc.fillText(text, x + 2, y + textYOffset, width - 20);
		remove.draw();
	}
	
	@Override
	public void draw() {		
		if (vg == null) {
			defaultDraw();
		} else {
			vg.draw(x, y, gc);
		}
	}
	
	@Override
	public void setX(double x) {
		double diff = this.x - x;
		this.x = x;
		remove.setX(remove.x() - diff);
	}
	
	@Override
	public void setY(double y) {
		double diff = this.y - y;
		this.y = y;
		remove.setY(remove.y() - diff);
	}
	
	@Override
	public boolean onNode(double x, double y) {	
		if (remove.onNode(x, y)) {
			return false;
		}
		if (x > this.x + width || x < this.x) {
			return false;
		}
		if (y > this.y + height || y < this.y) {
			return false;
		}
		return true;
	}
	
	public CanvasButton removeButton() {
		return remove;
	}
}
