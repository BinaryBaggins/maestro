package com.digero.maestro.noteeditor.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

class SelectionBoxTest {

    @Test
    void createsZeroSizedBoxAtStartPoint() {
        SelectionBox selectionBox = new SelectionBox(new Point(10, 20));

        assertEquals(new Rectangle(10, 20, 0, 0), selectionBox.getBounds());
    }

    @Test
    void updateCreatesBoxToBottomRight() {
        SelectionBox selectionBox = new SelectionBox(new Point(10, 20));

        selectionBox.update(new Point(30, 50));

        assertEquals(new Rectangle(10, 20, 20, 30), selectionBox.getBounds());
    }

    @Test
    void updateCreatesBoxToTopLeft() {
        SelectionBox selectionBox = new SelectionBox(new Point(30, 50));

        selectionBox.update(new Point(10, 20));

        assertEquals(new Rectangle(10, 20, 20, 30), selectionBox.getBounds());
    }

    @Test
    void updateCreatesBoxToTopRight() {
        SelectionBox selectionBox = new SelectionBox(new Point(10, 50));

        selectionBox.update(new Point(30, 20));

        assertEquals(new Rectangle(10, 20, 20, 30), selectionBox.getBounds());
    }

    @Test
    void updateCreatesBoxToBottomLeft() {
        SelectionBox selectionBox = new SelectionBox(new Point(30, 20));

        selectionBox.update(new Point(10, 50));

        assertEquals(new Rectangle(10, 20, 20, 30), selectionBox.getBounds());
    }

    @Test
    void getBoundsReturnsDefensiveCopy() {
        SelectionBox selectionBox = new SelectionBox(new Point(10, 20));
        Rectangle returnedBounds = selectionBox.getBounds();

        returnedBounds.setBounds(100, 200, 300, 400);

        assertEquals(new Rectangle(10, 20, 0, 0), selectionBox.getBounds());
    }
}
