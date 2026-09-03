package com.digero.maestro.noteeditor;

import java.awt.Color;

public final class NoteEditorLayout {

    public static final int MIDI_NOTE_COUNT = 128;

    public static final int NOTE_HEIGHT = 16;
    public static final int PIANO_WIDTH = 80;

    public static final int EDITOR_HEIGHT = MIDI_NOTE_COUNT * NOTE_HEIGHT;
    public static final int EDITOR_WIDTH = 3000;

    public static final int TIMELINE_HEIGHT = 32;

    public static final int BEATS_PER_MEASURE = 4;

    public static final Color BLACK_NOTE_ROW_COLOR = new Color(235, 235, 235);

    public static final Color PITCH_LINE_COLOR = new Color(210, 210, 210);

    public static final Color MEASURE_LINE_COLOR = new Color(160, 160, 160);

    public static final Color BEAT_LINE_COLOR = new Color(215, 215, 215);

    public static final Color NOTE_COLOR = new Color(100, 140, 190);

    public static final Color SELECTED_NOTE_BORDER_COLOR = new Color(60,90,130);

    private NoteEditorLayout() {
        // Prevent instantiation
    }

}
