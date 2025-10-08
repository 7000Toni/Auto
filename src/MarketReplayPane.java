import java.util.ArrayList;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MarketReplayPane extends GridPane implements ScrollBarOwner {
	private Stage stage;
	private MarketReplay mr;
	private Canvas canvas;
	private GraphicsContext gc;
	private HorizontalMRPaneScrollBar hsb;
	private ArrayList<Drawable> drawables;
	private ArrayList<CanvasNumberChooser> numbers;
	private boolean bPlay = true;
	private boolean bLive = true;
	private String name;
	
	private CanvasButton newChart;
	private ButtonVanGogh nvg = (x, y, gc) -> {
		gc.setFill(Color.WHITE);
		gc.setStroke(Color.BLACK);
		if (newChart.hover) {
			gc.setFill(Color.GRAY);
			gc.setStroke(Color.GRAY);
		}
		if (newChart.pressed) {
			gc.setFill(Color.DIMGRAY);
		}
		gc.strokeRect(x, y, 40, 20);
		gc.fillRect(x + 1, y + 1, 38, 18);
	};
	private CanvasButton pausePlay;
	private ButtonVanGogh pvg = (x, y, gc) -> {
		gc.setFill(Color.BLACK);
		gc.setStroke(Color.BLACK);
		if (pausePlay.hover) {
			gc.setFill(Color.GRAY);
			gc.setStroke(Color.GRAY);
		}
		if (bPlay) {
			gc.setFill(Color.ORANGE);
			gc.strokeRect(x, y, 40, 40);	
			gc.fillRect(x + 10, y + 10, 8, 20);
			gc.fillRect(x + 22, y + 10, 8, 20);
		} else {
			gc.setFill(Color.SPRINGGREEN);
			gc.strokeRect(x, y, 40, 40);	
			double[] xa = {x + 12, x + 32, x + 12, x + 12};
			double[] ya = {y + 10, y + 20, y + 30, y + 10};
			gc.fillPolygon(xa, ya, 4);
		}
	};
	private CanvasButton back;
	private ButtonVanGogh bvg = (x, y, gc) -> {
		gc.setFill(Color.BLACK);
		gc.setStroke(Color.BLACK);
		if (back.hover) {
			gc.setFill(Color.GRAY);
			gc.setStroke(Color.GRAY);
		}
		if (back.pressed) {
			gc.setFill(Color.DIMGRAY);
		}
		gc.strokeRect(x, y, 40, 40);	
		double[] xa = {x + 5, x + 20, x + 20, x + 35, x + 35, x + 20, x + 20, x + 5};
		double[] ya = {y + 20, y + 8, y + 15, y + 15, y + 25, y + 25, y + 32, y + 20};
		gc.fillPolygon(xa, ya, 8);
	};
	private CanvasButton forward;
	private ButtonVanGogh fvg = (x, y, gc) -> {
		gc.setFill(Color.BLACK);
		gc.setStroke(Color.BLACK);
		if (forward.hover) {
			gc.setFill(Color.GRAY);
			gc.setStroke(Color.GRAY);
		}
		if (forward.pressed) {
			gc.setFill(Color.DIMGRAY);
		}
		gc.strokeRect(x, y, 40, 40);	
		double[] xa = {x + 5, x + 20, x + 20, x + 35, x + 20, x + 20, x + 5, x + 5};
		double[] ya = {y + 15, y + 15, y + 8, y + 20, y + 32, y + 25, y + 25, y + 15};
		gc.fillPolygon(xa, ya, 8);
	};
	private CanvasButton live;
	private ButtonVanGogh lvg = (x, y, gc) -> {
		gc.setFill(Color.BLACK);
		gc.setStroke(Color.BLACK);
		if (live.hover) {
			gc.setFill(Color.GRAY);
			gc.setStroke(Color.GRAY);
		}
		if (bLive) {
			gc.setFill(Color.RED);
			gc.strokeRect(x, y, 40, 40);	
			gc.fillOval(x + 15, y + 15, 10, 10);
		} else {
			gc.setFill(Color.GRAY);
			gc.strokeRect(x, y, 40, 40);	
			gc.fillOval(x + 15, y + 15, 10, 10);
		}
	};
	
	private CanvasNumberChooser bf1;
	private CanvasNumberChooser bf2;
	private CanvasNumberChooser bf3;
	private CanvasNumberChooser bf4;
	
	private CanvasNumberChooser s1;
	private CanvasNumberChooser s2;
	private CanvasNumberChooser s3;
	
	public MarketReplayPane(Chart chart, int index, Stage stage) {
		this.stage = stage;
		name = chart.name();
		mr = new MarketReplay(chart, this, index);	
		canvas = new Canvas(399, 100);
		gc = canvas.getGraphicsContext2D();
		hsb = new HorizontalMRPaneScrollBar(this, chart.tickData().size(), 0, 399, 50, 10, 90);
		drawables = new ArrayList<Drawable>();
		numbers = new ArrayList<CanvasNumberChooser>();
		
		newChart = new CanvasButton(gc, 40, 20, 349, 10, null, 0, 0, nvg);
		pausePlay = new CanvasButton(gc, 40, 40, 10, 40, null, 0, 0, pvg);
		back = new CanvasButton(gc, 40, 40, 60, 40, null, 0, 0, bvg);
		forward = new CanvasButton(gc, 40, 40, 210, 40, null, 0, 0, fvg);
		live = new CanvasButton(gc, 40, 40, 349, 40, null, 0, 0, lvg);
		
		double h = CanvasNumberChooser.getHeightForDesiredNumberHight(40);
		double y = 40 - CanvasNumberChooser.buttonHeight(h);
		bf1 = new CanvasNumberChooser(gc, 25, h, 102, y);
		bf2 = new CanvasNumberChooser(gc, 25, h, 129, y);
		bf3 = new CanvasNumberChooser(gc, 25, h, 156, y);
		bf4 = new CanvasNumberChooser(gc, 25, h, 183, y);
		numbers.add(bf1);	
		numbers.add(bf2);
		numbers.add(bf3);
		numbers.add(bf4);
		
		s1 = new CanvasNumberChooser(gc, 25, h, 260, y);
		s2 = new CanvasNumberChooser(gc, 25, h, 287, y);
		s3 = new CanvasNumberChooser(gc, 25, h, 314, y);
		s3.setValue(1);
		numbers.add(s1);
		numbers.add(s2);
		numbers.add(s3);
		
		drawables.add(hsb);
		drawables.add(newChart);
		drawables.add(pausePlay);
		drawables.add(back);
		drawables.add(forward);
		drawables.add(live);
		drawables.add(bf1);
		drawables.add(bf2);
		drawables.add(bf3);
		drawables.add(bf4);
		drawables.add(s1);
		drawables.add(s2);
		drawables.add(s3);
		
		canvas.setOnMousePressed(e -> onMousePressed(e));
		canvas.setOnMouseReleased(e -> onMouseReleased(e));
		canvas.setOnMouseMoved(e -> onMouseMoved(e));
		canvas.setOnMouseDragged(e -> onMouseDragged(e));
		canvas.setOnMouseExited(e -> onMouseExited(e));
		
		this.add(canvas, 0, 0);
		mr.run();
		draw();
	}
	
	public String name() {
		return this.name;
	}
	
	public HorizontalMRPaneScrollBar hsb() {
		return this.hsb;
	}
	
	@Override
	public void draw() {
		gc.clearRect(0, 0, 399, 100);
		gc.setFont(new Font(20));
		gc.setFill(Color.BLACK);
		int percent = (int)(mr.index() * 100 / (double)(mr.maxSize() - 1));
		if (percent > 100) {
			percent = 100;
		} else if (percent < 0) {
			percent = 0;
		}
		gc.fillText(name + "  " + percent + "%", 10, 25, 240);
		gc.fillText("SPEED", 260, 25);
		for (Drawable d : drawables) {
			d.draw();
		}
	}

	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}
	
	private void onMousePressed(MouseEvent e) {
		hsb.onMousePressed(e);
		double x = e.getX();
		double y = e.getY();
		if (newChart.onButton(x, y)) {
			newChart.setPressed(true);
		} else if (pausePlay.onButton(x, y)) {
			pausePlay.setPressed(true);
		} else if (back.onButton(x, y)) {
			back.setPressed(true);
		} else if (forward.onButton(x, y)) {
			forward.setPressed(true);
		} else if (live.onButton(x, y)) {
			live.setPressed(true);
		} else {
			for (CanvasNumberChooser c : numbers) {
				if (c.onDown(x, y)) {
					c.setDownPressed(true);
					break;
				} else if (c.onUp(x, y)) {
					c.setUpPressed(true);
					break;
				}
			}
		}
		draw();
	}
	
	private void onMouseReleased(MouseEvent e) {
		hsb.onMouseReleased();
		double x = e.getX();
		double y = e.getY();
		if (newChart.onButton(x, y)) {
			if (newChart.pressed()) {
				Stage s = new Stage();
				ChartPane c = new ChartPane(s, 1280, 720, mr.data(), true, mr);
				Scene scene = new Scene(c);	
				scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
				s.setScene(scene);
				s.show();
			}
		} else if (pausePlay.onButton(x, y)) {
			if (pausePlay.pressed()) {
				if (bPlay) {
					bPlay = false;
					mr.togglePause();
				} else {
					bPlay = true;
					mr.togglePause();
				}
			}
			pausePlay.setPressed(false);
		} else if (back.onButton(x, y)) {
			if (back.pressed()) {
				mr.setIndex(-moveNumber(), true);
				for (Chart c : mr.charts()) {
					c.draw();
				}
			}
			back.setPressed(false);
		} else if (forward.onButton(x, y)) {
			if (forward.pressed()) {
				mr.setIndex(moveNumber(), true);
				for (Chart c : mr.charts()) {
					c.draw();
				}
			}
			forward.setPressed(false);
		} else if (live.onButton(x, y)) {
			if (live.pressed()) {
				if (bLive) {
					bLive = false;
					mr.toggleLive();
				} else {
					bLive = true;
					mr.toggleLive();
				}
			}
			live.setPressed(false);
		} else {
			for (CanvasNumberChooser c : numbers) {
				if (c.onDown(x, y)) {
					if (c.downPressed()) {
						c.decrementValue();
						if (speedNumber() == 0) {
							s3.incrementValue();
						}
						mr.setSpeed(speedNumber());
					}
					c.setDownPressed(false);
					break;
				} else if (c.onUp(x, y)) {
					if (c.upPressed()) {
						c.incrementValue();
						if (speedNumber() == 0) {
							s3.incrementValue();
						}
						mr.setSpeed(speedNumber());
					}
					c.setUpPressed(false);
					break;
				}
			}
		}
		draw();
	}
	
	public void endReplay() {
		Chart.closeAll(name, true);
		mr.stop();
		stage.close();
	}
	
	private void onMouseMoved(MouseEvent e) {
		hsb.onMouseMoved(e);
		double x = e.getX();
		double y = e.getY();
		ButtonChecks.mouseButtonHoverCheck(newChart, x, y);
		ButtonChecks.mouseButtonHoverCheck(pausePlay, x, y);
		ButtonChecks.mouseButtonHoverCheck(back, x, y);
		ButtonChecks.mouseButtonHoverCheck(forward, x, y);
		ButtonChecks.mouseButtonHoverCheck(live, x, y);
		for (CanvasNumberChooser c : numbers) {
			ButtonChecks.mouseNumberChooserUpHoverCheck(c, x, y);
			ButtonChecks.mouseNumberChooserDownHoverCheck(c, x, y);
		}
		draw();
	}
	
	private void onMouseDragged(MouseEvent e) {
		hsb.onMouseDragged(e);
		draw();
	}
	
	private void onMouseExited(MouseEvent e) {
		hsb.onMouseExited();
		draw();
	}
	
	private int moveNumber() {
		String num = ((Integer) bf1.value()).toString();
		num += ((Integer) bf2.value()).toString();
		num += ((Integer) bf3.value()).toString();
		num += ((Integer) bf4.value()).toString();
		return Integer.parseInt(num);
	}
	
	private int speedNumber() {
		String num = ((Integer) s1.value()).toString();
		num += ((Integer) s2.value()).toString();
		num += ((Integer) s3.value()).toString();
		return Integer.parseInt(num);
	}
}
