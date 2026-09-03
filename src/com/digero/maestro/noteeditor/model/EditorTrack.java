package com.digero.maestro.noteeditor.model;

import java.util.ArrayList;
import java.util.List;
public class EditorTrack {

    private final String name;
    private final List<EditorNote> notes;

    public EditorTrack(String name) {
        this.name = name;
        this.notes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<EditorNote> getNotes() {
        return notes;
    }

    public void addNote(EditorNote note) {
        notes.add(note);
    }


}
