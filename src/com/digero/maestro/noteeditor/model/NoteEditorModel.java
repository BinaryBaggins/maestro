package com.digero.maestro.noteeditor.model;

import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.undo.UndoHistory;
import com.digero.maestro.noteeditor.undo.UndoableAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class NoteEditorModel {

    private final UndoHistory undoHistory;
    private final List<EditorNote> notes;
    private Map<EditorNote, NoteState> activeStartStates;

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
        if (!notes.contains(note)) {
            return false;
        }

        return deleteNotes(List.of(note));
    }

    public boolean deleteNotes(Collection<EditorNote> notesToDelete) {
        Objects.requireNonNull(notesToDelete);

        Set<EditorNote> uniqueNotes = new LinkedHashSet<>(notesToDelete);

        if (uniqueNotes.isEmpty()) {
            return false;
        }

        List<DeletedNote> deletedNotes = new ArrayList<>();

        for (EditorNote note : uniqueNotes) {
            int index = notes.indexOf(note);

            if (index < 0) {
                return false;
            }

            deletedNotes.add(new DeletedNote(note, index));
        }

        deletedNotes.sort(Comparator.comparingInt(DeletedNote::originalIndex));

        for (DeletedNote deleted : deletedNotes) {
            notes.remove(deleted.note());
        }

        undoHistory.record(new DeleteNotesAction(deletedNotes));

        return true;
    }

    public void beginNoteStateChange(EditorNote note) {
        beginNoteStateChange(List.of(note));
    }

    public void beginNoteStateChange(Collection<EditorNote> changedNotes) {
        Objects.requireNonNull(changedNotes, "changedNotes cannot be null");

        if (activeStartStates != null) {
            throw new IllegalStateException("A note state change is already active");
        }

        Set<EditorNote> uniqueNotes = new LinkedHashSet<>(changedNotes);
        if (uniqueNotes.isEmpty()) {
            throw new IllegalArgumentException("At least one note must be provided");
        }

        for (EditorNote note : uniqueNotes) {
            if (!notes.contains(note)) {
                throw new IllegalArgumentException("Note does not exist in model");
            }
        }

        Map<EditorNote, NoteState> startStates = new LinkedHashMap<>();
        for (EditorNote note : uniqueNotes) {
            startStates.put(note, new NoteState(note));
        }

        activeStartStates = startStates;
    }

    public void endNoteStateChange() {
        if (activeStartStates == null) {
            throw new IllegalStateException("No note state change is active");
        }

        Map<EditorNote, NoteState> endStates = new LinkedHashMap<>();
        for (EditorNote note : activeStartStates.keySet()) {
            endStates.put(note, new NoteState(note));
        }

        if (!activeStartStates.equals(endStates)) {
            undoHistory.record(new NoteStateChangeAction(activeStartStates, endStates));
        }

        activeStartStates = null;
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

    public boolean moveNotes(Collection<EditorNote> notesToMove, int deltaMidiNotes, double deltaBeats) {
        Set<EditorNote> movingNotes = validateNoteGroup(notesToMove);

        if (movingNotes.isEmpty()) {
            return false;
        }

        int minimumMidiDelta = Integer.MIN_VALUE;
        int maximumMidiDelta = Integer.MAX_VALUE;
        double minimumBeatDelta = Double.NEGATIVE_INFINITY;

        for (EditorNote note : movingNotes) {
            NoteState base = getBaseState(note);

            minimumMidiDelta = Math.max(minimumMidiDelta, -base.midiNote());

            maximumMidiDelta = Math.min(maximumMidiDelta, NoteEditorLayout.MIDI_NOTE_COUNT - 1 - base.midiNote());

            minimumBeatDelta = Math.max(minimumBeatDelta, -base.startBeat());
        }

        int appliedMidiDelta = Math.max(minimumMidiDelta, Math.min(deltaMidiNotes, maximumMidiDelta));

        double appliedBeatDelta = Math.max(deltaBeats, minimumBeatDelta);

        Map<EditorNote, NoteState> targetStates = new LinkedHashMap<>();

        for (EditorNote note : movingNotes) {
            NoteState base = getBaseState(note);

            targetStates.put(
                note,
                new NoteState(
                    base.midiNote() + appliedMidiDelta,
                    base.startBeat() + appliedBeatDelta,
                    base.durationBeats()
                )
            );
        }

        if (!canApplyStates(targetStates)) {
            return false;
        }

        return applyTargetStates(targetStates);
    }

    private Set<EditorNote> validateNoteGroup(Collection<EditorNote> group) {
        Objects.requireNonNull(group);

        Set<EditorNote> result = new LinkedHashSet<>(group);

        for (EditorNote note : result) {
            if (!notes.contains(note)) {
                throw new IllegalArgumentException("Note does not exist in model");
            }
        }

        return result;
    }

    private boolean applyTargetStates(Map<EditorNote, NoteState> targetStates) {
        boolean changed = false;

        for (var entry : targetStates.entrySet()) {
            EditorNote note = entry.getKey();
            NoteState target = entry.getValue();

            if (!new NoteState(note).equals(target)) {
                changed = true;
            }

            note.setMidiNote(target.midiNote());
            note.setStartBeat(target.startBeat());
            note.setDurationBeats(target.durationBeats());
        }

        return changed;
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

    public boolean resizeNotesLeft(Collection<EditorNote> notesToResize, double deltaStartBeat) {
        Set<EditorNote> resizedNotes = validateNoteGroup(notesToResize);

        if (resizedNotes.isEmpty()) {
            return false;
        }

        double minimumDelta = Double.NEGATIVE_INFINITY;
        double maximumDelta = Double.POSITIVE_INFINITY;

        for (EditorNote note : resizedNotes) {
            NoteState base = getBaseState(note);

            // Left edge cannot move before beat 0.
            minimumDelta = Math.max(minimumDelta, -base.startBeat());

            // Duration cannot become shorter than minimum length.
            maximumDelta = Math.min(maximumDelta, base.durationBeats() - NoteEditorLayout.SNAP_BEATS);
        }

        double appliedDelta = Math.max(minimumDelta, Math.min(deltaStartBeat, maximumDelta));

        Map<EditorNote, NoteState> targetStates = new LinkedHashMap<>();

        for (EditorNote note : resizedNotes) {
            NoteState base = getBaseState(note);

            targetStates.put(
                note,
                new NoteState(base.midiNote(), base.startBeat() + appliedDelta, base.durationBeats() - appliedDelta)
            );
        }

        if (!canApplyStates(targetStates)) {
            return false;
        }

        return applyTargetStates(targetStates);
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

    public boolean resizeNotesRight(Collection<EditorNote> notesToResize, double deltaEndBeat) {
        Set<EditorNote> resizedNotes = validateNoteGroup(notesToResize);

        if (resizedNotes.isEmpty()) {
            return false;
        }

        double minimumDelta = Double.NEGATIVE_INFINITY;

        for (EditorNote note : resizedNotes) {
            NoteState base = getBaseState(note);

            minimumDelta = Math.max(minimumDelta, NoteEditorLayout.SNAP_BEATS - base.durationBeats());
        }

        double appliedDelta = Math.max(deltaEndBeat, minimumDelta);

        Map<EditorNote, NoteState> targetStates = new LinkedHashMap<>();

        for (EditorNote note : resizedNotes) {
            NoteState base = getBaseState(note);

            targetStates.put(
                note,
                new NoteState(base.midiNote(), base.startBeat(), base.durationBeats() + appliedDelta)
            );
        }

        if (!canApplyStates(targetStates)) {
            return false;
        }

        return applyTargetStates(targetStates);
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

    /**
     * Checks if the given target states can be applied to the notes in the model without violating any constraints.
     *
     * @param targetStates A map of notes to their desired target states.
     * @return true if the target states can be applied, false otherwise.
     */
    private boolean canApplyStates(Map<EditorNote, NoteState> targetStates) {
        for (NoteState state : targetStates.values()) {
            // midi note must be in range
            if (state.midiNote() < 0 || state.midiNote() >= NoteEditorLayout.MIDI_NOTE_COUNT) {
                return false;
            }
            // start beat must be non-negative
            if (state.startBeat() < 0) {
                return false;
            }
            // duration must be at least the minimum snap beats
            if (state.durationBeats() < NoteEditorLayout.SNAP_BEATS) {
                return false;
            }
        }

        for (int i = 0; i < notes.size(); i++) {
            EditorNote first = notes.get(i);
            NoteState firstState = targetStates.getOrDefault(first, new NoteState(first));

            for (int j = i + 1; j < notes.size(); j++) {
                EditorNote second = notes.get(j);
                NoteState secondState = targetStates.getOrDefault(second, new NoteState(second));

                if (firstState.midiNote() != secondState.midiNote()) {
                    continue;
                }

                double firstStart = firstState.startBeat();
                double firstEnd = firstStart + firstState.durationBeats();

                double secondStart = secondState.startBeat();
                double secondEnd = secondStart + secondState.durationBeats();

                if (firstStart < secondEnd && firstEnd > secondStart) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns the base state of a note, which is either the active start state (if a note state change is in progress) or the current state of the note.
     *
     * @param note The note for which to get the base state.
     * @return The base state of the note.
     */
    private NoteState getBaseState(EditorNote note) {
        if (activeStartStates != null) {
            NoteState activeState = activeStartStates.get(note);
            if (activeState != null) {
                return activeState;
            }
        }
        return new NoteState(note);
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

    private final class DeleteNotesAction implements UndoableAction {

        private final List<DeletedNote> deletedNotes;

        private DeleteNotesAction(List<DeletedNote> deletedNotes) {
            this.deletedNotes = new ArrayList<>(deletedNotes);
        }

        @Override
        public void undo() {
            for (DeletedNote deleted : deletedNotes) {
                notes.add(deleted.originalIndex(), deleted.note());
            }
        }

        @Override
        public void redo() {
            for (DeletedNote deleted : deletedNotes) {
                notes.remove(deleted.note());
            }
        }
    }

    private final class NoteStateChangeAction implements UndoableAction {

        private final Map<EditorNote, NoteState> startStates;
        private final Map<EditorNote, NoteState> endStates;

        private NoteStateChangeAction(Map<EditorNote, NoteState> startStates, Map<EditorNote, NoteState> endStates) {
            this.startStates = new LinkedHashMap<>(startStates);
            this.endStates = new LinkedHashMap<>(endStates);
        }

        @Override
        public void undo() {
            applyStates(startStates);
        }

        @Override
        public void redo() {
            applyStates(endStates);
        }

        private void applyStates(Map<EditorNote, NoteState> states) {
            for (var entry : states.entrySet()) {
                EditorNote note = entry.getKey();
                NoteState state = entry.getValue();

                note.setMidiNote(state.midiNote());
                note.setStartBeat(state.startBeat());
                note.setDurationBeats(state.durationBeats());
            }
        }
    }
}
