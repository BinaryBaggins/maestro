package com.digero.maestro.noteeditor.actions;

import com.digero.maestro.noteeditor.components.NoteGridPanel;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NoteGridMouseListener extends MouseAdapter {

    private static final int DRAG_THRESHOLD = 4;

    private final NoteGridPanel noteGridPanel;

    private Point pressPoint;
    private boolean pressedOnNote;
    private boolean dragging;
    private boolean ctrlDown;

    public NoteGridMouseListener(NoteGridPanel noteGridPanel) {
        this.noteGridPanel = noteGridPanel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressPoint = e.getPoint();
        pressedOnNote = noteGridPanel.hasNoteAt(pressPoint);
        ctrlDown = e.isControlDown();
        dragging = false;

        noteGridPanel.requestFocusInWindow();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressPoint == null) {
            return;
        }

        if (!dragging) {
            int dx = e.getX() - pressPoint.x;
            int dy = e.getY() - pressPoint.y;

            // multiply by itself to avoid the square root operation
            if (dx * dx + dy * dy < DRAG_THRESHOLD * DRAG_THRESHOLD) {
                return;
            }

            dragging = true;

            if (pressedOnNote) {
                noteGridPanel.beginNoteDrag(pressPoint);
            } else {
                noteGridPanel.beginSelectionBox(pressPoint, ctrlDown);
            }
        }

        if (pressedOnNote) {
            noteGridPanel.dragSelectedNoteTo(e.getPoint());
        } else {
            noteGridPanel.updateSelectionBox(e.getPoint());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (pressPoint == null) {
            return;
        }

        if (dragging) {
            if (pressedOnNote) {
                noteGridPanel.endNoteDrag();
            } else {
                noteGridPanel.endSelectionBox();
            }
        } else {
            noteGridPanel.handleSelectionClick(e.getPoint(), ctrlDown);
        }
        pressPoint = null;
        dragging = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            noteGridPanel.createNoteAt(e.getPoint());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        noteGridPanel.updateMouseCursor(e.getPoint());
    }

    @Override
    public void mouseExited(MouseEvent e) {
        noteGridPanel.setCursor(Cursor.getDefaultCursor());
    }
}
