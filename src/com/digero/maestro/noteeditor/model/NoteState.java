package com.digero.maestro.noteeditor.model;

public record NoteState(int midiNote, double startBeat, double durationBeats) {
    public NoteState(EditorNote note) {
        this(note.getMidiNote(), note.getStartBeat(), note.getDurationBeats());
    }
}
