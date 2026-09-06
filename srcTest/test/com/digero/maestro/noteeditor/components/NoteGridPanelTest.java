package com.digero.maestro.noteeditor.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.digero.maestro.noteeditor.NoteEditorGeometry;
import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.NoteEditorViewState;
import com.digero.maestro.noteeditor.model.EditorNote;
import com.digero.maestro.noteeditor.model.NoteEditorModel;
import java.awt.Point;
import java.util.List;
import org.junit.jupiter.api.Test;

class NoteGridPanelTest {

    private static final int PIXELS_PER_BEAT = NoteEditorViewState.DEFAULT_PIXELS_PER_BEAT;

    @Test
    void moveRightOverCWorksWhenGrabbingA() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(61, 3.0);
        EditorNote c = note(60, 5.0);
        NoteGridPanel panel = panel(a, b, c);
        selectBoth(panel, a, b);

        dragBy(panel, a, 4.5, 0);

        assertPosition(a, 60, 6.0);
        assertPosition(b, 61, 8.0);
    }

    @Test
    void moveRightOverCWorksWhenGrabbingB() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(61, 3.0);
        EditorNote c = note(60, 5.0);
        NoteGridPanel panel = panel(a, b, c);
        selectBoth(panel, a, b);

        dragBy(panel, b, 4.0, 0);

        assertPosition(a, 60, 6.0);
        assertPosition(b, 61, 8.0);
    }

    @Test
    void moveLeftOverCWorksWhenGrabbingA() {
        EditorNote a = note(60, 7.0);
        EditorNote b = note(61, 9.0);
        EditorNote c = note(60, 3.0);
        NoteGridPanel panel = panel(a, b, c);
        selectBoth(panel, a, b);

        dragBy(panel, a, -4.5, 0);

        assertPosition(a, 60, 2.0);
        assertPosition(b, 61, 4.0);
    }

    @Test
    void moveLeftOverCWorksWhenGrabbingB() {
        EditorNote a = note(60, 7.0);
        EditorNote b = note(61, 7.25);
        EditorNote c = note(60, 3.5);
        NoteGridPanel panel = panel(a, b, c);
        selectBoth(panel, a, b);

        dragBy(panel, b, -4.25, 0);

        assertPosition(a, 60, 2.5);
        assertPosition(b, 61, 2.75);
    }

    @Test
    void onlyACollidesWithCAndBothStillMovePastIt() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(61, 3.0);
        EditorNote c = note(60, 5.0);
        NoteGridPanel panel = panel(a, b, c);
        selectBoth(panel, a, b);

        dragBy(panel, a, 4.75, 0);

        assertPosition(a, 60, 6.0);
        assertPosition(b, 61, 8.0);
    }

    @Test
    void onlyBCollidesWithCAndBothStillMovePastIt() {
        EditorNote a = note(61, 1.0);
        EditorNote b = note(60, 3.0);
        EditorNote c = note(60, 5.0);
        NoteGridPanel panel = panel(a, b, c);
        selectBoth(panel, a, b);

        dragBy(panel, b, 2.75, 0);

        assertPosition(a, 61, 4.0);
        assertPosition(b, 60, 6.0);
    }

    @Test
    void bothNotesPassCSequentially() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(60, 3.0);
        EditorNote c = note(60, 5.0, 3.0);
        NoteGridPanel panel = panel(a, b, c);
        selectBoth(panel, a, b);

        dragBy(panel, a, 6.75, 0);

        assertPosition(a, 60, 8.0);
        assertPosition(b, 60, 10.0);
    }

    @Test
    void twoBlockersArePassedInSequence() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(60, 3.0);
        EditorNote c = note(60, 5.0);
        EditorNote d = note(60, 9.0, 0.5);
        NoteGridPanel panel = panel(a, b, c, d);
        selectBoth(panel, a, b);

        Point press = pointAt(b, 0.5);
        panel.beginNoteDrag(press);

        panel.dragSelectedNoteTo(new Point(press.x + (int) Math.round(4.5 * PIXELS_PER_BEAT), press.y));

        assertPosition(a, 60, 6.0);
        assertPosition(b, 60, 8.0);

        panel.dragSelectedNoteTo(new Point(press.x + (int) Math.round(6.0 * PIXELS_PER_BEAT), press.y));
        panel.endNoteDrag();

        assertPosition(a, 60, 7.5);
        assertPosition(b, 60, 9.5);
    }

    @Test
    void verticalGroupMoveAppliesOnePitchDelta() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(64, 3.0);
        NoteGridPanel panel = panel(a, b);
        selectBoth(panel, a, b);

        dragBy(panel, a, 0.0, 2);

        assertPosition(a, 62, 1.0);
        assertPosition(b, 66, 3.0);
    }

    @Test
    void diagonalGroupMoveUsesBothDeltasAndResolvesCollision() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(64, 3.0);
        EditorNote c = note(62, 5.0);
        NoteGridPanel panel = panel(a, b, c);
        selectBoth(panel, a, b);

        dragBy(panel, a, 4.75, 2);

        assertPosition(a, 62, 6.0);
        assertPosition(b, 66, 8.0);
    }

    @Test
    void groupResizeLeftAppliesOneEdgeDelta() {
        EditorNote a = note(60, 2.0, 2.0);
        EditorNote b = note(64, 5.0, 3.0);
        NoteGridPanel panel = panel(a, b);
        selectBoth(panel, a, b);

        dragResizeLeftBy(panel, a, -0.5);

        assertNote(a, 60, 1.5, 2.5);
        assertNote(b, 64, 4.5, 3.5);
    }

    @Test
    void groupResizeRightAppliesOneEdgeDelta() {
        EditorNote a = note(60, 1.0, 2.0);
        EditorNote b = note(64, 4.0, 3.0);
        NoteGridPanel panel = panel(a, b);
        selectBoth(panel, a, b);

        dragResizeRightBy(panel, a, 0.5);

        assertNote(a, 60, 1.0, 2.5);
        assertNote(b, 64, 4.0, 3.5);
    }

    @Test
    void undoRestoresGroupMove() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(64, 3.0);
        NoteGridPanel panel = panel(a, b);
        selectBoth(panel, a, b);

        dragBy(panel, a, 2.0, 1);
        panel.undo();

        assertPosition(a, 60, 1.0);
        assertPosition(b, 64, 3.0);
    }

    @Test
    void undoRestoresGroupResize() {
        EditorNote a = note(60, 1.0, 2.0);
        EditorNote b = note(64, 4.0, 3.0);
        NoteGridPanel panel = panel(a, b);
        selectBoth(panel, a, b);

        dragResizeRightBy(panel, a, 0.5);
        panel.undo();

        assertNote(a, 60, 1.0, 2.0);
        assertNote(b, 64, 4.0, 3.0);
    }

    @Test
    void groupStaysPutUntilMouseCrossesBlocker() {
        EditorNote a = note(60, 1.0);
        EditorNote b = note(64, 3.0);
        EditorNote c = note(60, 5.0);

        NoteGridPanel panel = panel(a, b, c);

        selectBoth(panel, a, b);

        dragBy(panel, a, 3.5, 0);

        assertPosition(a, 60, 1.0);
        assertPosition(b, 64, 3.0);
    }

    private static NoteGridPanel panel(EditorNote... notes) {
        NoteEditorModel model = new NoteEditorModel(List.of(notes));
        return new NoteGridPanel(new NoteEditorViewState(), model);
    }

    private static EditorNote note(int midiNote, double startBeat) {
        return note(midiNote, startBeat, 1.0);
    }

    private static EditorNote note(int midiNote, double startBeat, double durationBeats) {
        return new EditorNote(midiNote, startBeat, durationBeats);
    }

    private static void selectBoth(NoteGridPanel panel, EditorNote first, EditorNote second) {
        panel.handleSelectionClick(pointAt(first, 0.5), false);
        panel.handleSelectionClick(pointAt(second, 0.5), true);
    }

    private static void dragBy(NoteGridPanel panel, EditorNote grabbed, double deltaBeat, int deltaMidiNote) {
        Point press = pointAt(grabbed, 0.5);
        panel.beginNoteDrag(press);
        Point target = new Point(
            press.x + (int) Math.round(deltaBeat * PIXELS_PER_BEAT),
            press.y - deltaMidiNote * NoteEditorLayout.NOTE_HEIGHT
        );
        panel.dragSelectedNoteTo(target);
        panel.endNoteDrag();
    }

    private static void dragResizeLeftBy(NoteGridPanel panel, EditorNote grabbed, double deltaBeat) {
        Point press = pointAt(grabbed, 0.0);
        panel.beginNoteDrag(press);
        panel.dragSelectedNoteTo(new Point(press.x + (int) Math.round(deltaBeat * PIXELS_PER_BEAT), press.y));
        panel.endNoteDrag();
    }

    private static void dragResizeRightBy(NoteGridPanel panel, EditorNote grabbed, double deltaBeat) {
        Point press = pointAt(grabbed, grabbed.getDurationBeats());
        panel.beginNoteDrag(press);
        panel.dragSelectedNoteTo(new Point(press.x + (int) Math.round(deltaBeat * PIXELS_PER_BEAT), press.y));
        panel.endNoteDrag();
    }

    private static Point pointAt(EditorNote note, double offsetBeat) {
        double safeOffsetBeat = offsetBeat >= note.getDurationBeats() ? note.getDurationBeats() - 0.05 : offsetBeat;
        int x = NoteEditorGeometry.getXForBeat(note.getStartBeat() + safeOffsetBeat, PIXELS_PER_BEAT);
        int y = NoteEditorGeometry.getYForMidiNote(note.getMidiNote()) + NoteEditorLayout.NOTE_HEIGHT / 2;
        return new Point(x, y);
    }

    private static void assertPosition(EditorNote note, int midiNote, double startBeat) {
        assertEquals(midiNote, note.getMidiNote());
        assertEquals(startBeat, note.getStartBeat(), 0.000001);
    }

    private static void assertNote(EditorNote note, int midiNote, double startBeat, double durationBeats) {
        assertPosition(note, midiNote, startBeat);
        assertEquals(durationBeats, note.getDurationBeats(), 0.000001);
    }
}
