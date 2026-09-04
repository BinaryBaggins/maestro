package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.NoteEditorGeometry;
import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.NoteEditorViewState;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

public class TimelinePanel extends JPanel implements Scrollable {

    private static final long serialVersionUID = 1L;

    private final NoteEditorViewState viewState;

    public TimelinePanel(NoteEditorViewState viewSettings) {
        this.viewState = viewSettings;
        updatePreferredSize();
    }

    public void updateZoom() {
        updatePreferredSize();
        revalidate();
        repaint();
    }

    private void updatePreferredSize() {
        setPreferredSize(new Dimension(viewState.getEditorWidth(), NoteEditorLayout.TIMELINE_HEIGHT));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int beat = 0; NoteEditorGeometry.getXForBeat(beat, viewState.getPixelsPerBeat()) < getWidth(); beat++) {
            int x = NoteEditorGeometry.getXForBeat(beat, viewState.getPixelsPerBeat());

            if (NoteEditorGeometry.isMeasureStart(beat)) {
                g.setColor(NoteEditorLayout.MEASURE_LINE_COLOR);

                int measureNumber = NoteEditorGeometry.getMeasureForBeat(beat);

                g.drawString(String.valueOf(measureNumber), x + 4, 14);
            } else {
                g.setColor(NoteEditorLayout.BEAT_LINE_COLOR);
            }

            g.drawLine(x, 0, x, getHeight());
        }
    }
}
