package com.digero.maestro.noteeditor.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class NoteSelectionModel {

    private final Set<EditorNote> selectedNotes = new LinkedHashSet<>();

    public void setSelection(EditorNote note) {
        selectedNotes.clear();
        selectedNotes.add(Objects.requireNonNull(note));
    }

    public void setSelection(Collection<EditorNote> notes) {
        selectedNotes.clear();
        selectedNotes.addAll(Objects.requireNonNull(notes));
    }

    public SelectionState toggle(EditorNote note) {
        Objects.requireNonNull(note);

        if (selectedNotes.remove(note)) {
            return SelectionState.DESELECTED;
        }

        selectedNotes.add(note);
        return SelectionState.SELECTED;
    }

    public void addToSelection(Collection<EditorNote> notes) {
        selectedNotes.addAll(Objects.requireNonNull(notes));
    }

    public void clearSelection() {
        selectedNotes.clear();
    }

    public boolean isSelected(EditorNote note) {
        return selectedNotes.contains(note);
    }

    public Set<EditorNote> getSelectedNotes() {
        return Collections.unmodifiableSet(selectedNotes);
    }
}
