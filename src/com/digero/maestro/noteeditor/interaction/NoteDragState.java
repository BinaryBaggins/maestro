package com.digero.maestro.noteeditor.interaction;

import com.digero.maestro.noteeditor.model.DragMode;
import com.digero.maestro.noteeditor.model.EditorNote;
import com.digero.maestro.noteeditor.model.NoteSnapshot;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable snapshot of an in-progress move/resize gesture over a group of notes.
 */
public record NoteDragState(
    EditorNote grabbedNote,
    Map<EditorNote, NoteSnapshot> startStates,
    DragMode mode,
    double pointerOffsetBeats,
    int startY
) {

    public NoteDragState {
        Objects.requireNonNull(grabbedNote, "grabbedNote cannot be null");
        Objects.requireNonNull(startStates, "startStates cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");

        if (mode == DragMode.NONE) {
            throw new IllegalArgumentException("Drag mode must not be NONE");
        }

        startStates = Collections.unmodifiableMap(new LinkedHashMap<>(startStates));

        if (!startStates.containsKey(grabbedNote)) {
            throw new IllegalArgumentException("grabbedNote must be part of the drag group");
        }
    }

    public static NoteDragState capture(
        EditorNote grabbedNote,
        Collection<EditorNote> selectedNotes,
        DragMode mode,
        double mouseBeat,
        int startY
    ) {
        Objects.requireNonNull(selectedNotes, "selectedNotes cannot be null");

        Map<EditorNote, NoteSnapshot> startStates = new LinkedHashMap<>();
        for (EditorNote note : selectedNotes) {
            startStates.put(note, new NoteSnapshot(note));
        }

        NoteSnapshot grabbedSnapshot = startStates.get(grabbedNote);
        if (grabbedSnapshot == null) {
            throw new IllegalArgumentException("grabbedNote must be part of the drag group");
        }

        double grabbedStartBeat = grabbedSnapshot.startBeat();
        double grabbedStartEndBeat = grabbedStartBeat + grabbedSnapshot.durationBeats();

        double anchorBeat = switch (mode) {
            case MOVE, RESIZE_LEFT -> grabbedStartBeat;
            case RESIZE_RIGHT -> grabbedStartEndBeat;
            case NONE -> throw new IllegalArgumentException("Drag mode must not be NONE");
        };

        double pointerOffsetBeats = mouseBeat - anchorBeat;

        return new NoteDragState(grabbedNote, startStates, mode, pointerOffsetBeats, startY);
    }

    public Set<EditorNote> notes() {
        return startStates.keySet();
    }

    public Collection<NoteSnapshot> snapshots() {
        return startStates.values();
    }

    public NoteSnapshot grabbedStartState() {
        return startStates.get(grabbedNote);
    }

    public double grabbedStartBeat() {
        return grabbedStartState().startBeat();
    }

    public double grabbedStartEndBeat() {
        NoteSnapshot state = grabbedStartState();
        return state.startBeat() + state.durationBeats();
    }
}
