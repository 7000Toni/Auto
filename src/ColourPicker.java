import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public class ColourPicker implements CanvasNode, ScrollBarOwner {
	private double x;
	private double y;
	private double width;
	private double height;
	private GraphicsContext gc;
	private ColourPickerScrollBar hsb; 
	private Color[][] colours;
	private boolean coloursInitialized;
	
	private EventHandler<? super MouseEvent> onMouseDragged;
	private EventHandler<? super MouseEvent> onMouseEntered;
	private EventHandler<? super MouseEvent> onMouseExited;
	private EventHandler<? super MouseEvent> onMousePressed;
	private EventHandler<? super MouseEvent> onMouseClicked;
	private EventHandler<? super MouseEvent> onMouseReleased;
	private EventHandler<? super MouseEvent> onMouseMoved;
	private EventHandler<? super ScrollEvent> onScroll;
	
	public ColourPicker(double x, double y, double width, double height, GraphicsContext gc) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gc = gc;
		hsb = new ColourPickerScrollBar(this, 255*6, x - 2, x + 292, 15, 15, y + 150);
		colours = new Color[144][144];
		initializeColours();
	}
	
	private void initializeColours() {
		Color c = ColourCalculator.colour(hsb.x(), hsb.minPos(), hsb.maxPos() - hsb.sbWidth());
		int r = (int)(c.getRed() * 255);
		int g = (int)(c.getGreen() * 255);
		int b = (int)(c.getBlue() * 255);
		
		double hsbPerc = (hsb.x() - hsb.minPos()) / (hsb.maxPos() - hsb.sbWidth() - hsb.minPos());
		for (double i = x + 146; i < x + 290; i++) {
			double percx = (144 - (i - x - 146)) / 144;
			for (double j = y + 1; j < y + 145; j++) {
				double percy = (144 - (j - y - 1)) / 144;
				int r2;
				int g2;
				int b2;
				if (hsbPerc > 5.0/6 || hsbPerc < 1.0/6) {
					r2 = (int) (percy * r);
					g2 = (int) (((percx * (255 - g)) + g) * percy);
					b2 = (int) (((percx * (255 - b)) + b) * percy);
				} else if (hsbPerc > 1.0/6 && hsbPerc < 1.0/2) {
					r2 = (int) (((percx * (255 - r)) + r) * percy);
					g2 = (int) (percy * g);
					b2 = (int) (((percx * (255 - b)) + b) * percy);
				} else {
					r2 = (int) (((percx * (255 - r)) + r) * percy);
					g2 = (int) (((percx * (255 - g)) + g) * percy);
					b2 = (int) (percy * b);
				}				
				colours[(int)(i - x - 146)][(int)(j - y - 1)] = Color.web("rgb(" + r2 + "," + g2 + "," +  b2 + ")");
				gc.getPixelWriter().setColor((int)i, (int)j, Color.web("rgb(" + r2 + "," + g2 + "," +  b2 + ")"));
			}
		}
		coloursInitialized = true;
	}
	
	public void unintializeColours() {
		coloursInitialized = false;
	}
	
	private void fillBrightnessSquare() {
		if (coloursInitialized) {
			for (double i = x + 146; i < x + 290; i++) {
				for (double j = y + 1; j < y + 145; j++) {			
					gc.getPixelWriter().setColor((int)i, (int)j, colours[(int)(i - x - 146)][(int)(j - y - 1)]);
				}
			}
		} else {
			initializeColours();
		}
	}
	
	public ColourPickerScrollBar hsb() {
		return hsb;
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
		if (onMouseDragged == null) {
			return;
		}
		onMouseDragged.handle(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onMouseEntered == null) {
			return;
		}
		onMouseEntered.handle(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		if (onMouseExited == null) {
			return;
		}
		onMouseExited.handle(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		if (onMousePressed == null) {
			return;
		}
		onMousePressed.handle(e);
	}
	@Override
	public void onMouseClicked(MouseEvent e) {	
		if (onMouseClicked == null) {
			return;
		}
		onMouseClicked.handle(e);
	}
	
	@Override
	public void onMouseReleased(MouseEvent e) {	
		if (onMouseReleased == null) {
			return;
		}
		onMouseReleased.handle(e);		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		if (onMouseMoved == null) {
			return;
		}
		onMouseMoved.handle(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		if (onScroll == null) {
			return;
		}
		onScroll.handle(e);
	}

	@Override
	public void setOnMouseDragged(EventHandler<? super MouseEvent> e) {
		onMouseDragged = e;
	}

	@Override
	public void setOnMouseEntered(EventHandler<? super MouseEvent> e) {
		onMouseEntered = e;
	}

	@Override
	public void setOnMouseExited(EventHandler<? super MouseEvent> e) {
		onMouseExited = e;
	}

	@Override
	public void setOnMousePressed(EventHandler<? super MouseEvent> e) {
		onMousePressed = e;
	}

	@Override
	public void setOnMouseClicked(EventHandler<? super MouseEvent> e) {
		onMouseClicked = e;
	}
	
	@Override
	public void setOnMouseReleased(EventHandler<? super MouseEvent> e) {
		onMouseReleased = e;
	}

	@Override
	public void setOnMouseMoved(EventHandler<? super MouseEvent> e) {
		onMouseMoved = e;
	}

	@Override
	public void setOnScroll(EventHandler<? super ScrollEvent> e) {
		onScroll = e;
	}

	@Override
	public boolean onNode(double x, double y) {
		if (x >= this.x && x < this.x + width && y >= this.y && y < this.y + height) {
			return true;
		}
		return false;
	}	
	
	private void fillHSBBar() {
		for (double i = x + 1; i < x + 289; i++) {
			gc.setStroke(ColourCalculator.colour(i, x + 1, x + 289));
			gc.strokeLine(i, y + 156, i, y + 159);
		}
	}
	
	@Override
	public void draw() {		
		if (Chart.darkMode().get()) {
			gc.setFill(Color.BLACK);
			gc.fillRect(x, y, width, height);
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
		} else {
			gc.setFill(Color.WHITE);
			gc.fillRect(x, y, width, height);
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
		}	
		
		gc.strokeRect(x, y, width, 145);
		gc.strokeLine(x + 145, y, x + 145, y + 145);
		gc.strokeRect(x, y + 155, 290, 5);
		fillBrightnessSquare();
		fillHSBBar();
		hsb.draw();
	}

	@Override
	public GraphicsContext graphicsContext() {
		return gc;
	}

	@Override
	public void setGraphicsContext(GraphicsContext gc) {
		this.gc = gc;
	}

	@Override
	public void setX(double x) {
		double hsbOffset = hsb.x() - this.x;
		this.x = x;		
		hsb.setMinPos(x - 2);
		hsb.setMaxPos(x + 292);
		hsb.setX(hsbOffset + x);
	}

	@Override
	public void setY(double y) {
		this.y = y;
		hsb.setY(y + 150);
	}

	@Override
	public double x() {
		return x;
	}

	@Override
	public double y() {
		return y;
	}
}
