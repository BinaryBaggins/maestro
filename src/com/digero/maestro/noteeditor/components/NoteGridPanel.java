package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.NoteEditorGeometry;
import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.NoteEditorViewState;
import com.digero.maestro.noteeditor.actions.NoteEditorKeyBindings;
import com.digero.maestro.noteeditor.actions.NoteGridMouseListener;
import com.digero.maestro.noteeditor.interaction.GroupMoveResolver;
import com.digero.maestro.noteeditor.interaction.NoteDragState;
import com.digero.maestro.noteeditor.interaction.ResolvedGroupMove;
import com.digero.maestro.noteeditor.model.DragMode;
import com.digero.maestro.noteeditor.model.EditorNote;
import com.digero.maestro.noteeditor.model.NoteEditorModel;
import com.digero.maestro.noteeditor.model.NoteSelectionModel;
import com.digero.maestro.noteeditor.model.NoteSnapshot;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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

    private NoteDragState dragState;

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

    public void deleteSelectedNotes() {
        Set<EditorNote> selectedNotes = new LinkedHashSet<>(selectionModel.getSelectedNotes());

        if (selectedNotes.isEmpty()) {
            return;
        }

        if (model.deleteNotes(selectedNotes)) {
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

        EditorNote grabbedNote = findNoteAt(point);

        if (grabbedNote == null) {
            dragState = null;
            repaint();
            return;
        }

        if (!selectionModel.isSelected(grabbedNote)) {
            selectionModel.setSelection(grabbedNote);
        }

        Set<EditorNote> selectedNotes = new LinkedHashSet<>(selectionModel.getSelectedNotes());

        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());
        DragMode mode = getDragMode(grabbedNote, point);

        NoteDragState newDragState = NoteDragState.capture(grabbedNote, selectedNotes, mode, mouseBeat, point.y);

        model.beginNoteStateChange(newDragState.notes());

        dragState = newDragState;

        repaint();
    }

    public void endNoteDrag() {
        if (dragState == null) {
            return;
        }

        model.endNoteStateChange();

        dragState = null;
    }

    public void dragSelectedNoteTo(Point point) {
        if (dragState == null) {
            return;
        }

        switch (dragState.mode()) {
            case MOVE -> moveSelectedNotesTo(point);
            case RESIZE_LEFT -> resizeSelectedNotesLeft(point);
            case RESIZE_RIGHT -> resizeSelectedNotesRight(point);
            default -> {
            }
        }
    }

    private void moveSelectedNotesTo(Point point) {
        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());
        double newStartBeat = NoteEditorGeometry.snapBeat(mouseBeat - dragState.pointerOffsetBeats());
        double deltaBeat = newStartBeat - dragState.grabbedStartBeat();
        int deltaY = point.y - dragState.startY();
        int deltaNotes = Math.round((float) deltaY / NoteEditorLayout.NOTE_HEIGHT);
        int deltaMidiNotes = -deltaNotes;

        if (model.moveNotes(dragState.notes(), deltaMidiNotes, deltaBeat)) {
            repaint();
            return;
        }

        List<NoteSnapshot> movingSnapshots = List.copyOf(dragState.snapshots());
        List<NoteSnapshot> blockingSnapshots = model
            .getNotes()
            .stream()
            .filter(note -> !dragState.notes().contains(note))
            .map(NoteSnapshot::new)
            .toList();

        Optional<ResolvedGroupMove> resolvedMove = GroupMoveResolver.resolve(
            movingSnapshots,
            blockingSnapshots,
            deltaMidiNotes,
            deltaBeat,
            mouseBeat
        );
        if (resolvedMove.isEmpty()) {
            return;
        }
        ResolvedGroupMove resolved = resolvedMove.orElseThrow();
        if (model.moveNotes(dragState.notes(), resolved.midiDelta(), resolved.beatDelta())) {
            repaint();
        }
    }

    private void resizeSelectedNotesRight(Point point) {
        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());
        double newEndBeat = NoteEditorGeometry.snapBeat(mouseBeat - dragState.pointerOffsetBeats());
        double deltaEndBeat = newEndBeat - dragState.grabbedStartEndBeat();

        if (model.resizeNotesRight(dragState.notes(), deltaEndBeat)) {
            repaint();
        }
    }

    private void resizeSelectedNotesLeft(Point point) {
        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());
        double newStartBeat = NoteEditorGeometry.snapBeat(mouseBeat - dragState.pointerOffsetBeats());
        double deltaStartBeat = newStartBeat - dragState.grabbedStartBeat();

        if (model.resizeNotesLeft(dragState.notes(), deltaStartBeat)) {
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
