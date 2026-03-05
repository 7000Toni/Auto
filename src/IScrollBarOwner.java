import javafx.scene.canvas.GraphicsContext;

public interface IScrollBarOwner {
	public GraphicsContext graphicsContext();
	public void draw();
}
