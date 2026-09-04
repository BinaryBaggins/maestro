package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.NoteEditorGeometry;
import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.NoteEditorViewState;
import com.digero.maestro.noteeditor.actions.NoteEditorKeyBindings;
import com.digero.maestro.noteeditor.actions.NoteGridMouseListener;
import com.digero.maestro.noteeditor.model.EditorNote;
import com.digero.maestro.noteeditor.model.NoteEditorModel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JPanel;

public class NoteGridPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Model and view state
    private final NoteEditorModel model;
    private final NoteEditorViewState viewState;
    private EditorNote selectedNote;

    // Dragging state variables
    private double dragOffsetBeats;
    private int dragStartY;
    private int dragStartMidiNote;
    private double dragStartBeat;

    // Resizing state variables
    private double resizeOffsetBeats;

    // Drag and resize modes
    private enum DragMode {
        NONE,
        MOVE,
        RESIZE_LEFT,
        RESIZE_RIGHT,
    }

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
    }

    private void paintNote(Graphics g, EditorNote note) {
        Rectangle bounds = getNoteBounds(note);

        g.setColor(NoteEditorLayout.NOTE_COLOR);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (note == selectedNote) {
            g.setColor(NoteEditorLayout.SELECTED_NOTE_BORDER_COLOR);

            g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);

            if (bounds.width > 3 && bounds.height > 3) {
                g.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 3, bounds.height - 3);
            }
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

        selectedNote = note;

        repaint();
    }

    public void deleteSelectedNote() {
        if (selectedNote == null) {
            return;
        }

        if (model.deleteNote(selectedNote)) {
            selectedNote = null;
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

        selectedNote = findNoteAt(point);

        if (selectedNote == null) {
            dragMode = DragMode.NONE;
            repaint();
            return;
        }

        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());

        dragMode = getDragMode(selectedNote, point);

        switch (dragMode) {
            case MOVE -> {
                dragOffsetBeats = mouseBeat - selectedNote.getStartBeat();

                dragStartBeat = selectedNote.getStartBeat();
                dragStartY = point.y;
                dragStartMidiNote = selectedNote.getMidiNote();
            }
            case RESIZE_LEFT -> {
                resizeOffsetBeats = mouseBeat - selectedNote.getStartBeat();
            }
            case RESIZE_RIGHT -> {
                double endBeat = selectedNote.getStartBeat() + selectedNote.getDurationBeats();

                resizeOffsetBeats = mouseBeat - endBeat;
            }
            default -> {
            }
        }

        repaint();
    }

    public void dragSelectedNoteTo(Point point) {
        if (selectedNote == null) {
            return;
        }

        switch (dragMode) {
            case MOVE -> moveSelectedNoteTo(point);
            case RESIZE_LEFT -> resizeSelectedNoteLeft(point);
            case RESIZE_RIGHT -> resizeSelectedNoteRight(point);
            default -> {
            }
        }
    }

    private void moveSelectedNoteTo(Point point) {
        if (selectedNote == null) {
            return;
        }

        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());

        double newStartBeat = NoteEditorGeometry.snapBeat(mouseBeat - dragOffsetBeats);

        if (newStartBeat < 0) {
            newStartBeat = 0;
        }

        int deltaY = point.y - dragStartY;

        int deltaNotes = Math.round((float) deltaY / NoteEditorLayout.NOTE_HEIGHT);

        int newMidiNote = dragStartMidiNote - deltaNotes;

        newMidiNote = Math.max(0, Math.min(newMidiNote, NoteEditorLayout.MIDI_NOTE_COUNT - 1));

        double duration = selectedNote.getDurationBeats();

        if (!model.canPlaceNote(selectedNote, newMidiNote, newStartBeat, duration)) {
            boolean movingRight = newStartBeat > dragStartBeat;
            boolean movingLeft = newStartBeat < dragStartBeat;

            Double snappedStartBeat = findNearestValidStartBeat(
                selectedNote,
                newMidiNote,
                newStartBeat,
                duration,
                mouseBeat,
                movingRight,
                movingLeft
            );

            if (snappedStartBeat == null) {
                return;
            }

            newStartBeat = snappedStartBeat;

            // The snapped position itself must still be valid.
            if (!model.canPlaceNote(selectedNote, newMidiNote, newStartBeat, duration)) {
                return;
            }
        }

        if (model.moveNote(selectedNote, newMidiNote, newStartBeat)) {
            repaint();
        }
    }

    private void resizeSelectedNoteRight(Point point) {
        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());

        double newEndBeat = NoteEditorGeometry.snapBeat(mouseBeat - resizeOffsetBeats);

        if (model.resizeNoteRight(selectedNote, newEndBeat)) {
            repaint();
        }
    }

    private void resizeSelectedNoteLeft(Point point) {
        double mouseBeat = NoteEditorGeometry.getBeatForX(point.x, viewState.getPixelsPerBeat());

        double newStartBeat = NoteEditorGeometry.snapBeat(mouseBeat - resizeOffsetBeats);

        if (model.resizeNoteLeft(selectedNote, newStartBeat)) {
            repaint();
        }
    }

    public void endNoteDrag() {
        dragMode = DragMode.NONE;
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

    /**
     * Resolves the desired (overlapping) start beat to the nearest valid start beat in the
     * direction of movement. Notes fully crossed by the mouse are skipped over one after another
     * (so a whole crossed chain can be jumped in one drag step). As soon as a remaining obstacle
     * hasn't been passed by the mouse yet, no snap happens at all - the note must not jump while
     * the mouse is still positioned on the blocking note.
     */
    private Double findNearestValidStartBeat(
        EditorNote editedNote,
        int midiNote,
        double startBeat,
        double durationBeats,
        double mouseBeat,
        boolean movingRight,
        boolean movingLeft
    ) {
        if (!movingRight && !movingLeft) {
            return null;
        }

        double candidateStart = startBeat;

        while (true) {
            EditorNote blocker = findOverlappingNote(editedNote, midiNote, candidateStart, durationBeats);

            if (blocker == null) {
                return Math.max(0, candidateStart);
            }

            boolean mouseHasCrossed = movingRight
                ? mouseBeat >= blocker.getStartBeat() + blocker.getDurationBeats()
                : mouseBeat <= blocker.getStartBeat();

            if (!mouseHasCrossed) {
                return null;
            }

            candidateStart = movingRight
                ? blocker.getStartBeat() + blocker.getDurationBeats()
                : blocker.getStartBeat() - durationBeats;
        }
    }

    private EditorNote findOverlappingNote(
        EditorNote editedNote,
        int midiNote,
        double startBeat,
        double durationBeats
    ) {
        double endBeat = startBeat + durationBeats;

        for (EditorNote note : model.getNotes()) {
            if (note == editedNote) {
                continue;
            }

            if (note.getMidiNote() != midiNote) {
                continue;
            }

            double otherStart = note.getStartBeat();
            double otherEnd = otherStart + note.getDurationBeats();

            if (startBeat < otherEnd && endBeat > otherStart) {
                return note;
            }
        }

        return null;
    }
}
