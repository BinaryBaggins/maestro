package com.digero.maestro.noteeditor.model;

import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.undo.UndoHistory;
import com.digero.maestro.noteeditor.undo.UndoableAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class NoteEditorModel {

    private final UndoHistory undoHistory;
    private final List<EditorNote> notes;

    public NoteEditorModel(List<EditorNote> notes) {
        this.notes = new ArrayList<>(Objects.requireNonNull(notes));
        this.undoHistory = new UndoHistory();
    }

    public List<EditorNote> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public Optional<EditorNote> createNote(int midiNote, double startBeat, double durationBeats) {
        midiNote = clampMidiNote(midiNote);
        startBeat = Math.max(0, startBeat);

        if (durationBeats < NoteEditorLayout.SNAP_BEATS) {
            return Optional.empty();
        }

        if (!canPlaceNote(null, midiNote, startBeat, durationBeats)) {
            return Optional.empty();
        }

        EditorNote note = new EditorNote(midiNote, startBeat, durationBeats);

        int insertionIndex = notes.size();
        notes.add(note);
        undoHistory.record(new CreateNoteAction(note, insertionIndex));

        return Optional.of(note);
    }

    public boolean deleteNote(EditorNote note) {
        int originalIndex = notes.indexOf(note);

        if (originalIndex < 0) {
            return false;
        }

        notes.remove(originalIndex);

        undoHistory.record(new DeleteNoteAction(note, originalIndex));

        return true;
    }

    public boolean moveNote(EditorNote note, int midiNote, double startBeat) {
        if (!notes.contains(note)) {
            return false;
        }

        midiNote = clampMidiNote(midiNote);
        startBeat = Math.max(0, startBeat);

        if (!canPlaceNote(note, midiNote, startBeat, note.getDurationBeats())) {
            return false;
        }

        note.setStartBeat(startBeat);
        note.setMidiNote(midiNote);

        return true;
    }

    public boolean resizeNoteLeft(EditorNote note, double newStartBeat) {
        if (!notes.contains(note)) {
            return false;
        }

        double fixedEndBeat = note.getStartBeat() + note.getDurationBeats();

        double maximumStartBeat = fixedEndBeat - NoteEditorLayout.SNAP_BEATS;

        newStartBeat = Math.max(0, Math.min(newStartBeat, maximumStartBeat));

        double newDuration = fixedEndBeat - newStartBeat;

        if (!canPlaceNote(note, note.getMidiNote(), newStartBeat, newDuration)) {
            return false;
        }

        note.setStartBeat(newStartBeat);
        note.setDurationBeats(newDuration);

        return true;
    }

    public boolean resizeNoteRight(EditorNote note, double newEndBeat) {
        if (!notes.contains(note)) {
            return false;
        }

        double startBeat = note.getStartBeat();

        double minimumEndBeat = startBeat + NoteEditorLayout.SNAP_BEATS;

        newEndBeat = Math.max(newEndBeat, minimumEndBeat);

        double newDuration = newEndBeat - startBeat;

        if (!canPlaceNote(note, note.getMidiNote(), startBeat, newDuration)) {
            return false;
        }

        note.setDurationBeats(newDuration);

        return true;
    }

    public boolean canPlaceNote(EditorNote editedNote, int midiNote, double startBeat, double durationBeats) {
        double endBeat = startBeat + durationBeats;

        for (EditorNote note : notes) {
            if (note == editedNote) {
                continue;
            }

            if (note.getMidiNote() != midiNote) {
                continue;
            }

            double otherStart = note.getStartBeat();

            double otherEnd = otherStart + note.getDurationBeats();

            if (startBeat < otherEnd && endBeat > otherStart) {
                return false;
            }
        }

        return true;
    }

    public boolean canUndo() {
        return undoHistory.canUndo();
    }

    public boolean canRedo() {
        return undoHistory.canRedo();
    }

    public boolean undo() {
        return undoHistory.undo();
    }

    public boolean redo() {
        return undoHistory.redo();
    }

    private int clampMidiNote(int midiNote) {
        return Math.max(0, Math.min(midiNote, NoteEditorLayout.MIDI_NOTE_COUNT - 1));
    }

    private final class CreateNoteAction implements UndoableAction {

        private final EditorNote note;
        private final int insertionIndex;

        private CreateNoteAction(EditorNote note, int insertionIndex) {
            this.note = note;
            this.insertionIndex = insertionIndex;
        }

        @Override
        public void undo() {
            notes.remove(note);
        }

        @Override
        public void redo() {
            notes.add(insertionIndex, note);
        }
    }

    private final class DeleteNoteAction implements UndoableAction {

        private final EditorNote note;
        private final int originalIndex;

        private DeleteNoteAction(EditorNote note, int originalIndex) {
            this.note = note;
            this.originalIndex = originalIndex;
        }

        @Override
        public void undo() {
            notes.add(originalIndex, note);
        }

        @Override
        public void redo() {
            notes.remove(note);
        }
    }
}
