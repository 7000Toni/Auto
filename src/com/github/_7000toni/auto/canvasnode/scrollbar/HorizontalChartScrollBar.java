package com.github._7000toni.auto.canvasnode.scrollbar;
import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.chart.ChartNode;
import com.github._7000toni.auto.chart.CrossHair;
import com.github._7000toni.auto.dataset.timeframe.Timeframe;

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
		if (onScrollBar(e.getX(), e.getY())) {					
			dragging = true;
			initPos = e.getX();
		} else if (!((ChartNode) sbo).replayMode() && inScrollBarArea(e.getX(), e.getY())) {
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
			setPosition(x + posDiff, false);
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
		ChartNode cn = ((ChartNode) sbo);
		int startIndex = cn.startIndex();
		Timeframe tf = cn.timeframe();
		if (cn.drawCandlesticks().get() || !tf.base()) {			
			startIndex -= ChartNode.CNDL_INDX_MOVE_COEF * speed;
			newHSBPos = (cn.width() - sbWidth + ChartNode.CHT_MARGIN) * ((double)startIndex / (tf.size(cn.replayMode(), false) - cn.numCandlesticks() * ChartNode.END_MARGIN_COEF));
			cn.setKeepStartIndex(false);
		} else {			
			startIndex -= ChartNode.TICK_INDX_MOVE_COEF * speed;
			newHSBPos = (cn.width() - sbWidth + ChartNode.CHT_MARGIN) * ((double)startIndex / (tf.size(cn.replayMode(), true) - cn.numDataPoints() * ChartNode.END_MARGIN_COEF));
			cn.setKeepStartIndex(false);
		}			
		cn.setKeepStartIndex(false);
		setPosition(newHSBPos, false);
	}
	
	@Override
	protected void moveOwnerRight(boolean fast) {
		int speed = 10;
		if (fast) {
			speed *= 2;
		}
		double newHSBPos;
		ChartNode cn = ((ChartNode) sbo);
		int startIndex = cn.startIndex();
		Timeframe tf = cn.timeframe();
		if (cn.drawCandlesticks().get() || !tf.base()) {
			startIndex += ChartNode.CNDL_INDX_MOVE_COEF * speed;
			newHSBPos = (cn.width() - sbWidth + ChartNode.CHT_MARGIN) * ((double)startIndex / (tf.size(cn.replayMode(), false) - cn.numCandlesticks() * ChartNode.END_MARGIN_COEF));
			cn.setKeepStartIndex(false);
		} else {
			startIndex += ChartNode.TICK_INDX_MOVE_COEF * speed;	
			newHSBPos = (cn.width() - sbWidth + ChartNode.CHT_MARGIN) * ((double)startIndex / (tf.size(cn.replayMode(), true) - cn.numDataPoints() * ChartNode.END_MARGIN_COEF));
			cn.setKeepStartIndex(false);
		}			
		cn.setKeepStartIndex(false);
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
