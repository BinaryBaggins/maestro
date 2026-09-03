package com.digero.maestro.noteeditor.components;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;

import com.digero.maestro.noteeditor.NoteEditorGeometry;
import com.digero.maestro.noteeditor.NoteEditorLayout;

public class TimelinePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public TimelinePanel() {
        setPreferredSize(
                new Dimension(
                        NoteEditorLayout.EDITOR_WIDTH,
                        NoteEditorLayout.TIMELINE_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int beat = 0; NoteEditorGeometry.getXForBeat(beat) < getWidth(); beat++) {

            int x = NoteEditorGeometry.getXForBeat(beat);

            if (NoteEditorGeometry.isMeasureStart(beat)) {
                g.setColor(NoteEditorLayout.MEASURE_LINE_COLOR);

                int measureNumber = NoteEditorGeometry.getMeasureForBeat(beat);

                g.drawString(
                        String.valueOf(measureNumber),
                        x + 4,
                        14);
            } else {
                g.setColor(NoteEditorLayout.BEAT_LINE_COLOR);
            }

            g.drawLine(x, 0, x, getHeight());
        }
    }
}
