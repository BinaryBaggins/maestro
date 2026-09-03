package com.digero.maestro.noteeditor.actions;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.digero.maestro.noteeditor.components.NoteGridPanel;

public class NoteGridMouseListener extends MouseAdapter {
    private final NoteGridPanel noteGridPanel;

    public NoteGridMouseListener(NoteGridPanel noteGridPanel) {
        this.noteGridPanel = noteGridPanel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        noteGridPanel.beginNoteDrag(e.getPoint());
    }


     @Override
    public void mouseDragged(MouseEvent e) {
        noteGridPanel.dragSelectedNoteTo(e.getPoint());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        noteGridPanel.endNoteDrag();
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
