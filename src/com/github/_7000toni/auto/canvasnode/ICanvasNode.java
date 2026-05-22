package com.github._7000toni.auto.canvasnode;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public interface ICanvasNode {
	public void onMouseDragged(MouseEvent e);
	public void onMouseEntered(MouseEvent e);
	public void onMouseExited(MouseEvent e);
	public void onMousePressed(MouseEvent e);
	public void onMouseClicked(MouseEvent e);
	public void onMouseReleased(MouseEvent e);
	public void onMouseMoved(MouseEvent e);
	public void onScroll(ScrollEvent e);
	
	public void setOnMouseDragged(EventHandler<? super MouseEvent> e);
	public void setOnMouseEntered(EventHandler<? super MouseEvent> e);
	public void setOnMouseExited(EventHandler<? super MouseEvent> e);
	public void setOnMousePressed(EventHandler<? super MouseEvent> e);
	public void setOnMouseClicked(EventHandler<? super MouseEvent> e);
	public void setOnMouseReleased(EventHandler<? super MouseEvent> e);
	public void setOnMouseMoved(EventHandler<? super MouseEvent> e);
	public void setOnScroll(EventHandler<? super ScrollEvent> e);
	
	public boolean onNode(double x, double y);	
	public void draw();
	public GraphicsContext graphicsContext();
	public void setGraphicsContext(GraphicsContext gc);	
	public void setX(double x);
	public void setY(double y);
	public double x();
	public double y();
	public void setWidth(double width);
	public void setHeight(double height);
	public double width();
	public double height();
	public boolean enabled();
	public void enable();
	public void disable();
}
