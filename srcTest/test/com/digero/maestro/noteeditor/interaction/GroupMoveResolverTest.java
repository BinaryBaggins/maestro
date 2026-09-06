package com.digero.maestro.noteeditor.interaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.digero.maestro.noteeditor.model.NoteSnapshot;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GroupMoveResolverTest {

    @Test
    void resolvesRightMoveAfterMouseCrossesBlocker() {
        Optional<ResolvedGroupMove> result = GroupMoveResolver.resolve(
            List.of(snapshot(60, 1.0), snapshot(61, 3.0)),
            List.of(snapshot(60, 5.0)),
            0,
            4.5,
            6.0
        );

        assertResolved(result, 0, 5.0);
    }

    @Test
    void rejectsMoveUntilMouseCrossesBlocker() {
        Optional<ResolvedGroupMove> result = GroupMoveResolver.resolve(
            List.of(snapshot(60, 1.0)),
            List.of(snapshot(60, 5.0)),
            0,
            4.5,
            5.5
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void resolvesTwoBlockersInSequence() {
        Optional<ResolvedGroupMove> result = GroupMoveResolver.resolve(
            List.of(snapshot(60, 1.0), snapshot(60, 3.0)),
            List.of(snapshot(60, 5.0), snapshot(60, 8.0)),
            0,
            4.0,
            10.0
        );

        assertResolved(result, 0, 6.0);
    }

    @Test
    void clampsGroupToBeatZero() {
        Optional<ResolvedGroupMove> result = GroupMoveResolver.resolve(
            List.of(snapshot(60, 1.0), snapshot(61, 3.0)),
            List.of(),
            0,
            -5.0,
            -4.0
        );

        assertResolved(result, 0, -1.0);
    }

    @Test
    void returnsClampedMidiDeltaAndResolvedBeatDelta() {
        Optional<ResolvedGroupMove> result = GroupMoveResolver.resolve(
            List.of(snapshot(10, 1.0), snapshot(100, 3.0)),
            List.of(snapshot(0, 5.0)),
            -20,
            4.0,
            6.0
        );

        assertResolved(result, -10, 5.0);
    }

    private static void assertResolved(Optional<ResolvedGroupMove> result, int midiDelta, double beatDelta) {
        assertTrue(result.isPresent());
        assertEquals(midiDelta, result.orElseThrow().midiDelta());
        assertEquals(beatDelta, result.orElseThrow().beatDelta(), 0.000001);
    }

    private static NoteSnapshot snapshot(int midiNote, double startBeat) {
        return new NoteSnapshot(midiNote, startBeat, 1.0);
    }
}
