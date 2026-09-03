package com.digero.maestro.noteeditor;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import com.digero.maestro.noteeditor.components.EditorAreaPanel;
import com.digero.maestro.noteeditor.components.ToolbarPanel;

public class NoteEditorPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ToolbarPanel toolbarPanel;
    private final EditorAreaPanel editorAreaPanel;

    public NoteEditorPanel() {
        toolbarPanel = new ToolbarPanel();
        editorAreaPanel = new EditorAreaPanel();

        setLayout(new BorderLayout());

        add(toolbarPanel, BorderLayout.NORTH);
        add(editorAreaPanel, BorderLayout.CENTER);
    }

}
