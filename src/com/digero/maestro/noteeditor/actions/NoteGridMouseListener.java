package com.digero.maestro.noteeditor.actions;
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
        noteGridPanel.selectNoteAt(e.getPoint());
    }

}
