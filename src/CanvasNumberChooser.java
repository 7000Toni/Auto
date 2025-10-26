import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CanvasNumberChooser implements Drawable {
	private GraphicsContext gc;
	private double width;
	private double height;
	private double x;
	private double y;
	private int value = 0;
	private boolean upHover = false;
	private boolean upPressed = false;
	private boolean downHover = false;
	private boolean downPressed = false;
	
	private boolean l1 = true;
	private boolean l2 = true;
	private boolean l3 = true;
	private boolean l4 = true;
	private boolean l5 = true;
	private boolean l6 = true;
	private boolean l7 = false;
	
	private Color onColour = Color.BLACK;
	private Color offColour = Color.LIGHTGRAY;
	private Color hoverColour = Color.GRAY;
	private Color pressColour = Color.DIMGRAY;
	
	public CanvasNumberChooser(GraphicsContext gc, double width, double height, double x, double y) {
		this.gc = gc;
		this.width = width;
		this.height = height/(1+0.125*1.3);
		this.x = x;
		this.y = y + this.height*0.125*1.3;
	}
	
	public static int number(CanvasNumberChooser[] numbers) {	
		String num = "";
		for (CanvasNumberChooser c : numbers) {
			num += ((Integer) c.value()).toString();
		}		
		return Integer.parseInt(num);
	}
	
	public static double numberHeight(double height) {
		return height - 2*0.125*1.3*(height/(1+0.125*1.3));
	}
	
	public static double buttonHeight(double height) {
		return 0.125*1.3*(height/(1+0.125*1.3));
	}
	
	public static double getHeightForDesiredNumberHight(double height) {
		return height + 2*0.125*1.3*(height/(1+0.125*1.3));
	}
	
	public void setOnColour(Color onColour) {
		this.onColour = onColour;
	}
	
	public void setOffColour(Color offColour) {
		this.offColour = offColour;
	}
	
	public void setHoverColour(Color hoverColour) {
		this.hoverColour = hoverColour;
	}
	
	public void setPressColour(Color pressColour) {
		this.pressColour = pressColour;
	}
	
	public void resetColours() {
		onColour = Color.BLACK;
		offColour = Color.LIGHTGRAY;
		hoverColour = Color.GRAY;
		pressColour = Color.DIMGRAY;
	}
		
	public boolean upHover() {
		return upHover;
	}
	
	public boolean upPressed() {
		return upPressed;
	}
	
	public boolean downHover() {
		return downHover;
	}
	
	public boolean downPressed() {
		return downPressed;
	}
	
	public int value() {
		return value;
	}
	
	public void setUpHover(boolean upHover) {
		this.upHover = upHover;
	}
	
	public void setUpPressed(boolean upClicked) {
		this.upPressed = upClicked;
	}
	
	public void setDownHover(boolean downHover) {
		this.downHover = downHover;
	}
	
	public void setDownPressed(boolean downClicked) {
		this.downPressed = downClicked;
	}
	
	@Override
	public GraphicsContext graphicsContext() {
		return this.gc;
	}
	
	@Override
	public void setGraphicsContext(GraphicsContext gc) {
		this.gc = gc;
	}
	
	@Override
	public double x() {
		return this.x;
	}
	
	@Override
	public double y() {
		return this.y;
	}
	
	@Override
	public void setX(double x) {
		this.x = x;
	}
	
	@Override
	public void setY(double y) {
		this.y = y;
	}
	
	public void setValue(int value) {
		this.value = value % 10;
		if (value < 0) {
			value = 10 - value;
		}
	}
	
	public void incrementValue() {
		value = (value + 1) % 10;
	}
	
	public void decrementValue() {
		value -= 1;
		if (value < 0) {
			value = 9;
		}
	}
	
	private void setVars() {
		switch (value) {
			case 0:
				l1 = true;
				l2 = true;
				l3 = true;
				l4 = true;
				l5 = true;
				l6 = true;
				l7 = false;
				break;
			case 1:
				l1 = false;
				l2 = true;
				l3 = true;
				l4 = false;
				l5 = false;
				l6 = false;
				l7 = false;
				break;
			case 2:
				l1 = true;
				l2 = true;
				l3 = false;
				l4 = true;
				l5 = true;
				l6 = false;
				l7 = true;
				break;
			case 3:
				l1 = true;
				l2 = true;
				l3 = true;
				l4 = true;
				l5 = false;
				l6 = false;
				l7 = true;
				break;
			case 4:
				l1 = false;
				l2 = true;
				l3 = true;
				l4 = false;
				l5 = false;
				l6 = true;
				l7 = true;
				break;
			case 5:
				l1 = true;
				l2 = false;
				l3 = true;
				l4 = true;
				l5 = false;
				l6 = true;
				l7 = true;
				break;
			case 6:
				l1 = true;
				l2 = false;
				l3 = true;
				l4 = true;
				l5 = true;
				l6 = true;
				l7 = true;
				break;
			case 7:
				l1 = true;
				l2 = true;
				l3 = true;
				l4 = false;
				l5 = false;
				l6 = false;
				l7 = false;
				break;
			case 8:
				l1 = true;
				l2 = true;
				l3 = true;
				l4 = true;
				l5 = true;
				l6 = true;
				l7 = true;
				break;
			case 9:
				l1 = true;
				l2 = true;
				l3 = true;
				l4 = false;
				l5 = false;
				l6 = true;
				l7 = true;
				break;
			default:
		}
	}
	
	public void draw() {
		double t = 0.125*height;
		setVars();
		if (l1) {
			gc.setFill(onColour);
		} else {
			gc.setFill(offColour);
		}
		double[] x1 = {x,x+width,x+width-t*1.2,x+t*1.2,x};
		double[] y1 = {y,y,y+t,y+t,y};
		gc.fillPolygon(x1, y1, 5);
		if (l2) {
			gc.setFill(onColour);
		} else {
			gc.setFill(offColour);
		}
		double[] x2 = {x+width-t,x+width,x+width,x+width-t,x+width-t};
		double[] y2 = {y+t*1.2,y,y+height/2,y+height/2-t*1.2,y+t*1.2};
		gc.fillPolygon(x2, y2, 5);
		if (l3) {
			gc.setFill(onColour);
		} else {
			gc.setFill(offColour);
		}
		double[] x3 = {x+width-t,x+width,x+width,x+width-t,x+width-t};
		double[] y3 = {y+height/2+t*0.3,y+height/2,y+height-t,y+height-t*2.2,y+height/2+t*1.2};
		gc.fillPolygon(x3, y3, 5);
		if (l4) {
			gc.setFill(onColour);
		} else {
			gc.setFill(offColour);
		}
		double[] x4 = {x,x+t*1.2,x+width-t*1.2,x+width,x};
		double[] y4 = {y+height-t,y+height-t*2,y+height-t*2,y+height-t,y+height-t};
		gc.fillPolygon(x4, y4, 5);
		if (l5) {
			gc.setFill(onColour);
		} else {
			gc.setFill(offColour);
		}
		double[] x5 = {x,x+t,x+t,x,x};
		double[] y5 = {y+height/2,y+height/2+t*0.3,y+height-t*2.2,y+height-t,y+height/2};
		gc.fillPolygon(x5, y5, 5);
		if (l6) {
			gc.setFill(onColour);
		} else {
			gc.setFill(offColour);
		}
		double[] x6 = {x,x+t,x+t,x,x};
		double[] y6 = {y,y+t*1.2,y+height/2-t*1.2,y+height/2,y};
		gc.fillPolygon(x6, y6, 5);
		if (l7) {
			gc.setFill(onColour);
		} else {
			gc.setFill(offColour);
		}
		double[] x7 = {x,x+t*1.2,x+width-t*1.2,x+width,0};
		double[] y7 = {y+height/2,y+height/2-t,y+height/2-t,y+height/2,y+height/2};
		gc.fillPolygon(x7, y7, 5);
		
		gc.setFill(onColour);
		if (upHover) {
			gc.setFill(hoverColour);
		}
		if (upPressed) {
			gc.setFill(pressColour);
		}
		double[] x8 = {x,x+width/2,x+width,x};
		double[] y8 = {y-t*0.3,y-t*1.3,y-t*0.3,y-t*0.3};
		gc.fillPolygon(x8, y8, 4);
		gc.setFill(onColour);
		if (downHover) {
			gc.setFill(hoverColour);
		}
		if (downPressed) {
			gc.setFill(pressColour);
		}
		double[] x9 = {x,x+width,x+width/2,x};
		double[] y9 = {y+height-t+t*0.3,y+height-t+t*0.3,y+height-t+t*1.3,y+height-t+t*0.3};
		gc.fillPolygon(x9, y9, 4);
	}
	
	public boolean onUp(double x, double y) {
		double t = 0.125*height;		
		if (x > this.x + width || x < this.x) {
			return false;
		}
		if (y < this.y - t * 1.3 || y > this.y) {
			return false;
		}
		return true;
	}
	
	public boolean onDown(double x, double y) {
		double t = 0.125*height;
		if (x > this.x + width || x < this.x) {
			return false;
		}
		if (y < this.y + height - t || y > this.y + height - t + t * 1.3) {
			return false;
		}
		return true;
	}
}
