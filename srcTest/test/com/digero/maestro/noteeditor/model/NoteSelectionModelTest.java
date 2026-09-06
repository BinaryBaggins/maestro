package com.digero.maestro.noteeditor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class NoteSelectionModelTest {

    private final NoteSelectionModel model = new NoteSelectionModel();

    private final EditorNote firstNote = new EditorNote(60, 0.0, 1.0);

    private final EditorNote secondNote = new EditorNote(62, 1.0, 1.0);

    private final EditorNote thirdNote = new EditorNote(64, 2.0, 1.0);

    @Test
    void setSelectionSelectsSingleNote() {
        model.setSelection(firstNote);

        assertEquals(List.of(firstNote), List.copyOf(model.getSelectedNotes()));
    }

    @Test
    void setSelectionReplacesExistingSelection() {
        model.setSelection(firstNote);

        model.setSelection(secondNote);

        assertEquals(List.of(secondNote), List.copyOf(model.getSelectedNotes()));
    }

    @Test
    void setSelectionWithCollectionReplacesExistingSelection() {
        model.setSelection(firstNote);

        model.setSelection(List.of(secondNote, thirdNote));

        assertEquals(List.of(secondNote, thirdNote), List.copyOf(model.getSelectedNotes()));
    }

    @Test
    void toggleSelectsUnselectedNote() {
        assertEquals(SelectionState.SELECTED, model.toggle(firstNote));

        assertTrue(model.isSelected(firstNote));
    }

    @Test
    void toggleDeselectsSelectedNote() {
        model.setSelection(firstNote);

        assertEquals(SelectionState.DESELECTED, model.toggle(firstNote));

        assertFalse(model.isSelected(firstNote));
    }

    @Test
    void addToSelectionPreservesExistingSelection() {
        model.setSelection(firstNote);

        model.addToSelection(List.of(secondNote, thirdNote));

        assertEquals(List.of(firstNote, secondNote, thirdNote), List.copyOf(model.getSelectedNotes()));
    }

    @Test
    void clearSelectionRemovesAllSelectedNotes() {
        model.setSelection(List.of(firstNote, secondNote));

        model.clearSelection();

        assertTrue(model.getSelectedNotes().isEmpty());
    }

    @Test
    void isSelectedReflectsCurrentSelection() {
        model.setSelection(firstNote);

        assertTrue(model.isSelected(firstNote));
        assertFalse(model.isSelected(secondNote));
    }

    @Test
    void getSelectedNotesCannotBeModified() {
        model.setSelection(firstNote);

        assertThrows(UnsupportedOperationException.class, () -> model.getSelectedNotes().add(secondNote));
        assertTrue(model.isSelected(firstNote));
        assertFalse(model.isSelected(secondNote));
    }
}
