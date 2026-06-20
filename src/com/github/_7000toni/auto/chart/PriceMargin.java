package com.github._7000toni.auto.chart;

import com.github._7000toni.auto.canvasnode.CanvasNode;
import com.github._7000toni.auto.miscellaneous.Round;

import javafx.scene.Cursor;
import javafx.scene.paint.Color;

public class PriceMargin extends CanvasNode{
	public final static double PRICE_DASH_SPACING = 50;
	public final static double PRICE_DASH_SIZE = 5;
	public final static double PRICE_DASH_MARGIN = 5;
	
	private Chart c;
	private double priceMargin = 100;
	private double priceInitPos;
	
	public PriceMargin(Chart c, double dataMaxLength) {
		this.c = c;
		this.gc = c.graphicsContext();
		priceMargin = dataMaxLength * gc.getFont().getSize() / 2 + 20;
		if (priceMargin < 35) {
			priceMargin = 35;
		}
		setOnMouseMoved(e -> {			
			c.stage().getScene().setCursor(Cursor.N_RESIZE);
		});
		setOnMouseExited(e -> {
			c.stage().getScene().setCursor(Cursor.DEFAULT);
		});
		setOnMousePressed(e -> {
			priceInitPos = e.getY();
		});
		setOnMouseDragged(e -> {
			setCrossHairVars(e.getX(), e.getY());
			double posDiff = e.getY() - priceInitPos;
			adjustDataMargin(posDiff);
			priceInitPos = e.getY();
		});
		setOnScroll(e -> {
			double posDiff = -e.getDeltaY()*0.5;
			adjustDataMargin(posDiff);
		});
	}
	
	private void setCrossHairVars(double x, double y) {
		if (!c.chartNode().onChart(x, y)) {
			c.chartNode().setFocusedChart(false);
			c.draw();
		} else {
			c.chartNode().setFocusedChart(true);
			CrossHair.setX(x);
			CrossHair.setY(y);
			CrossHair.setPrice(c.chartNode().yCoordToPrice(y));
		}
	}
	
	private void adjustDataMargin(double posDiff) {
		if (posDiff < 0) {
			if (c.chartNode().chtDataMargin() + posDiff > ChartNode.CHT_MARGIN + c.chartNode().fontSize()) {
				c.chartNode().setChtDataMargin(c.chartNode().chtDataMargin() + posDiff);
			}
		} else if (c.chartNode().chtDataMargin() + posDiff < c.chartNode().height() * ChartNode.CHT_DATA_MARGIN_COEF) {
			c.chartNode().setChtDataMargin(c.chartNode().chtDataMargin() + posDiff);
		}
	}
	
	private void drawPriceDashes() {
		double spacing = c.chartNode().tickSizeOnChart() * (int)(PRICE_DASH_SPACING / c.chartNode().tickSizeOnChart());
		if (spacing == 0) {
			spacing = c.chartNode().tickSizeOnChart();
		}
		double index = c.chartNode().height() - c.chartNode().chtDataMargin() + ChartNode.CHT_MARGIN;
		int i = 0;
		while (true) {
			if (index + spacing < c.chartNode().height() + ChartNode.CHT_MARGIN - gc.getFont().getSize() / 2) {
				index += spacing;
				i -= 1;
			} else {
				break;
			}			
		}
		double priceDashPos = c.chartNode().width() + ChartNode.CHT_MARGIN;
		double pricePos = priceDashPos + PRICE_DASH_SIZE + PRICE_DASH_MARGIN;
		int pricePosYMargin = (int)(gc.getFont().getSize() / 3);
		double diff = (spacing / c.chartNode().tickSizeOnChart()) * c.chartNode().data().tickSize();		
		if (Chart.darkMode().get()) {
			gc.setStroke(Color.WHITE);
		} else {
			gc.setStroke(Color.BLACK);
		}
		while (index > ChartNode.CHT_MARGIN + gc.getFont().getSize() / 3) {
			gc.strokeLine(priceDashPos, index, priceDashPos + PRICE_DASH_SIZE, index);
			gc.strokeText(((Double)(Round.round(c.chartNode().lowest() + (diff * i), c.chartNode().data().numDecimalPts() + 1))).toString(), pricePos, index + pricePosYMargin, priceMargin - PRICE_DASH_SIZE - PRICE_DASH_MARGIN * 2);
			index -= spacing;
			i++;
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
		return c.chartNode().width() + ChartNode.CHT_MARGIN;
	}
	
	@Override
	public double y() {
		return 0;
	}
	
	@Override
	public double width() {
		return priceMargin;
	}
	
	@Override
	public double height() {
		return c.chartNode().height() + ChartNode.CHT_MARGIN;
	}
	
	@Override
	public void draw() {
		drawPriceDashes();
	}
}
