package com.digero.maestro.noteeditor.undo;

import java.util.ArrayDeque;
import java.util.Deque;

public final class UndoHistory {

    private final Deque<UndoableAction> undoStack = new ArrayDeque<>();
    private final Deque<UndoableAction> redoStack = new ArrayDeque<>();

    public void record(UndoableAction action) {
        undoStack.push(action);
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public boolean undo() {
        if (!canUndo()) {
            return false;
        }
        UndoableAction action = undoStack.peek();
        action.undo();

        undoStack.pop();
        redoStack.push(action);
        return true;
    }

    public boolean redo() {
        if (!canRedo()) {
            return false;
        }

        UndoableAction action = redoStack.peek();
        action.redo();

        redoStack.pop();
        undoStack.push(action);
        return true;
    }
}
