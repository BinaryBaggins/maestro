package com.digero.maestro.noteeditor;

public class NoteEditorViewState {

    public static final int DEFAULT_PIXELS_PER_BEAT = 80;
    private int pixelsPerBeat = DEFAULT_PIXELS_PER_BEAT;

    public int getPixelsPerBeat() {
        return pixelsPerBeat;
    }

    public void setPixelsPerBeat(int pixelsPerBeat) {
        this.pixelsPerBeat = pixelsPerBeat;
    }

    public int getZoomPercentage() {
        return (int) ((pixelsPerBeat / (double) DEFAULT_PIXELS_PER_BEAT) * 100);
    }

    public void setZoomPercentage(int zoomPercentage) {
        this.pixelsPerBeat = (int) ((zoomPercentage / 100.0) * DEFAULT_PIXELS_PER_BEAT);
    }

    public int getEditorWidth() {
        return (NoteEditorLayout.EDITOR_WIDTH * pixelsPerBeat) / DEFAULT_PIXELS_PER_BEAT;
    }
}
