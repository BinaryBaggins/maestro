package com.digero.maestro.noteeditor.interaction;

import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.model.NoteSnapshot;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class GroupMoveResolver {

    private GroupMoveResolver() {}

    /**
     * Resolves a requested group move using only captured note snapshots.
     *
     * <p>{@code movingNotes} must contain snapshots captured at the start of
     * the drag. {@code blockingNotes} must contain only snapshots of notes
     * outside the moving group. An empty result means that no permissible
     * resolve step exists: the moving group is empty, there is no horizontal
     * movement, the mouse has not crossed a blocker, or beat zero leaves no
     * valid position.</p>
     *
     * @param movingNotes snapshots captured at the start of the drag
     * @param blockingNotes snapshots of notes outside the moving group
     * @param requestedMidiDelta requested relative MIDI-note delta
     * @param requestedBeatDelta requested relative start-beat delta
     * @param mouseBeat current beat position of the mouse cursor
     * @return the fully resolved move, including clamped MIDI and beat deltas
     */
    public static Optional<ResolvedGroupMove> resolve(
        Collection<NoteSnapshot> movingNotes,
        Collection<NoteSnapshot> blockingNotes,
        int requestedMidiDelta,
        double requestedBeatDelta,
        double mouseBeat
    ) {
        List<NoteSnapshot> moving = List.copyOf(movingNotes);
        List<NoteSnapshot> blockers = List.copyOf(blockingNotes);

        if (moving.isEmpty()) {
            return Optional.empty();
        }

        boolean movingRight = requestedBeatDelta > 0;
        boolean movingLeft = requestedBeatDelta < 0;

        if (!movingRight && !movingLeft) {
            return Optional.empty();
        }

        int midiDelta = clampMidiDelta(moving, requestedMidiDelta);
        double minimumBeatDelta = getMinimumBeatDelta(moving);
        double candidateDelta = Math.max(requestedBeatDelta, minimumBeatDelta);
        Set<Integer> crossedBlockers = new HashSet<>();

        while (true) {
            Collision collision = findCollision(moving, blockers, midiDelta, candidateDelta);

            if (collision == null) {
                return Optional.of(new ResolvedGroupMove(midiDelta, candidateDelta));
            }

            NoteSnapshot movingState = moving.get(collision.movingIndex());
            NoteSnapshot blockingState = blockers.get(collision.blockingIndex());

            double blockerStart = blockingState.startBeat();
            double blockerEnd = blockerStart + blockingState.durationBeats();

            if (!crossedBlockers.contains(collision.blockingIndex())) {
                boolean mouseHasCrossed = movingRight ? mouseBeat >= blockerEnd : mouseBeat <= blockerStart;

                if (!mouseHasCrossed) {
                    return Optional.empty();
                }

                crossedBlockers.add(collision.blockingIndex());
            }

            if (movingRight) {
                double requiredDelta = blockerEnd - movingState.startBeat();
                candidateDelta = Math.max(candidateDelta, requiredDelta);
            } else {
                double requiredDelta = blockerStart - movingState.durationBeats() - movingState.startBeat();

                if (requiredDelta < minimumBeatDelta) {
                    return Optional.empty();
                }

                candidateDelta = Math.min(candidateDelta, requiredDelta);
            }
        }
    }

    private static int clampMidiDelta(List<NoteSnapshot> movingNotes, int requestedDelta) {
        int minimumDelta = Integer.MIN_VALUE;
        int maximumDelta = Integer.MAX_VALUE;

        for (NoteSnapshot state : movingNotes) {
            minimumDelta = Math.max(minimumDelta, -state.midiNote());
            maximumDelta = Math.min(maximumDelta, NoteEditorLayout.MIDI_NOTE_COUNT - 1 - state.midiNote());
        }

        return Math.max(minimumDelta, Math.min(requestedDelta, maximumDelta));
    }

    private static double getMinimumBeatDelta(List<NoteSnapshot> movingNotes) {
        double minimumDelta = Double.NEGATIVE_INFINITY;

        for (NoteSnapshot state : movingNotes) {
            minimumDelta = Math.max(minimumDelta, -state.startBeat());
        }

        return minimumDelta;
    }

    private static Collision findCollision(
        List<NoteSnapshot> movingNotes,
        List<NoteSnapshot> blockingNotes,
        int midiDelta,
        double beatDelta
    ) {
        for (int movingIndex = 0; movingIndex < movingNotes.size(); movingIndex++) {
            NoteSnapshot moving = movingNotes.get(movingIndex);
            int targetMidiNote = moving.midiNote() + midiDelta;
            double targetStartBeat = moving.startBeat() + beatDelta;
            double targetEndBeat = targetStartBeat + moving.durationBeats();

            for (int blockingIndex = 0; blockingIndex < blockingNotes.size(); blockingIndex++) {
                NoteSnapshot blocker = blockingNotes.get(blockingIndex);

                if (blocker.midiNote() != targetMidiNote) {
                    continue;
                }

                double blockerStart = blocker.startBeat();
                double blockerEnd = blockerStart + blocker.durationBeats();

                if (targetStartBeat < blockerEnd && targetEndBeat > blockerStart) {
                    return new Collision(movingIndex, blockingIndex);
                }
            }
        }

        return null;
    }

    private record Collision(int movingIndex, int blockingIndex) {}
}
