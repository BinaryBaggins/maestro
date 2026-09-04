package com.digero.maestro.noteeditor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.digero.maestro.noteeditor.NoteEditorLayout;

public class NoteEditorModelTest {

    private NoteEditorModel model;

    @BeforeEach
    public void setUp() {
        model = new NoteEditorModel(new ArrayList<>());
    }

    @Test
    public void createNoteAddsValidNote() {
        EditorNote note = model.createNote(
                60,
                1.0,
                1.0)
                .orElseThrow();

        assertEquals(1, model.getNotes().size());
        assertSame(note, model.getNotes().get(0));

        assertEquals(60, note.getMidiNote());
        assertEquals(1.0, note.getStartBeat(), 0.000001);
        assertEquals(1.0, note.getDurationBeats(), 0.000001);
    }

    @Test
    public void createNoteRejectsOverlap() {
        model.createNote(
                60,
                1.0,
                1.0)
                .orElseThrow();

        assertTrue(
                model.createNote(
                        60,
                        1.5,
                        1.0)
                        .isEmpty());

        assertEquals(1, model.getNotes().size());
    }

    @Test
    public void adjacentNotesDoNotOverlap() {
        model.createNote(
                60,
                0.0,
                1.0)
                .orElseThrow();

        assertTrue(
                model.createNote(
                        60,
                        1.0,
                        1.0)
                        .isPresent());

        assertEquals(2, model.getNotes().size());
    }

    @Test
    public void notesOnDifferentPitchesDoNotOverlap() {
        model.createNote(
                60,
                0.0,
                1.0)
                .orElseThrow();

        assertTrue(
                model.createNote(
                        61,
                        0.0,
                        1.0)
                        .isPresent());
    }

    @Test
    public void createNoteClampsStartBeatToZero() {
        EditorNote note = model.createNote(
                60,
                -1.0,
                1.0)
                .orElseThrow();

        assertEquals(
                0.0,
                note.getStartBeat(),
                0.000001);
    }

    @Test
    public void createNoteClampsMidiPitch() {
        EditorNote low = model.createNote(
                -10,
                0.0,
                1.0)
                .orElseThrow();

        EditorNote high = model.createNote(
                200,
                2.0,
                1.0)
                .orElseThrow();

        assertEquals(0, low.getMidiNote());
        assertEquals(
                NoteEditorLayout.MIDI_NOTE_COUNT - 1,
                high.getMidiNote());
    }

    @Test
    public void createNoteRejectsDurationBelowMinimum() {
        assertTrue(
                model.createNote(
                        60,
                        0.0,
                        NoteEditorLayout.SNAP_BEATS / 2.0)
                        .isEmpty());
    }

    @Test
    public void moveNoteChangesBeatAndPitch() {
        EditorNote note = model.createNote(
                60,
                0.0,
                1.0)
                .orElseThrow();

        assertTrue(
                model.moveNote(
                        note,
                        64,
                        2.0));

        assertEquals(64, note.getMidiNote());
        assertEquals(
                2.0,
                note.getStartBeat(),
                0.000001);
    }

    @Test
    public void moveNoteRejectsOverlapWithoutChangingNote() {
        EditorNote note = model.createNote(
                60,
                0.0,
                1.0)
                .orElseThrow();

        model.createNote(
                60,
                2.0,
                1.0)
                .orElseThrow();

        assertFalse(
                model.moveNote(
                        note,
                        60,
                        1.5));

        assertEquals(
                0.0,
                note.getStartBeat(),
                0.000001);

        assertEquals(60, note.getMidiNote());
    }

    @Test
    public void moveNoteRejectsUnknownNote() {
        EditorNote note = new EditorNote(
                60,
                0.0,
                1.0);

        assertFalse(
                model.moveNote(
                        note,
                        60,
                        2.0));

        assertEquals(
                0.0,
                note.getStartBeat(),
                0.000001);
    }

    @Test
    public void moveNoteClampsStartBeatToZero() {
        EditorNote note = model.createNote(
                60,
                2.0,
                1.0)
                .orElseThrow();

        assertTrue(
                model.moveNote(
                        note,
                        60,
                        -5.0));

        assertEquals(
                0.0,
                note.getStartBeat(),
                0.000001);
    }

    @Test
    public void resizeLeftChangesStartAndDuration() {
        EditorNote note = model.createNote(
                60,
                2.0,
                2.0)
                .orElseThrow();

        assertTrue(
                model.resizeNoteLeft(
                        note,
                        1.0));

        assertEquals(
                1.0,
                note.getStartBeat(),
                0.000001);

        assertEquals(
                3.0,
                note.getDurationBeats(),
                0.000001);
    }

    @Test
    public void resizeLeftEnforcesMinimumDuration() {
        EditorNote note = model.createNote(
                60,
                2.0,
                1.0)
                .orElseThrow();

        assertTrue(
                model.resizeNoteLeft(
                        note,
                        5.0));

        assertEquals(
                3.0 - NoteEditorLayout.SNAP_BEATS,
                note.getStartBeat(),
                0.000001);

        assertEquals(
                NoteEditorLayout.SNAP_BEATS,
                note.getDurationBeats(),
                0.000001);
    }

    @Test
    public void resizeLeftRejectsOverlap() {
        model.createNote(
                60,
                0.0,
                1.0)
                .orElseThrow();

        EditorNote note = model.createNote(
                60,
                2.0,
                1.0)
                .orElseThrow();

        assertFalse(
                model.resizeNoteLeft(
                        note,
                        0.5));

        assertEquals(
                2.0,
                note.getStartBeat(),
                0.000001);

        assertEquals(
                1.0,
                note.getDurationBeats(),
                0.000001);
    }

    @Test
    public void resizeRightChangesDuration() {
        EditorNote note = model.createNote(
                60,
                1.0,
                1.0)
                .orElseThrow();

        assertTrue(
                model.resizeNoteRight(
                        note,
                        3.0));

        assertEquals(
                2.0,
                note.getDurationBeats(),
                0.000001);
    }

    @Test
    public void resizeRightEnforcesMinimumDuration() {
        EditorNote note = model.createNote(
                60,
                2.0,
                1.0)
                .orElseThrow();

        assertTrue(
                model.resizeNoteRight(
                        note,
                        1.0));

        assertEquals(
                NoteEditorLayout.SNAP_BEATS,
                note.getDurationBeats(),
                0.000001);
    }

    @Test
    public void resizeRightRejectsOverlap() {
        EditorNote note = model.createNote(
                60,
                0.0,
                1.0)
                .orElseThrow();

        model.createNote(
                60,
                2.0,
                1.0)
                .orElseThrow();

        assertFalse(
                model.resizeNoteRight(
                        note,
                        2.5));

        assertEquals(
                1.0,
                note.getDurationBeats(),
                0.000001);
    }

    @Test
    public void deleteNoteRemovesExistingNote() {
        EditorNote note = model.createNote(
                60,
                0.0,
                1.0)
                .orElseThrow();

        assertTrue(model.deleteNote(note));
        assertTrue(model.getNotes().isEmpty());
    }

    @Test
    public void deleteNoteReturnsFalseForUnknownNote() {
        EditorNote note = new EditorNote(
                60,
                0.0,
                1.0);

        assertFalse(model.deleteNote(note));
    }
}