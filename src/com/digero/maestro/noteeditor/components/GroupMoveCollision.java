package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.model.EditorNote;

public record GroupMoveCollision(EditorNote movingNote, EditorNote blockingNote) {}
