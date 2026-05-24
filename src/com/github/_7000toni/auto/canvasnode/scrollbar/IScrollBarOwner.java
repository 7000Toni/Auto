package com.github._7000toni.auto.canvasnode.scrollbar;
import javafx.scene.canvas.GraphicsContext;

public interface IScrollBarOwner {
	public GraphicsContext graphicsContext();
	public void draw();
}
