package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.NoteEditorGeometry;
import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.NoteEditorViewState;
import com.digero.maestro.noteeditor.actions.NoteEditorKeyBindings;
import com.digero.maestro.noteeditor.actions.NoteGridMouseListener;
import com.digero.maestro.noteeditor.model.DragMode;
import com.digero.maestro.noteeditor.model.EditorNote;
import com.digero.maestro.noteeditor.model.NoteEditorModel;
import com.digero.maestro.noteeditor.model.NoteSelectionModel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JPanel;

public class NoteGridPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Model and view state
    private final NoteEditorModel model;
    private final NoteEditorViewState viewState;
    private final NoteSelectionModel selectionModel = new NoteSelectionModel();

    // Selection box state variables
    private SelectionBox selectionBox;
    private boolean additiveSelectionBox;
    private Set<EditorNote> selectionBeforeBox = Set.of();

    private EditorNote dragNote;
    private Set<EditorNote> dragNotes = Set.of();
    private double dragStartEndBeat;

    // Dragging state variables
    private double dragOffsetBeats;
    private int dragStartY;
    private double dragStartBeat;

    private Map<EditorNote, DragStartState> dragStartStates = Map.of();

    // Resizing state variables
    private double resizeOffsetBeats;
    private DragMode dragMode = DragMode.NONE;

    public NoteGridPanel(NoteEditorViewState viewSettings, NoteEditorModel model) {
        this.viewState = viewSettings;
        this.model = model;

        updatePreferredSize();

        NoteGridMouseListener mouseListener = new NoteGridMouseListener(this);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);

        NoteEditorKeyBindings.install(this);

        setFocusable(true);
    }

    public void updateZoom() {
        updatePreferredSize();
        revalidate();
        repaint();
    }

    private void updatePreferredSize() {
        setPreferredSize(new Dimension(viewState.getEditorWidth(), NoteEditorLayout.EDITOR_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        paintPitchRows(g);
        paintTimeGrid(g);
        paintNotes(g);
        paintSelectionBox(g);
    }

    private void paintSelectionBox(Graphics g) {
        if (selectionBox == null) {
            return;
        }
        Rectangle bounds = selectionBox.getBounds();
        g.setColor(NoteEditorLayout.SELECTION_BOX_FILL_COLOR);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(NoteEditorLayout.SELECTION_BOX_BORDER_COLOR);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        if (bounds.width > 2 && bounds.height > 2) {
            g.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
        }
    }

    private void paintPitchRows(Graphics g) {
        for (int midiNote = 0; midiNote < NoteEditorLayout.MIDI_NOTE_COUNT; midiNote++) {
            int y = NoteEditorGeometry.getYForMidiNote(midiNote);

            if (NoteEditorGeometry.isBlackKey(midiNote)) {
                g.setColor(NoteEditorLayout.BLACK_NOTE_ROW_COLOR);
            } else {
                g.setColor(Color.WHITE);
            }

            g.fillRect(0, y, getWidth(), NoteEditorLayout.NOTE_HEIGHT);

            g.setColor(NoteEditorLayout.PITCH_LINE_COLOR);
            g.drawLine(0, y, getWidth(), y);
        }
    }

    private void paintTimeGrid(Graphics g) {
        for (int beat = 0; NoteEditorGeometry.getXForBeat(beat, viewState.getPixelsPerBeat()) < getWidth(); beat++) {
            int x = NoteEditorGeometry.getXForBeat(beat, viewState.getPixelsPerBeat());

            if (NoteEditorGeometry.isMeasureStart(beat)) {
                g.setColor(NoteEditorLayout.MEASURE_LINE_COLOR);
            } else {
                g.setColor(NoteEditorLayout.BEAT_LINE_COLOR);
            }

            g.drawLine(x, 0, x, getHeight());
        }
    }

    private void paintNotes(Graphics g) {
        for (EditorNote note : model.getNotes()) {
            paintNote(g, note);
        }

        for (EditorNote note : selectionModel.getSelectedNotes()) {
            drawSelectedNoteOverlay(g, note);
        }
    }

    private void paintNote(Graphics g, EditorNote note) {
        Rectangle bounds = getNoteBounds(note);

        g.setColor(NoteEditorLayout.NOTE_COLOR);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void drawSelectedNoteOverlay(Graphics g, EditorNote note) {
        Rectangle bounds = getNoteBounds(note);

        g.setColor(NoteEditorLayout.SELECTED_NOTE_BORDER_COLOR);

        g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);

        if (bounds.width > 3 && bounds.height > 3) {
            g.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 3, bounds.height - 3);
        }
    }

    public void createNoteAt(Point point) {
        if (findNoteAt(point) != null) {
            return;
        }

        double beat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());
        double startBeat = NoteEditorGeometry.snapBeat(beat);
        int midiNote = NoteEditorGeometry.getMidiNoteForY(point.y);

        EditorNote note = model
            .createNote(midiNote, startBeat, NoteEditorLayout.DEFAULT_NOTE_DURATION_BEATS)
            .orElse(null);

        if (note == null) {
            return;
        }

        selectionModel.setSelection(note);

        repaint();
    }

    public void deleteSelectedNote() {
        // for now we only support deleting a single selected note at a time
        if (selectionModel.getSelectedNotes().size() != 1) {
            return;
        }

        EditorNote note = selectionModel.getSelectedNotes().iterator().next();

        if (model.deleteNote(note)) {
            selectionModel.clearSelection();
            repaint();
        }
    }

    private DragMode getDragMode(EditorNote note, Point point) {
        Rectangle bounds = getNoteBounds(note);

        int distanceFromLeft = point.x - bounds.x;

        int distanceFromRight = bounds.x + bounds.width - point.x;

        if (
            distanceFromLeft >= 0 &&
            distanceFromLeft <= NoteEditorLayout.NOTE_RESIZE_HANDLE_WIDTH &&
            distanceFromLeft <= distanceFromRight
        ) {
            return DragMode.RESIZE_LEFT;
        }

        if (distanceFromRight >= 0 && distanceFromRight <= NoteEditorLayout.NOTE_RESIZE_HANDLE_WIDTH) {
            return DragMode.RESIZE_RIGHT;
        }

        return DragMode.MOVE;
    }

    public void beginNoteDrag(Point point) {
        requestFocusInWindow();

        dragNote = findNoteAt(point);

        if (dragNote == null) {
            dragMode = DragMode.NONE;
            repaint();
            return;
        }

        if (!selectionModel.isSelected(dragNote)) {
            selectionModel.setSelection(dragNote);
        }

        dragNotes = new LinkedHashSet<>(selectionModel.getSelectedNotes());

        Map<EditorNote, DragStartState> startStates = new LinkedHashMap<>();

        for (EditorNote note : dragNotes) {
            startStates.put(note, new DragStartState(note.getMidiNote(), note.getStartBeat(), note.getDurationBeats()));
        }

        dragStartStates = startStates;

        model.beginNoteStateChange(dragNotes);

        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());

        dragMode = getDragMode(dragNote, point);

        dragStartBeat = dragNote.getStartBeat();
        dragStartEndBeat = dragNote.getStartBeat() + dragNote.getDurationBeats();
        dragStartY = point.y;

        switch (dragMode) {
            case MOVE -> {
                dragOffsetBeats = mouseBeat - dragStartBeat;
            }
            case RESIZE_LEFT -> {
                resizeOffsetBeats = mouseBeat - dragStartBeat;
            }
            case RESIZE_RIGHT -> {
                resizeOffsetBeats = mouseBeat - dragStartEndBeat;
            }
            default -> {
            }
        }

        repaint();
    }

    public void endNoteDrag() {
        if (dragMode == DragMode.NONE) {
            return;
        }

        model.endNoteStateChange();

        dragMode = DragMode.NONE;
        dragNote = null;
        dragNotes = Set.of();
        dragStartStates = Map.of();
    }

    public void dragSelectedNoteTo(Point point) {
        if (dragNote == null) {
            return;
        }

        switch (dragMode) {
            case MOVE -> moveSelectedNotesTo(point);
            case RESIZE_LEFT -> resizeSelectedNotesLeft(point);
            case RESIZE_RIGHT -> resizeSelectedNotesRight(point);
            default -> {
            }
        }
    }

    private void moveSelectedNotesTo(Point point) {
        if (dragNote == null) {
            return;
        }

        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());
        double newStartBeat = NoteEditorGeometry.snapBeat(mouseBeat - dragOffsetBeats);
        double deltaBeat = newStartBeat - dragStartBeat;
        int deltaY = point.y - dragStartY;
        int deltaNotes = Math.round((float) deltaY / NoteEditorLayout.NOTE_HEIGHT);
        int deltaMidiNotes = -deltaNotes;

        if (model.moveNotes(dragNotes, deltaMidiNotes, deltaBeat)) {
            repaint();
            return;
        }

        Double resolvedDeltaBeat = findNearestValidGroupMoveDelta(deltaMidiNotes, deltaBeat, mouseBeat);
        if (resolvedDeltaBeat == null) {
            return;
        }
        if (model.moveNotes(dragNotes, deltaMidiNotes, resolvedDeltaBeat)) {
            repaint();
        }
    }

    private void resizeSelectedNotesRight(Point point) {
        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());
        double newEndBeat = NoteEditorGeometry.snapBeat(mouseBeat - resizeOffsetBeats);
        double deltaEndBeat = newEndBeat - dragStartEndBeat;

        if (model.resizeNotesRight(dragNotes, deltaEndBeat)) {
            repaint();
        }
    }

    private void resizeSelectedNotesLeft(Point point) {
        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());
        double newStartBeat = NoteEditorGeometry.snapBeat(mouseBeat - resizeOffsetBeats);
        double deltaStartBeat = newStartBeat - dragStartBeat;

        if (model.resizeNotesLeft(dragNotes, deltaStartBeat)) {
            repaint();
        }
    }

    public boolean hasNoteAt(Point point) {
        return findNoteAt(point) != null;
    }

    private EditorNote findNoteAt(Point point) {
        for (EditorNote note : model.getNotes()) {
            if (getNoteBounds(note).contains(point)) {
                return note;
            }
        }
        return null;
    }

    private Rectangle getNoteBounds(EditorNote note) {
        int x = NoteEditorGeometry.getXForBeat(note.getStartBeat(), viewState.getPixelsPerBeat());

        int endX = NoteEditorGeometry.getXForBeat(
            note.getStartBeat() + note.getDurationBeats(),
            viewState.getPixelsPerBeat()
        );

        int rowY = NoteEditorGeometry.getYForMidiNote(note.getMidiNote());

        return new Rectangle(x, rowY + 1, endX - x, NoteEditorLayout.NOTE_HEIGHT - 1);
    }

    public void updateMouseCursor(Point point) {
        EditorNote note = findNoteAt(point);

        if (note == null) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }

        switch (getDragMode(note, point)) {
            case RESIZE_LEFT, RESIZE_RIGHT -> setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            case MOVE -> setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            default -> setCursor(Cursor.getDefaultCursor());
        }
    }

    private Double findNearestValidGroupMoveDelta(int requestedMidiDelta, double requestedBeatDelta, double mouseBeat) {
        boolean movingRight = requestedBeatDelta > 0;
        boolean movingLeft = requestedBeatDelta < 0;

        if (!movingRight && !movingLeft) {
            return null;
        }

        int midiDelta = clampGroupMidiDelta(requestedMidiDelta);

        double minimumBeatDelta = getMinimumGroupBeatDelta();

        double candidateDelta = Math.max(requestedBeatDelta, minimumBeatDelta);

        Set<EditorNote> crossedBlockers = new HashSet<>();

        while (true) {
            GroupMoveCollision collision = findGroupMoveCollision(midiDelta, candidateDelta);

            if (collision == null) {
                return candidateDelta;
            }

            DragStartState movingState = dragStartStates.get(collision.movingNote());

            EditorNote blocker = collision.blockingNote();

            double blockerStart = blocker.getStartBeat();

            double blockerEnd = blockerStart + blocker.getDurationBeats();

            /*
             * A blocker has to be crossed by the actual mouse
             * only once for the entire coupled group.
             */
            if (!crossedBlockers.contains(blocker)) {
                boolean mouseHasCrossed = movingRight ? mouseBeat >= blockerEnd : mouseBeat <= blockerStart;

                if (!mouseHasCrossed) {
                    return null;
                }

                crossedBlockers.add(blocker);
            }

            if (movingRight) {
                double requiredDelta = blockerEnd - movingState.startBeat();

                candidateDelta = Math.max(candidateDelta, requiredDelta);
            } else {
                double requiredDelta = blockerStart - movingState.durationBeats() - movingState.startBeat();

                if (requiredDelta < minimumBeatDelta) {
                    return null;
                }

                candidateDelta = Math.min(candidateDelta, requiredDelta);
            }
        }
    }

    private int clampGroupMidiDelta(int requestedDelta) {
        int minimumDelta = Integer.MIN_VALUE;
        int maximumDelta = Integer.MAX_VALUE;

        for (DragStartState state : dragStartStates.values()) {
            minimumDelta = Math.max(minimumDelta, -state.midiNote());
            maximumDelta = Math.min(maximumDelta, NoteEditorLayout.MIDI_NOTE_COUNT - 1 - state.midiNote());
        }

        return Math.max(minimumDelta, Math.min(requestedDelta, maximumDelta));
    }

    private double getMinimumGroupBeatDelta() {
        double minimumDelta = Double.NEGATIVE_INFINITY;

        for (DragStartState state : dragStartStates.values()) {
            minimumDelta = Math.max(minimumDelta, -state.startBeat());
        }

        return minimumDelta;
    }

    private GroupMoveCollision findGroupMoveCollision(int midiDelta, double beatDelta) {
        for (EditorNote movingNote : dragNotes) {
            DragStartState state = dragStartStates.get(movingNote);
            int targetMidiNote = state.midiNote() + midiDelta;
            double targetStartBeat = state.startBeat() + beatDelta;
            double targetEndBeat = targetStartBeat + state.durationBeats();

            for (EditorNote blocker : model.getNotes()) {
                // Notes in the moving group are not blockers.
                if (dragNotes.contains(blocker)) {
                    continue;
                }

                if (blocker.getMidiNote() != targetMidiNote) {
                    continue;
                }

                double blockerStart = blocker.getStartBeat();
                double blockerEnd = blockerStart + blocker.getDurationBeats();

                if (targetStartBeat < blockerEnd && targetEndBeat > blockerStart) {
                    return new GroupMoveCollision(movingNote, blocker);
                }
            }
        }

        return null;
    }

    public void undo() {
        if (!model.undo()) {
            return;
        }

        clearSelectionIfMissing();
        repaint();
    }

    public void redo() {
        if (!model.redo()) {
            return;
        }

        clearSelectionIfMissing();
        repaint();
    }

    /**
     * Removes any notes from the selection that no longer exist in the model.
     */
    private void clearSelectionIfMissing() {
        var existingSelection = selectionModel
            .getSelectedNotes()
            .stream()
            .filter(selected ->
                model
                    .getNotes()
                    .stream()
                    .anyMatch(note -> note == selected)
            )
            .toList();

        selectionModel.setSelection(existingSelection);
    }

    public void handleSelectionClick(Point point, boolean ctrlDown) {
        requestFocusInWindow();

        EditorNote clickedNote = findNoteAt(point);

        if (ctrlDown) {
            if (clickedNote != null) {
                selectionModel.toggle(clickedNote);
            }
            repaint();
            return;
        }

        if (clickedNote == null) {
            selectionModel.clearSelection();
        } else {
            selectionModel.setSelection(clickedNote);
        }
        repaint();
    }

    public void beginSelectionBox(Point point, boolean additive) {
        requestFocusInWindow();

        selectionBox = new SelectionBox(point);
        additiveSelectionBox = additive;

        selectionBeforeBox = new LinkedHashSet<>(selectionModel.getSelectedNotes());

        if (!additive) {
            selectionModel.clearSelection();
        }

        repaint();
    }

    public void updateSelectionBox(Point point) {
        if (selectionBox == null) {
            return;
        }

        selectionBox.update(point);
        updateSelectionFromBox();

        repaint();
    }

    public void endSelectionBox() {
        if (selectionBox == null) {
            return;
        }

        selectionBox = null;
        selectionBeforeBox = Set.of();
        additiveSelectionBox = false;

        repaint();
    }

    private void updateSelectionFromBox() {
        Rectangle box = selectionBox.getBounds();

        Set<EditorNote> notesInBox = model
            .getNotes()
            .stream()
            .filter(note -> box.intersects(getNoteBounds(note)))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (additiveSelectionBox) {
            Set<EditorNote> newSelection = new LinkedHashSet<>(selectionBeforeBox);

            newSelection.addAll(notesInBox);

            selectionModel.setSelection(newSelection);
        } else {
            selectionModel.setSelection(notesInBox);
        }
    }
}
