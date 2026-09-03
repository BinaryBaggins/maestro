package com.digero.maestro.noteeditor.components;

import com.digero.maestro.noteeditor.model.EditorNote;
import com.digero.maestro.noteeditor.model.EditorTrack;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import java.awt.Point;

import com.digero.maestro.noteeditor.NoteEditorLayout;
import com.digero.maestro.noteeditor.NoteEditorViewState;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;

public class EditorAreaPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private static final int[] ZOOM_LEVELS = { 40, 60, 80, 100, 120, 160, 240 };

        private final NoteEditorViewState viewState;

        private final PianoPanel pianoPanel;
        private final NoteGridPanel noteGridPanel;
        private final TimelinePanel timelinePanel;
        private final TimelineControlPanel timelineControlPanel;
        private JScrollPane mainScrollPane;
        private JScrollBar horizontalScrollBar;

        public EditorAreaPanel() {
                EditorTrack track = new EditorTrack("Test Track");
                track.addNote(new EditorNote(60, 1.0, 2.0));
                track.addNote(new EditorNote(64, 3.5, 1.0));
                track.addNote(new EditorNote(67, 5.0, 1.5));
                track.addNote(new EditorNote(72, 7.25, 0.5));

                viewState = new NoteEditorViewState();
                pianoPanel = new PianoPanel();
                noteGridPanel = new NoteGridPanel(viewState, track.getNotes());
                timelinePanel = new TimelinePanel(viewState);
                timelineControlPanel = new TimelineControlPanel();

                mainScrollPane = new JScrollPane(
                                noteGridPanel,
                                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                mainScrollPane.setRowHeaderView(pianoPanel);

                timelineControlPanel.setPreferredSize(
                                new Dimension(NoteEditorLayout.PIANO_WIDTH, NoteEditorLayout.TIMELINE_HEIGHT));

                JScrollPane timelineScrollPane = new JScrollPane(
                                timelinePanel,
                                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                timelineScrollPane.setPreferredSize(
                                new Dimension(0, NoteEditorLayout.TIMELINE_HEIGHT));
                timelineScrollPane.setRowHeaderView(timelineControlPanel);
                timelineControlPanel.addZoomInListener(e -> zoomIn());
                timelineControlPanel.addZoomOutListener(e -> zoomOut());

                horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
                horizontalScrollBar.setModel(mainScrollPane.getHorizontalScrollBar().getModel());

                JPanel scrollBarBlankPanel = new JPanel();
                scrollBarBlankPanel.setPreferredSize(
                                new Dimension(NoteEditorLayout.PIANO_WIDTH, 0));

                JPanel scrollBarRow = new JPanel(new BorderLayout());
                scrollBarRow.add(scrollBarBlankPanel, BorderLayout.WEST);
                scrollBarRow.add(horizontalScrollBar, BorderLayout.CENTER);

                JPanel rightSpacer = new JPanel();
                rightSpacer.setPreferredSize(
                                new Dimension(
                                                mainScrollPane.getVerticalScrollBar().getPreferredSize().width,
                                                0));

                JPanel bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.add(timelineScrollPane, BorderLayout.CENTER);
                bottomPanel.add(scrollBarRow, BorderLayout.SOUTH);
                bottomPanel.add(rightSpacer, BorderLayout.EAST);

                mainScrollPane.getViewport().addChangeListener(e -> {
                        Point gridPosition = mainScrollPane.getViewport().getViewPosition();

                        timelineScrollPane.getViewport().setViewPosition(
                                        new Point(gridPosition.x, 0));
                });

                mainScrollPane.getVerticalScrollBar().setUnitIncrement(NoteEditorLayout.NOTE_HEIGHT);
                mainScrollPane.getVerticalScrollBar().setBlockIncrement(NoteEditorLayout.NOTE_HEIGHT * 4);

                updateHorizontalScrollIncrements();

                setLayout(new BorderLayout());
                add(mainScrollPane, BorderLayout.CENTER);
                add(bottomPanel, BorderLayout.SOUTH);
        }

        private void updateHorizontalScrollIncrements() {
                int pixelsPerBeat = viewState.getPixelsPerBeat();

                mainScrollPane.getHorizontalScrollBar().setUnitIncrement(pixelsPerBeat);

                mainScrollPane.getHorizontalScrollBar().setBlockIncrement(
                                pixelsPerBeat * NoteEditorLayout.BEATS_PER_MEASURE);

                horizontalScrollBar.setUnitIncrement(pixelsPerBeat);

                horizontalScrollBar.setBlockIncrement(
                                pixelsPerBeat * NoteEditorLayout.BEATS_PER_MEASURE);
        }

        private void zoomIn() {
                int currentZoom = viewState.getPixelsPerBeat();

                for (int zoomLevel : ZOOM_LEVELS) {
                        if (zoomLevel > currentZoom) {
                                setZoom(zoomLevel);
                                return;
                        }
                }
        }

        private void zoomOut() {
                int currentZoom = viewState.getPixelsPerBeat();

                for (int i = ZOOM_LEVELS.length - 1; i >= 0; i--) {
                        if (ZOOM_LEVELS[i] < currentZoom) {
                                setZoom(ZOOM_LEVELS[i]);
                                return;
                        }
                }
        }

        private void setZoom(int pixelsPerBeat) {
                viewState.setPixelsPerBeat(pixelsPerBeat);

                noteGridPanel.updateZoom();
                timelinePanel.updateZoom();

                timelineControlPanel.setZoomLabel(
                                viewState.getZoomPercentage());

                updateHorizontalScrollIncrements();
        }
}
