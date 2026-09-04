package com.digero.maestro.noteeditor;

import javax.swing.JFrame;

public class NoteEditorFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final NoteEditorPanel editorPanel;

    public NoteEditorFrame() {
        editorPanel = new NoteEditorPanel();

        setTitle("Maestro Note Editor");
        setContentPane(editorPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // later change to DISPOSE_ON_CLOSE

        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    new NoteEditorFrame().setVisible(true);
                }
            }
        );
    }
}
