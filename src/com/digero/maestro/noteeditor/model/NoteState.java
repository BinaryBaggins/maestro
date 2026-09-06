package com.digero.maestro.noteeditor.model;

record NoteState(int midiNote, double startBeat, double durationBeats) {
    NoteState(EditorNote note) {
        this(note.getMidiNote(), note.getStartBeat(), note.getDurationBeats());
    }
}
