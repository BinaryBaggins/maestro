package com.digero.maestro.noteeditor.undo;

public interface UndoableAction {
    void undo();

    void redo();
}
