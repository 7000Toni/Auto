import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
	public void draw();
	public void setGraphicsContext(GraphicsContext gc);
	public GraphicsContext graphicsContext();
	public void setX(double x);
	public void setY(double y);
	public double x();
	public double y();
}
