import java.time.LocalDateTime;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
	private ArrayList<CanvasNumberChooser> numbers;
	private boolean bPlay = true;
	private boolean bLive = true;
	private String name;	
	
	private Tree<CanvasNode> sceneGraph;
	private CanvasWrapper cw;
	private TNode<CanvasNode> lastNode = null;
	
	private static ArrayList<MarketReplayPane> panes = new ArrayList<MarketReplayPane>();
	
	private CanvasButton newChart;
	private ButtonVanGogh nvg = (x, y, gc) -> {
		if (Chart.darkMode()) {
			gc.setFill(Color.BLACK);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setFill(Color.WHITE);
			gc.setStroke(Color.BLACK);
		}			
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
		if (Chart.darkMode()) {
			gc.setFill(Color.WHITE);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
			gc.setStroke(Color.BLACK);
		}	
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
		if (Chart.darkMode()) {
			gc.setFill(Color.WHITE);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
			gc.setStroke(Color.BLACK);
		}
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
		if (Chart.darkMode()) {
			gc.setFill(Color.WHITE);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
			gc.setStroke(Color.BLACK);
		}
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
		if (Chart.darkMode()) {
			gc.setFill(Color.WHITE);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
			gc.setStroke(Color.BLACK);
		}
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
		stage.setTitle(name + " Replay");
		mr = new MarketReplay(chart, this, index);	
		canvas = new Canvas(399, 100);
		gc = canvas.getGraphicsContext2D();
		hsb = new HorizontalMRPaneScrollBar(this, chart.tickData().size(), 0, 399, 50, 10, 90);
		numbers = new ArrayList<CanvasNumberChooser>();
		
		newChart = new CanvasButton(gc, 40, 20, 349, 10, null, 0, 0);
		newChart.setVanGogh(nvg);
		pausePlay = new CanvasButton(gc, 40, 40, 10, 40, null, 0, 0);
		pausePlay.setVanGogh(pvg);
		back = new CanvasButton(gc, 40, 40, 60, 40, null, 0, 0);
		back.setVanGogh(bvg);
		forward = new CanvasButton(gc, 40, 40, 210, 40, null, 0, 0);
		forward.setVanGogh(fvg);
		live = new CanvasButton(gc, 40, 40, 349, 40, null, 0, 0);
		live.setVanGogh(lvg);
		
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
		
		sceneGraph = new Tree<CanvasNode>();
		cw = new CanvasWrapper(canvas, sceneGraph);
		sceneGraph.addNode(new TNode<CanvasNode>(cw, null));
		
		hsb.setOnMouseDragged(e -> {
			hsb.defaultOnMouseDragged(e);
			mr.setIndex((int)(((hsb.x() - hsb.minPos()) / (hsb.maxPos() - hsb.sbWidth() - hsb.minPos())) * mr.tickDataSize().get()), false);
		});
		newChart.setOnMouseReleased(e -> {
			Stage s = new Stage();
			ChartPane c = new ChartPane(s, 1280, 720, mr.data(), true, mr, this);
			Scene scene = new Scene(c);	
			scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> c.getChart().hsb().keyPressed(ev));
			s.setScene(scene);
			s.show();
		});
		pausePlay.setOnMouseReleased(e -> {
			if (bPlay) {
				bPlay = false;
				mr.togglePause();
			} else {
				bPlay = true;
				mr.togglePause();
			}
		});
		back.setOnMouseReleased(e -> {
			mr.setIndex(-moveNumber(), true);
			for (Chart c : mr.charts()) {
				c.draw();
			}		
		});
		forward.setOnMouseReleased(e -> {
			mr.setIndex(moveNumber(), true);
			for (Chart c : mr.charts()) {
				c.draw();
			}		
		});
		live.setOnMouseReleased(e -> {
			if (bLive) {
				bLive = false;
				mr.toggleLive();
			} else {
				bLive = true;
				mr.toggleLive();
			}
		});
		s1.setOnMouseReleased(e -> {
			if (speedNumber() == 0) {
				s3.incrementValue();
			}
			mr.setSpeed(speedNumber());
		});
		s2.setOnMouseReleased(e -> {
			if (speedNumber() == 0) {
				s3.incrementValue();
			}
			mr.setSpeed(speedNumber());
		});
		s3.setOnMouseReleased(e -> {
			if (speedNumber() == 0) {
				s3.incrementValue();
			}
			mr.setSpeed(speedNumber());
		});

		sceneGraph.addNode(new TNode<CanvasNode>(hsb, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(newChart, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(pausePlay, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(back, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(forward, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(live, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(bf1, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(bf2, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(bf3, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(bf4, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(s1, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(s2, sceneGraph.root()));
		sceneGraph.addNode(new TNode<CanvasNode>(s3, sceneGraph.root()));			
		
		canvas.addEventFilter(Event.ANY, e -> {
			canvasEventFilter(e);
		});
		
		this.add(canvas, 0, 0);
		mr.run();
		draw();
		panes.add(this);
	}
	
	private void canvasEventFilter(Event e) {
		boolean onNode = false;
		MouseEvent me = null;
		ScrollEvent se = null;
		for (TNode<CanvasNode> t : sceneGraph.postOrderArray()) {	
			CanvasNode cn = t.element();
			if (e instanceof MouseEvent) {
				me = (MouseEvent)e;
				if (!cn.onNode(me.getX(), me.getY())) {
					continue;
				}
				if (!(cn instanceof CanvasWrapper)) {
					onNode = true;
				}
				if (lastNode == null) {
					lastNode = t;
					cn.onMouseEntered(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
					cn.onMouseDragged(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
					cn.onMouseExited(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
					cn.onMousePressed(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
					cn.onMouseReleased(me);
				} else if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
					cn.onMouseMoved(me);
				}
				break;
			} else if (e instanceof ScrollEvent) {
				se = (ScrollEvent)e;
				if (!cn.onNode(se.getX(), se.getY())) {
					continue;
				}
				if (!(cn instanceof CanvasWrapper)) {
					onNode = true;
				}
				if (e.getEventType() == ScrollEvent.SCROLL) {
					cn.onScroll(se);
				}
				break;
			}
		}		
		if (me != null) {
			if (!onNode && lastNode != null) {
				lastNode.element().onMouseExited(me);
				lastNode = null;
			}
			sceneGraph.root().element().onMouseMoved(me);
		}
		draw();
	}
	
	public Canvas canvas() {
		return canvas;
	}
	
	public String name() {
		return this.name;
	}
	
	public HorizontalMRPaneScrollBar hsb() {
		return this.hsb;
	}
	
	public static void drawReplayPanes() {
		for (MarketReplayPane m : panes) {
			m.draw();
		}
	}
	
	@Override
	public void draw() {
		if (Platform.isFxApplicationThread()) {
			draw(gc, 0, 0);
		} else {
			Platform.runLater(() -> {
				draw(gc, 0, 0);
			});
		}		
	}
	
	private void draw(GraphicsContext gc, double x, double y) {
		double fontSize = gc.getFont().getSize();
		if (Chart.darkMode()) {
			gc.setFill(Color.BLACK);
			gc.setStroke(Color.WHITE);
		} else {
			gc.setFill(Color.WHITE);
			gc.setStroke(Color.BLACK);
		}
		gc.fillRect(x - 1, y - 1, 401, 102);		
		gc.strokeRect(x - 1, y - 1, 401, 102);
		gc.setFont(new Font(20));		
		int percent = (int)(mr.index().get() * 100 / (double)(mr.maxSize().get() - 1));
		if (percent > 100) {
			percent = 100;
		} else if (percent < 0) {
			percent = 0;
		}
		DataSet data = mr.data();		
		int index = data.tickDataSize(true).get() - 1;
		LocalDateTime tick = null;
		String time = "";
		if (index > -1) {
			tick = data.tickData().get(index).dateTime();
			time = tick.minusNanos(tick.getNano()).toString().replace('T', ' ');
		}
		if (Chart.darkMode()) {
			gc.setFill(Color.WHITE);
		} else {
			gc.setFill(Color.BLACK);
		}
		gc.fillText(percent + "%  " + time, x + 10, y + 25, 240);
		gc.fillText("SPEED", x + 260, y + 25);
		gc.setFont(new Font(fontSize));
		int i = 0;
		for (TNode<CanvasNode> tn : sceneGraph.postOrderArray()) {
			CanvasNode d = tn.element();
			if (d instanceof CanvasNumberChooser) {
				((CanvasNumberChooser)d).resetColours();
			}
			GraphicsContext g = d.graphicsContext();
			d.setGraphicsContext(gc);
			double x2 = d.x();
			double y2 = d.y();	
			if (i == 0) {
				HorizontalMRPaneScrollBar sb = (HorizontalMRPaneScrollBar)sceneGraph.postOrderArray().get(0).element();		
				double minPos = sb.minPos();
				double maxPos = sb.maxPos();
				sb.setMinPos(x + minPos);
				sb.setMaxPos(x + maxPos);
				sb.setX(x + minPos + x2);
				sb.setY(y + y2);				
				sb.draw();
				sb.setMinPos(minPos);
				sb.setMaxPos(maxPos);
				sb.setX(x2);
				sb.setY(y2);
				i++;
			} else {											
				d.setX(x + x2);
				d.setY(y + y2);
				d.draw();							
				d.setX(x2);
				d.setY(y2);
			}		
			d.setGraphicsContext(g);
		}				
	}
	
	public void drawPane(GraphicsContext gc, double x, double y) {
		draw(gc, x, y);
	}
	
	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}
	
	public void endReplay() {
		panes.remove(this);
		Chart.closeAll(name, true);
		mr.data().setReplayM1CandlesDataSize(0);
		mr.data().setReplayTickDataSize(0);
		mr.stop();
		stage.close();
	}
	
	private int moveNumber() {
		CanvasNumberChooser[] c = {bf1, bf2, bf3, bf4};
		return CanvasNumberChooser.number(c);
	}
	
	private int speedNumber() {
		CanvasNumberChooser[] c = {s1, s2, s3};
		return CanvasNumberChooser.number(c);
	}
}
