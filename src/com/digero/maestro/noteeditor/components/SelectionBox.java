package com.digero.maestro.noteeditor.components;

import java.awt.Point;
import java.awt.Rectangle;

public final class SelectionBox {

    private final Point start;
    private Rectangle bounds;

    public SelectionBox(Point start) {
        this.start = new Point(start);
        this.bounds = new Rectangle(start);
    }

    public void update(Point current) {
        int x = Math.min(start.x, current.x);
        int y = Math.min(start.y, current.y);

        int width = Math.abs(current.x - start.x);
        int height = Math.abs(current.y - start.y);

        bounds = new Rectangle(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }
}
