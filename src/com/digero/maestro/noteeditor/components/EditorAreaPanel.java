package com.digero.maestro.noteeditor.components;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import java.awt.Point;

import com.digero.maestro.noteeditor.NoteEditorLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;

public class EditorAreaPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final PianoPanel pianoPanel;
    private final NoteGridPanel noteGridPanel;
    private final TimelinePanel timelinePanel;

    public EditorAreaPanel() {
        pianoPanel = new PianoPanel();
        noteGridPanel = new NoteGridPanel();
        timelinePanel = new TimelinePanel();

        JScrollPane mainScrollPane = new JScrollPane(
                noteGridPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainScrollPane.setRowHeaderView(pianoPanel);

        JPanel blankPanel = new JPanel();
        blankPanel.setPreferredSize(
                new Dimension(NoteEditorLayout.PIANO_WIDTH, NoteEditorLayout.TIMELINE_HEIGHT));

        JScrollPane timelineScrollPane = new JScrollPane(
                timelinePanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        timelineScrollPane.setPreferredSize(
                new Dimension(0, NoteEditorLayout.TIMELINE_HEIGHT));
        timelineScrollPane.setRowHeaderView(blankPanel);

        JScrollBar horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
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

        setLayout(new BorderLayout());

        add(mainScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        mainScrollPane.getViewport().addChangeListener(e -> {
            Point gridPosition = mainScrollPane.getViewport().getViewPosition();

            timelineScrollPane.getViewport().setViewPosition(
                    new Point(gridPosition.x, 0));
        });
    }
}
