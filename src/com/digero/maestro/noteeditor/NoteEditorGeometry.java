package com.digero.maestro.noteeditor;

public final class NoteEditorGeometry {

    private NoteEditorGeometry() {
    }

    /**
     * Returns the Y coordinate for the given MIDI note in the note editor.
     * 
     * @param midiNote the MIDI note number (0-127)
     * @return the Y coordinate for the given MIDI note in the note editor
     */
    public static int getYForMidiNote(int midiNote) {
        return (NoteEditorLayout.MIDI_NOTE_COUNT - 1 - midiNote)
                * NoteEditorLayout.NOTE_HEIGHT;
    }

    /**
     * Returns the MIDI note number for the given Y coordinate in the note editor.
     * 
     * @param y the Y coordinate
     * @return the MIDI note number for the given Y coordinate in the note editor
     */
    public static int getMidiNoteForY(int y) {
        return NoteEditorLayout.MIDI_NOTE_COUNT - 1 - (y / NoteEditorLayout.NOTE_HEIGHT);
    }

    /**
     * Returns true if the given MIDI note is a black key, false otherwise.
     * 
     * @param midiNote the MIDI note number (0-127)
     * @return true if the given MIDI note is a black key, false otherwise
     */
    public static boolean isBlackKey(int midiNote) {
        return switch (midiNote % 12) {
            case 1, 3, 6, 8, 10 -> true;
            default -> false;
        };
    }

    /**
     * Returns the X coordinate for the given beat in the note editor.
     * 
     * @param beat the beat number
     * @return the X coordinate for the given beat in the note editor
     */
    public static int getXForBeat(double beat) {
        return (int) Math.round(
                beat * NoteEditorLayout.PIXELS_PER_BEAT);
    }

    /**
     * Returns the beat number for the given X coordinate in the note editor.
     * 
     * @param x the X coordinate
     * @return the beat number for the given X coordinate in the note editor
     */
    public static double getBeatForX(int x) {
        return (double) x / NoteEditorLayout.PIXELS_PER_BEAT;
    }

    /**
     * Returns true if the given beat is the start of a measure, false otherwise.
     * 
     * @param beat the beat number
     * @return true if the given beat is the start of a measure, false otherwise
     */
    public static boolean isMeasureStart(int beat) {
        return beat % NoteEditorLayout.BEATS_PER_MEASURE == 0;
    }

    /**
     * Returns the measure number for the given beat in the note editor.
     * 
     * @param beat the beat number
     * @return the measure number for the given beat in the note editor
     */
    public static int getMeasureForBeat(int beat) {
        return beat / NoteEditorLayout.BEATS_PER_MEASURE + 1;
    }

}
