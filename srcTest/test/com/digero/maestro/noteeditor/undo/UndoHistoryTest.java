package com.digero.maestro.noteeditor.undo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UndoHistoryTest {

    private UndoHistory history;
    private ValueHolder value;

    @BeforeEach
    public void setUp() {
        history = new UndoHistory();
        value = new ValueHolder();
    }

    @Test
    public void newHistoryCannotUndoOrRedo() {
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());

        assertFalse(history.undo());
        assertFalse(history.redo());
    }

    @Test
    public void recordedActionCanBeUndone() {
        value.value = 10;

        history.record(new ValueChangeAction(value, 0, 10));

        assertTrue(history.canUndo());

        assertTrue(history.undo());

        assertEquals(0, value.value);
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
    }

    @Test
    public void undoneActionCanBeRedone() {
        value.value = 10;

        history.record(new ValueChangeAction(value, 0, 10));

        history.undo();

        assertTrue(history.redo());

        assertEquals(10, value.value);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void actionsAreUndoneInReverseOrder() {
        value.value = 10;
        history.record(new ValueChangeAction(value, 0, 10));

        value.value = 20;
        history.record(new ValueChangeAction(value, 10, 20));

        history.undo();

        assertEquals(10, value.value);

        history.undo();

        assertEquals(0, value.value);
    }

    @Test
    public void actionsAreRedoneInOriginalOrder() {
        value.value = 10;
        history.record(new ValueChangeAction(value, 0, 10));

        value.value = 20;
        history.record(new ValueChangeAction(value, 10, 20));

        history.undo();
        history.undo();

        history.redo();

        assertEquals(10, value.value);

        history.redo();

        assertEquals(20, value.value);
    }

    @Test
    public void recordingNewActionClearsRedoHistory() {
        value.value = 10;
        history.record(new ValueChangeAction(value, 0, 10));

        history.undo();

        assertTrue(history.canRedo());

        value.value = 20;
        history.record(new ValueChangeAction(value, 0, 20));

        assertFalse(history.canRedo());
        assertTrue(history.canUndo());
    }

    private static final class ValueHolder {

        private int value;
    }

    private static final class ValueChangeAction implements UndoableAction {

        private final ValueHolder value;
        private final int before;
        private final int after;

        private ValueChangeAction(ValueHolder value, int before, int after) {
            this.value = value;
            this.before = before;
            this.after = after;
        }

        @Override
        public void undo() {
            value.value = before;
        }

        @Override
        public void redo() {
            value.value = after;
        }
    }
}
