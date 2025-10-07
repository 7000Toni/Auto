import javafx.scene.canvas.GraphicsContext;

public interface ScrollBarOwner {
	public GraphicsContext graphicsContext();
	public void draw();
}
