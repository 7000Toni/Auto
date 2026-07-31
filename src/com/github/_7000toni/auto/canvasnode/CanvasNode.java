package com.github._7000toni.auto.canvasnode;

import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public abstract class CanvasNode implements ICanvasNode {
	protected GraphicsContext gc;
	protected double width;
	protected double height;
	protected double x;
	protected double y;
	protected boolean enabled = true;
	protected boolean pressed = false;
	protected boolean hover = false;
	protected boolean focused = false;
	
	protected EventHandler<? super MouseEvent> onMouseDragged;
	protected EventHandler<? super MouseEvent> onMouseEntered;
	protected EventHandler<? super MouseEvent> onMouseExited;
	protected EventHandler<? super MouseEvent> onMousePressed;
	protected EventHandler<? super MouseEvent> onMouseClicked;
	protected EventHandler<? super MouseEvent> onMouseReleased;
	protected EventHandler<? super MouseEvent> onMouseMoved;
	protected EventHandler<? super ScrollEvent> onScroll;
	protected EventHandler<? super KeyEvent> onKeyPressed;
	protected EventHandler<? super KeyEvent> onKeyReleased;
	protected EventHandler<? super KeyEvent> onKeyTyped;
	
	@Override
	public void setHover(boolean hover) {
		this.hover = hover;		
		if (!enabled) {
			this.hover = false;
		}
	}
	
	@Override
	public void setPressed(boolean pressed) {		
		this.pressed = pressed;
		if (!enabled) {
			this.pressed = false;
		}
	}
	
	@Override
	public boolean hover() {
		return hover;
	}
	
	@Override
	public boolean pressed() {
		return pressed;
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
	public double x() {
		return x;
	}
	
	@Override
	public double y() {
		return y;
	}
	
	@Override
	public void setX(double x) {
		this.x = x;
	}
	
	@Override
	public void setY(double y) {
		this.y = y;
	}
	
	@Override
	public double width() {
		return this.width;
	}
	
	@Override
	public double height() {
		return this.height;
	}
	
	@Override
	public void setWidth(double width) {
		this.width = width;
	}
	
	@Override
	public void setHeight(double height) {
		this.height = height;
	}

	@Override
	public void onMouseDragged(MouseEvent e) {
		if (onMouseDragged == null || !enabled) {
			return;
		}
		onMouseDragged.handle(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onMouseEntered == null || !enabled) {
			return;
		}
		onMouseEntered.handle(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		setPressed(false);
		setHover(false);
		if (onMouseExited == null || !enabled) {
			return;
		}
		onMouseExited.handle(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		setPressed(true);
		if (onMousePressed == null || !enabled) {
			return;
		}
		onMousePressed.handle(e);
	}
	
	@Override
	public void onMouseClicked(MouseEvent e) {	
		if (onMouseClicked == null || !enabled || !pressed || e.getButton() != MouseButton.PRIMARY) {
			setPressed(false);
			return;
		}
		setPressed(false);
		onMouseClicked.handle(e);
	}
	
	@Override
	public void onMouseReleased(MouseEvent e) {		
		if (onMouseReleased == null || !enabled) {
			return;
		}
		onMouseReleased.handle(e);		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {		
		NodeChecks.mouseNodeHoverCheck(this, e.getX(), e.getY());
		if (onMouseMoved == null || !enabled) {
			return;
		}
		onMouseMoved.handle(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		if (onScroll == null || !enabled) {
			return;
		}
		onScroll.handle(e);
	}

	@Override
	public void onKeyPressed(KeyEvent e) {
		if (onKeyPressed == null || !enabled) {
			return;
		}
		onKeyPressed.handle(e);
	}
	
	@Override
	public void onKeyReleased(KeyEvent e) {
		if (onKeyReleased == null || !enabled) {
			return;
		}
		onKeyReleased.handle(e);
	}

	@Override
	public void onKeyTyped(KeyEvent e) {
		if (onKeyTyped == null || !enabled) {
			return;
		}
		onKeyTyped.handle(e);
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
	public void setOnKeyPressed(EventHandler<? super KeyEvent> e) {
		onKeyPressed = e;
	}
	
	@Override
	public void setOnKeyReleased(EventHandler<? super KeyEvent> e) {
		onKeyReleased = e;
	}

	@Override
	public void setOnKeyTyped(EventHandler<? super KeyEvent> e) {
		onKeyTyped = e;
	}

	@Override
	public boolean onNode(double x, double y) {
		if (x > this.x + width || x < this.x) {
			return false;
		}
		if (y > this.y + height || y < this.y) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean enabled() {
		return enabled;
	}
	
	@Override
	public void enable() {
		enabled = true;
	}
	
	@Override
	public void disable() {
		enabled = false;
	}
	
	@Override
	public boolean focused() {
		return focused;
	}
	
	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}
}
