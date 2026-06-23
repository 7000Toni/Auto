package com.github._7000toni.auto.canvasnode.scrollbar;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.chart.CrossHair;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class HorizontalChartScrollBar extends HorizontalScrollBar {
	
	public HorizontalChartScrollBar(ChartNode chart, double minPos, double maxPos, double sbWidth, double sbHeight, double yPos) {
		super(chart, minPos, maxPos, sbWidth, sbHeight, yPos);
	}
	
	@Override
	public void onMousePressed(MouseEvent e) {
		if (((ChartNode) sbo).replayMode()) {
			if (onScrollBar(e.getX(), e.getY())) {
				dragging = true;
				initPos = e.getX();
			}
		} else if (onScrollBar(e.getX(), e.getY())) {					
			dragging = true;
			initPos = e.getX();
		} else if (inScrollBarArea(e.getX(), e.getY())) {
			clickedInScrollBarArea = true;
			initPos = e.getX();
			new AnimationTimer() {
				long lastTick = 0;
				boolean add;
				
				@Override
				public void handle(long now) {
					if (lastTick == 0) {
						if (initPos > x) {
							add = true;
						} else {
							add = false;
						}
						lastTick = now;
						return;
					}
					
					if (!clickedInScrollBarArea) {
						this.stop();
					}
					
					if (initPos >= x && initPos <= x + sbWidth) {
						this.stop();
					}
					
					if (now - lastTick >= NANO_TO_MILLI*16) {						
						lastTick = now;		
						if (add) {
							setPosition(sbWidth / 2, true);
							((ChartNode) sbo).setKeepStartIndex(false);
							sbo.draw();
						} else {
							setPosition(-(sbWidth / 2), true);
							((ChartNode) sbo).setKeepStartIndex(false);
							sbo.draw();
						}
					} 
				}
			}.start();
		}
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
		if (dragging) {
			double posDiff = e.getX() - initPos;
			if (x + posDiff > maxPos - sbWidth) {
				x = maxPos - sbWidth;
			} else if (x + posDiff < minPos) {
				x = minPos;
			} else {
				x += posDiff;
			}
			initPos = (int)e.getX();
			((ChartNode) sbo).setKeepStartIndex(false);
			if (!((ChartNode) sbo).onChart(e.getX(), e.getY())) {
				((ChartNode) sbo).setFocusedChart(false);
			} else {
				((ChartNode) sbo).setFocusedChart(true);
				CrossHair.setX(e.getX());
				CrossHair.setY(e.getY());
				CrossHair.setPrice(((ChartNode) sbo).yCoordToPrice(e.getY()));
			}
		}
	}
	
	@Override
	protected void moveOwnerLeft(boolean fast) {
		int speed = 10;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos;
		int startIndex = ((ChartNode) sbo).startIndex();
		if (((ChartNode) sbo).drawCandlesticks().get()) {
			startIndex -= ChartNode.CNDL_INDX_MOVE_COEF * speed;	
			newHSBPos = (((ChartNode) sbo).chart().width() - sbWidth - ((ChartNode) sbo).chart().priceMargin().width()) * ((double)startIndex /(((ChartNode) sbo).data().m1CandlesDataSize(((ChartNode) sbo).replayMode()).get() - ((ChartNode) sbo).numCandlesticks() * ChartNode.END_MARGIN_COEF));
			((ChartNode) sbo).setKeepStartIndex(false);
		} else {
			startIndex -= ChartNode.TICK_INDX_MOVE_COEF * speed;	
			newHSBPos = (((ChartNode) sbo).chart().width() - sbWidth - ((ChartNode) sbo).chart().priceMargin().width()) * ((double)startIndex /(((ChartNode) sbo).data().tickDataSize(((ChartNode) sbo).replayMode()).get() - ((ChartNode) sbo).numDataPoints() * ChartNode.END_MARGIN_COEF));
			((ChartNode) sbo).setKeepStartIndex(false);
		}			
		((ChartNode) sbo).setKeepStartIndex(false);
		setPosition(newHSBPos, false);
	}
	
	@Override
	protected void moveOwnerRight(boolean fast) {
		int speed = 10;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos;
		int startIndex = ((ChartNode) sbo).startIndex();
		if (((ChartNode) sbo).drawCandlesticks().get()) {
			startIndex += ChartNode.CNDL_INDX_MOVE_COEF * speed;
			newHSBPos = (((ChartNode) sbo).chart().width() - sbWidth - ((ChartNode) sbo).chart().priceMargin().width()) * ((double)startIndex /(((ChartNode) sbo).data().m1CandlesDataSize(((ChartNode) sbo).replayMode()).get() - ((ChartNode) sbo).numCandlesticks() * ChartNode.END_MARGIN_COEF));
			((ChartNode) sbo).setKeepStartIndex(false);
		} else {
			startIndex += ChartNode.TICK_INDX_MOVE_COEF * speed;	
			newHSBPos = (((ChartNode) sbo).chart().width() - sbWidth - ((ChartNode) sbo).chart().priceMargin().width()) * ((double)startIndex /(((ChartNode) sbo).data().tickDataSize(((ChartNode) sbo).replayMode()).get() - ((ChartNode) sbo).numDataPoints() * ChartNode.END_MARGIN_COEF));
			((ChartNode) sbo).setKeepStartIndex(false);
		}			
		((ChartNode) sbo).setKeepStartIndex(false);
		setPosition(newHSBPos, false);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getCode()) {
			case KeyCode.LEFT:				
				reduceSBPos(e);
				Chart.drawCharts(((ChartNode) sbo).name());
				break;
			case KeyCode.RIGHT:			
				increaseSBPos(e);
				Chart.drawCharts(((ChartNode) sbo).name());
				break;
			default:				
		}
	}
}
