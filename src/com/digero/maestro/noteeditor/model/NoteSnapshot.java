package com.digero.maestro.noteeditor.model;

public record NoteSnapshot(int midiNote, double startBeat, double durationBeats) {
    public NoteSnapshot(EditorNote note) {
        this(note.getMidiNote(), note.getStartBeat(), note.getDurationBeats());
    }
}