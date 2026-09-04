package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.NoteEditorGeometry;
import com.digero.maestro.noteeditor.NoteEditorLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class PianoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color WHITE_KEY_COLOR = Color.WHITE;
    private static final Color BLACK_KEY_COLOR = Color.DARK_GRAY;
    private static final Color KEY_BORDER_COLOR = Color.GRAY;

    private static final double BLACK_KEY_WIDTH_RATIO = 0.65;

    // private static final int MIDI_NOTE_COUNT = 128;

    public PianoPanel() {
        setPreferredSize(new Dimension(NoteEditorLayout.PIANO_WIDTH, NoteEditorLayout.EDITOR_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(WHITE_KEY_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        paintWhiteKeyBoundaries(g);
        paintBlackKeys(g);
    }

    private void paintWhiteKeyBoundaries(Graphics g) {
        g.setColor(KEY_BORDER_COLOR);
        int blackKeyWidth = (int) (getWidth() * BLACK_KEY_WIDTH_RATIO);

        for (int midiNote = 0; midiNote < NoteEditorLayout.MIDI_NOTE_COUNT; midiNote++) {
            int y = NoteEditorGeometry.getYForMidiNote(midiNote);

            if (NoteEditorGeometry.isBlackKey(midiNote)) {
                // the boundaries between the surrounding white keys
                // passes through the center of the black key row
                int boundaryY = y + NoteEditorLayout.NOTE_HEIGHT / 2;
                g.drawLine(blackKeyWidth, boundaryY, getWidth() - 1, boundaryY);
            } else if (
                midiNote < NoteEditorLayout.MIDI_NOTE_COUNT - 1 && !NoteEditorGeometry.isBlackKey(midiNote + 1)
            ) {
                // E-F and B-C white key boundaries have no black key between them
                g.drawLine(0, y, getWidth() - 1, y);
            }
        }

        // Right edge of keyboard
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);

        // Bottom edge
        g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
    }

    private void paintBlackKeys(Graphics g) {
        int blackKeyWidth = (int) (getWidth() * BLACK_KEY_WIDTH_RATIO);
        for (int midiNote = 0; midiNote < NoteEditorLayout.MIDI_NOTE_COUNT; midiNote++) {
            if (NoteEditorGeometry.isBlackKey(midiNote)) {
                int y = NoteEditorGeometry.getYForMidiNote(midiNote);

                g.setColor(BLACK_KEY_COLOR);
                g.fillRect(0, y, blackKeyWidth, NoteEditorLayout.NOTE_HEIGHT);

                g.setColor(KEY_BORDER_COLOR);

                g.drawRect(0, y, blackKeyWidth - 1, NoteEditorLayout.NOTE_HEIGHT - 1);
            }
        }
    }
}
