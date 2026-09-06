package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.model.EditorNote;

record DragStartState(int midiNote, double startBeat, double durationBeats) {
    DragStartState(EditorNote note) {
        this(note.getMidiNote(), note.getStartBeat(), note.getDurationBeats());
    }
}
