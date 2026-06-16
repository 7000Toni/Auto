package com.github._7000toni.auto.chart;

import com.github._7000toni.auto.canvasnode.CanvasNode;

import javafx.scene.Cursor;

public class DateMargin extends CanvasNode {
	public final static double PRICE_DASH_SPACING = 50;
	public final static double PRICE_DASH_SIZE = 5;
	public final static double PRICE_DASH_MARGIN = 5;
	
	private ChartNode cn;
	private double chartInitPos;
	
	public DateMargin(ChartNode cn) {
		this.cn = cn;
		this.gc = cn.graphicsContext();
		setOnMouseEntered(e -> {
			cn.chart().stage().getScene().setCursor(Cursor.E_RESIZE);
			cn.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		setOnMouseMoved(e -> {
			cn.chart().stage().getScene().setCursor(Cursor.E_RESIZE);
			cn.setFocusedChart(true);
			setCrossHairVars(e.getX(), e.getY());
		});
		setOnMouseExited(e -> {
			cn.chart().stage().getScene().setCursor(Cursor.DEFAULT);
			setCrossHairVars(e.getX(), e.getY());
		});
		setOnMousePressed(e -> {
			chartInitPos = e.getX();
		});
		setOnMouseDragged(e -> {
			setCrossHairVars(e.getX(), e.getY());
			if (cn.drawCandlesticks().get()) {
				cn.zoomCandlesticks(e.getX() - chartInitPos, false);
			} else {
				cn.zoomTicks(e.getX() - chartInitPos, false);
			}
			chartInitPos = e.getX();
		});
		setOnScroll(e -> {
			if (cn.drawCandlesticks().get()) {
				cn.zoomCandlesticks(e.getDeltaY(), true);
			} else {
				cn.zoomTicks(e.getDeltaY(), true);
			}
		});
	}
	
	private void setCrossHairVars(double x, double y) {
		if (!cn.onChart(x, y)) {
			cn.setFocusedChart(false);
		} else {
			cn.setFocusedChart(true);
			CrossHair.setX(x);
			CrossHair.setY(y);
			CrossHair.setPrice(cn.yCoordToPrice(y));
		}
	}
	
	@Override
	public boolean onNode(double x, double y) {
		if (x > this.x() + width() || x < this.x()) {
			return false;
		}
		if (y > this.y() + height() || y < this.y()) {
			return false;
		}
		return true;
	}
	
	@Override
	public double x() {
		return ChartNode.CHT_MARGIN;
	}
	
	@Override
	public double y() {
		return cn.height() - cn.fontSize() + ChartNode.CHT_MARGIN;
	}
	
	@Override
	public double width() {
		return cn.width();
	}
	
	@Override
	public double height() {
		return cn.fontSize();
	}

	@Override
	public void draw() {
	}
}
