package com.digero.maestro.noteeditor.components;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.NoteEditorGeometry;

public class NoteGridPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public NoteGridPanel() {
        setPreferredSize(
                new Dimension(
                        NoteEditorLayout.EDITOR_WIDTH,
                        NoteEditorLayout.EDITOR_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        paintPitchRows(g);
        paintTimeGrid(g);
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
        for (int beat = 0; NoteEditorGeometry.getXForBeat(beat) < getWidth(); beat++) {
            int x = NoteEditorGeometry.getXForBeat(beat);

            if (NoteEditorGeometry.isMeasureStart(beat)) {
                g.setColor(NoteEditorLayout.MEASURE_LINE_COLOR);
            } else {
                g.setColor(NoteEditorLayout.BEAT_LINE_COLOR);
            }

            g.drawLine(x, 0, x, getHeight());
        }
    }
}
