package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.model.EditorNote;

record GroupMoveCollision(EditorNote movingNote, EditorNote blockingNote) {}
