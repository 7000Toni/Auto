package com.github._7000toni.auto.canvasnode;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public abstract class CanvasNode implements ICanvasNode {
	protected GraphicsContext gc;
	protected BooleanProperty skipDraw = new SimpleBooleanProperty();
	protected DoubleProperty width = new SimpleDoubleProperty();
	protected DoubleProperty height = new SimpleDoubleProperty();
	protected DoubleProperty x = new SimpleDoubleProperty();
	protected DoubleProperty y = new SimpleDoubleProperty();
	protected BooleanProperty enabled = new SimpleBooleanProperty(true);
	protected BooleanProperty pressed = new SimpleBooleanProperty(false);
	protected BooleanProperty hover = new SimpleBooleanProperty(false);
	protected BooleanProperty focused = new SimpleBooleanProperty(false);
	
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
		this.hover.set(hover);		
		if (!enabled.get()) {
			this.hover.set(false);
		}
	}
	
	@Override
	public void setPressed(boolean pressed) {		
		this.pressed.set(pressed);
		if (!enabled.get()) {
			this.pressed.set(false);
		}
	}
	
	@Override
	public boolean hover() {
		return hover.get();
	}
	
	@Override
	public BooleanProperty hoverProperty() {
		return hover;
	}
	
	@Override
	public boolean pressed() {
		return pressed.get();
	}
	
	@Override
	public BooleanProperty pressedProperty() {
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
		return x.get();
	}
	
	@Override
	public DoubleProperty xProperty() {
		return x;
	}
	
	@Override
	public double y() {
		return y.get();
	}
	
	@Override
	public DoubleProperty yProperty() {
		return y;
	}
	
	@Override
	public void setX(double x) {
		this.x.set(x);
	}
	
	@Override
	public void setY(double y) {
		this.y.set(y);
	}
	
	@Override
	public double width() {
		return width.get();
	}
	
	@Override
	public DoubleProperty widthProperty() {
		return width;
	}
	
	@Override
	public double height() {
		return height.get();
	}
	
	@Override
	public DoubleProperty heightProperty() {
		return height;
	}
	
	@Override
	public void setWidth(double width) {
		this.width.set(width);
	}
	
	@Override
	public void setHeight(double height) {
		this.height.set(height);
	}
	
	@Override
	public void onMouseDragged(MouseEvent e) {
		if (onMouseDragged == null || !enabled.get()) {
			return;
		}
		onMouseDragged.handle(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		if (onMouseEntered == null || !enabled.get()) {
			return;
		}
		onMouseEntered.handle(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		setPressed(false);
		setHover(false);
		if (onMouseExited == null || !enabled.get()) {
			return;
		}
		onMouseExited.handle(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		setPressed(true);
		if (onMousePressed == null || !enabled.get()) {
			return;
		}
		onMousePressed.handle(e);
	}
	
	@Override
	public void onMouseClicked(MouseEvent e) {	
		if (onMouseClicked == null || !enabled.get() || !pressed.get() || e.getButton() != MouseButton.PRIMARY) {
			setPressed(false);
			return;
		}
		setPressed(false);
		onMouseClicked.handle(e);
	}
	
	@Override
	public void onMouseReleased(MouseEvent e) {		
		if (onMouseReleased == null || !enabled.get()) {
			return;
		}
		onMouseReleased.handle(e);		
	}

	@Override
	public void onMouseMoved(MouseEvent e) {		
		NodeChecks.mouseNodeHoverCheck(this, e.getX(), e.getY());
		if (onMouseMoved == null || !enabled.get()) {
			return;
		}
		onMouseMoved.handle(e);
	}

	@Override
	public void onScroll(ScrollEvent e) {
		if (onScroll == null || !enabled.get()) {
			return;
		}
		onScroll.handle(e);
	}

	@Override
	public void onKeyPressed(KeyEvent e) {
		if (onKeyPressed == null || !enabled.get()) {
			return;
		}
		onKeyPressed.handle(e);
	}
	
	@Override
	public void onKeyReleased(KeyEvent e) {
		if (onKeyReleased == null || !enabled.get()) {
			return;
		}
		onKeyReleased.handle(e);
	}

	@Override
	public void onKeyTyped(KeyEvent e) {
		if (onKeyTyped == null || !enabled.get()) {
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
		if (x > this.x.get() + width.get() || x < this.x.get()) {
			return false;
		}
		if (y > this.y.get() + height.get() || y < this.y.get()) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean enabled() {
		return enabled.get();
	}
	
	@Override
	public BooleanProperty enabledProperty() {
		return enabled;
	}
	
	@Override
	public void enable() {
		enabled.set(true);
	}
	
	@Override
	public void disable() {
		enabled.set(false);
	}
	
	@Override
	public boolean focused() {
		return focused.get();
	}
	
	@Override
	public BooleanProperty focusedProperty() {
		return focused;
	}
	
	@Override
	public void setFocused(boolean focused) {
		this.focused.set(focused);
	}
}
