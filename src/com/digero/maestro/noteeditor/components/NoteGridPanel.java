package com.digero.maestro.noteeditor.components;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.awt.Color;

import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.NoteEditorViewState;
import com.digero.maestro.noteeditor.actions.NoteEditorKeyBindings;
import com.digero.maestro.noteeditor.actions.NoteGridMouseListener;
import com.digero.maestro.noteeditor.NoteEditorGeometry;
import com.digero.maestro.noteeditor.model.EditorNote;

public class NoteGridPanel extends JPanel {
    private final List<EditorNote> notes;
    private EditorNote selectedNote;

    private static final long serialVersionUID = 1L;
    private final NoteEditorViewState viewState;

    public NoteGridPanel(NoteEditorViewState viewSettings, List<EditorNote> notes) {
        this.viewState = viewSettings;
        this.notes = notes;

        updatePreferredSize();
        addMouseListener(new NoteGridMouseListener(this));
        NoteEditorKeyBindings.install(this);
    }

    public void updateZoom() {
        updatePreferredSize();
        revalidate();
        repaint();
    }

    private void updatePreferredSize() {
        setPreferredSize(
                new Dimension(
                        viewState.getEditorWidth(),
                        NoteEditorLayout.EDITOR_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        paintPitchRows(g);
        paintTimeGrid(g);
        paintNotes(g);
    }

    private void paintPitchRows(Graphics g) {
        for (int midiNote = 0; midiNote < NoteEditorLayout.MIDI_NOTE_COUNT; midiNote++) {

            int y = NoteEditorGeometry.getYForMidiNote(midiNote);

            if (NoteEditorGeometry.isBlackKey(midiNote)) {
                g.setColor(NoteEditorLayout.BLACK_NOTE_ROW_COLOR);
            } else {
                g.setColor(Color.WHITE);
            }

            g.fillRect(
                    0,
                    y,
                    getWidth(),
                    NoteEditorLayout.NOTE_HEIGHT);

            g.setColor(NoteEditorLayout.PITCH_LINE_COLOR);
            g.drawLine(
                    0,
                    y,
                    getWidth(),
                    y);
        }
    }

    private void paintTimeGrid(Graphics g) {
        for (int beat = 0; NoteEditorGeometry.getXForBeat(beat, viewState.getPixelsPerBeat()) < getWidth(); beat++) {
            int x = NoteEditorGeometry.getXForBeat(beat, viewState.getPixelsPerBeat());

            if (NoteEditorGeometry.isMeasureStart(beat)) {
                g.setColor(NoteEditorLayout.MEASURE_LINE_COLOR);
            } else {
                g.setColor(NoteEditorLayout.BEAT_LINE_COLOR);
            }

            g.drawLine(x, 0, x, getHeight());
        }
    }

    private void paintNotes(Graphics g) {
        for (EditorNote note : notes) {
            paintNote(g, note);
        }
    }

    private void paintNote(Graphics g, EditorNote note) {
        int x = NoteEditorGeometry.getXForBeat(
                note.getStartBeat(),
                viewState.getPixelsPerBeat());

        int endX = NoteEditorGeometry.getXForBeat(
                note.getStartBeat() + note.getDurationBeats(),
                viewState.getPixelsPerBeat());

        int rowY = NoteEditorGeometry.getYForMidiNote(
                note.getMidiNote());

        int noteHeight = NoteEditorLayout.NOTE_HEIGHT - 1;
        int noteY = rowY + 1;

        g.setColor(NoteEditorLayout.NOTE_COLOR);
        g.fillRect(
                x,
                noteY,
                endX - x,
                noteHeight);

        if (note == selectedNote) {
            int width = endX - x;

            g.setColor(NoteEditorLayout.SELECTED_NOTE_BORDER_COLOR);
            g.drawRect(
                    x,
                    noteY,
                    endX - x - 1,
                    noteHeight - 1);

            if (width > 3 && noteHeight > 3) {
                g.drawRect(
                        x + 1,
                        noteY + 1,
                        width - 3,
                        noteHeight - 3);
            }
        }
    }

    public void selectNoteAt(Point point) {
        requestFocusInWindow();
        selectedNote = findNoteAt(point);
        repaint();
    }

    public void deleteSelectedNote() {
        if (selectedNote != null) {
            notes.remove(selectedNote);
            selectedNote = null;
            repaint();
        }
    }

    private EditorNote findNoteAt(Point point) {
        for (EditorNote note : notes) {
            int x = NoteEditorGeometry.getXForBeat(
                    note.getStartBeat(),
                    viewState.getPixelsPerBeat());

            int endX = NoteEditorGeometry.getXForBeat(
                    note.getStartBeat() + note.getDurationBeats(),
                    viewState.getPixelsPerBeat());

            int rowY = NoteEditorGeometry.getYForMidiNote(
                    note.getMidiNote());

            int noteHeight = NoteEditorLayout.NOTE_HEIGHT - 1;
            int noteY = rowY + 1;

            Rectangle bounds = new Rectangle(
                    x,
                    noteY,
                    endX - x,
                    noteHeight);

            if (bounds.contains(point)) {
                return note;
            }
        }

        return null;
    }
}
